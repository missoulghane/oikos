package com.architek.oikos.document.application.port.in;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.query.GetDocumentQuery;

public interface GetDocumentUseCase {

    DocumentView getDocument(GetDocumentQuery query);
}
