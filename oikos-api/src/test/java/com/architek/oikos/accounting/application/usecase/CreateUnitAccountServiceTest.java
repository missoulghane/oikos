package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.exception.DuplicateUnitAccountException;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateUnitAccountServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UnitAccountRepository unitAccountRepository;

    private CreateUnitAccountService newService() {
        return new CreateUnitAccountService(unitAccountRepository, CLOCK);
    }

    @Test
    void creating_an_account_for_a_new_unit_persists_it_with_a_zero_balance() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        when(unitAccountRepository.existsByUnitId(unitId)).thenReturn(false);
        when(unitAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EntityId accountId = newService().create(unitId, propertyId);

        assertThat(accountId).isNotNull();
    }

    @Test
    void creating_a_second_account_for_the_same_unit_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.existsByUnitId(unitId)).thenReturn(true);

        assertThatThrownBy(() -> newService().create(unitId, EntityId.newId()))
                .isInstanceOf(DuplicateUnitAccountException.class);
    }
}
