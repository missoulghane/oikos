-- =========================================================================
-- V18: message drafts. A draft is a message-in-progress owned by exactly
-- one user (created_by) - unlike conversation/message it may be genuinely
-- incomplete (subject/body nullable, recipient list possibly empty) since
-- it only represents "not sent yet". is_broadcast mirrors the GROUP/
-- BROADCAST split on conversation: a broadcast draft never has stored
-- recipients (its "recipient" will be resolved dynamically to the whole
-- property, same as a real BROADCAST conversation), enforced by
-- MessageDraft's constructor rather than a DB CHECK (no NOT NULL/blank
-- constraint to enforce here, unlike conversation's chk_conversation_subject
-- - a draft's subject/body only get validated when it is actually sent, by
-- reusing StartGroupConversationService/SendBroadcastMessageService's own
-- validation). Sending a draft deletes its row (see SendMessageDraftService)
-- - there is no "sent" status to track here, only "exists = still a draft".
-- =========================================================================

CREATE TABLE message_draft (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    created_by          UUID NOT NULL REFERENCES app_user (id),
    is_broadcast        BOOLEAN NOT NULL DEFAULT FALSE,
    subject             VARCHAR(200),
    body                TEXT,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_message_draft_created_by ON message_draft (created_by);

CREATE TABLE message_draft_recipient (
    message_draft_id UUID NOT NULL REFERENCES message_draft (id) ON DELETE CASCADE,
    user_id          UUID NOT NULL REFERENCES app_user (id),
    CONSTRAINT pk_message_draft_recipient PRIMARY KEY (message_draft_id, user_id)
);
