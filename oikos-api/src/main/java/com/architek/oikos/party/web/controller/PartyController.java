package com.architek.oikos.party.web.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.application.command.DeletePartyCommand;
import com.architek.oikos.party.application.command.UpdatePartyCommand;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.DeletePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.ListPartiesUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.application.query.ListPartiesQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.party.web.request.CreatePartyRequest;
import com.architek.oikos.party.web.request.UpdatePartyRequest;
import com.architek.oikos.party.web.response.PartyResponse;
import com.architek.oikos.party.web.response.PagedPartyResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Administrative management of parties (identity records independent of any
 * application account). Reserved to ROLE_ADMIN for now; will be revisited once
 * per-copropriete contextual access (RG-ACC-02) is introduced.
 */
@RestController
@RequestMapping("/parties")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PartyController {

    private final CreatePartyUseCase createPartyUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final UpdatePartyUseCase updatePartyUseCase;
    private final ListPartiesUseCase listPartiesUseCase;
    private final DeletePartyUseCase deletePartyUseCase;

    public PartyController(CreatePartyUseCase createPartyUseCase,
                              GetPartyUseCase getPartyUseCase,
                              UpdatePartyUseCase updatePartyUseCase,
                              ListPartiesUseCase listPartiesUseCase,
                              DeletePartyUseCase deletePartyUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.updatePartyUseCase = updatePartyUseCase;
        this.listPartiesUseCase = listPartiesUseCase;
        this.deletePartyUseCase = deletePartyUseCase;
    }

    @GetMapping
    public PagedPartyResponse list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String search) {
        ListPartiesQuery query = new ListPartiesQuery(PageRequest.of(page, size), new PartySearchCriteria(search));
        return PagedPartyResponse.from(listPartiesUseCase.listParties(query));
    }

    @GetMapping("/{id}")
    public PartyResponse getById(@PathVariable String id) {
        return PartyResponse.from(getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(id))));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePartyRequest request) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                request.fullName(), request.partyType(), EmailVO.of(request.email()), request.phone()));
        return ResponseEntity.created(URI.create("/api/v1/parties/" + id)).build();
    }

    @PutMapping("/{id}")
    public PartyResponse update(@PathVariable String id, @Valid @RequestBody UpdatePartyRequest request) {
        UpdatePartyCommand command = new UpdatePartyCommand(
                PartyId.of(id), request.fullName(), request.partyType(), EmailVO.of(request.email()), request.phone());
        return PartyResponse.from(updatePartyUseCase.update(command));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        deletePartyUseCase.delete(new DeletePartyCommand(PartyId.of(id)));
    }
}
