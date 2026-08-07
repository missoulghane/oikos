-- =========================================================================
-- V9: extends invitation to also cover "join the board" invitations
-- (PROPERTY_BOARD_MEMBER), alongside the existing PROPERTY_OWNER flow.
-- target_board_role only applies to board invitations (which BoardRole the
-- invitee is being invited into); null for every owner invitation.
-- =========================================================================

ALTER TABLE invitation ADD COLUMN target_board_role VARCHAR(50);
