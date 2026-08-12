package com.architek.oikos.user.infrastructure.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.ListPaymentsByUnitUseCase;
import com.architek.oikos.installment.application.query.ListPaymentsByUnitQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.OwnedPaymentMode;
import com.architek.oikos.user.application.port.out.OwnedPaymentView;
import com.architek.oikos.user.application.port.out.PaymentDirectoryPort;

@Component
public class PaymentDirectoryAdapter implements PaymentDirectoryPort {

    private final ListPaymentsByUnitUseCase listPaymentsByUnitUseCase;

    public PaymentDirectoryAdapter(ListPaymentsByUnitUseCase listPaymentsByUnitUseCase) {
        this.listPaymentsByUnitUseCase = listPaymentsByUnitUseCase;
    }

    @Override
    public List<OwnedPaymentView> listPaymentsForUnit(EntityId unitId) {
        return listPaymentsByUnitUseCase.listPayments(new ListPaymentsByUnitQuery(unitId)).stream()
                .map(PaymentDirectoryAdapter::toOwnedPaymentView)
                .toList();
    }

    private static OwnedPaymentView toOwnedPaymentView(PaymentView view) {
        // .name() only, never the enum type itself - installment.domain.valueobject.PaymentMode
        // must not be named in this module (rule 4), same as OwnedPaymentMode's own rationale.
        return new OwnedPaymentView(EntityId.of(view.id().toString()), view.propertyId(), view.unitId(),
                OwnedPaymentMode.valueOf(view.mode().name()), view.valueDate(), view.amount(), view.journalEntryId());
    }
}
