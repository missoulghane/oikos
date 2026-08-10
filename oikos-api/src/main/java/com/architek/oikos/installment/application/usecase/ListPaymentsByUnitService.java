package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.ListPaymentsByUnitUseCase;
import com.architek.oikos.installment.application.query.ListPaymentsByUnitQuery;
import com.architek.oikos.installment.domain.repository.PaymentRepository;

@Component
public class ListPaymentsByUnitService implements ListPaymentsByUnitUseCase {

    private final PaymentRepository paymentRepository;

    public ListPaymentsByUnitService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<PaymentView> listPayments(ListPaymentsByUnitQuery query) {
        return paymentRepository.findAllByUnitId(query.unitId()).stream().map(PaymentView::from).toList();
    }
}
