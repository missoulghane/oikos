package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class FindUnitAccountByUnitServiceTest {

    @Mock
    private UnitAccountRepository unitAccountRepository;

    private FindUnitAccountByUnitService newService() {
        return new FindUnitAccountByUnitService(unitAccountRepository);
    }

    @Test
    void returns_the_account_id_when_the_unit_has_one() {
        EntityId unitId = EntityId.newId();
        UnitAccountId accountId = UnitAccountId.newId();
        UnitAccount account = UnitAccount.reconstruct(accountId, unitId, EntityId.newId(), java.math.BigDecimal.ZERO,
                Instant.now());
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(account));

        Optional<EntityId> result = newService().findByUnitId(unitId);

        assertThat(result).contains(accountId.value());
    }

    @Test
    void returns_empty_when_the_unit_has_no_account() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        assertThat(newService().findByUnitId(unitId)).isEmpty();
    }
}
