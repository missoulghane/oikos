package com.architek.oikos.accounting.application.usecase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.ValidateBulkLettrageCommand;
import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;
import com.architek.oikos.accounting.application.dto.PendingLettrageView;
import com.architek.oikos.accounting.application.port.in.ListPendingLettragesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateUnitLettrageUseCase;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ValidateBulkLettrageServiceTest {

    @Mock
    private ValidateUnitLettrageUseCase validateUnitLettrageUseCase;

    @Mock
    private ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase;

    private ValidateBulkLettrageService newService() {
        return new ValidateBulkLettrageService(validateUnitLettrageUseCase, listPendingLettragesByPropertyUseCase);
    }

    @Test
    void an_explicit_unit_list_validates_only_those_units_without_listing_pending_ones() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId validatedByUserId = EntityId.newId();

        newService().validate(new ValidateBulkLettrageCommand(propertyId, List.of(unitId), validatedByUserId));

        verify(validateUnitLettrageUseCase).validate(new ValidateUnitLettrageCommand(unitId, validatedByUserId));
        verify(listPendingLettragesByPropertyUseCase, never()).list(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void a_null_unit_list_validates_every_unit_currently_pending() {
        EntityId propertyId = EntityId.newId();
        EntityId unit1 = EntityId.newId();
        EntityId unit2 = EntityId.newId();
        EntityId validatedByUserId = EntityId.newId();

        when(listPendingLettragesByPropertyUseCase.list(new ListPendingLettragesByPropertyQuery(propertyId)))
                .thenReturn(List.of(new PendingLettrageView(unit1, new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO),
                        new PendingLettrageView(unit2, new BigDecimal("200"), BigDecimal.ZERO, BigDecimal.ZERO)));

        newService().validate(new ValidateBulkLettrageCommand(propertyId, null, validatedByUserId));

        verify(validateUnitLettrageUseCase).validate(new ValidateUnitLettrageCommand(unit1, validatedByUserId));
        verify(validateUnitLettrageUseCase).validate(new ValidateUnitLettrageCommand(unit2, validatedByUserId));
        verify(validateUnitLettrageUseCase, times(2)).validate(org.mockito.ArgumentMatchers.any());
    }
}
