package com.architek.oikos.user.application.port.in;

import java.util.List;

import com.architek.oikos.user.application.port.out.OwnedInstallmentView;
import com.architek.oikos.user.application.query.GetMyInstallmentsQuery;

public interface GetMyInstallmentsUseCase {

    List<OwnedInstallmentView> getMyInstallments(GetMyInstallmentsQuery query);
}
