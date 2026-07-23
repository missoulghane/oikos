package com.architek.oikos.installment.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.InstallmentCallDetailView;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.repository.AllocationRepository;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

@Component
public class GetInstallmentCallService implements GetInstallmentCallUseCase {

    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;
    private final AllocationRepository allocationRepository;
    private final Clock clock;

    public GetInstallmentCallService(InstallmentCallRepository installmentCallRepository,
                                     InstallmentRepository installmentRepository,
                                     AllocationRepository allocationRepository, Clock clock) {
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
        this.allocationRepository = allocationRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public InstallmentCallDetailView getInstallmentCall(GetInstallmentCallQuery query) {
        InstallmentCall installmentCall = installmentCallRepository.findById(query.id())
                .orElseThrow(() -> new InstallmentCallNotFoundException(query.id()));

        LocalDate today = LocalDate.now(clock);
        List<InstallmentView> installments = installmentRepository.findAllByInstallmentCallId(installmentCall.getId()).stream()
                .map(installment -> InstallmentViewFactory.build(installment, allocationRepository, today))
                .toList();

        return new InstallmentCallDetailView(InstallmentCallView.from(installmentCall), installments);
    }
}
