package com.architek.oikos.property.domain.repository;

import java.util.List;

import com.architek.oikos.property.domain.model.BoardRoleOption;

/**
 * The board seat catalog. Read-only from the application's side: rows are
 * seeded by migration and edited by whoever operates the product.
 */
public interface BoardRoleOptionRepository {

    /** Every seat still offered, in display order. */
    List<BoardRoleOption> findAllActive();
}
