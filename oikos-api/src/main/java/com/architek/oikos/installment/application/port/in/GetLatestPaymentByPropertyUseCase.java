package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.query.GetLatestPaymentByPropertyQuery;

public interface GetLatestPaymentByPropertyUseCase {

    /** Most recent payments for the property, newest first, capped at {@link GetLatestPaymentByPropertyQuery#size()}. */
    List<PaymentView> getLatestPayment(GetLatestPaymentByPropertyQuery query);
}
