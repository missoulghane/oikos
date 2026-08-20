package com.architek.oikos.accounting.application.usecase;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.ReopenPeriodCommand;
import com.architek.oikos.accounting.application.dto.PeriodView;
import com.architek.oikos.accounting.application.port.in.ReopenPeriodUseCase;
import com.architek.oikos.accounting.domain.exception.PeriodNotReopenableException;
import com.architek.oikos.accounting.domain.exception.PeriodNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;

/**
 * Rouvre une période close, pour corriger ce que la clôture a figé trop tôt.
 *
 * <p>Deux règles encadrent le geste, symétriques de celles de la clôture.
 *
 * <p>D'abord, seule la <em>dernière</em> période close se rouvre. Les périodes
 * closes forment un préfixe continu - c'est ce que garantit la clôture, qui
 * refuse tant que la précédente est ouverte - et rouvrir au milieu percerait ce
 * préfixe : on se retrouverait avec février ouvert entre janvier et mars clos,
 * un état que la clôture elle-même n'aurait jamais pu produire.
 *
 * <p>Ensuite, l'exercice doit être ouvert. Un exercice clos a soldé ses classes
 * 6 et 7 et arrêté son résultat ; rouvrir un de ses mois rendrait ce résultat
 * faux sans que rien ne le recalcule.
 */
@Component
public class ReopenPeriodService implements ReopenPeriodUseCase {

    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final PeriodRepository periodRepository;

    public ReopenPeriodService(EnforceExerciseOpenService enforceExerciseOpenService, PeriodRepository periodRepository) {
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.periodRepository = periodRepository;
    }

    @Override
    @Transactional
    public PeriodView reopen(ReopenPeriodCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        Period period = periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), command.period())
                .orElseThrow(() -> new PeriodNotFoundException(command.period()));

        List<YearMonth> closedAfter = periodRepository.findAllByExerciseId(exercise.getId()).stream()
                .filter(candidate -> !candidate.isOpen())
                .map(Period::getYearMonth)
                .filter(month -> month.isAfter(command.period()))
                .sorted()
                .toList();
        if (!closedAfter.isEmpty()) {
            throw new PeriodNotReopenableException(command.period(), closedAfter);
        }

        Period reopened = period.reopen(Instant.now(), command.reopenedByUserId());
        return PeriodView.from(periodRepository.save(reopened));
    }
}
