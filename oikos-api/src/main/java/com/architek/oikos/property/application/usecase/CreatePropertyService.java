package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Cree une property et sa ligne de type de lot par defaut ("OTHERS"), dans la
 * meme transaction. La property est creee seule: ses buildings sont ajoutes
 * separement, plus tard, via AddBuildingUseCase. Provisionne aussi son
 * compte de caisse PCM (ADR 0001, "exigence supplementaire") - les comptes
 * bancaires restent configures manuellement, plus tard.
 */
@Component
public class CreatePropertyService implements CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final LedgerAccountProvisioningPort ledgerAccountProvisioningPort;

    public CreatePropertyService(PropertyRepository propertyRepository,
                                  UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                  LedgerAccountProvisioningPort ledgerAccountProvisioningPort) {
        this.propertyRepository = propertyRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.ledgerAccountProvisioningPort = ledgerAccountProvisioningPort;
    }

    @Override
    @Transactional
    public PropertyId create(CreatePropertyCommand command) {
        Property property = Property.create(PropertyId.newId(), command.name(), command.address());
        Property saved = propertyRepository.save(property);

        unitTypeDefinitionRepository.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), saved.getId(),
                UnitTypeDefinition.DEFAULT_NAME));

        ledgerAccountProvisioningPort.provisionPropertyCashAccount(saved.getId().value());

        return saved.getId();
    }
}
