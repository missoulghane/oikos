package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class InstallmentCallTest {

    @Test
    void create_builds_a_installment_call_with_the_given_fields() {
        EntityId propertyId = EntityId.newId();
        YearMonth period = YearMonth.of(2026, 1);
        LocalDate dueDate = LocalDate.of(2026, 2, 5);

        InstallmentCall installmentCall = InstallmentCall.create(InstallmentCallId.newId(), propertyId, period, dueDate);

        assertThat(installmentCall.getPropertyId()).isEqualTo(propertyId);
        assertThat(installmentCall.getPeriod()).isEqualTo(period);
        assertThat(installmentCall.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        InstallmentCallId id = InstallmentCallId.newId();
        InstallmentCall a = InstallmentCall.create(id, EntityId.newId(), YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5));
        InstallmentCall b = InstallmentCall.create(id, EntityId.newId(), YearMonth.of(2026, 3), LocalDate.of(2026, 4, 5));

        assertThat(a).isEqualTo(b);
    }
}
