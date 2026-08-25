package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Narrowly named after the notification events invitation produces
 * (REQUEST_RECEIVED, REQUEST_DECIDED) rather than a generic
 * {@code create(type, ...)} - add a sibling method here, not a type
 * parameter, if a third producer event is ever needed from this module.
 */
public interface NotificationPort {

    void notifyRequestReceived(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath);

    /**
     * Une demande tranchée, annoncée aux deux bords : au demandeur (son accès
     * est ouvert ou refusé) et au reste du bureau, pour qu'une demande déjà
     * traitée ne soit pas réexaminée par un second membre.
     */
    void notifyRequestDecided(EntityId recipientUserId, EntityId propertyId, String title, String body, String linkPath);
}
