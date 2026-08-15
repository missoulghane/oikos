package com.architek.oikos.property.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.response.PagedPropertyContactResponse;
import com.architek.oikos.property.domain.valueobject.ContactSortField;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;

@RestController
public class PropertyContactController {

    private final ListContactsByPropertyUseCase listContactsByPropertyUseCase;

    public PropertyContactController(ListContactsByPropertyUseCase listContactsByPropertyUseCase) {
        this.listContactsByPropertyUseCase = listContactsByPropertyUseCase;
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/contacts")
    public PagedPropertyContactResponse list(@PathVariable String propertyId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) Boolean hasLinkedAccount,
                                              @RequestParam(required = false) ContactSortField sortBy,
                                              @RequestParam(required = false) SortDirection sortDirection) {
        ListContactsByPropertyQuery query = new ListContactsByPropertyQuery(
                PropertyId.of(propertyId), PageRequest.of(page, size), search, hasLinkedAccount, sortBy,
                sortDirection);
        return PagedPropertyContactResponse.from(listContactsByPropertyUseCase.listContacts(query));
    }
}
