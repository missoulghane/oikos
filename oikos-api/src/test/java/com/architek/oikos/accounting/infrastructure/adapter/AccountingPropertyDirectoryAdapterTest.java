package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AccountingPropertyDirectoryAdapterTest {

    @Mock
    private GetPropertyUseCase getPropertyUseCase;

    private AccountingPropertyDirectoryAdapter newAdapter() {
        return new AccountingPropertyDirectoryAdapter(getPropertyUseCase);
    }

    @Test
    void exists_returns_true_when_the_property_is_found() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        when(getPropertyUseCase.getProperty(new GetPropertyQuery(propertyIdValue)))
                .thenReturn(new PropertyView(propertyIdValue, "Residence", "1 rue Test"));

        boolean exists = newAdapter().exists(propertyId);

        assertThat(exists).isTrue();
    }

    @Test
    void exists_returns_false_when_the_property_is_not_found() {
        EntityId propertyId = EntityId.newId();
        PropertyId propertyIdValue = new PropertyId(propertyId);
        when(getPropertyUseCase.getProperty(new GetPropertyQuery(propertyIdValue)))
                .thenThrow(new PropertyNotFoundException(propertyIdValue));

        boolean exists = newAdapter().exists(propertyId);

        assertThat(exists).isFalse();
    }
}
