package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.PropertyContactView;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListContactsByPropertyUseCase {

    Page<PropertyContactView> listContacts(ListContactsByPropertyQuery query);
}
