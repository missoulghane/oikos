package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.UnitOfProperty;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentCallPersistenceMapperImpl;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InstallmentRepositoryAdapter.class, InstallmentPersistenceMapperImpl.class,
        InstallmentCallRepositoryAdapter.class, InstallmentCallPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class InstallmentRepositoryAdapterFindUnitsWithOutstandingDueDataJpaTest {

    private static final LocalDate AS_OF = LocalDate.of(2026, 10, 28);

    @Autowired
    private InstallmentRepositoryAdapter installmentAdapter;

    @Autowired
    private InstallmentCallRepositoryAdapter callAdapter;

    private InstallmentCallId call(EntityId propertyId) {
        return callAdapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 10),
                LocalDate.of(2026, 10, 5)).issue().post(EntityId.newId())).getId();
    }

    private void installment(EntityId unitId, InstallmentCallId callId, LocalDate dueDate, String outstanding) {
        installmentAdapter.save(Installment.create(InstallmentId.newId(), unitId, dueDate,
                Amount.of(new BigDecimal("300")), callId).withOutstandingAmount(new BigDecimal(outstanding)));
    }

    @Test
    void returns_each_unit_owing_something_already_due_once_with_its_property() {
        EntityId propertyId = EntityId.newId();
        InstallmentCallId callId = call(propertyId);
        EntityId owingTwice = EntityId.newId();
        EntityId settled = EntityId.newId();
        EntityId notYetDue = EntityId.newId();
        installment(owingTwice, callId, LocalDate.of(2026, 9, 5), "300");
        installment(owingTwice, callId, AS_OF, "120");
        installment(settled, callId, LocalDate.of(2026, 9, 5), "0");
        installment(notYetDue, callId, LocalDate.of(2026, 11, 5), "300");

        assertThat(installmentAdapter.findUnitsWithOutstandingDue(AS_OF))
                .containsExactly(new UnitOfProperty(propertyId, owingTwice));
    }
}
