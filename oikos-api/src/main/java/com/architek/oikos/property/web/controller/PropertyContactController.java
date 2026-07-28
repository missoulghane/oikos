package com.architek.oikos.property.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.response.PropertyContactResponse;

@RestController
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
public class PropertyContactController {

    private final ListContactsByPropertyUseCase listContactsByPropertyUseCase;

    public PropertyContactController(ListContactsByPropertyUseCase listContactsByPropertyUseCase) {
        this.listContactsByPropertyUseCase = listContactsByPropertyUseCase;
    }

    @GetMapping("/properties/{propertyId}/contacts")
    public List<PropertyContactResponse> list(@PathVariable String propertyId) {
        return listContactsByPropertyUseCase.listContacts(new ListContactsByPropertyQuery(PropertyId.of(propertyId))).stream()
                .map(PropertyContactResponse::from)
                .toList();
    }
}
