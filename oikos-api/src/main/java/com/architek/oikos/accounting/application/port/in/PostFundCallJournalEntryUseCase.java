package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.PostFundCallJournalEntryCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * P1 (spec &sect;6): posts the VT journal entry for a fund call - debit
 * each charged unit's own UNIT_RECEIVABLE account (ADR 0001 decision 5),
 * credit the property's DUES_INCOME account for the total. Public entry
 * point for installment's GenerateInstallmentCallUseCase (cross-module,
 * via installment's own out-port - never called with accounting's
 * internals, per the cross-module convention).
 */
public interface PostFundCallJournalEntryUseCase {

    JournalEntryId post(PostFundCallJournalEntryCommand command);
}
