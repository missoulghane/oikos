package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.installment.application.dto.InstallmentAllocationView;
import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.dto.RecordOwnerPaymentResult;
import com.architek.oikos.installment.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.installment.application.port.out.OwnerPaymentJournalEntryPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.domain.exception.PropertyNotFoundException;
import com.architek.oikos.installment.domain.exception.UnitNotFoundException;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.model.PaymentAllocationCalculator;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.repository.PaymentRepository;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * P2/P3 (spec &sect;4.1/&sect;4.2): the FIFO imputation/advance split
 * (PaymentAllocationCalculator) and the resulting Installment.outstandingAmount
 * updates are done here, in installment - the module that owns both Payment
 * and Installment - rather than pushed back through accounting's
 * UpdateInstallmentSettlementUseCase in-port (reserved for a future
 * accounting-triggered lettrage path, e.g. P10 reversing a payment).
 * Note: this posts one aggregate CREDIT line per (unit, imputed-total) and
 * one per (unit, advance-total) rather than one line per settled
 * Installment - the generic journal_entry_line-to-journal_entry_line
 * `allocation` table (I8's full audit trail) is not yet populated; today's
 * source of truth for "how much of this installment is settled" is
 * Installment.outstandingAmount, kept in sync in the same transaction (see
 * ADR 0001).
 */
@Component
public class RecordOwnerPaymentService implements RecordOwnerPaymentUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final OwnerPaymentJournalEntryPort ownerPaymentJournalEntryPort;

    public RecordOwnerPaymentService(PropertyDirectoryPort propertyDirectoryPort, UnitDirectoryPort unitDirectoryPort,
                                      InstallmentRepository installmentRepository, PaymentRepository paymentRepository,
                                      OwnerPaymentJournalEntryPort ownerPaymentJournalEntryPort) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.ownerPaymentJournalEntryPort = ownerPaymentJournalEntryPort;
    }

    @Override
    @Transactional
    public RecordOwnerPaymentResult record(RecordOwnerPaymentCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        if (!unitDirectoryPort.exists(command.unitId())) {
            throw new UnitNotFoundException(command.unitId());
        }

        List<Installment> unitInstallments = installmentRepository.findAllByUnitId(command.unitId());
        List<PaymentAllocationCalculator.UnsettledInstallment> unsettled = unitInstallments.stream()
                .filter(installment -> installment.getOutstandingAmount().signum() > 0)
                .map(installment -> new PaymentAllocationCalculator.UnsettledInstallment(
                        EntityId.of(installment.getId().asUuid()), installment.getDueDate(),
                        installment.getOutstandingAmount()))
                .toList();
        PaymentAllocationCalculator.Result allocationResult = PaymentAllocationCalculator.allocate(command.amount(), unsettled);

        BigDecimal imputedTotal = allocationResult.allocations().stream()
                .map(PaymentAllocationCalculator.InstallmentAllocation::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        EntityId journalEntryId = ownerPaymentJournalEntryPort.postOwnerPaymentEntry(command.propertyId(),
                command.unitId(), command.treasuryAccountId(), command.valueDate(), imputedTotal,
                allocationResult.advanceAmount(), "Reglement coproprietaire", command.createdByUserId());

        for (PaymentAllocationCalculator.InstallmentAllocation allocation : allocationResult.allocations()) {
            Installment installment = unitInstallments.stream()
                    .filter(candidate -> candidate.getId().asUuid().equals(allocation.installmentId().value()))
                    .findFirst()
                    .orElseThrow();
            BigDecimal newOutstanding = installment.getOutstandingAmount().subtract(allocation.amount());
            installmentRepository.save(installment.withOutstandingAmount(newOutstanding));
        }

        Payment payment = Payment.create(PaymentId.newId(), command.propertyId(), command.unitId(), command.mode(),
                command.valueDate(), Amount.of(command.amount()), journalEntryId);
        Payment savedPayment = paymentRepository.save(payment);

        List<InstallmentAllocationView> allocationViews = allocationResult.allocations().stream()
                .map(allocation -> new InstallmentAllocationView(allocation.installmentId(), allocation.amount()))
                .toList();
        return new RecordOwnerPaymentResult(PaymentView.from(savedPayment), allocationViews, allocationResult.advanceAmount());
    }
}
