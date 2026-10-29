package com.architek.oikos.property.domain.repository;

import java.util.List;

import com.architek.oikos.property.domain.model.UnitTypeChoice;

/**
 * The unit type catalog. Read-only from the application's side: rows are seeded
 * by migration and edited by whoever operates the product.
 */
public interface UnitTypeChoiceRepository {

    /** Every type still offered, in display order. */
    List<UnitTypeChoice> findAllActive();
}
