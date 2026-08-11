-- =========================================================================
-- V22: optional lot label a GROUP conversation concerns (e.g. "Appartement 3")
-- - disambiguates a thread with a recipient who owns several units in the
-- property. Free text, GROUP only, never a foreign key to the unit registry:
-- purely a display hint set once at compose time, see Conversation's javadoc.
-- =========================================================================

ALTER TABLE conversation ADD COLUMN concerns_unit VARCHAR(100);

ALTER TABLE conversation ADD CONSTRAINT chk_conversation_concerns_unit
    CHECK (concerns_unit IS NULL OR type = 'GROUP');
