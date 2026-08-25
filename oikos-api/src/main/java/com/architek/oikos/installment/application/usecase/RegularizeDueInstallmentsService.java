package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.command.RegularizeDueInstallmentsCommand;
import com.architek.oikos.installment.application.command.RegularizePropertyInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeDueInstallmentsResult;
import com.architek.oikos.installment.application.dto.RegularizePropertyInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizeDueInstallmentsUseCase;
import com.architek.oikos.installment.application.port.in.RegularizePropertyInstallmentsUseCase;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Imputes each lot's advance onto its echeances the day they fall due, over the
 * whole product. This is what makes an echeance settle itself: the money is
 * already there, only the due date was missing, and nobody has to press
 * anything for the two to meet.
 *
 * It replaces the manual "Regulariser les avances" action, which asked a syndic
 * to notice, every morning and on every lot, something a date could tell.
 * The mechanism underneath is unchanged - RegularizeUnitInstallmentsService and
 * its FIFO allocation - only the trigger moved: {@code asOf} is the cutoff, so a
 * sweep run today imputes exactly the echeances due up to today and leaves the
 * rest alone. Running it twice in a day is harmless: the second pass finds the
 * advance already consumed and has nothing to impute.
 *
 * Deliberately NOT @Transactional. RegularizePropertyInstallmentsService is,
 * and a failure on one copropriété inside a shared transaction would mark the
 * whole sweep rollback-only - one closed exercise, and no other copropriété
 * would be regularized that night. Without a transaction here, each property
 * opens and commits its own, and a failure costs exactly that property (logged,
 * and picked up by the next run once the cause is fixed).
 */
@Component
public class RegularizeDueInstallmentsService implements RegularizeDueInstallmentsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegularizeDueInstallmentsService.class);

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final RegularizePropertyInstallmentsUseCase regularizePropertyInstallmentsUseCase;

    public RegularizeDueInstallmentsService(PropertyDirectoryPort propertyDirectoryPort,
                                             RegularizePropertyInstallmentsUseCase regularizePropertyInstallmentsUseCase) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.regularizePropertyInstallmentsUseCase = regularizePropertyInstallmentsUseCase;
    }

    @Override
    public RegularizeDueInstallmentsResult regularize(RegularizeDueInstallmentsCommand command) {
        List<EntityId> propertyIds = propertyDirectoryPort.listAllIds();
        int failed = 0;
        int unitsRegularized = 0;
        BigDecimal totalAmountApplied = BigDecimal.ZERO;

        for (EntityId propertyId : propertyIds) {
            try {
                RegularizePropertyInstallmentsResult result = regularizePropertyInstallmentsUseCase
                        .regularize(new RegularizePropertyInstallmentsCommand(propertyId, command.asOf(),
                                command.createdByUserId()));
                unitsRegularized += result.regularizedUnits().size();
                totalAmountApplied = totalAmountApplied.add(result.totalAmountApplied());
            } catch (RuntimeException e) {
                // Typically an exercise not open, or a chart of accounts left
                // incomplete on that copropriété: real, worth seeing, and no
                // reason for the other copropriétés to go unregularized.
                failed++;
                log.error("Imputation of due echeances failed for property {} - other properties are unaffected, "
                        + "the next run picks this one up", propertyId, e);
            }
        }

        log.info("Due-echeance imputation as of {}: {} MAD imputed on {} lot(s), {} propert(ies) visited, {} failed",
                command.asOf(), totalAmountApplied, unitsRegularized, propertyIds.size(), failed);
        return new RegularizeDueInstallmentsResult(propertyIds.size(), failed, unitsRegularized, totalAmountApplied);
    }
}
