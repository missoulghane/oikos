package com.architek.oikos.user.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.port.in.GetMyUnitsUseCase;
import com.architek.oikos.user.application.port.out.OwnedUnitView;
import com.architek.oikos.user.application.port.out.UnitDirectoryPort;
import com.architek.oikos.user.application.query.GetMyUnitsQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetMyUnitsService implements GetMyUnitsUseCase {

    private final UserRepository userRepository;
    private final UnitDirectoryPort unitDirectoryPort;

    public GetMyUnitsService(UserRepository userRepository, UnitDirectoryPort unitDirectoryPort) {
        this.userRepository = userRepository;
        this.unitDirectoryPort = unitDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnedUnitView> getMyUnits(GetMyUnitsQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        return user.getLinkedPartyIds().stream()
                .flatMap(partyId -> unitDirectoryPort.listUnitsOwnedByParty(partyId).stream())
                .toList();
    }
}
