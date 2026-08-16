package com.architek.oikos.document.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.port.out.DocumentOwnerExistencePort;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemQuery;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;
import com.architek.oikos.meeting.domain.exception.AgendaItemNotFoundException;
import com.architek.oikos.meeting.domain.exception.ConvocationNotFoundException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
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
    private final GetAgendaItemUseCase getAgendaItemUseCase;
    private final GetConvocationUseCase getConvocationUseCase;
    private final GetGeneralMeetingUseCase getGeneralMeetingUseCase;

    public DocumentOwnerExistenceAdapter(GetPropertyUseCase getPropertyUseCase, GetUnitUseCase getUnitUseCase,
                                          GetPaymentUseCase getPaymentUseCase, GetAgendaItemUseCase getAgendaItemUseCase,
                                          GetConvocationUseCase getConvocationUseCase,
                                          GetGeneralMeetingUseCase getGeneralMeetingUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
        this.getAgendaItemUseCase = getAgendaItemUseCase;
        this.getConvocationUseCase = getConvocationUseCase;
        this.getGeneralMeetingUseCase = getGeneralMeetingUseCase;
    }

    @Override
    public boolean exists(DocumentOwnerType ownerType, EntityId ownerId) {
        return switch (ownerType) {
            case PROPERTY -> propertyExists(ownerId);
            case UNIT -> unitExists(ownerId);
            case PAYMENT -> paymentExists(ownerId);
            case AGENDA_ITEM -> agendaItemExists(ownerId);
            case GENERAL_MEETING -> generalMeetingExists(ownerId);
            case CONVOCATION -> convocationExists(ownerId);
            // The minutes are 1-1 with their meeting and carry its id as owner, so there is
            // nothing else to look up - a meeting that exists can hold its minutes' PDF.
            case MEETING_MINUTES -> generalMeetingExists(ownerId);
        };
    }

    private boolean agendaItemExists(EntityId ownerId) {
        try {
            getAgendaItemUseCase.getAgendaItem(new GetAgendaItemQuery(AgendaItemId.of(ownerId.value())));
            return true;
        } catch (AgendaItemNotFoundException e) {
            return false;
        }
    }

    private boolean convocationExists(EntityId ownerId) {
        try {
            getConvocationUseCase.getConvocation(new GetConvocationQuery(ConvocationId.of(ownerId.value())));
            return true;
        } catch (ConvocationNotFoundException e) {
            return false;
        }
    }

    private boolean generalMeetingExists(EntityId ownerId) {
        try {
            getGeneralMeetingUseCase.getGeneralMeeting(new GetGeneralMeetingQuery(GeneralMeetingId.of(ownerId.value())));
            return true;
        } catch (GeneralMeetingNotFoundException e) {
            return false;
        }
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
