-- =========================================================================
-- V7: Invitation feature. Lets a manager/board admin let an unknown user
-- create an account on a residence and self-attach to a unit, without a
-- manager having to create the Party by hand first (property.AddUnitOwnerService's
-- existing flow assumes the opposite order: lot affected first, invited
-- after). invitation is its own aggregate keyed by its own id (NOT party_id,
-- unlike party_invitation_token) because for every type no Party may exist
-- yet when the invitation is issued. membership_request is PUBLIC-type only:
-- one invitation can receive many pending candidacies (the unit stays
-- selectable by other candidates while pending, per product decision), so it
-- cannot be folded into the invitation row itself.
-- =========================================================================

CREATE TABLE invitation (
    id                  UUID PRIMARY KEY,
    property_id         UUID NOT NULL,
    type                VARCHAR(30) NOT NULL,
    target_role         VARCHAR(30) NOT NULL,
    unit_id             UUID,
    target_email        VARCHAR(150),
    token               VARCHAR(255) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at          TIMESTAMPTZ NOT NULL,
    created_by_user_id  UUID NOT NULL,
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_invitation_token UNIQUE (token),
    CONSTRAINT fk_invitation_property FOREIGN KEY (property_id) REFERENCES property (id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_unit FOREIGN KEY (unit_id) REFERENCES unit (id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT chk_invitation_unit_required
        CHECK (type <> 'PRIVATE_WITH_UNIT' OR unit_id IS NOT NULL),
    CONSTRAINT chk_invitation_email_required
        CHECK (type = 'PUBLIC' OR target_email IS NOT NULL)
);

CREATE INDEX idx_invitation_property_status ON invitation (property_id, status);
CREATE INDEX idx_invitation_unit ON invitation (unit_id);

CREATE TABLE membership_request (
    id                  UUID PRIMARY KEY,
    invitation_id       UUID NOT NULL,
    property_id         UUID NOT NULL,
    unit_id             UUID NOT NULL,
    party_id            UUID NOT NULL,
    user_id             UUID NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decided_at          TIMESTAMPTZ,
    decided_by_user_id  UUID,
    rejection_reason    VARCHAR(300),
    created_date        TIMESTAMPTZ NOT NULL,
    last_modified_date  TIMESTAMPTZ NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_membership_request_invitation FOREIGN KEY (invitation_id) REFERENCES invitation (id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_request_unit FOREIGN KEY (unit_id) REFERENCES unit (id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_request_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_membership_request_decided_by FOREIGN KEY (decided_by_user_id) REFERENCES app_user (id)
);

CREATE INDEX idx_membership_request_unit_status ON membership_request (unit_id, status);
CREATE INDEX idx_membership_request_property_status ON membership_request (property_id, status);

-- Dedicated permission: neither party:invite (already granted to MEMBER-tier
-- roles, broader than intended here) nor property:member:invite (scoped to
-- inviting board/manager staff, not owners) is the right fit - see the
-- design note on PropertyAccessEvaluator.canManageInvitations. Manager and
-- board admin have identical permissions on invitations, so this mirrors
-- exactly the ADMIN-tier-only bundle canInviteMemberOnProperty already uses.
INSERT INTO permission (key, description) VALUES
    ('invitation:manage', 'Create/disable invitations and review membership requests');

INSERT INTO role_permission (role_name, permission_key) VALUES
    ('PROPERTY_BOARD_ADMIN', 'invitation:manage'),
    ('PROPERTY_MANAGER_ADMIN', 'invitation:manage'),
    ('ROLE_ADMIN', 'invitation:manage');
