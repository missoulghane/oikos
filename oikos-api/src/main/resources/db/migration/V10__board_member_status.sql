-- =========================================================================
-- Board member validation workflow: a board member created from the
-- invitation-acceptance flow starts PENDING_VALIDATION (user_id records who
-- accepted) and must be explicitly validated by an admin before becoming
-- ACTIVE and having the PROPERTY_BOARD_MEMBER role granted. Board members
-- added directly by an admin stay ACTIVE with no user_id.
-- =========================================================================

ALTER TABLE board_member ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE board_member ADD COLUMN user_id UUID;
