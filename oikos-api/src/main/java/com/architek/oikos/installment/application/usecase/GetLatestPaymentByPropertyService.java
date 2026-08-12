package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.GetLatestPaymentByPropertyUseCase;
import com.architek.oikos.installment.application.query.GetLatestPaymentByPropertyQuery;
import com.architek.oikos.installment.domain.repository.PaymentRepository;

@Component
public class GetLatestPaymentByPropertyService implements GetLatestPaymentByPropertyUseCase {

    private final PaymentRepository paymentRepository;

    public GetLatestPaymentByPropertyService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentView> getLatestPayment(GetLatestPaymentByPropertyQuery query) {
        return paymentRepository.findLatestByPropertyId(query.propertyId(), query.size()).stream()
                .map(PaymentView::from)
                .toList();
    }
}
