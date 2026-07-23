package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.query.ListMovementsForAllocationQuery;

/**
 * Unpaginated movement listing by account - unlike ListMovementsUseCase
 * (paginated, for display), this exists specifically for FIFO auto-allocation
 * (installment module's AutoAllocationEngine), which needs the full ledger of
 * an account to match credits against due amounts, not a page of it.
 */
public interface ListMovementsForAllocationUseCase {

    List<MovementView> listMovements(ListMovementsForAllocationQuery query);
}
