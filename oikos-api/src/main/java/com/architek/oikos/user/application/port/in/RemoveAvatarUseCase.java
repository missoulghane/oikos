package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface RemoveAvatarUseCase {

    UserView removeAvatar(UserId userId);
}
