package com.architek.oikos.document.application.port.in;

import com.architek.oikos.document.application.command.DeleteDocumentCommand;

public interface DeleteDocumentUseCase {

    void delete(DeleteDocumentCommand command);
}
