package com.architek.oikos.user.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.user.domain.model.OnboardingLead;
import com.architek.oikos.user.domain.repository.OnboardingLeadRepository;
import com.architek.oikos.user.domain.valueobject.OnboardingLeadId;
import com.architek.oikos.user.infrastructure.persistence.OnboardingLeadEntity;
import com.architek.oikos.user.infrastructure.persistence.OnboardingLeadJpaRepository;

@Component
public class OnboardingLeadRepositoryAdapter implements OnboardingLeadRepository {

    private final OnboardingLeadJpaRepository jpaRepository;

    public OnboardingLeadRepositoryAdapter(OnboardingLeadJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OnboardingLead save(OnboardingLead lead) {
        // Upsert by email: the same address coming back updates its row instead of
        // hitting the unique constraint (see V20). The stored row keeps its own id -
        // overwriting the primary key of an already-managed entity is illegal, and
        // nothing references a lead by id anyway.
        Optional<OnboardingLeadEntity> existing = jpaRepository.findByEmail(lead.email().value());
        OnboardingLeadEntity entity = existing.orElseGet(OnboardingLeadEntity::new);
        if (existing.isEmpty()) {
            entity.setId(lead.id().asUuid());
        }
        entity.setEmail(lead.email().value());
        entity.setFirstName(lead.firstName());
        entity.setLastName(lead.lastName());
        entity.setConvertedAt(lead.convertedAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<OnboardingLead> findByEmail(EmailVO email) {
        return jpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    private OnboardingLead toDomain(OnboardingLeadEntity entity) {
        return new OnboardingLead(
                OnboardingLeadId.of(entity.getId()),
                EmailVO.of(entity.getEmail()),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getConvertedAt());
    }
}
