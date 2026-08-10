package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.query.ListPaymentsByUnitQuery;

public interface ListPaymentsByUnitUseCase {

    List<PaymentView> listPayments(ListPaymentsByUnitQuery query);
}
