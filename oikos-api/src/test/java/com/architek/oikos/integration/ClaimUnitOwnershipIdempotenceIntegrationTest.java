package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.command.AddBuildingCommand;
import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.command.AddUnitTypeDefinitionCommand;
import com.architek.oikos.property.application.command.ClaimUnitOwnershipCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.AddBuildingUseCase;
import com.architek.oikos.property.application.port.in.AddUnitTypeDefinitionUseCase;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.in.ClaimUnitOwnershipUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Le cas nominal d'une invitation privée, validé dans une vraie transaction :
 * le syndic rattache le lot au contact, l'invite pour ce lot, puis valide sa
 * demande d'adhésion - et la revendication porte alors sur un lot que ce
 * contact détient déjà.
 *
 * <p>Un test à mocks ne pouvait pas voir ce qui cassait : l'échec ne venait pas
 * de la revendication elle-même mais du commit. AddUnitOwnershipService étant
 * transactionnel, son exception marquait la transaction rollback-only en
 * franchissant son proxy, et le commit de AcceptMembershipRequestService
 * échouait en UnexpectedRollbackException - même si l'exception avait été
 * rattrapée entre-temps. D'où ce test avec un vrai gestionnaire de
 * transactions : il vérifie que la transaction de l'appelant commite.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClaimUnitOwnershipIdempotenceIntegrationTest {

    @Autowired
    private CreatePropertyUseCase createPropertyUseCase;

    @Autowired
    private AddBuildingUseCase addBuildingUseCase;

    @Autowired
    private AddUnitTypeDefinitionUseCase addUnitTypeDefinitionUseCase;

    @Autowired
    private AddUnitUseCase addUnitUseCase;

    @Autowired
    private CreatePartyUseCase createPartyUseCase;

    @Autowired
    private ClaimUnitOwnershipUseCase claimUnitOwnershipUseCase;

    @Autowired
    private UnitOwnershipRepository unitOwnershipRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void re_claiming_a_unit_for_its_own_owner_lets_the_caller_s_transaction_commit() {
        PropertyId propertyId = createPropertyUseCase.create(
                new CreatePropertyCommand("Idempotent Claim Property", "2 rue Test", "Casablanca"));
        BuildingId buildingId = addBuildingUseCase.add(new AddBuildingCommand(propertyId, "Bâtiment A", 3));
        UnitTypeDefinitionId unitTypeId =
                addUnitTypeDefinitionUseCase.add(new AddUnitTypeDefinitionCommand(propertyId, "Appartement"));
        UnitId unitId = addUnitUseCase.add(new AddUnitCommand(buildingId, "B2", unitTypeId, BigDecimal.TEN, null));

        EntityId propertyEntityId = EntityId.of(propertyId.asUuid());
        PartyId party = createPartyUseCase.create(new CreatePartyCommand(propertyEntityId, "Jane Doe",
                PartyType.INDIVIDUAL, EmailVO.of("jane.doe@example.com"), null, false));
        EntityId partyId = EntityId.of(party.asUuid());

        // Le rattachement du syndic, avant l'invitation.
        claimUnitOwnershipUseCase.claim(new ClaimUnitOwnershipCommand(unitId, partyId));

        // La validation de la demande : une transaction englobante, comme celle
        // de AcceptMembershipRequestService, qui revendique puis commite.
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        assertThatCode(() -> transaction.executeWithoutResult(
                status -> claimUnitOwnershipUseCase.claim(new ClaimUnitOwnershipCommand(unitId, partyId))))
                .doesNotThrowAnyException();

        // Et rien n'a été écrit en double au passage.
        assertThat(unitOwnershipRepository.findAllByUnitId(unitId)).hasSize(1);
    }
}
