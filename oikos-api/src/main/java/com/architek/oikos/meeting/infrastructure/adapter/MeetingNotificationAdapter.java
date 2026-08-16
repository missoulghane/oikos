package com.architek.oikos.meeting.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.MeetingNotificationPort;
import com.architek.oikos.notification.application.command.CreateNotificationCommand;
import com.architek.oikos.notification.application.port.in.CreateNotificationUseCase;
import com.architek.oikos.notification.domain.model.NotificationType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * First producer wired into CreateNotificationUseCase, and the one
 * NotificationType.GENERAL_MEETING_CALLED was named after before this module
 * existed. Titles and bodies are in French: they are read by the
 * copropriétaires, like every other user-facing string.
 */
@Component
public class MeetingNotificationAdapter implements MeetingNotificationPort {

    private final CreateNotificationUseCase createNotificationUseCase;

    public MeetingNotificationAdapter(CreateNotificationUseCase createNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
    }

    @Override
    public void notifyGeneralMeetingCalled(EntityId recipientUserId, EntityId propertyId, String meetingTitle,
                                            String linkPath) {
        createNotificationUseCase.create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.GENERAL_MEETING_CALLED, "Convocation à une assemblée générale",
                "Vous êtes convoqué(e) à : " + meetingTitle, linkPath));
    }

    @Override
    public void notifyMinutesPublished(EntityId recipientUserId, EntityId propertyId, String meetingTitle,
                                        String linkPath) {
        createNotificationUseCase.create(new CreateNotificationCommand(recipientUserId, propertyId,
                NotificationType.GENERAL_MEETING_CALLED, "Procès-verbal disponible",
                "Le procès-verbal de « " + meetingTitle + " » vient d'être publié.", linkPath));
    }
}
