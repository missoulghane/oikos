package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentSortField;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InstallmentRepositoryAdapter.class, InstallmentPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class InstallmentRepositoryAdapterFindPageByUnitIdsDataJpaTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 22);

    @Autowired
    private InstallmentRepositoryAdapter installmentAdapter;

    private EntityId unitA;
    private EntityId unitB;
    private Installment notSettled;
    private Installment partiallySettled;
    private Installment settled;
    private Installment otherNotSettled;

    private void seed() {
        unitA = EntityId.newId();
        unitB = EntityId.newId();
        EntityId otherUnit = EntityId.newId();

        notSettled = Installment.create(InstallmentId.newId(), unitA, TODAY.plusDays(10), Amount.of(new BigDecimal("100")));
        installmentAdapter.save(notSettled);

        partiallySettled = Installment.create(InstallmentId.newId(), unitA, TODAY.minusDays(5), Amount.of(new BigDecimal("200")))
                .withOutstandingAmount(new BigDecimal("80"));
        installmentAdapter.save(partiallySettled);

        settled = Installment.create(InstallmentId.newId(), unitA, TODAY.minusDays(10), Amount.of(new BigDecimal("50")))
                .withOutstandingAmount(BigDecimal.ZERO);
        installmentAdapter.save(settled);

        otherNotSettled = Installment.create(InstallmentId.newId(), unitB, TODAY.plusDays(20), Amount.of(new BigDecimal("300")));
        installmentAdapter.save(otherNotSettled);

        // belongs to a unit outside the residence being queried - must never appear
        installmentAdapter.save(Installment.create(InstallmentId.newId(), otherUnit, TODAY, Amount.of(new BigDecimal("999"))));
    }

    @Test
    void restricts_results_to_the_given_unit_ids_and_sorts_by_amount_descending() {
        seed();

        Page<Installment> page = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                new InstallmentFilter(Set.of(), null, null, InstallmentSortField.AMOUNT, SortDirection.DESC),
                PageRequest.of(0, 10));

        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.content()).extracting(i -> i.getAmount().value().doubleValue())
                .containsExactly(300d, 200d, 100d, 50d);
    }

    @Test
    void filters_by_status_matching_InstallmentStatusCalculator_semantics() {
        seed();
        List<EntityId> unitIds = List.of(unitA, unitB);

        Page<Installment> notSettledPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.NOT_SETTLED), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                PageRequest.of(0, 10));
        assertThat(notSettledPage.content()).extracting(Installment::getId)
                .containsExactly(notSettled.getId(), otherNotSettled.getId());

        Page<Installment> partiallySettledPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.PARTIALLY_SETTLED), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                PageRequest.of(0, 10));
        assertThat(partiallySettledPage.content()).extracting(Installment::getId).containsExactly(partiallySettled.getId());

        Page<Installment> settledPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.SETTLED), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                PageRequest.of(0, 10));
        assertThat(settledPage.content()).extracting(Installment::getId).containsExactly(settled.getId());
    }

    @Test
    void filters_by_due_date_range() {
        seed();

        Page<Installment> page = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                new InstallmentFilter(Set.of(), TODAY, TODAY.plusDays(15), InstallmentSortField.DUE_DATE, SortDirection.ASC),
                PageRequest.of(0, 10));

        assertThat(page.content()).extracting(Installment::getId).containsExactly(notSettled.getId());
    }

    @Test
    void filters_by_installment_call_id() {
        seed();
        InstallmentCallId callId = InstallmentCallId.newId();
        Installment fromCall = Installment.create(InstallmentId.newId(), unitA, TODAY, Amount.of(new BigDecimal("120")), callId);
        installmentAdapter.save(fromCall);

        Page<Installment> page = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                new InstallmentFilter(Set.of(), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC, callId),
                PageRequest.of(0, 10));

        assertThat(page.content()).extracting(Installment::getId).containsExactly(fromCall.getId());
    }

    @Test
    void paginates_results() {
        seed();

        Page<Installment> firstPage = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                InstallmentFilter.defaultFilter(), PageRequest.of(0, 2));

        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.totalElements()).isEqualTo(4);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }
}
