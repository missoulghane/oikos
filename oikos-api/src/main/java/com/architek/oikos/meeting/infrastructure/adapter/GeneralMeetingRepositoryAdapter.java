package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.infrastructure.mapper.GeneralMeetingPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.GeneralMeetingEntity;
import com.architek.oikos.meeting.infrastructure.persistence.GeneralMeetingJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class GeneralMeetingRepositoryAdapter implements GeneralMeetingRepository {

    private final GeneralMeetingJpaRepository jpaRepository;
    private final GeneralMeetingPersistenceMapper mapper;

    public GeneralMeetingRepositoryAdapter(GeneralMeetingJpaRepository jpaRepository,
                                            GeneralMeetingPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public GeneralMeeting save(GeneralMeeting generalMeeting) {
        // Reloads the managed row first so the audit columns and @Version survive an update
        // (same pattern as ConversationRepositoryAdapter) - saving a detached instance built
        // from the domain would otherwise reset them and break optimistic locking.
        GeneralMeetingEntity entity = jpaRepository.findById(generalMeeting.getId().asUuid())
                .orElseGet(GeneralMeetingEntity::new);
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(generalMeeting, entity)));
    }

    @Override
    public Optional<GeneralMeeting> findById(GeneralMeetingId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(GeneralMeetingId id) {
        jpaRepository.deleteById(id.asUuid());
    }

    @Override
    public Page<GeneralMeeting> findByProperty(EntityId propertyId, MeetingStatus status, MeetingType meetingType,
                                                PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.pageSize()).withPage(pageRequest.pageNumber());
        org.springframework.data.domain.Page<GeneralMeetingEntity> springPage =
                jpaRepository.findByPropertyIdFiltered(propertyId.value(), status, meetingType, pageable);
        List<GeneralMeeting> content = springPage.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, pageRequest.pageNumber(), pageRequest.pageSize(), springPage.getTotalElements());
    }
}
