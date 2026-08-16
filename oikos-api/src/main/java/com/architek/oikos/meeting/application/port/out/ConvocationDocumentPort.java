package com.architek.oikos.meeting.application.port.out;

import java.util.Optional;

import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Stores the generated convocation PDF through the document module, rather
 * than adding a second way to keep a file - same reuse as the payment receipt
 * (GeneratePaymentReceiptService). Replaces rather than accumulates: the
 * document module refuses a byte-identical upload for the same owner, so a
 * re-send that changed nothing would otherwise fail as a duplicate.
 */
public interface ConvocationDocumentPort {

    void replaceConvocationDocument(ConvocationId convocationId, String fileName, byte[] content,
                                     EntityId uploadedByUserId);

    /**
     * The PDF actually filed for this convocation, if one has been sent. Empty
     * before the first send - the download path then renders a preview instead
     * of pretending nothing exists.
     */
    Optional<StoredDocument> findConvocationDocument(ConvocationId convocationId);

    record StoredDocument(String fileName, byte[] content) {
    }
}
