package com.architek.oikos.user.domain.repository;

import java.util.Optional;

import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByContactId(EntityId contactId);

    Optional<User> findByLogin(String login);

    boolean existsByLogin(String login);

    Page<User> findAll(PageRequest pageRequest, UserSearchCriteria criteria);

    void deleteById(UserId id);
}
