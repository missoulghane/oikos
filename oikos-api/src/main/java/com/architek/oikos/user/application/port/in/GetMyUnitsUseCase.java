package com.architek.oikos.user.application.port.in;

import java.util.List;

import com.architek.oikos.user.application.port.out.OwnedUnitView;
import com.architek.oikos.user.application.query.GetMyUnitsQuery;

public interface GetMyUnitsUseCase {

    List<OwnedUnitView> getMyUnits(GetMyUnitsQuery query);
}
