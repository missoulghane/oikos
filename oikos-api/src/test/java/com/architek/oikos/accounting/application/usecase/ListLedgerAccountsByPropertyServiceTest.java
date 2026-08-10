package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.LedgerAccountView;
import com.architek.oikos.accounting.application.query.ListLedgerAccountsByPropertyQuery;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListLedgerAccountsByPropertyServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    private ListLedgerAccountsByPropertyService newService() {
        return new ListLedgerAccountsByPropertyService(ledgerAccountRepository);
    }

    @Test
    void lists_every_account_visible_to_the_property() {
        EntityId propertyId = EntityId.newId();
        LedgerAccount cash = LedgerAccount.create(LedgerAccountId.newId(), propertyId, null,
                AccountNumber.of("51610001"), "Caisse", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH);
        when(ledgerAccountRepository.findAllVisibleToProperty(propertyId)).thenReturn(List.of(cash));

        List<LedgerAccountView> views = newService().list(new ListLedgerAccountsByPropertyQuery(propertyId));

        assertThat(views).extracting(LedgerAccountView::accountNumber).containsExactly("51610001");
    }
}
