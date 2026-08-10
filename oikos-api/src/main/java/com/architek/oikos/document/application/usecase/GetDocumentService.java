package com.architek.oikos.document.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.GetDocumentUseCase;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.domain.exception.DocumentNotFoundException;
import com.architek.oikos.document.domain.repository.DocumentRepository;

@Component
public class GetDocumentService implements GetDocumentUseCase {

    private final DocumentRepository documentRepository;

    public GetDocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentView getDocument(GetDocumentQuery query) {
        return documentRepository.findById(query.id())
                .map(DocumentView::from)
                .orElseThrow(() -> new DocumentNotFoundException(query.id().toString()));
    }
}
