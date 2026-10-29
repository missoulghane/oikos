package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.architek.oikos.accounting.infrastructure.persistence.JournalEntryJpaRepository;

/**
 * Guards the treasury-account operations query against PostgreSQL type-resolution
 * failures. H2 accepts {@code '%'||NULL||'%'} happily, so these cases only break
 * on the real database: a null {@code :search} used to bind as an untyped
 * parameter, which PostgreSQL resolved to {@code bytea||bytea} and then rejected
 * with {@code function lower(bytea) does not exist}.
 *
 * <p>No fixture data - the assertion is that the SQL executes at all, which is
 * exactly what was broken.
 */
class JournalEntrySearchPostgresIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private JournalEntryJpaRepository repository;

    @Test
    void searchByTreasuryAccount_executes_when_every_optional_filter_is_null() {
        assertThat(repository.searchByTreasuryAccount(UUID.randomUUID(), UUID.randomUUID(), null, null, null, null,
                PageRequest.of(0, 20))).isEmpty();
    }

    @Test
    void searchByTreasuryAccount_executes_when_every_optional_filter_is_set() {
        assertThat(repository.searchByTreasuryAccount(UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), "VIR-2026", "POSTED", PageRequest.of(0, 20))).isEmpty();
    }

    @Test
    void searchByTreasuryAccount_executes_when_the_order_by_comes_from_the_pageable() {
        // The query carries no `order by` of its own any more: Spring appends one
        // from the Pageable, which this checks PostgreSQL accepts.
        assertThat(repository.searchByTreasuryAccount(UUID.randomUUID(), UUID.randomUUID(), null, null, null, null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "pieceNumber")))).isEmpty();
    }

    /**
     * Every column the journal table sorts on, the amount included: it is a
     * subquery over the lines (@Formula), which an empty table still makes
     * PostgreSQL parse and plan.
     */
    @ParameterizedTest
    @ValueSource(strings = {"pieceDate", "pieceNumber", "externalReference", "debitTotal", "status"})
    void searchByTreasuryAccount_executes_with_each_sortable_column(String property) {
        assertThat(repository.searchByTreasuryAccount(UUID.randomUUID(), UUID.randomUUID(), null, null, null, null,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, property).and(Sort.by("pieceDate"))))).isEmpty();
    }
}
