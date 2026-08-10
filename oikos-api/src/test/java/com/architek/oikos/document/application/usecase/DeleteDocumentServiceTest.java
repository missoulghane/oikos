package com.architek.oikos.document.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.port.out.FileStoragePort;
import com.architek.oikos.document.domain.exception.DocumentNotFoundException;
import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.repository.DocumentRepository;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class DeleteDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    private DeleteDocumentService newService() {
        return new DeleteDocumentService(documentRepository, fileStoragePort);
    }

    @Test
    void deleting_an_existing_document_removes_the_db_row_and_the_physical_file() {
        Document document = Document.create(DocumentOwnerType.PROPERTY, EntityId.newId(), "notice.pdf",
                "application/pdf", 10L, "checksum", EntityId.newId());
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));

        newService().delete(new DeleteDocumentCommand(document.getId()));

        verify(documentRepository).deleteById(document.getId());
        verify(fileStoragePort).delete(document.getStorageKey());
    }

    @Test
    void deleting_an_unknown_document_is_rejected() {
        DocumentId id = DocumentId.newId();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().delete(new DeleteDocumentCommand(id)))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void a_physical_delete_failure_does_not_fail_the_operation_since_the_db_row_is_already_gone() {
        Document document = Document.create(DocumentOwnerType.PROPERTY, EntityId.newId(), "notice.pdf",
                "application/pdf", 10L, "checksum", EntityId.newId());
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        doThrow(new RuntimeException("disk unavailable")).when(fileStoragePort).delete(any());

        assertThatCode(() -> newService().delete(new DeleteDocumentCommand(document.getId())))
                .doesNotThrowAnyException();
        verify(documentRepository).deleteById(document.getId());
    }
}
