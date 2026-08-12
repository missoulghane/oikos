package com.architek.oikos.notification.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.notification.application.command.RegisterDevicePushTokenCommand;
import com.architek.oikos.notification.application.port.in.RegisterDevicePushTokenUseCase;
import com.architek.oikos.notification.domain.model.DevicePushToken;
import com.architek.oikos.notification.domain.repository.DevicePushTokenRepository;

@Component
public class RegisterDevicePushTokenService implements RegisterDevicePushTokenUseCase {

    private final DevicePushTokenRepository devicePushTokenRepository;

    public RegisterDevicePushTokenService(DevicePushTokenRepository devicePushTokenRepository) {
        this.devicePushTokenRepository = devicePushTokenRepository;
    }

    @Override
    @Transactional
    public void register(RegisterDevicePushTokenCommand command) {
        devicePushTokenRepository.save(DevicePushToken.register(command.userId(), command.expoPushToken()));
    }
}
