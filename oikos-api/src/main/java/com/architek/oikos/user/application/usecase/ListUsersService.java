package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.ListUsersUseCase;
import com.architek.oikos.user.application.query.ListUsersQuery;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class ListUsersService implements ListUsersUseCase {

    private final UserRepository userRepository;

    public ListUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserView> listUsers(ListUsersQuery query) {
        return userRepository.findAll(query.pageRequest(), query.criteria()).map(UserView::of);
    }
}
