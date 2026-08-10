package com.architek.oikos.document.web.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.in.GetDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.document.web.response.DocumentResponse;
import com.architek.oikos.document.web.response.PagedDocumentResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.auth.infrastructure.security.UserPrincipal;

@RestController
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;
    private final GetDocumentUseCase getDocumentUseCase;
    private final DownloadDocumentUseCase downloadDocumentUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;

    public DocumentController(UploadDocumentUseCase uploadDocumentUseCase,
                              ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase,
                              GetDocumentUseCase getDocumentUseCase,
                              DownloadDocumentUseCase downloadDocumentUseCase,
                              DeleteDocumentUseCase deleteDocumentUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.listDocumentsByOwnerUseCase = listDocumentsByOwnerUseCase;
        this.getDocumentUseCase = getDocumentUseCase;
        this.downloadDocumentUseCase = downloadDocumentUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteDocument(authentication, #ownerType, #ownerId)")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam String ownerType,
                                                    @RequestParam String ownerId,
                                                    @RequestParam MultipartFile file,
                                                    Authentication authentication) {
        if (file.isEmpty()) {
            throw new BusinessException("The uploaded file must not be empty");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        DocumentView view = uploadDocumentUseCase.upload(new UploadDocumentCommand(
                DocumentOwnerType.valueOf(ownerType.toUpperCase()), EntityId.of(ownerId), file.getOriginalFilename(),
                file.getContentType(), readBytes(file), EntityId.of(principal.getUserId())));
        return ResponseEntity.created(URI.create("/api/v1/documents/" + view.id())).body(DocumentResponse.from(view));
    }

    @PreAuthorize("@propertyAccess.canReadDocument(authentication, #ownerType, #ownerId)")
    @GetMapping("/documents")
    public PagedDocumentResponse list(@RequestParam String ownerType,
                                      @RequestParam String ownerId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        ListDocumentsByOwnerQuery query = new ListDocumentsByOwnerQuery(
                DocumentOwnerType.valueOf(ownerType.toUpperCase()), EntityId.of(ownerId), PageRequest.of(page, size));
        return PagedDocumentResponse.from(listDocumentsByOwnerUseCase.list(query));
    }

    @PreAuthorize("@propertyAccess.canReadDocumentEntry(authentication, #id)")
    @GetMapping("/documents/{id}")
    public DocumentResponse getById(@PathVariable String id) {
        return DocumentResponse.from(getDocumentUseCase.getDocument(new GetDocumentQuery(DocumentId.of(id))));
    }

    @PreAuthorize("@propertyAccess.canReadDocumentEntry(authentication, #id)")
    @GetMapping("/documents/{id}/content")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) {
        DocumentContentView content = downloadDocumentUseCase.download(new GetDocumentQuery(DocumentId.of(id)));
        ContentDisposition disposition = ContentDisposition.attachment().filename(content.fileName()).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.sizeBytes())
                .body(new ByteArrayResource(content.content()));
    }

    @PreAuthorize("@propertyAccess.canWriteDocumentEntry(authentication, #id)")
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteDocumentUseCase.delete(new DeleteDocumentCommand(DocumentId.of(id)));
        return ResponseEntity.noContent().build();
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }
}
