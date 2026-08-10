package com.architek.oikos.document.application.port.in;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListDocumentsByOwnerUseCase {

    Page<DocumentView> list(ListDocumentsByOwnerQuery query);
}
