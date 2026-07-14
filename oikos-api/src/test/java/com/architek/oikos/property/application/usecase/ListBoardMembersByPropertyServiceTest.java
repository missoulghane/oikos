package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.model.BoardMember;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListBoardMembersByPropertyServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Test
    void listing_membres_syndic_maps_the_repository_entries_to_views() {
        PropertyId propertyId = PropertyId.newId();
        BoardMember boardMember = BoardMember.create(BoardMemberId.newId(), propertyId, EntityId.newId(), BoardRole.PRESIDENT);
        when(boardMemberRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(boardMember));

        var views = new ListBoardMembersByPropertyService(boardMemberRepository)
                .listBoardMembers(new ListBoardMembersByPropertyQuery(propertyId));

        assertThat(views).extracting(view -> view.boardRole()).containsExactly(BoardRole.PRESIDENT);
    }
}
