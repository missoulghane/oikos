package com.architek.oikos.installment.application.port.in;

import java.util.Optional;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.query.GetLatestPaymentByPropertyQuery;

public interface GetLatestPaymentByPropertyUseCase {

    Optional<PaymentView> getLatestPayment(GetLatestPaymentByPropertyQuery query);
}
