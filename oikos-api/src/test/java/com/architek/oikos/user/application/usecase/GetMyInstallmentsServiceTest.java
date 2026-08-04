package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.port.out.InstallmentDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedInstallmentStatus;
import com.architek.oikos.user.application.port.out.OwnedInstallmentView;
import com.architek.oikos.user.application.port.out.OwnedUnitView;
import com.architek.oikos.user.application.port.out.UnitDirectoryPort;
import com.architek.oikos.user.application.query.GetMyInstallmentsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class GetMyInstallmentsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private InstallmentDirectoryPort installmentDirectoryPort;

    private GetMyInstallmentsService newService() {
        return new GetMyInstallmentsService(userRepository, unitDirectoryPort, installmentDirectoryPort);
    }

    @Test
    void aggregates_installments_across_every_unit_owned_through_every_linked_party() {
        UserId userId = UserId.newId();
        EntityId partyIdA = EntityId.newId();
        EntityId partyIdB = EntityId.newId();
        User user = User.register(userId, EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"))
                .withLinkedParty(partyIdA)
                .withLinkedParty(partyIdB);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        EntityId unitA = EntityId.newId();
        EntityId unitB = EntityId.newId();
        when(unitDirectoryPort.listUnitsOwnedByParty(partyIdA)).thenReturn(List.of(ownedUnit(unitA)));
        when(unitDirectoryPort.listUnitsOwnedByParty(partyIdB)).thenReturn(List.of(ownedUnit(unitB)));

        OwnedInstallmentView installmentA = installment(unitA);
        OwnedInstallmentView installmentB = installment(unitB);
        when(installmentDirectoryPort.listInstallmentsForUnit(unitA)).thenReturn(List.of(installmentA));
        when(installmentDirectoryPort.listInstallmentsForUnit(unitB)).thenReturn(List.of(installmentB));

        List<OwnedInstallmentView> result = newService().getMyInstallments(new GetMyInstallmentsQuery(userId));

        assertThat(result).containsExactlyInAnyOrder(installmentA, installmentB);
    }

    @Test
    void unknown_user_is_rejected() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getMyInstallments(new GetMyInstallmentsQuery(userId)))
                .isInstanceOf(UserNotFoundException.class);
    }

    private static OwnedUnitView ownedUnit(EntityId unitId) {
        return new OwnedUnitView(unitId, "101", EntityId.newId(), "Bâtiment A", EntityId.newId(), "Résidence",
                new BigDecimal("50"));
    }

    private static OwnedInstallmentView installment(EntityId unitId) {
        return new OwnedInstallmentView(EntityId.newId(), unitId, LocalDate.now(), new BigDecimal("100"),
                new BigDecimal("100"), OwnedInstallmentStatus.NOT_SETTLED);
    }
}
