package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.meeting.application.dto.ConvocationDocumentView;

/**
 * Turns the convocation of one lot into the bytes of a PDF, keeping Thymeleaf
 * and the PDF renderer out of the application layer entirely - same contract
 * as installment's PaymentReceiptRendererPort.
 */
public interface ConvocationRendererPort {

    byte[] render(ConvocationDocumentView convocation);
}
