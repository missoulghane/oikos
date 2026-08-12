package com.architek.oikos.user.application.port.in;

import java.util.List;

import com.architek.oikos.user.application.port.out.OwnedPaymentView;
import com.architek.oikos.user.application.query.GetMyPaymentsQuery;

public interface GetMyPaymentsUseCase {

    List<OwnedPaymentView> getMyPayments(GetMyPaymentsQuery query);
}
