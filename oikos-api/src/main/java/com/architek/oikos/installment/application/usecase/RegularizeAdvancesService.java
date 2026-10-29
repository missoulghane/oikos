package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;
import com.architek.oikos.installment.application.port.in.RegularizeAdvancesUseCase;
import com.architek.oikos.installment.application.port.in.RegularizeUnitInstallmentsUseCase;
import com.architek.oikos.installment.domain.exception.NothingToRegularizeException;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.UnitOfProperty;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Both triggers date the regularization today: what is imputed is what is due
 * today, and the entry lands in the exercise covering today.
 *
 * <p>No transaction here: RegularizeUnitInstallmentsService opens its own
 * (REQUIRES_NEW) per lot, so in the sweep one lot that fails - no open
 * exercise covering today, an account not configured - is logged and skipped
 * without rolling back the lots already done.
 *
 * <p>The sweep has no user behind it: its entries are attributed to
 * SYSTEM_ACTOR_ID, a fixed id that matches no account (journal_entry's
 * author column carries no foreign key, unlike the documents a payment
 * produces).
 */
@Component
public class RegularizeAdvancesService implements RegularizeAdvancesUseCase {

    static final EntityId SYSTEM_ACTOR_ID = EntityId.of(new UUID(0L, 0L));

    private static final Logger log = LoggerFactory.getLogger(RegularizeAdvancesService.class);

    private final InstallmentRepository installmentRepository;
    private final RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase;
    private final Clock clock;

    public RegularizeAdvancesService(InstallmentRepository installmentRepository,
                                      RegularizeUnitInstallmentsUseCase regularizeUnitInstallmentsUseCase,
                                      Clock clock) {
        this.installmentRepository = installmentRepository;
        this.regularizeUnitInstallmentsUseCase = regularizeUnitInstallmentsUseCase;
        this.clock = clock;
    }

    @Override
    public RegularizeUnitInstallmentsResult regularizeUnit(EntityId propertyId, EntityId unitId,
                                                           EntityId requestedByUserId) {
        return regularizeUnitInstallmentsUseCase.regularize(new RegularizeUnitInstallmentsCommand(propertyId, unitId,
                LocalDate.now(clock), requestedByUserId));
    }

    @Override
    public int regularizeAllDue() {
        LocalDate today = LocalDate.now(clock);
        int regularized = 0;
        for (UnitOfProperty unit : installmentRepository.findUnitsWithOutstandingDue(today)) {
            try {
                regularizeUnitInstallmentsUseCase.regularize(new RegularizeUnitInstallmentsCommand(unit.propertyId(),
                        unit.unitId(), today, SYSTEM_ACTOR_ID));
                regularized++;
            } catch (NothingToRegularizeException nothingLeftToImpute) {
                // The ordinary case: the lot owes something due but holds no advance.
            } catch (RuntimeException e) {
                log.error("Advance regularization sweep failed for unit {} of property {}", unit.unitId(),
                        unit.propertyId(), e);
            }
        }
        return regularized;
    }
}
