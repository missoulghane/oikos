package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;

@Component
public class ListBoardMembersByPropertyService implements ListBoardMembersByPropertyUseCase {

    private final BoardMemberRepository boardMemberRepository;

    public ListBoardMembersByPropertyService(BoardMemberRepository boardMemberRepository) {
        this.boardMemberRepository = boardMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardMemberView> listBoardMembers(ListBoardMembersByPropertyQuery query) {
        return boardMemberRepository.findAllByPropertyId(query.propertyId()).stream()
                .map(BoardMemberView::from).toList();
    }
}
