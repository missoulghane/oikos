package com.architek.oikos.messaging.web.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.messaging.application.command.CreateRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.DeleteRecipientGroupCommand;
import com.architek.oikos.messaging.application.command.UpdateRecipientGroupCommand;
import com.architek.oikos.messaging.application.port.in.CreateRecipientGroupUseCase;
import com.architek.oikos.messaging.application.port.in.DeleteRecipientGroupUseCase;
import com.architek.oikos.messaging.application.port.in.ListRecipientGroupsUseCase;
import com.architek.oikos.messaging.application.port.in.UpdateRecipientGroupUseCase;
import com.architek.oikos.messaging.application.query.ListRecipientGroupsQuery;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.messaging.web.request.SaveRecipientGroupRequest;
import com.architek.oikos.messaging.web.response.RecipientGroupReferenceResponse;
import com.architek.oikos.messaging.web.response.RecipientGroupResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Les groupes de destinataires d'une copropriété. Tenir le carnet est réservé
 * au bureau (managesProperty couvre bénévole comme professionnel) ; le lire est
 * ouvert à tout membre de la copropriété, faute de quoi un groupe ne pourrait
 * jamais être choisi au moment de composer.
 */
@RestController
public class RecipientGroupController {

    private final CreateRecipientGroupUseCase createRecipientGroupUseCase;
    private final UpdateRecipientGroupUseCase updateRecipientGroupUseCase;
    private final DeleteRecipientGroupUseCase deleteRecipientGroupUseCase;
    private final ListRecipientGroupsUseCase listRecipientGroupsUseCase;

    public RecipientGroupController(CreateRecipientGroupUseCase createRecipientGroupUseCase,
                                     UpdateRecipientGroupUseCase updateRecipientGroupUseCase,
                                     DeleteRecipientGroupUseCase deleteRecipientGroupUseCase,
                                     ListRecipientGroupsUseCase listRecipientGroupsUseCase) {
        this.createRecipientGroupUseCase = createRecipientGroupUseCase;
        this.updateRecipientGroupUseCase = updateRecipientGroupUseCase;
        this.deleteRecipientGroupUseCase = deleteRecipientGroupUseCase;
        this.listRecipientGroupsUseCase = listRecipientGroupsUseCase;
    }

    @PreAuthorize("@propertyAccess.isPropertyMember(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/messaging/groups")
    public List<RecipientGroupResponse> list(@PathVariable String propertyId) {
        return listRecipientGroupsUseCase.listGroups(new ListRecipientGroupsQuery(EntityId.of(propertyId)))
                .stream().map(RecipientGroupResponse::from).toList();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/messaging/groups")
    public ResponseEntity<RecipientGroupReferenceResponse> create(@PathVariable String propertyId,
                                                                    @Valid @RequestBody SaveRecipientGroupRequest request,
                                                                    Authentication authentication) {
        RecipientGroupId id = createRecipientGroupUseCase.create(new CreateRecipientGroupCommand(
                EntityId.of(propertyId), currentUserId(authentication), request.name(), memberIds(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipientGroupReferenceResponse.from(id));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PutMapping("/properties/{propertyId}/messaging/groups/{groupId}")
    public ResponseEntity<Void> update(@PathVariable String propertyId, @PathVariable String groupId,
                                        @Valid @RequestBody SaveRecipientGroupRequest request) {
        updateRecipientGroupUseCase.update(new UpdateRecipientGroupCommand(RecipientGroupId.of(groupId),
                EntityId.of(propertyId), request.name(), memberIds(request)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @DeleteMapping("/properties/{propertyId}/messaging/groups/{groupId}")
    public ResponseEntity<Void> delete(@PathVariable String propertyId, @PathVariable String groupId) {
        deleteRecipientGroupUseCase.delete(new DeleteRecipientGroupCommand(RecipientGroupId.of(groupId),
                EntityId.of(propertyId)));
        return ResponseEntity.noContent().build();
    }

    private static Set<EntityId> memberIds(SaveRecipientGroupRequest request) {
        return request.memberUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
