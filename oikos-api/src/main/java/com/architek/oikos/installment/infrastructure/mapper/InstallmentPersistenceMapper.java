package com.architek.oikos.installment.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface InstallmentPersistenceMapper {

    default InstallmentEntity toEntity(Installment installment) {
        return toEntity(installment, new InstallmentEntity());
    }

    default InstallmentEntity toEntity(Installment installment, InstallmentEntity entity) {
        entity.setId(installment.getId().asUuid());
        entity.setUnitId(installment.getUnitId().value());
        entity.setDueDate(installment.getDueDate());
        entity.setAmount(installment.getAmount().value());
        entity.setInstallmentCallId(installment.getInstallmentCallId() == null ? null : installment.getInstallmentCallId().asUuid());
        entity.setOutstandingAmount(installment.getOutstandingAmount());
        return entity;
    }

    default Installment toDomain(InstallmentEntity entity) {
        InstallmentCallId installmentCallId = entity.getInstallmentCallId() == null ? null : InstallmentCallId.of(entity.getInstallmentCallId());
        return Installment.reconstruct(InstallmentId.of(entity.getId()),
                EntityId.of(entity.getUnitId()), entity.getDueDate(), Amount.of(entity.getAmount()), installmentCallId,
                entity.getOutstandingAmount());
    }
}
