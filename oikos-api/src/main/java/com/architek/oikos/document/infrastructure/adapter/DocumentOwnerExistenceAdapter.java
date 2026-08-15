package com.architek.oikos.document.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.port.out.DocumentOwnerExistencePort;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.query.GetPaymentQuery;
import com.architek.oikos.installment.domain.exception.PaymentNotFoundException;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Single extension point for a new attachable DocumentOwnerType: add the
 * enum constant, inject the owning module's GetXxxUseCase, and add a case
 * here - cross-module access stays through a port-in use case, never
 * another module's repository/domain internals directly (rule 4/6).
 */
@Component
public class DocumentOwnerExistenceAdapter implements DocumentOwnerExistencePort {

    private final GetPropertyUseCase getPropertyUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    public DocumentOwnerExistenceAdapter(GetPropertyUseCase getPropertyUseCase, GetUnitUseCase getUnitUseCase,
                                          GetPaymentUseCase getPaymentUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @Override
    public boolean exists(DocumentOwnerType ownerType, EntityId ownerId) {
        return switch (ownerType) {
            case PROPERTY -> propertyExists(ownerId);
            case UNIT -> unitExists(ownerId);
            case PAYMENT -> paymentExists(ownerId);
        };
    }

    private boolean paymentExists(EntityId ownerId) {
        try {
            getPaymentUseCase.getPayment(new GetPaymentQuery(PaymentId.of(ownerId.value())));
            return true;
        } catch (PaymentNotFoundException e) {
            return false;
        }
    }

    private boolean propertyExists(EntityId ownerId) {
        try {
            getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(ownerId.value())));
            return true;
        } catch (PropertyNotFoundException e) {
            return false;
        }
    }

    private boolean unitExists(EntityId ownerId) {
        try {
            getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(ownerId.value())));
            return true;
        } catch (UnitNotFoundException e) {
            return false;
        }
    }
}
