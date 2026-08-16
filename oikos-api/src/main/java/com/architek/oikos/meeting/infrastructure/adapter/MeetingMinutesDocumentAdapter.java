package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.meeting.application.port.out.MeetingMinutesDocumentPort;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Files the final PDF, owned by the meeting itself - the minutes being 1-1
 * with it, there is no separate owner to address. Same replace-don't-accumulate
 * rule as the convocation, for the same reason.
 */
@Component
public class MeetingMinutesDocumentAdapter implements MeetingMinutesDocumentPort {

    private static final int EXISTING_DOCUMENTS_PAGE_SIZE = 50;

    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;

    public MeetingMinutesDocumentAdapter(UploadDocumentUseCase uploadDocumentUseCase,
                                          ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase,
                                          DeleteDocumentUseCase deleteDocumentUseCase) {
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.listDocumentsByOwnerUseCase = listDocumentsByOwnerUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
    }

    @Override
    public void replaceMinutesDocument(GeneralMeetingId generalMeetingId, String fileName, byte[] content,
                                        EntityId uploadedByUserId) {
        EntityId ownerId = EntityId.of(generalMeetingId.asUuid());
        List<DocumentView> existing = listDocumentsByOwnerUseCase.list(
                new ListDocumentsByOwnerQuery(DocumentOwnerType.MEETING_MINUTES, ownerId,
                        PageRequest.of(0, EXISTING_DOCUMENTS_PAGE_SIZE))).content();
        for (DocumentView document : existing) {
            deleteDocumentUseCase.delete(new DeleteDocumentCommand(document.id()));
        }
        uploadDocumentUseCase.upload(new UploadDocumentCommand(DocumentOwnerType.MEETING_MINUTES, ownerId, fileName,
                "application/pdf", content, uploadedByUserId));
    }
}
