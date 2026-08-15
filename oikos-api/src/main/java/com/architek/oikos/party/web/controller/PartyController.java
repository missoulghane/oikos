package com.architek.oikos.party.web.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
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
import com.architek.oikos.party.application.command.UpdatePartyPhoneCommand;
import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.DeletePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.ListPartiesUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyPhoneUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.application.query.ListPartiesQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;
import com.architek.oikos.party.domain.valueobject.PartySortField;
import com.architek.oikos.party.web.request.CreatePartyRequest;
import com.architek.oikos.party.web.request.UpdatePartyPhoneRequest;
import com.architek.oikos.party.web.request.UpdatePartyRequest;
import com.architek.oikos.party.web.response.InvitePartyResponse;
import com.architek.oikos.party.web.response.PartyResponse;
import com.architek.oikos.party.web.response.PagedPartyResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.InvitePartyCommand;
import com.architek.oikos.user.application.port.in.InvitePartyUseCase;

/**
 * Administrative management of parties (identity records scoped to one
 * property). Mutations are open to ADMIN or to a MANAGER of the party's own
 * property (see PropertyAccessEvaluator), so a manager can create/update/
 * remove owners within their own copropriete without needing ADMIN. Reading
 * a single party (getById) additionally allows the party's own linked
 * account (ownsParty) - a USER can view their own contact record.
 */
@RestController
@RequestMapping("/parties")
public class PartyController {

    private final CreatePartyUseCase createPartyUseCase;
    private final GetPartyUseCase getPartyUseCase;
    private final UpdatePartyUseCase updatePartyUseCase;
    private final UpdatePartyPhoneUseCase updatePartyPhoneUseCase;
    private final ListPartiesUseCase listPartiesUseCase;
    private final DeletePartyUseCase deletePartyUseCase;
    private final InvitePartyUseCase invitePartyUseCase;

    public PartyController(CreatePartyUseCase createPartyUseCase,
                              GetPartyUseCase getPartyUseCase,
                              UpdatePartyUseCase updatePartyUseCase,
                              UpdatePartyPhoneUseCase updatePartyPhoneUseCase,
                              ListPartiesUseCase listPartiesUseCase,
                              DeletePartyUseCase deletePartyUseCase,
                              InvitePartyUseCase invitePartyUseCase) {
        this.createPartyUseCase = createPartyUseCase;
        this.getPartyUseCase = getPartyUseCase;
        this.updatePartyUseCase = updatePartyUseCase;
        this.updatePartyPhoneUseCase = updatePartyPhoneUseCase;
        this.listPartiesUseCase = listPartiesUseCase;
        this.deletePartyUseCase = deletePartyUseCase;
        this.invitePartyUseCase = invitePartyUseCase;
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping
    public PagedPartyResponse list(@RequestParam String propertyId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String search,
                                      @RequestParam(required = false) PartySortField sortBy,
                                      @RequestParam(required = false) SortDirection sortDirection) {
        ListPartiesQuery query = new ListPartiesQuery(PageRequest.of(page, size),
                new PartySearchCriteria(EntityId.of(propertyId), search, sortBy, sortDirection));
        return PagedPartyResponse.from(listPartiesUseCase.listParties(query));
    }

    @PreAuthorize("@propertyAccess.managesParty(authentication, #id) or @propertyAccess.ownsParty(authentication, #id)")
    @GetMapping("/{id}")
    public PartyResponse getById(@PathVariable String id) {
        return PartyResponse.from(getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(id))));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #request.propertyId())")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePartyRequest request) {
        PartyId id = createPartyUseCase.create(new CreatePartyCommand(
                EntityId.of(request.propertyId()), request.fullName(), request.partyType(),
                EmailVO.of(request.email()), request.phone()));
        return ResponseEntity.created(URI.create("/api/v1/parties/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.managesParty(authentication, #id)")
    @PutMapping("/{id}")
    public PartyResponse update(@PathVariable String id, @Valid @RequestBody UpdatePartyRequest request) {
        UpdatePartyCommand command = new UpdatePartyCommand(
                PartyId.of(id), request.fullName(), request.partyType(), EmailVO.of(request.email()), request.phone());
        return PartyResponse.from(updatePartyUseCase.update(command));
    }

    /**
     * Narrower self-service edit: lets the party's own linked account (ownsParty) keep its phone
     * number current, without the identity fields (fullName/partyType/email) that stay
     * manager/admin-only via {@link #update}.
     */
    @PreAuthorize("@propertyAccess.managesParty(authentication, #id) or @propertyAccess.ownsParty(authentication, #id)")
    @PatchMapping("/{id}/phone")
    public PartyResponse updatePhone(@PathVariable String id, @Valid @RequestBody UpdatePartyPhoneRequest request) {
        UpdatePartyPhoneCommand command = new UpdatePartyPhoneCommand(PartyId.of(id), request.phone());
        return PartyResponse.from(updatePartyPhoneUseCase.updatePhone(command));
    }

    @PreAuthorize("@propertyAccess.managesParty(authentication, #id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        deletePartyUseCase.delete(new DeletePartyCommand(PartyId.of(id)));
    }

    @PreAuthorize("@propertyAccess.managesParty(authentication, #id)")
    @PostMapping("/{id}/invite")
    public InvitePartyResponse invite(@PathVariable String id) {
        PartyView party = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(id)));
        boolean invited = invitePartyUseCase.invite(
                new InvitePartyCommand(EntityId.of(id), EmailVO.of(party.email()), party.fullName()));
        return new InvitePartyResponse(invited);
    }
}
