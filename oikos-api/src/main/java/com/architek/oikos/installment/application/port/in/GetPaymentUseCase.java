package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.query.GetPaymentQuery;

/**
 * PaymentRepository.findById existed with no use case exposing it. Needed by two
 * callers outside this module: the receipt generation, and PropertyAccessEvaluator,
 * which has to resolve a payment to its unit before it can decide who may read
 * its receipt (cross-module access goes through a port-in, never a repository).
 */
public interface GetPaymentUseCase {

    PaymentView getPayment(GetPaymentQuery query);
}
