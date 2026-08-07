package com.architek.oikos.installment.infrastructure.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.domain.model.Installment;
import com.architek.oikos.installment.domain.repository.InstallmentRepository;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentSortField;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.installment.infrastructure.mapper.InstallmentPersistenceMapper;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentEntity;
import com.architek.oikos.installment.infrastructure.persistence.InstallmentJpaRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class InstallmentRepositoryAdapter implements InstallmentRepository {

    private final InstallmentJpaRepository jpaRepository;
    private final InstallmentPersistenceMapper mapper;

    public InstallmentRepositoryAdapter(InstallmentJpaRepository jpaRepository, InstallmentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Installment save(Installment installment) {
        InstallmentEntity entity = jpaRepository.findById(installment.getId().asUuid()).orElseGet(InstallmentEntity::new);
        mapper.toEntity(installment, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Installment> findById(InstallmentId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public List<Installment> findAllByUnitId(EntityId unitId) {
        return jpaRepository.findAllByUnitId(unitId.value()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Installment> findAllByInstallmentCallId(InstallmentCallId installmentCallId) {
        return jpaRepository.findAllByInstallmentCallId(installmentCallId.asUuid()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteAllByInstallmentCallId(InstallmentCallId installmentCallId) {
        jpaRepository.deleteAllByInstallmentCallId(installmentCallId.asUuid());
    }

    @Override
    public Page<Installment> findPageByUnitIds(List<EntityId> unitIds, InstallmentFilter filter, PageRequest pageRequest) {
        List<UUID> ids = unitIds.stream().map(EntityId::value).toList();
        boolean hasStatusFilter = !filter.statuses().isEmpty();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(pageRequest.pageNumber(), pageRequest.pageSize(), toSort(filter));

        org.springframework.data.domain.Page<InstallmentEntity> page = jpaRepository.search(ids,
                filter.dueDateFrom(), filter.dueDateTo(), hasStatusFilter,
                filter.statuses().contains(InstallmentStatus.NOT_SETTLED),
                filter.statuses().contains(InstallmentStatus.PARTIALLY_SETTLED),
                filter.statuses().contains(InstallmentStatus.SETTLED),
                filter.installmentCallId() != null ? filter.installmentCallId().asUuid() : null,
                pageable);

        List<Installment> content = page.getContent().stream().map(mapper::toDomain).toList();
        return Page.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private static Sort toSort(InstallmentFilter filter) {
        Sort.Direction direction = filter.sortDirection() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
        String property = filter.sortField() == InstallmentSortField.AMOUNT ? "amount" : "dueDate";
        return Sort.by(direction, property);
    }
}
