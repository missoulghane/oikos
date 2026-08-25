package com.architek.oikos.property.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.property.application.port.in.FindContactByAccountEmailUseCase;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.query.FindContactByAccountEmailQuery;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.response.LinkedContactResponse;
import com.architek.oikos.property.web.response.PagedPropertyContactResponse;
import com.architek.oikos.property.domain.valueobject.ContactSortField;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.pagination.SortDirection;

@RestController
public class PropertyContactController {

    private final ListContactsByPropertyUseCase listContactsByPropertyUseCase;
    private final FindContactByAccountEmailUseCase findContactByAccountEmailUseCase;

    public PropertyContactController(ListContactsByPropertyUseCase listContactsByPropertyUseCase,
                                      FindContactByAccountEmailUseCase findContactByAccountEmailUseCase) {
        this.listContactsByPropertyUseCase = listContactsByPropertyUseCase;
        this.findContactByAccountEmailUseCase = findContactByAccountEmailUseCase;
    }

    /**
     * « Quelqu'un se connecte-t-il avec cette adresse, et quelle est sa fiche
     * ici ? » Sert aux écrans de rattachement (lot, conseil syndical) à
     * annoncer le rapprochement avant l'envoi. Corps vide quand personne ne
     * correspond - le cas de l'immense majorité des frappes.
     *
     * <p>Une adresse mal formée renvoie « personne » plutôt qu'une erreur : le
     * champ est interrogé à chaque frappe, et « jane@ » n'est pas une faute du
     * syndic, seulement une adresse en cours de saisie.
     */
    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/contacts/by-account-email")
    public LinkedContactResponse byAccountEmail(@PathVariable String propertyId, @RequestParam String email) {
        EmailVO accountEmail;
        try {
            accountEmail = EmailVO.of(email);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return LinkedContactResponse.from(findContactByAccountEmailUseCase.findContact(
                new FindContactByAccountEmailQuery(PropertyId.of(propertyId), accountEmail)));
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
