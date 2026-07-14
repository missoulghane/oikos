package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.query.GetUserQuery;

public interface GetUserUseCase {

    UserView getUser(GetUserQuery query);
}
