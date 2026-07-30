package com.architek.oikos.accounting.domain.repository;

import java.util.List;

import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;

public interface UnitAccountAllocationRepository {

    UnitAccountAllocation save(UnitAccountAllocation allocation);

    List<UnitAccountAllocation> findAllByUnitAccountId(UnitAccountId unitAccountId);
}
