package com.architek.oikos.document.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.out.DocumentOwnerExistencePort;
import com.architek.oikos.document.application.port.out.FileStoragePort;
import com.architek.oikos.document.domain.exception.DocumentOwnerNotFoundException;
import com.architek.oikos.document.domain.exception.DuplicateDocumentException;
import com.architek.oikos.document.domain.exception.FileTooLargeException;
import com.architek.oikos.document.domain.exception.UnsupportedFileTypeException;
import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class UploadDocumentServiceTest {

    private static final long MAX_SIZE = 1_000L;
    private static final List<String> ALLOWED_TYPES = List.of("application/pdf", "image/png");

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentOwnerExistencePort documentOwnerExistencePort;

    @Mock
    private FileStoragePort fileStoragePort;

    private UploadDocumentService service;

    @BeforeEach
    void setUp() {
        service = new UploadDocumentService(documentRepository, documentOwnerExistencePort, fileStoragePort);
        ReflectionTestUtils.setField(service, "maxFileSizeBytes", MAX_SIZE);
        ReflectionTestUtils.setField(service, "allowedContentTypes", ALLOWED_TYPES);
    }

    private UploadDocumentCommand command(byte[] content, String contentType) {
        return new UploadDocumentCommand(DocumentOwnerType.PROPERTY, EntityId.newId(), "notice.pdf", contentType,
                content, EntityId.newId());
    }

    @Test
    void uploading_a_valid_file_onto_an_existing_owner_persists_it() {
        when(documentOwnerExistencePort.exists(any(), any())).thenReturn(true);
        when(documentRepository.existsByOwnerAndChecksum(any(), any(), anyString())).thenReturn(false);
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentView view = service.upload(command(new byte[] {1, 2, 3}, "application/pdf"));

        assertThat(view.fileName()).isEqualTo("notice.pdf");
        verify(fileStoragePort).store(anyString(), any());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void uploading_onto_an_unknown_owner_is_rejected() {
        when(documentOwnerExistencePort.exists(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.upload(command(new byte[] {1}, "application/pdf")))
                .isInstanceOf(DocumentOwnerNotFoundException.class);
    }

    @Test
    void a_file_exceeding_the_configured_max_size_is_rejected() {
        when(documentOwnerExistencePort.exists(any(), any())).thenReturn(true);

        byte[] tooLarge = new byte[(int) MAX_SIZE + 1];
        assertThatThrownBy(() -> service.upload(command(tooLarge, "application/pdf")))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void a_disallowed_content_type_is_rejected() {
        when(documentOwnerExistencePort.exists(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.upload(command(new byte[] {1}, "application/x-msdownload")))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void reuploading_the_same_content_onto_the_same_owner_is_rejected_as_a_duplicate() {
        when(documentOwnerExistencePort.exists(any(), any())).thenReturn(true);
        when(documentRepository.existsByOwnerAndChecksum(any(), any(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.upload(command(new byte[] {1, 2, 3}, "application/pdf")))
                .isInstanceOf(DuplicateDocumentException.class);
    }
}
