package com.architek.oikos.messaging.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.dto.RecipientCandidateView;
import com.architek.oikos.messaging.application.port.in.ListRecipientCandidatesUseCase;
import com.architek.oikos.messaging.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberDirectoryPort;
import com.architek.oikos.messaging.application.port.out.PropertyMemberInfo;
import com.architek.oikos.messaging.application.query.ListRecipientCandidatesQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Not paginated (property member counts are small, same in-memory assumption
 * as ListContactsByPropertyService), capped at MAX_RESULTS server-side.
 * Excludes both members without a linked account (can't receive a message)
 * and the caller themselves.
 */
@Component
public class ListRecipientCandidatesService implements ListRecipientCandidatesUseCase {

    private static final int MAX_RESULTS = 50;

    private final PropertyMemberDirectoryPort propertyMemberDirectoryPort;
    private final PartyAccountDirectoryPort partyAccountDirectoryPort;

    public ListRecipientCandidatesService(PropertyMemberDirectoryPort propertyMemberDirectoryPort,
                                           PartyAccountDirectoryPort partyAccountDirectoryPort) {
        this.propertyMemberDirectoryPort = propertyMemberDirectoryPort;
        this.partyAccountDirectoryPort = partyAccountDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientCandidateView> listCandidates(ListRecipientCandidatesQuery query) {
        List<PropertyMemberInfo> members = propertyMemberDirectoryPort.listMembers(query.propertyId()).stream()
                .filter(PropertyMemberInfo::hasLinkedAccount)
                .toList();

        Map<EntityId, EntityId> userIdByPartyId = partyAccountDirectoryPort.resolveUserIds(
                members.stream().map(PropertyMemberInfo::partyId).toList());

        return members.stream()
                .map(member -> new RecipientCandidateView(userIdByPartyId.get(member.partyId()), member.fullName(), member.roleLabel()))
                .filter(candidate -> candidate.userId() != null && !candidate.userId().equals(query.userId()))
                .filter(candidate -> matchesSearch(candidate.fullName(), query.search()))
                .sorted(Comparator.comparing(RecipientCandidateView::fullName, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_RESULTS)
                .toList();
    }

    private static boolean matchesSearch(String fullName, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        return fullName != null && fullName.toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT));
    }
}
