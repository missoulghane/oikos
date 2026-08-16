package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.meeting.application.port.out.ConvocationDocumentPort;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Files the convocation PDF through the document module - the same reuse (and
 * the same two traps) as GeneratePaymentReceiptService.
 *
 * <p>Replaces rather than accumulates: the document module refuses a
 * byte-identical upload for the same owner (uk_document_owner_checksum), so a
 * re-send that changed nothing would otherwise fail as a duplicate rather than
 * simply re-issuing the letter.
 *
 * <p>uploadedBy is the requesting user and not the property:
 * document.uploaded_by is a foreign key onto app_user, and anything else
 * violates it at flush time - invisible on H2, fatal on PostgreSQL.
 */
@Component
public class MeetingConvocationDocumentAdapter implements ConvocationDocumentPort {

    /** A convocation owns at most one document; the page size only has to cover that. */
    private static final int EXISTING_DOCUMENTS_PAGE_SIZE = 50;

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;
    private final DownloadDocumentUseCase downloadDocumentUseCase;

    public MeetingConvocationDocumentAdapter(UploadDocumentUseCase uploadDocumentUseCase,
                                              ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase,
                                              DeleteDocumentUseCase deleteDocumentUseCase,
                                              DownloadDocumentUseCase downloadDocumentUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.listDocumentsByOwnerUseCase = listDocumentsByOwnerUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
        this.downloadDocumentUseCase = downloadDocumentUseCase;
    }

    @Override
    public Optional<StoredDocument> findConvocationDocument(ConvocationId convocationId) {
        return listDocumentsByOwnerUseCase.list(new ListDocumentsByOwnerQuery(DocumentOwnerType.CONVOCATION,
                        EntityId.of(convocationId.asUuid()), PageRequest.of(0, EXISTING_DOCUMENTS_PAGE_SIZE)))
                .content().stream().findFirst()
                .map(document -> {
                    DocumentContentView content = downloadDocumentUseCase.download(new GetDocumentQuery(document.id()));
                    return new StoredDocument(content.fileName(), content.content());
                });
    }

    @Override
    public void replaceConvocationDocument(ConvocationId convocationId, String fileName, byte[] content,
                                            EntityId uploadedByUserId) {
        EntityId ownerId = EntityId.of(convocationId.asUuid());
        List<DocumentView> existing = listDocumentsByOwnerUseCase.list(
                new ListDocumentsByOwnerQuery(DocumentOwnerType.CONVOCATION, ownerId,
                        PageRequest.of(0, EXISTING_DOCUMENTS_PAGE_SIZE))).content();
        for (DocumentView document : existing) {
            deleteDocumentUseCase.delete(new DeleteDocumentCommand(document.id()));
        }
        uploadDocumentUseCase.upload(new UploadDocumentCommand(DocumentOwnerType.CONVOCATION, ownerId, fileName,
                "application/pdf", content, uploadedByUserId));
    }
}
