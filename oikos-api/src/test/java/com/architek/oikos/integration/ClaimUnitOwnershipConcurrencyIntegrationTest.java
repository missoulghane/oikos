package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
import com.architek.oikos.property.domain.exception.UnitAlreadyClaimedException;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Proves the pessimistic lock added to AddUnitOwnershipService (via
 * UnitRepository#findByIdForUpdate) actually serializes concurrent writers:
 * two threads claiming the same unit at once must not both succeed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClaimUnitOwnershipConcurrencyIntegrationTest {

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

    @Test
    void two_concurrent_claims_on_the_same_unit_leave_exactly_one_winner() throws Exception {
        PropertyId propertyId = createPropertyUseCase.create(new CreatePropertyCommand("Concurrency Test Property", "1 rue Test", "Casablanca"));
        BuildingId buildingId = addBuildingUseCase.add(new AddBuildingCommand(propertyId, "Bâtiment A", 3));
        UnitTypeDefinitionId unitTypeId =
                addUnitTypeDefinitionUseCase.add(new AddUnitTypeDefinitionCommand(propertyId, "Appartement"));
        UnitId unitId = addUnitUseCase.add(new AddUnitCommand(buildingId, "A1", unitTypeId, BigDecimal.TEN, null));

        EntityId propertyEntityId = EntityId.of(propertyId.asUuid());
        PartyId partyOne = createPartyUseCase.create(new CreatePartyCommand(
                propertyEntityId, "Party One", PartyType.INDIVIDUAL, EmailVO.of("party-one@example.com"), null, false));
        PartyId partyTwo = createPartyUseCase.create(new CreatePartyCommand(
                propertyEntityId, "Party Two", PartyType.INDIVIDUAL, EmailVO.of("party-two@example.com"), null, false));

        CountDownLatch bothReady = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> resultOne = executor.submit(claimTask(unitId, EntityId.of(partyOne.asUuid()), bothReady, go));
            Future<Boolean> resultTwo = executor.submit(claimTask(unitId, EntityId.of(partyTwo.asUuid()), bothReady, go));

            // Wait until both threads are parked on `go`, then release them at
            // the same instant so their claims genuinely race on the unit lock
            // rather than merely running one after the other.
            assertThat(bothReady.await(10, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            long successCount = List.of(resultOne, resultTwo).stream().filter(this::succeeded).count();

            assertThat(successCount).isEqualTo(1);
            assertThat(unitOwnershipRepository.findAllByUnitId(unitId)).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> claimTask(UnitId unitId, EntityId partyId, CountDownLatch bothReady, CountDownLatch go) {
        return () -> {
            bothReady.countDown();
            go.await();
            try {
                claimUnitOwnershipUseCase.claim(new ClaimUnitOwnershipCommand(unitId, partyId));
                return true;
            } catch (UnitAlreadyClaimedException e) {
                return false;
            }
        };
    }

    private boolean succeeded(Future<Boolean> future) {
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
}
