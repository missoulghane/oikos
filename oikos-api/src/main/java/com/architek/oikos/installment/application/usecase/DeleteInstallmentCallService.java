package com.architek.oikos.installment.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.DeleteInstallmentCallCommand;
import com.architek.oikos.installment.application.port.in.DeleteInstallmentCallUseCase;
import com.architek.oikos.installment.domain.exception.InstallmentCallNotFoundException;
import com.architek.oikos.installment.domain.repository.InstallmentCallRepository;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;

/**
 * Deletes an installment call along with every installment it raised.
 * Unconditional: no accounting settlement to check, since installment no
 * longer creates any accounting movement when a call is raised (that
 * coupling was removed - accounting will get its own deletion story
 * separately).
 */
@Component
public class DeleteInstallmentCallService implements DeleteInstallmentCallUseCase {

    private final InstallmentCallRepository installmentCallRepository;
    private final InstallmentRepository installmentRepository;

    public DeleteInstallmentCallService(InstallmentCallRepository installmentCallRepository,
                                         InstallmentRepository installmentRepository) {
        this.installmentCallRepository = installmentCallRepository;
        this.installmentRepository = installmentRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteInstallmentCallCommand command) {
        installmentCallRepository.findById(command.id())
                .orElseThrow(() -> new InstallmentCallNotFoundException(command.id()));
        installmentRepository.deleteAllByInstallmentCallId(command.id());
        installmentCallRepository.deleteById(command.id());
    }
}
