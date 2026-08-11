package com.architek.oikos.notification.application.port.in;

import com.architek.oikos.notification.application.command.MarkNotificationReadCommand;

public interface MarkNotificationReadUseCase {

    void markRead(MarkNotificationReadCommand command);
}
