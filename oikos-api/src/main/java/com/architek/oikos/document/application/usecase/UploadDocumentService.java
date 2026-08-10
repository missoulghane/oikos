package com.architek.oikos.document.application.usecase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.application.port.out.DocumentOwnerExistencePort;
import com.architek.oikos.document.application.port.out.FileStoragePort;
import com.architek.oikos.document.domain.exception.DocumentOwnerNotFoundException;
import com.architek.oikos.document.domain.exception.DuplicateDocumentException;
import com.architek.oikos.document.domain.exception.FileTooLargeException;
import com.architek.oikos.document.domain.exception.UnsupportedFileTypeException;
import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;

@Component
public class UploadDocumentService implements UploadDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final DocumentOwnerExistencePort documentOwnerExistencePort;
    private final FileStoragePort fileStoragePort;

    @Value("${oikos.document.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Value("#{'${oikos.document.allowed-content-types}'.split(',')}")
    private List<String> allowedContentTypes;

    public UploadDocumentService(DocumentRepository documentRepository,
                                  DocumentOwnerExistencePort documentOwnerExistencePort,
                                  FileStoragePort fileStoragePort) {
        this.documentRepository = documentRepository;
        this.documentOwnerExistencePort = documentOwnerExistencePort;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    @Transactional
    public DocumentView upload(UploadDocumentCommand command) {
        if (!documentOwnerExistencePort.exists(command.ownerType(), command.ownerId())) {
            throw new DocumentOwnerNotFoundException(command.ownerType(), command.ownerId().toString());
        }
        if (command.content().length > maxFileSizeBytes) {
            throw new FileTooLargeException(command.content().length, maxFileSizeBytes);
        }
        if (!allowedContentTypes.contains(command.contentType())) {
            throw new UnsupportedFileTypeException(command.contentType());
        }

        String checksum = sha256(command.content());
        if (documentRepository.existsByOwnerAndChecksum(command.ownerType(), command.ownerId(), checksum)) {
            throw new DuplicateDocumentException(command.fileName());
        }

        Document document = Document.create(command.ownerType(), command.ownerId(), command.fileName(),
                command.contentType(), command.content().length, checksum, command.uploadedBy());

        fileStoragePort.store(document.getStorageKey(), command.content());
        Document saved = documentRepository.save(document);
        return DocumentView.from(saved);
    }

    private static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
