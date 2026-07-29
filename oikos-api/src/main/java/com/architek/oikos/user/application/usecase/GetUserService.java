package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.query.GetUserQuery;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserView getUser(GetUserQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException(query.userId()));
        return UserView.of(user);
    }
}
