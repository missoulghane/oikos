package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.command.RegisterDevicePushTokenCommand;

public interface RegisterDevicePushTokenUseCase {

    void register(RegisterDevicePushTokenCommand command);
}
