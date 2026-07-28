package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;

public interface ListContactsByPropertyUseCase {

    List<PropertyContactView> listContacts(ListContactsByPropertyQuery query);
}
