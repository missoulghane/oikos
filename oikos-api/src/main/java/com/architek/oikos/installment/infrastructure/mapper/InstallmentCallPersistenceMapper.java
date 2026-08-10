package com.architek.oikos.installment.infrastructure.mapper;

import java.time.YearMonth;

import org.mapstruct.Mapper;

import com.architek.oikos.installment.domain.model.InstallmentCall;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentCallEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface InstallmentCallPersistenceMapper {

    default InstallmentCallEntity toEntity(InstallmentCall installmentCall) {
        return toEntity(installmentCall, new InstallmentCallEntity());
    }

    default InstallmentCallEntity toEntity(InstallmentCall installmentCall, InstallmentCallEntity entity) {
        entity.setId(installmentCall.getId().asUuid());
        entity.setPropertyId(installmentCall.getPropertyId().value());
        entity.setPeriod(installmentCall.getPeriod().atDay(1));
        entity.setDueDate(installmentCall.getDueDate());
        entity.setStatus(installmentCall.getStatus().name());
        entity.setJournalEntryId(installmentCall.getJournalEntryId().map(EntityId::value).orElse(null));
        return entity;
    }

    default InstallmentCall toDomain(InstallmentCallEntity entity) {
        return InstallmentCall.reconstruct(InstallmentCallId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                YearMonth.from(entity.getPeriod()), entity.getDueDate(),
                InstallmentCallStatus.valueOf(entity.getStatus()),
                entity.getJournalEntryId() == null ? null : EntityId.of(entity.getJournalEntryId()));
    }
}
