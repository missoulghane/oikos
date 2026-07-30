package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.ValidateBulkLettrageCommand;
import com.architek.oikos.accounting.application.command.ValidateUnitLettrageCommand;
import com.architek.oikos.accounting.application.dto.PendingLettrageView;
import com.architek.oikos.accounting.application.port.in.ListPendingLettragesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateBulkLettrageUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateUnitLettrageUseCase;
import com.architek.oikos.accounting.application.query.ListPendingLettragesByPropertyQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** "Valider tout" (unitIds null/empty) or a chosen subset - one transaction for the whole batch. */
@Component
public class ValidateBulkLettrageService implements ValidateBulkLettrageUseCase {

    private final ValidateUnitLettrageUseCase validateUnitLettrageUseCase;
    private final ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase;

    public ValidateBulkLettrageService(ValidateUnitLettrageUseCase validateUnitLettrageUseCase,
                                        ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase) {
        this.validateUnitLettrageUseCase = validateUnitLettrageUseCase;
        this.listPendingLettragesByPropertyUseCase = listPendingLettragesByPropertyUseCase;
    }

    @Override
    @Transactional
    public void validate(ValidateBulkLettrageCommand command) {
        for (EntityId unitId : resolveTargetUnitIds(command)) {
            validateUnitLettrageUseCase.validate(new ValidateUnitLettrageCommand(unitId, command.validatedByUserId()));
        }
    }

    private List<EntityId> resolveTargetUnitIds(ValidateBulkLettrageCommand command) {
        if (command.unitIds() != null && !command.unitIds().isEmpty()) {
            return command.unitIds();
        }
        return listPendingLettragesByPropertyUseCase
                .list(new ListPendingLettragesByPropertyQuery(command.propertyId())).stream()
                .map(PendingLettrageView::unitId)
                .toList();
    }
}
