package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRoleOptionJpaRepository extends JpaRepository<BoardRoleOptionEntity, String> {

    List<BoardRoleOptionEntity> findByActiveTrueOrderByPositionAsc();
}
