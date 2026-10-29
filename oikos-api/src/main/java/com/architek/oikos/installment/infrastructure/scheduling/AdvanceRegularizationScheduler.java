package com.architek.oikos.installment.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.in.RegularizeAdvancesUseCase;

/**
 * The daily regularization sweep: imputes each lot's advance on the calls that
 * fell due since its last payment. Without it, a lot that paid the whole year
 * up front would keep both a debt and an advance until its next payment.
 * Disabled under test (oikos.installment.advance-regularization.enabled).
 */
@Component
@ConditionalOnProperty(prefix = "oikos.installment.advance-regularization", name = "enabled", havingValue = "true")
public class AdvanceRegularizationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdvanceRegularizationScheduler.class);

    private final RegularizeAdvancesUseCase regularizeAdvancesUseCase;

    public AdvanceRegularizationScheduler(RegularizeAdvancesUseCase regularizeAdvancesUseCase) {
        this.regularizeAdvancesUseCase = regularizeAdvancesUseCase;
    }

    @Scheduled(cron = "${oikos.installment.advance-regularization.cron}", zone = "UTC")
    public void sweep() {
        int regularized = regularizeAdvancesUseCase.regularizeAllDue();
        log.info("Advance regularization sweep: {} lot(s) regularized", regularized);
    }
}
