package com.architek.oikos.invitation.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * propertyId is the generic {@link EntityId} rather than property's own
 * PropertyId, to keep invitation decoupled from property's identity types
 * (same rationale as PropertyRoleGrant.propertyId) - resolved back to
 * property-specific types only at the adapter boundary
 * (InvitationUnitDirectoryAdapter/InvitationPropertyDirectoryAdapter).
 * targetRole is the raw name of a user.domain.model.PropertyRole constant
 * (e.g. "PROPERTY_OWNER"), kept as a plain String here rather than
 * referencing that enum directly - unlike PropertyController, which
 * references it from the web layer, no cross-module domain-to-domain
 * reference exists elsewhere in this codebase, so this keeps invitation's
 * domain layer decoupled from user's; resolved back to PropertyRole only in
 * InvitationAccountDirectoryAdapter, right before calling into user's port.
 * targetPartyId is the contact a PRIVATE owner invitation is addressed to -
 * elle part de sa fiche. C'est lui, et non l'adresse email, qui rattache la
 * demande au bon contact une fois le compte créé : un invité qui s'inscrit
 * avec une autre adresse ferait sinon naître un second contact pour la même
 * personne, et l'attribution du lot échouerait - il appartient déjà au
 * premier. C'est aussi ce qui permet à la fiche de savoir qu'une invitation
 * court encore (voir PartyAccountStatus.INVITED). Nul pour un lien PUBLIC et
 * pour une invitation au bureau.
 * targetUnitId is the lot a PRIVATE owner invitation designates : le syndic
 * invite un contact précis pour un lot précis, depuis sa fiche. Nul pour une
 * invitation PUBLIC (le lien circule, chacun choisit son lot) et pour une
 * invitation au bureau (un siège, pas un lot). Ce n'est qu'un point de
 * départ, pas un verrou : l'invité peut désigner un autre lot depuis la page
 * d'accueil (« ce n'est pas votre lot ? »), et c'est le syndic qui tranche à
 * la validation de la demande - d'où le choix d'un champ nullable plutôt que
 * du type PRIVATE_WITH_UNIT qui existait ici autrefois et qui, lui, figeait
 * le lot.
 * consumedEmail records the email of whoever actually accepted a PRIVATE
 * invitation (null until consumed), so a manager can spot - after the fact,
 * non-blocking - a PRIVATE invitation accepted by someone other than
 * targetEmail; see Invitation.emailMismatch().
 * targetBoardRole is the raw name of a property.domain.valueobject.BoardRole
 * constant (e.g. "PRESIDENT"), kept as a plain String for the same
 * decoupling reason as targetRole - never referenced directly, resolved back
 * to BoardRole only in InvitationBoardDirectoryAdapter. Null for every
 * PROPERTY_OWNER invitation; set only when targetRole is
 * "PROPERTY_BOARD_MEMBER" (CreateBoardInvitationService).
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class Invitation {

    private final InvitationId id;
    private final EntityId propertyId;
    private final InvitationType type;
    private final String targetRole;
    private final EmailVO targetEmail;
    private final String token;
    private final InvitationStatus status;
    private final Instant expiresAt;
    private final EntityId createdByUserId;
    private final EmailVO consumedEmail;
    private final String targetBoardRole;
    private final EntityId targetUnitId;
    private final EntityId targetPartyId;

    private Invitation(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                        EmailVO targetEmail, String token, InvitationStatus status, Instant expiresAt,
                        EntityId createdByUserId, EmailVO consumedEmail, String targetBoardRole, EntityId targetUnitId,
                        EntityId targetPartyId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.targetRole = Objects.requireNonNull(targetRole, "targetRole must not be null");
        this.targetEmail = targetEmail;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        this.token = token;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
        this.consumedEmail = consumedEmail;
        this.targetBoardRole = targetBoardRole;
        this.targetUnitId = targetUnitId;
        this.targetPartyId = targetPartyId;
    }

    public static Invitation issue(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                                    EmailVO targetEmail, String token, Instant expiresAt, EntityId createdByUserId,
                                    String targetBoardRole, EntityId targetUnitId, EntityId targetPartyId) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.ACTIVE,
                expiresAt, createdByUserId, null, targetBoardRole, targetUnitId, targetPartyId);
    }

    public static Invitation reconstruct(InvitationId id, EntityId propertyId, InvitationType type,
                                          String targetRole, EmailVO targetEmail, String token,
                                          InvitationStatus status, Instant expiresAt, EntityId createdByUserId,
                                          EmailVO consumedEmail, String targetBoardRole, EntityId targetUnitId,
                                          EntityId targetPartyId) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, status, expiresAt,
                createdByUserId, consumedEmail, targetBoardRole, targetUnitId, targetPartyId);
    }

    public Invitation disable() {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.DISABLED,
                expiresAt, createdByUserId, consumedEmail, targetBoardRole, targetUnitId, targetPartyId);
    }

    /**
     * Remet en service un lien désactivé, sur son propre jeton : c'est la
     * seule façon de rouvrir le lien public d'une copropriété, qui ne se
     * recrée jamais (voir CreateInvitationService) - un QR code imprimé,
     * affiché dans le hall ou collé sur un avis reste valable après une
     * fermeture temporaire.
     *
     * <p>L'échéance est repoussée au passage : un lien désactivé six mois
     * reviendrait sinon déjà expiré, ACTIVE en base et inutilisable en
     * pratique (isUsable exige les deux).
     *
     * <p>Une invitation CONSUMED ne se rouvre pas : son jeton a déjà servi et
     * la réactiver rendrait le même lien à usage unique utilisable deux fois.
     */
    public Invitation enable(Instant newExpiresAt) {
        if (status == InvitationStatus.CONSUMED) {
            throw new IllegalStateException("A consumed invitation cannot be re-enabled");
        }
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.ACTIVE,
                Objects.requireNonNull(newExpiresAt, "newExpiresAt must not be null"), createdByUserId, consumedEmail,
                targetBoardRole, targetUnitId, targetPartyId);
    }

    /** consumedEmail is the accepting caller's resolved account email - recorded even when it matches
     * targetEmail, so every PRIVATE invitation carries an audit trail of who actually consumed it. */
    public Invitation consume(EmailVO consumedEmail) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.CONSUMED,
                expiresAt, createdByUserId, consumedEmail, targetBoardRole, targetUnitId, targetPartyId);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsable(Instant now) {
        return status == InvitationStatus.ACTIVE && !isExpired(now);
    }

    /** True once a PRIVATE invitation has been accepted by an email different from the one it targeted -
     * never blocks acceptance (see AcceptInvitationService), only surfaced to managers for a later look. */
    public boolean emailMismatch() {
        return consumedEmail != null && targetEmail != null && !consumedEmail.equals(targetEmail);
    }

    public InvitationId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public InvitationType getType() {
        return type;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public EmailVO getTargetEmail() {
        return targetEmail;
    }

    public String getToken() {
        return token;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public EntityId getCreatedByUserId() {
        return createdByUserId;
    }

    public EmailVO getConsumedEmail() {
        return consumedEmail;
    }

    public String getTargetBoardRole() {
        return targetBoardRole;
    }

    /** Le lot désigné par une invitation privée, nul partout ailleurs - voir le javadoc de la classe. */
    public EntityId getTargetUnitId() {
        return targetUnitId;
    }

    /** Le contact destinataire d'une invitation privée, nul partout ailleurs - voir le javadoc de la classe. */
    public EntityId getTargetPartyId() {
        return targetPartyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Invitation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
