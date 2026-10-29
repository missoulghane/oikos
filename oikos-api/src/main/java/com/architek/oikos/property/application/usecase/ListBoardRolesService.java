package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.port.in.ListBoardRolesUseCase;
import com.architek.oikos.property.domain.model.BoardRoleOption;
import com.architek.oikos.property.domain.repository.BoardRoleOptionRepository;

/** Active rows only, in display order. */
@Component
public class ListBoardRolesService implements ListBoardRolesUseCase {

    private final BoardRoleOptionRepository boardRoleOptionRepository;

    public ListBoardRolesService(BoardRoleOptionRepository boardRoleOptionRepository) {
        this.boardRoleOptionRepository = boardRoleOptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardRoleOption> listBoardRoles() {
        return boardRoleOptionRepository.findAllActive();
    }
}
