package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.UnitAccountView;
import com.architek.oikos.accounting.application.query.GetUnitAccountQuery;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetUnitAccountServiceTest {

    @Mock
    private UnitAccountRepository unitAccountRepository;

    private GetUnitAccountService newService() {
        return new GetUnitAccountService(unitAccountRepository);
    }

    @Test
    void returns_the_view_of_an_existing_unit_account() {
        EntityId unitId = EntityId.newId();
        UnitAccount account = UnitAccount.create(UnitAccountId.newId(), unitId, EntityId.newId(), Instant.now());
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(account));

        UnitAccountView view = newService().get(new GetUnitAccountQuery(unitId));

        assertThat(view.unitId()).isEqualTo(unitId);
    }

    @Test
    void rejects_a_unit_with_no_account() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().get(new GetUnitAccountQuery(unitId)))
                .isInstanceOf(UnitAccountNotFoundException.class);
    }
}
