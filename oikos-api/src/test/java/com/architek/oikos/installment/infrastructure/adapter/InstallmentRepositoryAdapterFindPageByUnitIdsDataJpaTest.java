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

import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentSortField;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.installment.infrastructure.mapper.AllocationPersistenceMapperImpl;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InstallmentRepositoryAdapter.class, InstallmentPersistenceMapperImpl.class,
        AllocationRepositoryAdapter.class, AllocationPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class InstallmentRepositoryAdapterFindPageByUnitIdsDataJpaTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 22);

    @Autowired
    private InstallmentRepositoryAdapter installmentAdapter;

    @Autowired
    private AllocationRepositoryAdapter allocationAdapter;

    private EntityId unitA;
    private EntityId unitB;
    private Installment notPaid;
    private Installment overdue;
    private Installment partiallyPaid;
    private Installment paid;

    private void seed() {
        unitA = EntityId.newId();
        unitB = EntityId.newId();
        EntityId otherUnit = EntityId.newId();
        EntityId account = EntityId.newId();

        notPaid = Installment.create(InstallmentId.newId(), account, unitA, TODAY.plusDays(10), Amount.of(new BigDecimal("100")));
        installmentAdapter.save(notPaid);

        overdue = Installment.create(InstallmentId.newId(), account, unitA, TODAY.minusDays(5), Amount.of(new BigDecimal("200")));
        installmentAdapter.save(overdue);

        partiallyPaid = Installment.create(InstallmentId.newId(), account, unitB, TODAY.plusDays(20), Amount.of(new BigDecimal("300")));
        installmentAdapter.save(partiallyPaid);
        allocationAdapter.save(Allocation.create(AllocationId.newId(), EntityId.newId(),partiallyPaid.getId(), Amount.of(new BigDecimal("50"))));

        paid = Installment.create(InstallmentId.newId(), account, unitB, TODAY.plusDays(1), Amount.of(new BigDecimal("400")));
        installmentAdapter.save(paid);
        allocationAdapter.save(Allocation.create(AllocationId.newId(), EntityId.newId(),paid.getId(), Amount.of(new BigDecimal("400"))));

        // belongs to a unit outside the residence being queried - must never appear
        installmentAdapter.save(Installment.create(InstallmentId.newId(), account, otherUnit, TODAY, Amount.of(new BigDecimal("999"))));
    }

    @Test
    void restricts_results_to_the_given_unit_ids_and_sorts_by_amount_descending() {
        seed();

        Page<Installment> page = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                new InstallmentFilter(Set.of(), null, null, InstallmentSortField.AMOUNT, SortDirection.DESC),
                TODAY, PageRequest.of(0, 10));

        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.content()).extracting(i -> i.getAmount().value().doubleValue())
                .containsExactly(400d, 300d, 200d, 100d);
    }

    @Test
    void filters_by_status_matching_InstallmentStatusCalculator_semantics() {
        seed();
        List<EntityId> unitIds = List.of(unitA, unitB);

        Page<Installment> overduePage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.OVERDUE), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                TODAY, PageRequest.of(0, 10));
        assertThat(overduePage.content()).extracting(Installment::getId).containsExactly(overdue.getId());

        Page<Installment> paidPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.PAID), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                TODAY, PageRequest.of(0, 10));
        assertThat(paidPage.content()).extracting(Installment::getId).containsExactly(paid.getId());

        Page<Installment> partiallyPaidPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.PARTIALLY_PAID), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                TODAY, PageRequest.of(0, 10));
        assertThat(partiallyPaidPage.content()).extracting(Installment::getId).containsExactly(partiallyPaid.getId());

        Page<Installment> notPaidPage = installmentAdapter.findPageByUnitIds(unitIds,
                new InstallmentFilter(Set.of(InstallmentStatus.NOT_PAID), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC),
                TODAY, PageRequest.of(0, 10));
        assertThat(notPaidPage.content()).extracting(Installment::getId).containsExactly(notPaid.getId());
    }

    @Test
    void filters_by_due_date_range() {
        seed();

        Page<Installment> page = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                new InstallmentFilter(Set.of(), TODAY, TODAY.plusDays(15), InstallmentSortField.DUE_DATE, SortDirection.ASC),
                TODAY, PageRequest.of(0, 10));

        assertThat(page.content()).extracting(Installment::getId).containsExactly(paid.getId(), notPaid.getId());
    }

    @Test
    void paginates_results() {
        seed();

        Page<Installment> firstPage = installmentAdapter.findPageByUnitIds(List.of(unitA, unitB),
                InstallmentFilter.defaultFilter(), TODAY, PageRequest.of(0, 2));

        assertThat(firstPage.content()).hasSize(2);
        assertThat(firstPage.totalElements()).isEqualTo(4);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }
}
