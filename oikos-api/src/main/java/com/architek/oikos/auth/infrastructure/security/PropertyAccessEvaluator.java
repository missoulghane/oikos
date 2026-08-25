package com.architek.oikos.auth.infrastructure.security;

import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.GetDocumentUseCase;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.application.query.GetPaymentQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.invitation.application.port.in.GetInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetMembershipRequestUseCase;
import com.architek.oikos.invitation.application.query.GetInvitationQuery;
import com.architek.oikos.invitation.application.query.GetMembershipRequestQuery;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemQuery;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.messaging.application.dto.ConversationView;
import com.architek.oikos.messaging.application.dto.MessageDraftView;
import com.architek.oikos.messaging.application.port.in.GetConversationUseCase;
import com.architek.oikos.messaging.application.port.in.GetMessageDraftUseCase;
import com.architek.oikos.messaging.application.query.GetConversationQuery;
import com.architek.oikos.messaging.application.query.GetMessageDraftQuery;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.notification.application.dto.NotificationView;
import com.architek.oikos.notification.application.port.in.GetNotificationUseCase;
import com.architek.oikos.notification.application.query.GetNotificationQuery;
import com.architek.oikos.notification.domain.valueobject.NotificationId;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * SpEL-callable authorization bean (referenced from @PreAuthorize expressions
 * as {@code @propertyAccess.xxx(...)}). Resolves access per request from the
 * database (no property-scoped claims embedded in the JWT - see
 * GetUserAccessUseCase), so a newly granted/revoked access takes effect
 * immediately without requiring the caller to re-authenticate. Every
 * cross-feature lookup goes through a port-in use case (rule 6), never a
 * repository directly.
 */
@Component("propertyAccess")
public class PropertyAccessEvaluator {

    private final GetUserAccessUseCase getUserAccessUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final GetBuildingUseCase getBuildingUseCase;
    private final GetInstallmentUseCase getInstallmentUseCase;
    private final GetInstallmentCallUseCase getInstallmentCallUseCase;
    private final GetPaymentUseCase getPaymentUseCase;
    private final ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final GetInvitationUseCase getInvitationUseCase;
    private final GetMembershipRequestUseCase getMembershipRequestUseCase;
    private final GetConversationUseCase getConversationUseCase;
    private final GetMessageDraftUseCase getMessageDraftUseCase;
    private final GetDocumentUseCase getDocumentUseCase;
    private final GetNotificationUseCase getNotificationUseCase;
    private final GetGeneralMeetingUseCase getGeneralMeetingUseCase;
    private final GetAgendaItemUseCase getAgendaItemUseCase;
    private final GetConvocationUseCase getConvocationUseCase;

    public PropertyAccessEvaluator(GetUserAccessUseCase getUserAccessUseCase,
                                    GetUnitUseCase getUnitUseCase,
                                    GetBuildingUseCase getBuildingUseCase,
                                    GetInstallmentUseCase getInstallmentUseCase,
                                    GetInstallmentCallUseCase getInstallmentCallUseCase,
                                    GetPaymentUseCase getPaymentUseCase,
                                    ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase,
                                    GetPartyUseCase getPartyUseCase,
                                    GetInvitationUseCase getInvitationUseCase,
                                    GetMembershipRequestUseCase getMembershipRequestUseCase,
                                    GetConversationUseCase getConversationUseCase,
                                    GetMessageDraftUseCase getMessageDraftUseCase,
                                    GetDocumentUseCase getDocumentUseCase,
                                    GetNotificationUseCase getNotificationUseCase,
                                    GetGeneralMeetingUseCase getGeneralMeetingUseCase,
                                    GetAgendaItemUseCase getAgendaItemUseCase,
                                    GetConvocationUseCase getConvocationUseCase) {
        this.getUserAccessUseCase = getUserAccessUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.getBuildingUseCase = getBuildingUseCase;
        this.getInstallmentUseCase = getInstallmentUseCase;
        this.getInstallmentCallUseCase = getInstallmentCallUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.listUnitOwnershipsByUnitUseCase = listUnitOwnershipsByUnitUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.getInvitationUseCase = getInvitationUseCase;
        this.getMembershipRequestUseCase = getMembershipRequestUseCase;
        this.getConversationUseCase = getConversationUseCase;
        this.getMessageDraftUseCase = getMessageDraftUseCase;
        this.getDocumentUseCase = getDocumentUseCase;
        this.getNotificationUseCase = getNotificationUseCase;
        this.getGeneralMeetingUseCase = getGeneralMeetingUseCase;
        this.getAgendaItemUseCase = getAgendaItemUseCase;
        this.getConvocationUseCase = getConvocationUseCase;
    }

