package com.architek.oikos.user.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.UserSearchCriteria;
import com.architek.oikos.user.infrastructure.mapper.UserPersistenceMapper;
import com.architek.oikos.user.infrastructure.persistence.UserEntity;
import com.architek.oikos.user.infrastructure.persistence.UserJpaRepository;
import com.architek.oikos.user.infrastructure.persistence.UserSpecifications;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = jpaRepository.findById(user.getId().asUuid()).orElseGet(UserEntity::new);
        UserEntity saved = jpaRepository.save(mapper.toEntity(user, entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByLinkedPartyId(EntityId partyId) {
        return jpaRepository.existsByLinkedPartyId(partyId.value());
    }

    @Override
    public Page<User> findAll(com.architek.oikos.shared.domain.pagination.PageRequest pageRequest, UserSearchCriteria criteria) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        Specification<UserEntity> specification = UserSpecifications.matching(criteria);
        org.springframework.data.domain.Page<UserEntity> springPage = jpaRepository.findAll(specification, pageable);
        List<User> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.asUuid());
    }
}
