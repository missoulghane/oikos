package com.architek.oikos.user.application.usecase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindUsersByPartyIdsUseCase;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class FindUsersByPartyIdsService implements FindUsersByPartyIdsUseCase {

    private final UserRepository userRepository;

    public FindUsersByPartyIdsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<EntityId, EntityId> findUserIdsByPartyIds(Collection<EntityId> partyIds) {
        Map<EntityId, EntityId> userIdByPartyId = new LinkedHashMap<>();
        for (User user : userRepository.findByLinkedPartyIds(partyIds)) {
            for (EntityId partyId : user.getLinkedPartyIds()) {
                if (partyIds.contains(partyId)) {
                    userIdByPartyId.put(partyId, user.getId().value());
                }
            }
        }
        return userIdByPartyId;
    }
}
