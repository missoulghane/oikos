package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.UpdateAvatarCommand;
import com.architek.oikos.user.application.dto.UserView;

public interface UpdateAvatarUseCase {

    UserView updateAvatar(UpdateAvatarCommand command);
}
