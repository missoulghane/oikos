package com.architek.oikos.installment.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.out.AccountLedgerPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.PropertyUnitPricingPort;
import com.architek.oikos.installment.application.port.out.UnitPriceLine;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.exception.AccountNotFoundException;
import com.architek.oikos.installment.domain.exception.InstallmentCallAlreadyExistsException;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Generates a full installment call for a property in one batch: one
 * Installment (+ triggering debit Movement, RG003) per unit, priced from
 * UnitTypePricing. A unit is skipped and reported rather than blocking the
 * whole property when either its type has no configured price (not an
 * error - see UnitTypePricing), or it has no UNIT account yet (accounts are
 * provisioned separately, not automatically at unit creation - same
 * precondition already true of the PROPERTY account below). The property's
 * PROPERTY account, on the other hand, is checked to exist up front
 * (AccountBalanceService's mirroring precondition, see RG010bis): unlike a
 * single unit, it gates the entire batch, so failing fast there avoids
 * aborting partway through. All ledger effects (movement + balance/mirror)
 * are applied through AccountLedgerPort, never accounting's repositories
 * directly (rule 4).
 */
@Component
public class GenerateInstallmentCallService implements GenerateInstallmentCallUseCase {

    private static final String INSTALLMENT_MOVEMENT_LABEL = "Appel de cotisation";

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final PropertyUnitPricingPort propertyUnitPricingPort;
    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;
    private final AccountLedgerPort accountLedgerPort;
    private final AutoAllocationEngine autoAllocationEngine;

    public GenerateInstallmentCallService(PropertyDirectoryPort propertyDirectoryPort,
                                         PropertyUnitPricingPort propertyUnitPricingPort,
                                         InstallmentCallRepository installmentCallRepository,
                                         InstallmentRepository installmentRepository,
                                         AccountLedgerPort accountLedgerPort,
                                         AutoAllocationEngine autoAllocationEngine) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.propertyUnitPricingPort = propertyUnitPricingPort;
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
        this.accountLedgerPort = accountLedgerPort;
        this.autoAllocationEngine = autoAllocationEngine;
    }

    @Override
    @Transactional
    public GenerateInstallmentCallResult generate(GenerateInstallmentCallCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        if (installmentCallRepository.existsByPropertyIdAndPeriod(command.propertyId(), command.period())) {
            throw new InstallmentCallAlreadyExistsException(command.propertyId(), command.period());
        }
        // Fail fast: every unit-side movement below mirrors onto this account
        // (RG010bis); better to reject the whole call now than partway through.
        if (!accountLedgerPort.propertyAccountExists(command.propertyId())) {
            throw AccountNotFoundException.forProperty(command.propertyId());
        }

        List<UnitPriceLine> unitPrices = propertyUnitPricingPort.listUnitPrices(command.propertyId());
        List<UnitPriceLine> priced = unitPrices.stream().filter(line -> line.price() != null).toList();
        List<EntityId> skippedUnitIds = new ArrayList<>(unitPrices.stream().filter(line -> line.price() == null)
                .map(UnitPriceLine::unitId).toList());

        InstallmentCall savedCall = installmentCallRepository.save(
                InstallmentCall.create(InstallmentCallId.newId(), command.propertyId(), command.period(), command.dueDate()));

        List<EntityId> chargedUnitIds = new ArrayList<>();
        for (UnitPriceLine line : priced) {
            Optional<EntityId> unitAccountId = accountLedgerPort.findUnitAccountId(line.unitId());
            if (unitAccountId.isEmpty()) {
                skippedUnitIds.add(line.unitId());
                continue;
            }
            EntityId accountId = unitAccountId.get();

            Installment installment = Installment.create(InstallmentId.newId(), accountId, line.unitId(),
                    command.dueDate(), Amount.of(line.price()), savedCall.getId());
            installmentRepository.save(installment);

            accountLedgerPort.recordDebit(accountId, line.price(), INSTALLMENT_MOVEMENT_LABEL);
            autoAllocationEngine.allocate(accountId);

            chargedUnitIds.add(line.unitId());
        }

        return new GenerateInstallmentCallResult(InstallmentCallView.from(savedCall), chargedUnitIds, skippedUnitIds);
    }
}
