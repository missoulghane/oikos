package com.architek.oikos.document.application.command;

import com.architek.oikos.document.domain.valueobject.DocumentId;

public record DeleteDocumentCommand(DocumentId id) {
}
