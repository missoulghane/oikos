package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.query.GetInstallmentCollectionSummaryQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentCollectionSummary;

/** What a copropriété still has to collect - the syndic's dashboard badge. */
public interface GetInstallmentCollectionSummaryUseCase {

    InstallmentCollectionSummary getSummary(GetInstallmentCollectionSummaryQuery query);
}
