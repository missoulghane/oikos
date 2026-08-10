-- =========================================================================
-- V17: internal messaging. GROUP conversations between 2 or more members of
-- the same property (the sender plus one or more applicatively-chosen
-- recipients, Outlook-style "To: A, B, C" composition - never idempotent:
-- composing a new GROUP conversation to the same set of people never reuses
-- an earlier conversation, always creates a brand-new one, see
-- Conversation/StartGroupConversationService), plus one persistent
-- BROADCAST announcement channel per property (board/manager -> every
-- current member) - never a one-off broadcast message, always reusing the
-- same channel (see Conversation/SendBroadcastMessageService). GROUP
-- participants are stored in conversation_participant (one row per
-- conversation/user, no uniqueness beyond the pair itself - see
-- ConversationPersistenceMapper). BROADCAST membership is resolved
-- dynamically from the property's current roster at read time, never
-- stored: a BROADCAST conversation has no rows in conversation_participant.
-- message is a historian-only, immutable table (no last_modified_date/
-- version, unlike conversation): a message is never edited nor deleted.
-- conversation_read_marker is created lazily (upsert) on first read, even
-- for a BROADCAST channel never opened before. subject is the GROUP
-- conversation's email-style title, set once at compose time - a BROADCAST
-- channel never has one (its identity is already the fixed channel label
-- the frontend renders), enforced by chk_conversation_subject below.
-- =========================================================================

CREATE TABLE conversation (
    id                      UUID PRIMARY KEY,
    property_id             UUID NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    type                    VARCHAR(20) NOT NULL CHECK (type IN ('GROUP', 'BROADCAST')),
    created_by              UUID NOT NULL REFERENCES app_user (id),
    subject                 VARCHAR(200),
    created_date            TIMESTAMPTZ NOT NULL,
    last_modified_date      TIMESTAMPTZ NOT NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_conversation_subject CHECK (
        (type = 'GROUP' AND subject IS NOT NULL) OR (type = 'BROADCAST' AND subject IS NULL)
    )
);

CREATE UNIQUE INDEX uk_conversation_broadcast_property
    ON conversation (property_id) WHERE type = 'BROADCAST';
CREATE INDEX idx_conversation_property ON conversation (property_id);

CREATE TABLE conversation_participant (
    conversation_id UUID NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES app_user (id),
    CONSTRAINT pk_conversation_participant PRIMARY KEY (conversation_id, user_id)
);

CREATE INDEX idx_conversation_participant_user ON conversation_participant (user_id);

CREATE TABLE message (
    id              UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES app_user (id),
    body            TEXT NOT NULL,
    created_date    TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_message_conversation ON message (conversation_id, created_date);

CREATE TABLE conversation_read_marker (
    conversation_id      UUID NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    user_id              UUID NOT NULL REFERENCES app_user (id),
    last_read_message_id UUID REFERENCES message (id),
    last_read_at         TIMESTAMPTZ,
    CONSTRAINT pk_conversation_read_marker PRIMARY KEY (conversation_id, user_id)
);

-- Dedicated permission for posting to a property's BROADCAST channel - a
-- GROUP conversation itself is gated only by property membership
-- (PropertyAccessEvaluator.isPropertyMember), not by a fine-grained
-- permission, same rationale as ownsUnit/ownsParty elsewhere.
INSERT INTO permission (key, description) VALUES
    ('messaging:broadcast', 'Post an announcement to every member of a property');

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_BOARD_ADMIN', 'messaging:broadcast'),
    ('PROPERTY_BOARD_MEMBER', 'messaging:broadcast'),
    ('PROPERTY_MANAGER_ADMIN', 'messaging:broadcast'),
    ('PROPERTY_MANAGER_MEMBER', 'messaging:broadcast'),
    ('ROLE_ADMIN', 'messaging:broadcast');
