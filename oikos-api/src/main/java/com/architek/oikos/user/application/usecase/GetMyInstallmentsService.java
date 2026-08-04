package com.architek.oikos.user.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.port.in.GetMyInstallmentsUseCase;
import com.architek.oikos.user.application.port.out.InstallmentDirectoryPort;
import com.architek.oikos.user.application.port.out.OwnedInstallmentView;
import com.architek.oikos.user.application.port.out.UnitDirectoryPort;
import com.architek.oikos.user.application.query.GetMyInstallmentsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetMyInstallmentsService implements GetMyInstallmentsUseCase {

    private final UserRepository userRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final InstallmentDirectoryPort installmentDirectoryPort;

    public GetMyInstallmentsService(UserRepository userRepository, UnitDirectoryPort unitDirectoryPort,
                                     InstallmentDirectoryPort installmentDirectoryPort) {
        this.userRepository = userRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.installmentDirectoryPort = installmentDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnedInstallmentView> getMyInstallments(GetMyInstallmentsQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        return user.getLinkedPartyIds().stream()
                .flatMap(partyId -> unitDirectoryPort.listUnitsOwnedByParty(partyId).stream())
                .flatMap(unit -> installmentDirectoryPort.listInstallmentsForUnit(unit.unitId()).stream())
                .toList();
    }
}
