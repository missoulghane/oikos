package com.architek.oikos.installment.domain.valueobject;

import java.time.LocalDate;
import java.util.Set;

import com.architek.oikos.shared.domain.pagination.SortDirection;

/**
 * Search criteria for an installment listing. statuses is empty when no
 * status filter is requested (matches every status), rather than null, so
 * callers never need a null check. Filtering by status still happens at the
 * repository/SQL level despite status being computed, never stored (RG011) -
 * see InstallmentRepository#findPageByUnitIds.
 *
 * hideNotYetDueAsOf, when set, drops the installments that are not settled and
 * fall due after that date: money that will be owed, and is not yet. A date
 * rather than a boolean, so the repository holds no notion of "now" - the
 * caller decides what today means. Distinct from dueDateTo, which would also
 * hide an installment already settled in advance.
 */
public record InstallmentFilter(Set<InstallmentStatus> statuses, LocalDate dueDateFrom, LocalDate dueDateTo,
                                    InstallmentSortField sortField, SortDirection sortDirection,
                                    InstallmentCallId installmentCallId, LocalDate hideNotYetDueAsOf) {

    public InstallmentFilter {
        statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
        sortField = sortField == null ? InstallmentSortField.DUE_DATE : sortField;
        sortDirection = sortDirection == null ? SortDirection.ASC : sortDirection;
        if (dueDateFrom != null && dueDateTo != null && dueDateFrom.isAfter(dueDateTo)) {
            throw new IllegalArgumentException("dueDateFrom must not be after dueDateTo");
        }
    }

    public InstallmentFilter(Set<InstallmentStatus> statuses, LocalDate dueDateFrom, LocalDate dueDateTo,
                              InstallmentSortField sortField, SortDirection sortDirection) {
        this(statuses, dueDateFrom, dueDateTo, sortField, sortDirection, null, null);
    }

    public InstallmentFilter(Set<InstallmentStatus> statuses, LocalDate dueDateFrom, LocalDate dueDateTo,
                              InstallmentSortField sortField, SortDirection sortDirection,
                              InstallmentCallId installmentCallId) {
        this(statuses, dueDateFrom, dueDateTo, sortField, sortDirection, installmentCallId, null);
    }

    public static InstallmentFilter defaultFilter() {
        return new InstallmentFilter(Set.of(), null, null, InstallmentSortField.DUE_DATE, SortDirection.ASC, null, null);
    }
}
