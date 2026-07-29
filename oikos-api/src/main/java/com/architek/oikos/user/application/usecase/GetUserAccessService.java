package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetUserAccessService implements GetUserAccessUseCase {

    private final UserRepository userRepository;

    public GetUserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccessView getAccess(GetUserAccessQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        return UserAccessView.of(user);
    }
}
