-- =========================================================================
-- V8: rework of the invitation feature after the first end-to-end test pass.
-- PRIVATE_WITH_UNIT is dropped entirely (never exercised outside this dev
-- cycle, and the "pick a unit at invitation-creation time" flow it enabled
-- turned out to add complexity without a real use case - both remaining
-- types always leave the unit choice to the invitee). PRIVATE_WITHOUT_UNIT
-- is renamed to plain PRIVATE now that there is nothing left to disambiguate
-- it from. unit_id on invitation was PRIVATE_WITH_UNIT's only consumer, so
-- it is dropped along with its FK/index rather than left dangling.
-- consumed_email records who actually accepted a PRIVATE invitation, so a
-- manager can spot (after the fact, non-blocking) that it was accepted by
-- someone other than the invited address.
-- =========================================================================

UPDATE invitation SET type = 'PRIVATE' WHERE type = 'PRIVATE_WITHOUT_UNIT';
DELETE FROM invitation WHERE type = 'PRIVATE_WITH_UNIT';

ALTER TABLE invitation DROP CONSTRAINT chk_invitation_unit_required;
ALTER TABLE invitation DROP CONSTRAINT fk_invitation_unit;
DROP INDEX IF EXISTS idx_invitation_unit;
ALTER TABLE invitation DROP COLUMN unit_id;

ALTER TABLE invitation ADD COLUMN consumed_email VARCHAR(150);