    /** ADMIN is a global, JWT-embedded authority (same trust boundary as the existing
     * hasAuthority('ROLE_ADMIN') checks elsewhere) - checked directly off the token so
     * admins short-circuit every rule below without a DB round-trip. */
    public boolean isAdmin(Authentication authentication) {
        return isAdminAuthority(authentication);
    }

    public boolean managesProperty(Authentication authentication, String propertyId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesBuilding(Authentication authentication, String buildingId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getBuildingUseCase.getBuilding(new GetBuildingQuery(BuildingId.of(buildingId)))
                .propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesUnit(Authentication authentication, String unitId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId))).propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesInstallment(Authentication authentication, String installmentId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String unitId = getInstallmentUseCase.getInstallment(new GetInstallmentQuery(InstallmentId.of(installmentId)))
                .unitId().toString();
        return managesUnit(authentication, unitId) || ownsUnit(authentication, unitId);
    }

    public boolean managesInstallmentCall(Authentication authentication, String installmentCallId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getInstallmentCallUseCase
                .getInstallmentCall(new GetInstallmentCallQuery(InstallmentCallId.of(installmentCallId)))
                .installmentCall().propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesParty(Authentication authentication, String partyId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId))).propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    /** True for ADMIN, or for an account holding property:create anywhere (any PROPERTY_BOARD_ADMIN/
     * PROPERTY_MANAGER_ADMIN grant). Used to gate the authenticated "create a property" endpoint:
     * a MEMBER-tier or OWNER account, or a plain USER with no property grant at all, must not be
     * able to create additional properties this way (the public self-registration bootstrap flows
     * are separate, unrelated code paths). Replaces the former role-name-based isManagerOfAny. */
    public boolean canCreateProperty(Authentication authentication) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return access(authentication).canCreateProperty();
    }

    /** Permission-based check, scoped to a specific property: true for ADMIN, or if the caller's
     * role on this property (or a global role) bundles the given permission. */
    public boolean hasPermission(Authentication authentication, String propertyId, Permission permission) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return access(authentication).hasPermission(propertyId, permission);
    }

    /** Gates POST /properties/{id}/managers: the property's own ADMIN-tier holder (board or
     * manager-firm admin) may invite a MEMBER onto it, not just the platform admin. */
    public boolean canInviteMemberOnProperty(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.PROPERTY_MEMBER_INVITE);
    }

    /** Gates the invitation module's manager-facing endpoints scoped by property id
     * (create/list invitations, list membership requests) - manager and board admin have
     * identical rights here (see invitation:manage's bundle in V7__invitation.sql). */
    public boolean canManageInvitations(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.INVITATION_MANAGE);
    }

    /** Same check as canManageInvitations, but for endpoints addressed by invitation id
     * rather than property id (get/disable a single invitation) - resolves the owning
     * property first. */
    public boolean canManageInvitation(Authentication authentication, String invitationId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getInvitationUseCase.getInvitation(new GetInvitationQuery(InvitationId.of(invitationId)))
                .propertyId().toString();
        return access(authentication).hasPermission(propertyId, Permission.INVITATION_MANAGE);
    }

    /** Same check as canManageInvitations, but for endpoints addressed by membership
     * request id (accept/reject a membership request) - resolves the owning property first. */
    public boolean canManageMembershipRequest(Authentication authentication, String membershipRequestId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getMembershipRequestUseCase
                .getMembershipRequest(new GetMembershipRequestQuery(MembershipRequestId.of(membershipRequestId)))
                .propertyId().toString();
        return access(authentication).hasPermission(propertyId, Permission.INVITATION_MANAGE);
    }

    /** Concrete "accounting/installment write" example of a permission-scoped (rather than
     * generic managesProperty) check. */
    public boolean canWriteInstallmentCall(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.INSTALLMENT_CALL_WRITE);
    }

    /** Gates read access to the accounting module (exercises, financial accounts, journal,
     * expenses, unit accounts of the whole property) - board/manager tiers only. */
    public boolean canReadAccounting(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.ACCOUNTING_READ);
    }

    /** Gates write access to the accounting module (opening an exercise, recording expenses/
     * transfers/payments). */
    public boolean canWriteAccounting(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.ACCOUNTING_WRITE);
    }

    /** Gates read access to a property's general meetings (agenda, convocations, published
     * minutes) - the only meeting permission PROPERTY_OWNER holds. */
    public boolean canReadMeetings(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.MEETING_READ);
    }

    /** Gates every write on a property's general meetings: creating one, editing its agenda,
     * generating/sending convocations, opening and closing the session, recording votes. */
    public boolean canManageMeetings(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.MEETING_MANAGE);
    }

    /** Gates validating and publishing a general meeting's minutes. Deliberately separate from
     * canManageMeetings: publication is irreversible and broadcast to every owner, so a role may
     * legitimately run a meeting without being allowed to publish its record of it. */
    public boolean canPublishMeetingMinutes(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.MEETING_MINUTES_PUBLISH);
    }

    /** Same rule as canReadMeetings, for the endpoints addressed by meeting id: the meeting's
     * property is resolved through its port-in use case, never a repository (rule 6). */
    public boolean canReadMeeting(Authentication authentication, String meetingId) {
        return canReadMeetings(authentication, propertyIdOfMeeting(meetingId));
    }

    /** Same rule as canManageMeetings, for the endpoints addressed by meeting id. */
    public boolean canManageMeeting(Authentication authentication, String meetingId) {
        return canManageMeetings(authentication, propertyIdOfMeeting(meetingId));
    }

    /** An agenda item inherits its meeting's property; two hops, both through port-in use cases. */
    public boolean canManageAgendaItem(Authentication authentication, String agendaItemId) {
        return canManageMeeting(authentication, meetingIdOfAgendaItem(agendaItemId));
    }

    /** Same rule as canPublishMeetingMinutes, for the endpoints addressed by meeting id. */
    public boolean canPublishMinutesOfMeeting(Authentication authentication, String meetingId) {
        return canPublishMeetingMinutes(authentication, propertyIdOfMeeting(meetingId));
    }

    /** Reading one agenda item and its result - open to every member, a vote result is not a secret. */
    public boolean canReadAgendaItem(Authentication authentication, String agendaItemId) {
        return canReadMeeting(authentication, meetingIdOfAgendaItem(agendaItemId));
    }

    private String meetingIdOfAgendaItem(String agendaItemId) {
        AgendaItemView item = getAgendaItemUseCase.getAgendaItem(new GetAgendaItemQuery(AgendaItemId.of(agendaItemId)));
        return item.generalMeetingId().toString();
    }

    /** Managing one lot's convocation: staff of the meeting's property, same rule as the meeting itself. */
    public boolean canManageConvocation(Authentication authentication, String convocationId) {
        return canManageMeeting(authentication, convocation(convocationId).generalMeetingId().toString());
    }

    /**
     * Answering a convocation: the syndic for any lot, or the copropriétaire for their own.
     * Deliberately wider than canManageConvocation - self-service is the whole point of the
     * reply endpoint - and deliberately narrower than "any member of the property": a
     * neighbour must not be able to answer on someone else's behalf.
     */
    public boolean canReplyToConvocation(Authentication authentication, String convocationId) {
        ConvocationView convocation = convocation(convocationId);
        return canManageMeeting(authentication, convocation.generalMeetingId().toString())
                || ownsUnit(authentication, convocation.unitId().toString());
    }

    private ConvocationView convocation(String convocationId) {
        return getConvocationUseCase.getConvocation(new GetConvocationQuery(ConvocationId.of(convocationId)));
    }

    private String propertyIdOfMeeting(String meetingId) {
        GeneralMeetingView meeting = getGeneralMeetingUseCase.getGeneralMeeting(
                new GetGeneralMeetingQuery(GeneralMeetingId.of(meetingId)));
        return meeting.propertyId().toString();
    }

    /** True for ADMIN, or if the caller holds any role (staff or plain owner) on this property -
     * unlike managesProperty, this INCLUDES PROPERTY_OWNER, directly readable off UserAccessView
     * without any change needed in user. Gates messaging endpoints scoped by propertyId that any
     * member (not just staff) may use: starting a GROUP conversation, listing recipient
     * candidates. */
    public boolean isPropertyMember(Authentication authentication, String propertyId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return access(authentication).rolesByProperty().containsKey(propertyId);
    }

    /** Gates POST /properties/{id}/broadcast-messages: only a board/manager-tier holder of
     * Permission.MESSAGING_BROADCAST may post to a property's persistent announcement channel. */
    public boolean canBroadcastOnProperty(Authentication authentication, String propertyId) {
        return hasPermission(authentication, propertyId, Permission.MESSAGING_BROADCAST);
    }

    /** Gates every messaging endpoint scoped by conversationId (list/send messages, mark read):
     * true for ADMIN, or if the caller is one of the GROUP conversation's participants, or - for
     * BROADCAST - if the caller is still a member of the conversation's property, or - for
     * BOARD_PRIVATE - if the caller currently holds a staff role on it (stricter than BROADCAST: a
     * plain owner must never read the board's private thread just by being a property member).
     * Membership for BROADCAST/BOARD_PRIVATE is resolved dynamically, never stored - see
     * Conversation's javadoc. */
    public boolean isConversationParticipant(Authentication authentication, String conversationId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        ConversationView conversation =
                getConversationUseCase.getConversation(new GetConversationQuery(ConversationId.of(conversationId)));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        EntityId callerId = EntityId.of(principal.getUserId());

        if (conversation.type() == ConversationType.GROUP) {
            return conversation.participantUserIds().contains(callerId);
        }
        if (conversation.type() == ConversationType.BOARD_PRIVATE) {
            return managesProperty(authentication, conversation.propertyId().toString());
        }
        return isPropertyMember(authentication, conversation.propertyId().toString());
    }

    /** Gates POST /conversations/{id}/messages specifically (replying), stricter than
     * isConversationParticipant which also gates read-only access (list messages, mark read):
     * true for ADMIN, or if the caller is one of the GROUP conversation's participants (any
     * participant may reply), or - for BROADCAST - only if the caller holds
     * for BOARD_PRIVATE - only if the caller currently holds a staff role on it. Never for
     * BROADCAST, whoever asks: un envoi groupé ne se répond pas, on en émet un autre. A plain
     * owner must not be able to post into either just because the generic "send a message"
     * endpoint doesn't otherwise know which conversation type it's posting into. */
    public boolean canSendToConversation(Authentication authentication, String conversationId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        ConversationView conversation =
                getConversationUseCase.getConversation(new GetConversationQuery(ConversationId.of(conversationId)));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        EntityId callerId = EntityId.of(principal.getUserId());

        if (conversation.type() == ConversationType.GROUP) {
            return conversation.participantUserIds().contains(callerId);
        }
        if (conversation.type() == ConversationType.BOARD_PRIVATE) {
            return managesProperty(authentication, conversation.propertyId().toString());
        }
        // BROADCAST : personne, pas même l'émetteur. Un envoi groupé est un
        // message adressé à toute la copropriété, pas un fil - le suivant est
        // un nouvel envoi, avec son propre objet (voir SendBroadcastMessageService).
        return false;
    }

    /** Gates every message-draft endpoint scoped by draft id (update/get/delete/send): true for
     * ADMIN, or if the caller is the draft's own author - a draft is never visible to anyone
     * else, unlike a conversation which is shared with its other participants. */
    public boolean isDraftOwner(Authentication authentication, String draftId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        MessageDraftView draft = getMessageDraftUseCase.getDraft(new GetMessageDraftQuery(MessageDraftId.of(draftId)));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return draft.createdBy().equals(EntityId.of(principal.getUserId()));
    }

    /** Gates every notification endpoint scoped by notification id (get/mark read): true for
     * ADMIN, or if the caller is the notification's own recipient - a notification is never
     * visible to anyone else, same rationale as isDraftOwner. */
    public boolean isNotificationOwner(Authentication authentication, String notificationId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        NotificationView notification =
                getNotificationUseCase.getNotification(new GetNotificationQuery(NotificationId.of(notificationId)));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return notification.recipientUserId().equals(EntityId.of(principal.getUserId()));
    }

    /** Gates GET /documents (list) and POST /documents (upload), scoped by the owner
     * (ownerType, ownerId) the document is/would be attached to - resolves to the owning
     * property the same way managesUnit resolves a unit id, then checks DOCUMENT_READ. */
    public boolean canReadDocument(Authentication authentication, String ownerType, String ownerId) {
        return hasPermission(authentication, resolveDocumentOwnerPropertyId(ownerType, ownerId), Permission.DOCUMENT_READ);
    }

    /** Same as canReadDocument but for DOCUMENT_WRITE (upload). */
    public boolean canWriteDocument(Authentication authentication, String ownerType, String ownerId) {
        return hasPermission(authentication, resolveDocumentOwnerPropertyId(ownerType, ownerId), Permission.DOCUMENT_WRITE);
    }

    /** Gates GET /documents/{id} and GET /documents/{id}/content, addressed by document id
     * rather than owner - resolves the document's (ownerType, ownerId) first. */
    public boolean canReadDocumentEntry(Authentication authentication, String documentId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        DocumentView document = getDocumentUseCase.getDocument(new GetDocumentQuery(DocumentId.of(documentId)));
        // A payment receipt is not a copropriété-wide document: it names one
        // owner and states what they paid. The permission path below is
        // property-scoped (DOCUMENT_READ on the property), and every
        // copropriétaire holds DOCUMENT_READ - it would let any of them read
        // any other's receipt. Narrowed here to the lot's own owner (or staff).
        if (document.ownerType() == DocumentOwnerType.PAYMENT) {
            return managesPayment(authentication, document.ownerId().toString());
        }
        return canReadDocument(authentication, document.ownerType().name(), document.ownerId().toString());
    }

    /**
     * Read access to a single payment and its receipt: staff of the property, or
     * the owner of the lot it was paid on. Same shape as managesInstallment.
     */
    public boolean managesPayment(Authentication authentication, String paymentId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String unitId = getPaymentUseCase.getPayment(new GetPaymentQuery(PaymentId.of(paymentId)))
                .unitId().toString();
        return managesUnit(authentication, unitId) || ownsUnit(authentication, unitId);
    }

    /** Gates DELETE /documents/{id}. */
    public boolean canWriteDocumentEntry(Authentication authentication, String documentId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        DocumentView document = getDocumentUseCase.getDocument(new GetDocumentQuery(DocumentId.of(documentId)));
        return canWriteDocument(authentication, document.ownerType().name(), document.ownerId().toString());
    }

    private String resolveDocumentOwnerPropertyId(String ownerType, String ownerId) {
        return switch (DocumentOwnerType.valueOf(ownerType.toUpperCase())) {
            case PROPERTY -> ownerId;
            case UNIT -> getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(ownerId))).propertyId().toString();
            case PAYMENT -> getPaymentUseCase.getPayment(new GetPaymentQuery(PaymentId.of(ownerId)))
                    .propertyId().toString();
            case AGENDA_ITEM -> propertyIdOfMeeting(getAgendaItemUseCase
                    .getAgendaItem(new GetAgendaItemQuery(AgendaItemId.of(ownerId))).generalMeetingId().toString());
            case GENERAL_MEETING -> propertyIdOfMeeting(ownerId);
            case CONVOCATION -> propertyIdOfMeeting(convocation(ownerId).generalMeetingId().toString());
            case MEETING_MINUTES -> propertyIdOfMeeting(ownerId);
        };
    }

    /** Self-service: the current account's own linked party. */
    public boolean ownsParty(Authentication authentication, String partyId) {
        return access(authentication).ownedPartyIds().contains(partyId);
    }

    /** Read-only access for the owner of the unit, in addition to admin/manager. */
    public boolean ownsUnit(Authentication authentication, String unitId) {
        UserAccessView access = access(authentication);
        if (access.ownedPartyIds().isEmpty()) {
            return false;
        }
        return listUnitOwnershipsByUnitUseCase.listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(UnitId.of(unitId)))
                .stream()
                .anyMatch(ownership -> access.ownedPartyIds().contains(ownership.partyId().toString()));
    }

    /**
     * The volunteer-syndic wizard finishing its configuration: accepted either
     * with the short-lived onboarding token issued when the account was created
     * (the account is not verified yet, so it cannot log in), or with an ordinary
     * board-admin session once the visitor has verified their email and come back
     * - which is what makes an expired token a non-event rather than a dead end.
     */
    public boolean canConfigureOnboarding(Authentication authentication, String propertyId) {
        return hasOnboardingScope(authentication, propertyId) || managesProperty(authentication, propertyId);
    }

    private static boolean hasOnboardingScope(Authentication authentication, String propertyId) {
        String expected = JwtService.ONBOARDING_AUTHORITY_PREFIX + propertyId;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expected::equals);
    }

    private static boolean isOnboardingToken(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.startsWith(JwtService.ONBOARDING_AUTHORITY_PREFIX));
    }

    private UserAccessView access(Authentication authentication) {
        // Every rule but canConfigureOnboarding funnels through here, so denying an
        // onboarding token once - rather than per rule - is what keeps it from
        // behaving as a full session for an account that never verified its email.
        // The grants exist in database (registration made the caller board admin of
        // the property it just created); only the token's scope withholds them.
        if (isOnboardingToken(authentication)) {
            return new UserAccessView(Set.of(), Map.of(), Map.of(), Set.of(), Set.of());
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return getUserAccessUseCase.getAccess(new GetUserAccessQuery(UserId.of(principal.getUserId())));
    }

    private static boolean isAdminAuthority(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_MASTER"));
    }
}
