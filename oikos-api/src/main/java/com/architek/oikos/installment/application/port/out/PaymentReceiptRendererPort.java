package com.architek.oikos.installment.application.port.out;

import com.architek.oikos.installment.application.dto.PaymentReceiptView;

/**
 * Turns a receipt view into the bytes of a PDF. Keeps the templating engine and
 * the PDF renderer out of the application layer entirely, in the same spirit as
 * shared.application.port.out.EmailSenderPort: the use case only ever sees a
 * byte[]. Implemented by installment.infrastructure.receipt.
 */
public interface PaymentReceiptRendererPort {

    byte[] render(PaymentReceiptView receipt);
}
