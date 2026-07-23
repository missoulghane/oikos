package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentCallSummaryView;
import com.architek.oikos.installment.application.port.in.ListInstallmentCallsByPropertyUseCase;
import com.architek.oikos.installment.application.query.ListInstallmentCallsByPropertyQuery;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListInstallmentCallsByPropertyService implements ListInstallmentCallsByPropertyUseCase {

    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;

    public ListInstallmentCallsByPropertyService(InstallmentCallRepository installmentCallRepository,
                                                 InstallmentRepository installmentRepository) {
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstallmentCallSummaryView> listInstallmentCalls(ListInstallmentCallsByPropertyQuery query) {
        return installmentCallRepository.findPageByPropertyId(query.propertyId(), query.pageRequest())
                .map(this::toSummary);
    }

    private InstallmentCallSummaryView toSummary(InstallmentCall installmentCall) {
        List<Installment> installments = installmentRepository.findAllByInstallmentCallId(installmentCall.getId());
        BigDecimal totalAmount = installments.stream()
                .map(installment -> installment.getAmount().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new InstallmentCallSummaryView(installmentCall.getId(), installmentCall.getPropertyId(),
                installmentCall.getPeriod(), installmentCall.getDueDate(), installments.size(), totalAmount);
    }
}
