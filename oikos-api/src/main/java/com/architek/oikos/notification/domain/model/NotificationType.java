package com.architek.oikos.notification.domain.model;

/**
 * Categorizes a Notification for display (icon/tone) and future filtering.
 * Named after the concrete producer events identified in the target UX
 * (GAP.md §3.1) - none of them create a notification yet (no producer is
 * wired into CreateNotificationUseCase as of this module's introduction),
 * this catalog only documents what the eventual fanout is expected to cover:
 * INSTALLMENT_OVERDUE (an owner's unpaid installment passes its due date),
 * GENERAL_MEETING_CALLED (an AG is convened), RELAUNCH_TO_VALIDATE (a board
 * member's relaunch letter is queued for admin approval), REQUEST_RECEIVED
 * (a membership request/invitation acceptance lands on a manager's desk).
 * GENERAL is the catch-all for anything that doesn't fit those four.
 */
public enum NotificationType {
    INSTALLMENT_OVERDUE,
    GENERAL_MEETING_CALLED,
    RELAUNCH_TO_VALIDATE,
    REQUEST_RECEIVED,
    GENERAL
}
