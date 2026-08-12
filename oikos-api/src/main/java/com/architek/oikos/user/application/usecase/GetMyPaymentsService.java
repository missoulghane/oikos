package com.architek.oikos.user.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.port.in.GetMyPaymentsUseCase;
import com.architek.oikos.user.application.port.out.OwnedPaymentView;
import com.architek.oikos.user.application.port.out.PaymentDirectoryPort;
import com.architek.oikos.user.application.port.out.UnitDirectoryPort;
import com.architek.oikos.user.application.query.GetMyPaymentsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetMyPaymentsService implements GetMyPaymentsUseCase {

    private final UserRepository userRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final PaymentDirectoryPort paymentDirectoryPort;

    public GetMyPaymentsService(UserRepository userRepository, UnitDirectoryPort unitDirectoryPort,
                                 PaymentDirectoryPort paymentDirectoryPort) {
        this.userRepository = userRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.paymentDirectoryPort = paymentDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnedPaymentView> getMyPayments(GetMyPaymentsQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        return user.getLinkedPartyIds().stream()
                .flatMap(partyId -> unitDirectoryPort.listUnitsOwnedByParty(partyId).stream())
                .flatMap(unit -> paymentDirectoryPort.listPaymentsForUnit(unit.unitId()).stream())
                .toList();
    }
}
