package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.UpdateUserProfileCommand;
import com.architek.oikos.user.application.dto.UserView;

public interface UpdateUserProfileUseCase {

    UserView updateProfile(UpdateUserProfileCommand command);
}
