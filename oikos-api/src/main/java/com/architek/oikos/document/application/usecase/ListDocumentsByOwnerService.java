package com.architek.oikos.document.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.repository.DocumentRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListDocumentsByOwnerService implements ListDocumentsByOwnerUseCase {

    private final DocumentRepository documentRepository;

    public ListDocumentsByOwnerService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentView> list(ListDocumentsByOwnerQuery query) {
        return documentRepository.findAllByOwner(query.ownerType(), query.ownerId(), query.pageRequest())
                .map(DocumentView::from);
    }
}
