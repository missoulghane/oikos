package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.BoardMemberView;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;

public interface ListBoardMembersByPropertyUseCase {

    List<BoardMemberView> listBoardMembers(ListBoardMembersByPropertyQuery query);
}
