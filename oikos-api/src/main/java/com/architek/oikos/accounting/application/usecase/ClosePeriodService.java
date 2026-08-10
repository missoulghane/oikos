package com.architek.oikos.accounting.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.ClosePeriodCommand;
import com.architek.oikos.accounting.application.dto.PeriodView;
import com.architek.oikos.accounting.application.port.in.ClosePeriodUseCase;
import com.architek.oikos.accounting.domain.exception.PeriodNotClosableException;
import com.architek.oikos.accounting.domain.exception.PeriodNotOpenException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.model.PeriodClosingValidator;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;

/**
 * P8 (spec &sect;6): no bank/cash reconciliation feature exists yet, so
 * PeriodClosingValidator's treasuryReconciliation check is always skipped
 * (null) - documented gap, see ADR 0001.
 */
@Component
public class ClosePeriodService implements ClosePeriodUseCase {

    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final PeriodRepository periodRepository;
    private final JournalEntryRepository journalEntryRepository;

    public ClosePeriodService(EnforceExerciseOpenService enforceExerciseOpenService, PeriodRepository periodRepository,
                               JournalEntryRepository journalEntryRepository) {
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.periodRepository = periodRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public PeriodView close(ClosePeriodCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        Period period = periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), command.period())
                .orElseThrow(() -> new PeriodNotOpenException(command.period().atDay(1)));

        List<String> violations = new ArrayList<>();
        Optional<Period> previousPeriod = periodRepository.findByExerciseIdAndYearMonth(exercise.getId(),
                command.period().minusMonths(1));
        if (previousPeriod.isPresent() && previousPeriod.get().isOpen()) {
            violations.add("Previous period is not closed yet");
        }

        List<JournalEntry> entries = journalEntryRepository.findAllByPeriodId(period.getId());
        List<JournalEntryStatus> statuses = entries.stream().map(JournalEntry::getStatus).toList();
        Map<JournalCode, List<Integer>> pieceNumbersByJournal = entries.stream()
                .filter(entry -> entry.getPieceNumber().isPresent())
                .collect(Collectors.groupingBy(JournalEntry::getJournalCode,
                        Collectors.mapping(entry -> entry.getPieceNumber().get(), Collectors.toList())));
        violations.addAll(PeriodClosingValidator.violations(statuses, pieceNumbersByJournal, null));

        if (!violations.isEmpty()) {
            throw new PeriodNotClosableException(period.getId(), violations);
        }

        Period closed = period.close(Instant.now(), command.closedByUserId());
        return PeriodView.from(periodRepository.save(closed));
    }
}
