package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.dto.AvatarView;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface GetAvatarUseCase {

    AvatarView getAvatar(UserId userId);
}
