package com.architek.oikos.installment.infrastructure.adapter;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.FundCallLineCommand;
import com.architek.oikos.accounting.application.command.PostFundCallJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.PostFundCallJournalEntryUseCase;
import com.architek.oikos.installment.application.dto.FundCallLine;
import com.architek.oikos.installment.application.port.out.FundCallJournalEntryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class InstallmentFundCallJournalEntryAdapter implements FundCallJournalEntryPort {

    private final PostFundCallJournalEntryUseCase postFundCallJournalEntryUseCase;

    public InstallmentFundCallJournalEntryAdapter(PostFundCallJournalEntryUseCase postFundCallJournalEntryUseCase) {
        this.postFundCallJournalEntryUseCase = postFundCallJournalEntryUseCase;
    }

    @Override
    public EntityId postFundCallEntry(EntityId propertyId, LocalDate pieceDate, String externalReference,
                                       EntityId createdByUserId, List<FundCallLine> lines) {
        List<FundCallLineCommand> lineCommands = lines.stream()
                .map(line -> new FundCallLineCommand(line.unitId(), line.amount()))
                .toList();
        return postFundCallJournalEntryUseCase.post(new PostFundCallJournalEntryCommand(propertyId, pieceDate,
                externalReference, createdByUserId, lineCommands)).value();
    }
}
