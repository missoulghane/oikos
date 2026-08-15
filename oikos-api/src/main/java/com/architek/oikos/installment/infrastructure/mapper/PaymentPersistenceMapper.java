package com.architek.oikos.installment.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.installment.infrastructure.persistence.PaymentEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface PaymentPersistenceMapper {

    default PaymentEntity toEntity(Payment payment) {
        return toEntity(payment, new PaymentEntity());
    }

    default PaymentEntity toEntity(Payment payment, PaymentEntity entity) {
        entity.setId(payment.getId().asUuid());
        entity.setPropertyId(payment.getPropertyId().value());
        entity.setUnitId(payment.getUnitId().value());
        entity.setMode(payment.getMode().name());
        entity.setValueDate(payment.getValueDate());
        entity.setAmount(payment.getAmount().value());
        entity.setJournalEntryId(payment.getJournalEntryId().value());
        entity.setReceiptNumber(payment.getReceiptNumber().map(ReceiptNumber::format).orElse(null));
        return entity;
    }

    default Payment toDomain(PaymentEntity entity) {
        return Payment.reconstruct(PaymentId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                EntityId.of(entity.getUnitId()), PaymentMode.valueOf(entity.getMode()), entity.getValueDate(),
                Amount.of(entity.getAmount()), EntityId.of(entity.getJournalEntryId()),
                entity.getReceiptNumber() == null ? null : ReceiptNumber.parse(entity.getReceiptNumber()));
    }
}
