-- =========================================================================
-- CONTACT FEATURE: identity record of a physical person, independent of any
-- application account (see app_user, which will reference contact by id).
-- =========================================================================

CREATE TABLE contact (
    id                  UUID PRIMARY KEY,
    last_name           VARCHAR(100) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(20),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_contact_email UNIQUE (email)
);
