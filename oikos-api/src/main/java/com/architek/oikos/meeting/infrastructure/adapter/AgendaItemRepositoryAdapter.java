package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.infrastructure.mapper.AgendaItemPersistenceMapper;
import com.architek.oikos.meeting.infrastructure.persistence.AgendaItemEntity;
import com.architek.oikos.meeting.infrastructure.persistence.AgendaItemJpaRepository;

@Component
public class AgendaItemRepositoryAdapter implements AgendaItemRepository {

    private final AgendaItemJpaRepository jpaRepository;
    private final AgendaItemPersistenceMapper mapper;

    public AgendaItemRepositoryAdapter(AgendaItemJpaRepository jpaRepository, AgendaItemPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AgendaItem save(AgendaItem agendaItem) {
        return mapper.toDomain(jpaRepository.save(toManagedEntity(agendaItem)));
    }

    @Override
    public List<AgendaItem> saveAll(List<AgendaItem> agendaItems) {
        List<AgendaItemEntity> entities = agendaItems.stream().map(this::toManagedEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<AgendaItem> findById(AgendaItemId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(AgendaItemId id) {
        jpaRepository.deleteById(id.asUuid());
        // Forces the DELETE out before the callers renumber the survivors: without it Hibernate
        // may order the UPDATEs first and momentarily collide with the row being removed.
        jpaRepository.flush();
    }

    @Override
    public List<AgendaItem> findByGeneralMeetingId(GeneralMeetingId generalMeetingId) {
        return jpaRepository.findByGeneralMeetingIdOrderByPositionAsc(generalMeetingId.asUuid()).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public long countByGeneralMeetingId(GeneralMeetingId generalMeetingId) {
        return jpaRepository.countByGeneralMeetingId(generalMeetingId.asUuid());
    }

    @Override
    public Optional<Integer> findMaxPosition(GeneralMeetingId generalMeetingId) {
        return jpaRepository.findMaxPosition(generalMeetingId.asUuid());
    }

    private AgendaItemEntity toManagedEntity(AgendaItem agendaItem) {
        AgendaItemEntity entity = jpaRepository.findById(agendaItem.getId().asUuid()).orElseGet(AgendaItemEntity::new);
        return mapper.toEntity(agendaItem, entity);
    }
}
