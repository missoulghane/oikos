package com.architek.oikos.property.application.port.in;

import java.util.List;

import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.query.ListUnitTypePricesByPropertyQuery;

public interface ListUnitTypePricesByPropertyUseCase {

    List<UnitTypePriceView> listUnitTypePrices(ListUnitTypePricesByPropertyQuery query);
}
