package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;

/**
 * Cree une property, sans aucun type de lot: ses types sont ceux que le syndic
 * declare ensuite. La property est creee seule: ses buildings sont ajoutes
 * separement, plus tard, via AddBuildingUseCase. Provisionne aussi son
 * compte de caisse PCM (ADR 0001, "exigence supplementaire") - les comptes
 * bancaires restent configures manuellement, plus tard.
 */
@Component
public class CreatePropertyService implements CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final LedgerAccountProvisioningPort ledgerAccountProvisioningPort;

    public CreatePropertyService(PropertyRepository propertyRepository,
                                  LedgerAccountProvisioningPort ledgerAccountProvisioningPort) {
        this.propertyRepository = propertyRepository;
        this.ledgerAccountProvisioningPort = ledgerAccountProvisioningPort;
    }

    @Override
    @Transactional
    public PropertyId create(CreatePropertyCommand command) {
        Property property = Property.create(PropertyId.newId(), command.name(), command.address(), command.city());
        Property saved = propertyRepository.save(property);

        ledgerAccountProvisioningPort.provisionPropertyCashAccount(saved.getId().value());

        return saved.getId();
    }
}
