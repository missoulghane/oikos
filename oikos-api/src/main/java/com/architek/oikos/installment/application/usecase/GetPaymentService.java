package com.architek.oikos.installment.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.query.GetPaymentQuery;
import com.architek.oikos.installment.domain.exception.PaymentNotFoundException;
import com.architek.oikos.installment.domain.repository.PaymentRepository;

@Component
public class GetPaymentService implements GetPaymentUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentView getPayment(GetPaymentQuery query) {
        return paymentRepository.findById(query.id())
                .map(PaymentView::from)
                .orElseThrow(() -> new PaymentNotFoundException(query.id()));
    }
}
