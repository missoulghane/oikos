package com.architek.oikos.document.application.port.in;

import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;

public interface UploadDocumentUseCase {

    DocumentView upload(UploadDocumentCommand command);
}
