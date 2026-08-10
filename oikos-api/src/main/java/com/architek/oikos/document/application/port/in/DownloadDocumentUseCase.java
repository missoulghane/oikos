package com.architek.oikos.document.application.port.in;

import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.query.GetDocumentQuery;

public interface DownloadDocumentUseCase {

    DocumentContentView download(GetDocumentQuery query);
}
