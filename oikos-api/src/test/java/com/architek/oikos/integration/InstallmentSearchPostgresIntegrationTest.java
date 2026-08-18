package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCollectionSummary;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentSortField;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The échéances listing, on a real PostgreSQL - which is the whole point.
 *
 * <p>Its filters are all optional, and an optional filter is a parameter that
 * is sometimes null. H2 in {@code MODE=PostgreSQL} binds an untyped null
 * happily; PostgreSQL refuses to prepare the statement at all ("could not
 * determine data type of parameter"), because {@code ? is null} on its own
 * tells it nothing about what type the parameter is. So every combination of
 * "this filter is not set" has to be exercised here, where it can fail.
 *
 * <p>No data is seeded: the failure is at statement preparation, before a row
 * is ever read.
 */
class InstallmentSearchPostgresIntegrationTest extends PostgresIntegrationTestBase {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 18);

    @Autowired
    private InstallmentRepository installmentRepository;

    private void search(InstallmentFilter filter) {
        installmentRepository.findPageByUnitIds(List.of(EntityId.newId()), filter, PageRequest.of(0, 20));
    }

    @Test
    void searches_with_no_filter_set_at_all() {
        assertThatCode(() -> search(InstallmentFilter.defaultFilter())).doesNotThrowAnyException();
    }

    @Test
    void searches_with_the_not_yet_due_cutoff_lifted() {
        // Unticking "à échoir" in the tracking screen: every other filter may be set, and
        // that one is not. It is the combination the product actually failed on.
        assertThatCode(() -> search(new InstallmentFilter(Set.of(InstallmentStatus.NOT_SETTLED), TODAY.minusMonths(1),
                TODAY.plusMonths(1), InstallmentSortField.DUE_DATE, SortDirection.DESC, null, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void summarises_what_is_left_to_collect_without_reading_every_row() {
        // The dashboard badge: an aggregate, so it goes through the same PostgreSQL type
        // resolution as the listing and deserves the same proof that it runs there.
        assertThatCode(() -> installmentRepository.summariseCollectible(List.of(EntityId.newId()), TODAY))
                .doesNotThrowAnyException();
        assertThat(installmentRepository.summariseCollectible(List.of(), TODAY))
                .isEqualTo(InstallmentCollectionSummary.EMPTY);
    }

    @Test
    void searches_with_the_cutoff_set_and_the_dates_lifted() {
        assertThatCode(() -> search(new InstallmentFilter(Set.of(), null, null, InstallmentSortField.DUE_DATE,
                SortDirection.ASC, null, TODAY))).doesNotThrowAnyException();
    }
}
