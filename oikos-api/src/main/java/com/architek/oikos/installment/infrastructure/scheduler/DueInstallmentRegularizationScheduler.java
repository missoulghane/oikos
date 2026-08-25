package com.architek.oikos.installment.infrastructure.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.command.RegularizeDueInstallmentsCommand;
import com.architek.oikos.installment.application.port.in.RegularizeDueInstallmentsUseCase;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The clock that drives the imputation: once a night, every echeance that has
 * reached its due date gets whatever advance its lot carries imputed on it.
 *
 * Inbound adapter, hence infrastructure - the "when" is a deployment concern,
 * the "what" lives in RegularizeDueInstallmentsService. The cron is a property
 * so an environment can move or, by pointing it at an hour it never reaches,
 * effectively disable the run without a release.
 *
 * Single instance assumed, like the in-memory rate limiter (see the
 * oikos.security.rate-limit comment in application.yml): two API containers
 * would each run the sweep, and two concurrent runs reading the same advance
 * could impute it twice. The deployment runs one; a second one would need a
 * lock (ShedLock or equivalent) before this is switched on.
 */
@Component
public class DueInstallmentRegularizationScheduler {

    /**
     * Author stamped on the entries the sweep posts.
     * {@code journal_entry.created_by_user_id} is NOT NULL but carries no foreign
     * key onto app_user (unlike document.uploaded_by), so a fixed, well-known id
     * is both possible and better than borrowing a real syndic's identity for
     * something no human did: an entry created by this id is one the nightly
     * imputation posted, and reads that way in the journal.
     */
    static final EntityId SYSTEM_USER_ID = EntityId.of(UUID.fromString("00000000-0000-0000-0000-00000000d0e5"));

    private final RegularizeDueInstallmentsUseCase regularizeDueInstallmentsUseCase;
    private final Clock clock;

    public DueInstallmentRegularizationScheduler(RegularizeDueInstallmentsUseCase regularizeDueInstallmentsUseCase,
                                                  Clock clock) {
        this.regularizeDueInstallmentsUseCase = regularizeDueInstallmentsUseCase;
        this.clock = clock;
    }

    /**
     * Zone pinned to UTC, matching the application Clock (ClockConfiguration):
     * the hour it fires and the date it sweeps are then read in the same
     * timezone, so they cannot disagree about which day it is.
     */
    @Scheduled(cron = "${oikos.installment.regularization-cron}", zone = "UTC")
    public void regularizeDueInstallments() {
        regularizeDueInstallmentsUseCase.regularize(
                new RegularizeDueInstallmentsCommand(LocalDate.now(clock), SYSTEM_USER_ID));
    }
}
