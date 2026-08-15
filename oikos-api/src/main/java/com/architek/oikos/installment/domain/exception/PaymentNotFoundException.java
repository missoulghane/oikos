package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(PaymentId id) {
        super("Payment not found with id: " + id);
    }
}
