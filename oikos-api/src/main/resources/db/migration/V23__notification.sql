-- =========================================================================
-- V23: notification inbox (target UX principle 6, GAP.md: "Notifications
-- toujours globales"). A single-recipient notice, never shared between
-- several readers (unlike conversation) - see Notification's javadoc.
-- property_id is nullable: most notifications concern one property, but the
-- type also covers an eventual account-wide notice that doesn't. No
-- producer inserts a row here yet (see NotificationType's javadoc) - this
-- table sits empty until a future domain event handler in another module
-- calls CreateNotificationUseCase.
-- =========================================================================

CREATE TABLE notification (
    id                 UUID PRIMARY KEY,
    user_id            UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    property_id        UUID REFERENCES property (id) ON DELETE CASCADE,
    type               VARCHAR(30) NOT NULL
        CHECK (type IN ('INSTALLMENT_OVERDUE', 'GENERAL_MEETING_CALLED', 'RELAUNCH_TO_VALIDATE', 'REQUEST_RECEIVED', 'GENERAL')),
    title              VARCHAR(200) NOT NULL,
    body               VARCHAR(1000),
    link_path          VARCHAR(300),
    read_at            TIMESTAMPTZ,
    created_date       TIMESTAMPTZ NOT NULL,
    last_modified_date TIMESTAMPTZ NOT NULL,
    version            BIGINT NOT NULL DEFAULT 0
);

-- Backs ListMyNotificationsUseCase's findByUserIdOrderByCreatedDateDesc.
CREATE INDEX idx_notification_user_created ON notification (user_id, created_date DESC);

-- Backs GetUnreadNotificationCountUseCase (the header bell badge) - partial
-- index since only the unread subset is ever queried by this predicate,
-- same pattern as uk_conversation_broadcast_property in V17__messaging.sql.
CREATE INDEX idx_notification_user_unread ON notification (user_id) WHERE read_at IS NULL;
