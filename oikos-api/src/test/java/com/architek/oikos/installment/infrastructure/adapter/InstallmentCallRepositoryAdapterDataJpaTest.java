package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentCallPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InstallmentCallRepositoryAdapter.class, InstallmentCallPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class InstallmentCallRepositoryAdapterDataJpaTest {

    @Autowired
    private InstallmentCallRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_installment_call() {
        EntityId propertyId = EntityId.newId();
        InstallmentCall saved = adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId,
                YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));

        InstallmentCall reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.getPeriod()).isEqualTo(YearMonth.of(2026, 1));
        assertThat(reloaded.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 5));
    }

    @Test
    void exists_by_property_id_and_period_reflects_what_was_saved() {
        EntityId propertyId = EntityId.newId();
        adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));

        assertThat(adapter.existsByPropertyIdAndPeriod(propertyId, YearMonth.of(2026, 1))).isTrue();
        assertThat(adapter.existsByPropertyIdAndPeriod(propertyId, YearMonth.of(2026, 2))).isFalse();
    }

    @Test
    void finds_all_installment_calls_matching_the_given_ids_in_one_batch() {
        EntityId propertyId = EntityId.newId();
        InstallmentCall first = adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId,
                YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));
        InstallmentCall second = adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId,
                YearMonth.of(2026, 2), LocalDate.of(2026, 3, 5)).issue().post(EntityId.newId()));
        adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 3), LocalDate.of(2026, 4, 5)).issue().post(EntityId.newId()));

        List<InstallmentCall> found = adapter.findAllByIds(List.of(first.getId(), second.getId()));

        assertThat(found).extracting(InstallmentCall::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void finds_a_page_of_installment_calls_by_property_id() {
        EntityId propertyId = EntityId.newId();
        adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));
        adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 2), LocalDate.of(2026, 3, 5)).issue().post(EntityId.newId()));
        adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(), YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));

        Page<InstallmentCall> page = adapter.findPageByPropertyId(propertyId, PageRequest.of(0, 20));

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).allMatch(call -> call.getPropertyId().equals(propertyId));
    }

    @Test
    void a_cancelled_call_no_longer_holds_its_period() {
        EntityId propertyId = EntityId.newId();
        InstallmentCall saved = adapter.save(InstallmentCall.draft(InstallmentCallId.newId(), propertyId,
                YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId()));

        adapter.save(saved.cancelPosted());

        assertThat(adapter.existsByPropertyIdAndPeriod(propertyId, YearMonth.of(2026, 1))).isFalse();
        assertThat(adapter.findById(saved.getId())).get()
                .extracting(InstallmentCall::getStatus).isEqualTo(InstallmentCallStatus.CANCELLED);
    }
}
