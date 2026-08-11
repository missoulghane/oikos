-- =========================================================================
-- V21: BOARD_PRIVATE conversation type (a subject-bearing thread visible
-- only to a property's current staff, membership resolved dynamically like
-- BROADCAST rather than stored - see Conversation's javadoc) and
-- Message.sender_identity (OWNER | BOARD - which hat the sender chose to
-- post under, a property of the message, not the conversation, see
-- SenderIdentity/Message).
--
-- Backfill rule for existing messages (no real production data predates this
-- migration, only dev-seeded rows, so this is a best-effort approximation
-- rather than a historically exact reconstruction): every BROADCAST message
-- becomes BOARD (only staff can post there, unambiguous); every other
-- message becomes BOARD if its sender *currently* holds a staff role on the
-- conversation's property, else OWNER.
-- =========================================================================

ALTER TABLE conversation DROP CONSTRAINT conversation_type_check;
ALTER TABLE conversation ADD CONSTRAINT conversation_type_check
    CHECK (type IN ('GROUP', 'BOARD_PRIVATE', 'BROADCAST'));

ALTER TABLE conversation DROP CONSTRAINT chk_conversation_subject;
ALTER TABLE conversation ADD CONSTRAINT chk_conversation_subject CHECK (
    (type IN ('GROUP', 'BOARD_PRIVATE') AND subject IS NOT NULL) OR (type = 'BROADCAST' AND subject IS NULL)
);

ALTER TABLE message ADD COLUMN sender_identity VARCHAR(10);

UPDATE message m
SET sender_identity = 'BOARD'
FROM conversation c
WHERE m.conversation_id = c.id
  AND c.type = 'BROADCAST';

UPDATE message m
SET sender_identity = 'BOARD'
FROM conversation c
WHERE m.conversation_id = c.id
  AND m.sender_identity IS NULL
  AND EXISTS (
      SELECT 1
      FROM app_user_party_role apr
      JOIN app_user_party aup ON aup.party_id = apr.party_id
      WHERE aup.app_user_id = m.sender_id
        AND apr.property_id = c.property_id
        AND apr.role IN ('PROPERTY_BOARD_ADMIN', 'PROPERTY_BOARD_MEMBER', 'PROPERTY_MANAGER_ADMIN', 'PROPERTY_MANAGER_MEMBER')
  );

UPDATE message SET sender_identity = 'OWNER' WHERE sender_identity IS NULL;

ALTER TABLE message ALTER COLUMN sender_identity SET NOT NULL;
ALTER TABLE message ADD CONSTRAINT chk_message_sender_identity CHECK (sender_identity IN ('OWNER', 'BOARD'));

-- BOARD_PRIVATE threads are aggregated per-property the same way BROADCAST
-- channels already are (ConversationAggregator/findAllByPropertyIdsAndType) -
-- no new index needed beyond the existing idx_conversation_property.
