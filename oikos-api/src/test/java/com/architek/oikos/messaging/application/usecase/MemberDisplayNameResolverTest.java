package com.architek.oikos.messaging.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.messaging.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberInfo;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class MemberDisplayNameResolverTest {

    @Mock
    private PropertyMemberDirectoryPort propertyMemberDirectoryPort;

    @Mock
    private PartyAccountDirectoryPort partyAccountDirectoryPort;

    private MemberDisplayNameResolver newResolver() {
        return new MemberDisplayNameResolver(propertyMemberDirectoryPort, partyAccountDirectoryPort);
    }

    @Test
    void maps_userId_to_fullName_and_skips_members_without_a_linked_account() {
        EntityId propertyId = EntityId.newId();
        EntityId aliceParty = EntityId.newId();
        EntityId aliceUser = EntityId.newId();
        EntityId bobParty = EntityId.newId();

        when(propertyMemberDirectoryPort.listMembers(propertyId)).thenReturn(List.of(
                new PropertyMemberInfo(aliceParty, "Alice", "Copropriétaire", true, List.of(), false),
                new PropertyMemberInfo(bobParty, "Bob", "Copropriétaire", false, List.of(), false)));
        when(partyAccountDirectoryPort.resolveUserIds(List.of(aliceParty, bobParty))).thenReturn(Map.of(aliceParty, aliceUser));

        Map<EntityId, String> names = newResolver().namesByUserId(propertyId);

        assertThat(names).containsExactly(Map.entry(aliceUser, "Alice"));
    }
}
