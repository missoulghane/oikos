package com.architek.oikos.document.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.out.FileStoragePort;
import com.architek.oikos.document.domain.exception.DocumentNotFoundException;
import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;

/**
 * The DB row is the source of truth: it is removed first, then the physical
 * file is deleted best-effort. A physical-delete failure is logged, not
 * propagated - re-raising here would leave the DB row deleted but the
 * caller thinking the whole operation failed, which is worse than a leaked
 * orphan file (no distributed transaction is available across the two).
 */
@Component
public class DeleteDocumentService implements DeleteDocumentUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteDocumentService.class);

    private final DocumentRepository documentRepository;
    private final FileStoragePort fileStoragePort;

    public DeleteDocumentService(DocumentRepository documentRepository, FileStoragePort fileStoragePort) {
        this.documentRepository = documentRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    @Transactional
    public void delete(DeleteDocumentCommand command) {
        Document document = documentRepository.findById(command.id())
                .orElseThrow(() -> new DocumentNotFoundException(command.id().toString()));
        documentRepository.deleteById(document.getId());
        try {
            fileStoragePort.delete(document.getStorageKey());
        } catch (RuntimeException e) {
            log.warn("Failed to delete physical file for document {} (storageKey={})", document.getId(),
                    document.getStorageKey(), e);
        }
    }
}
