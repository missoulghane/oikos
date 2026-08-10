package com.architek.oikos.messaging.application.usecase;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberInfo;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves userId -> display name for every current member of a property, by
 * crossing PropertyMemberDirectoryPort (partyId -> fullName) with
 * PartyAccountDirectoryPort (partyId -> userId). Shared by
 * ListConversationMessagesService (senderName per message) and
 * ListRecipientCandidatesService (candidate fullName), and
 * StartGroupConversationService (recipient membership-with-linked-account
 * check), so the two-port lookup isn't duplicated - every sender/recipient
 * of a message is, by construction, a property member with a linked account
 * (see StartGroupConversationService/SendBroadcastMessageService), so this
 * never needs to fall back to an "unknown user" placeholder in practice.
 */
@Component
class MemberDisplayNameResolver {

    private final PropertyMemberDirectoryPort propertyMemberDirectoryPort;
    private final PartyAccountDirectoryPort partyAccountDirectoryPort;

    MemberDisplayNameResolver(PropertyMemberDirectoryPort propertyMemberDirectoryPort,
                               PartyAccountDirectoryPort partyAccountDirectoryPort) {
        this.propertyMemberDirectoryPort = propertyMemberDirectoryPort;
        this.partyAccountDirectoryPort = partyAccountDirectoryPort;
    }

    Map<EntityId, String> namesByUserId(EntityId propertyId) {
        var members = propertyMemberDirectoryPort.listMembers(propertyId);
        Map<EntityId, EntityId> userIdByPartyId = partyAccountDirectoryPort.resolveUserIds(
                members.stream().map(PropertyMemberInfo::partyId).toList());

        return members.stream()
                .filter(member -> userIdByPartyId.containsKey(member.partyId()))
                .collect(Collectors.toMap(member -> userIdByPartyId.get(member.partyId()), PropertyMemberInfo::fullName,
                        (first, second) -> first));
    }
}
