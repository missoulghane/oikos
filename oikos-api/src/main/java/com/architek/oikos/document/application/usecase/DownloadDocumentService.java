package com.architek.oikos.document.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.out.FileStoragePort;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.domain.exception.DocumentNotFoundException;
import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;

@Component
public class DownloadDocumentService implements DownloadDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final FileStoragePort fileStoragePort;

    public DownloadDocumentService(DocumentRepository documentRepository, FileStoragePort fileStoragePort) {
        this.documentRepository = documentRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentContentView download(GetDocumentQuery query) {
        Document document = documentRepository.findById(query.id())
                .orElseThrow(() -> new DocumentNotFoundException(query.id().toString()));
        byte[] content = fileStoragePort.retrieve(document.getStorageKey());
        return new DocumentContentView(document.getFileName(), document.getContentType(), document.getSizeBytes(),
                content);
    }
}
