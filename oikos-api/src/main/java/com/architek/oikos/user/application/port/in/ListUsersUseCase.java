package com.architek.oikos.user.application.port.in;

import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.query.ListUsersQuery;

public interface ListUsersUseCase {

    Page<UserView> listUsers(ListUsersQuery query);
}
