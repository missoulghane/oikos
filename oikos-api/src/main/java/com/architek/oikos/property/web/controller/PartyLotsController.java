package com.architek.oikos.property.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.property.application.port.in.ListLotsByPartyUseCase;
import com.architek.oikos.property.application.query.ListLotsByPartyQuery;
import com.architek.oikos.property.web.response.PartyLotResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
public class PartyLotsController {

    private final ListLotsByPartyUseCase listLotsByPartyUseCase;

    public PartyLotsController(ListLotsByPartyUseCase listLotsByPartyUseCase) {
        this.listLotsByPartyUseCase = listLotsByPartyUseCase;
    }

    @GetMapping("/parties/{partyId}/lots")
    public List<PartyLotResponse> list(@PathVariable String partyId) {
        return listLotsByPartyUseCase.listLots(new ListLotsByPartyQuery(EntityId.of(partyId))).stream()
                .map(PartyLotResponse::from)
                .toList();
    }
}
