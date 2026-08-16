package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.query.GetConvocationQuery;

public interface GetConvocationDocumentUseCase {

    ConvocationDocument getDocument(GetConvocationQuery query);

    /** alreadySent tells the screen whether this is the letter that went out or a preview of it. */
    record ConvocationDocument(String fileName, byte[] content, boolean alreadySent) {
    }
}
