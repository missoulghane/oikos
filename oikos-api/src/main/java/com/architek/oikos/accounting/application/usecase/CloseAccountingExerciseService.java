package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CloseAccountingExerciseCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.dto.ExerciseClosingView;
import com.architek.oikos.accounting.application.port.in.CloseAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.ExerciseNotClosableException;
import com.architek.oikos.accounting.domain.model.AccountNetAmount;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.ExerciseClosingCalculator;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * P9 : la clôture annuelle. Elle arrête l'exercice en trois gestes - vérifier
 * que tous ses mois sont clos, solder les classes 6 et 7 dans le compte de
 * résultat, puis sceller l'exercice.
 *
 * <p><b>Les soldes viennent des écritures, pas des comptes.</b> Une partie du
 * plan de comptes est partagée entre toutes les copropriétés (les produits
 * d'appels de fonds, les avances, les dettes de personnel) : le solde porté par
 * le compte totalise donc toutes les copropriétés à la fois. Solder une
 * copropriété à partir de ce total effacerait la part des autres. Le calcul part
 * des lignes comptabilisées de <em>cette</em> copropriété sur <em>cet</em>
 * exercice.
 *
 * <p><b>Pas d'écriture d'à-nouveaux.</b> Le calculateur sait les produire, mais
 * les soldes du grand livre sont cumulés par copropriété et ne sont pas remis à
 * zéro d'un exercice à l'autre : les comptes de bilan gardent naturellement leur
 * solde, et générer des à-nouveaux les doublerait. Le jour où les soldes
 * deviendront propres à chaque exercice, cette écriture aura un sens - pas
 * avant.
 *
 * <p><b>L'écriture de clôture ne passe pas par la garde de période.</b> Elle est
 * datée du dernier jour de l'exercice, donc dans un mois nécessairement clos
 * puisque c'est la condition même de la clôture. La règle « on ne saisit pas
 * dans une période close » protège la saisie humaine ; l'écriture qui scelle
 * l'exercice est produite par le moteur, et elle est la seule.
 */
@Component
public class CloseAccountingExerciseService implements CloseAccountingExerciseUseCase {

    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final AccountingExerciseRepository accountingExerciseRepository;
    private final PeriodRepository periodRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final ResultAccountResolver resultAccountResolver;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public CloseAccountingExerciseService(EnforceExerciseOpenService enforceExerciseOpenService,
                                           AccountingExerciseRepository accountingExerciseRepository,
                                           PeriodRepository periodRepository,
                                           JournalEntryRepository journalEntryRepository,
                                           LedgerAccountRepository ledgerAccountRepository,
                                           ResultAccountResolver resultAccountResolver,
                                           PostJournalEntryUseCase postJournalEntryUseCase) {
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.accountingExerciseRepository = accountingExerciseRepository;
        this.periodRepository = periodRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.resultAccountResolver = resultAccountResolver;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public ExerciseClosingView close(CloseAccountingExerciseCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());

        List<YearMonth> openPeriods = periodRepository.findAllByExerciseId(exercise.getId()).stream()
                .filter(Period::isOpen)
                .map(Period::getYearMonth)
                .sorted()
                .toList();
        if (!openPeriods.isEmpty()) {
            throw new ExerciseNotClosableException(openPeriods);
        }

        List<ExerciseClosingCalculator.AccountBalance> balances = incomeStatementBalances(command.propertyId(),
                exercise);
        LedgerAccountId resultAccountId = resultAccountResolver.resolve(command.propertyId());
        // balances.size() + 1 : une ligne par compte à solder, plus celle du résultat.
        List<JournalEntryLineId> lineIds = IntStream.rangeClosed(0, balances.size())
                .mapToObj(index -> JournalEntryLineId.newId())
                .toList();
        ExerciseClosingCalculator.ClosingResult closing = ExerciseClosingCalculator.closeIncomeStatement(balances,
                resultAccountId, lineIds);

        JournalEntryId closingEntryId = postClosingEntry(command, exercise, closing.lines());

        AccountingExercise closed = accountingExerciseRepository.save(
                exercise.close(Instant.now(), command.closedByUserId()));
        return new ExerciseClosingView(AccountingExerciseView.from(closed), closing.netResult(), closingEntryId);
    }

    /**
     * Le solde de chaque compte de charge ou de produit mouvementé par la
     * copropriété, exprimé dans le sens normal du compte - la convention
     * qu'attend le calculateur : positif quand le compte est du côté où on
     * l'attend, négatif quand il est à l'envers.
     */
    private List<ExerciseClosingCalculator.AccountBalance> incomeStatementBalances(EntityId propertyId,
                                                                                     AccountingExercise exercise) {
        Map<LedgerAccountId, BigDecimal> netByAccount = journalEntryRepository
                .sumNetAmountByAccountForExercise(propertyId, exercise.getId()).stream()
                .collect(Collectors.toMap(AccountNetAmount::accountId, AccountNetAmount::creditMinusDebit));

        Map<LedgerAccountId, LedgerAccount> accountsById = ledgerAccountRepository
                .findAllVisibleToProperty(propertyId).stream()
                .collect(Collectors.toMap(LedgerAccount::getId, Function.identity()));

        List<ExerciseClosingCalculator.AccountBalance> balances = new ArrayList<>();
        for (Map.Entry<LedgerAccountId, BigDecimal> entry : netByAccount.entrySet()) {
            LedgerAccount account = accountsById.get(entry.getKey());
            if (account == null || !isIncomeStatement(account)) {
                continue;
            }
            BigDecimal creditMinusDebit = entry.getValue();
            BigDecimal inNormalSide = account.getNature().normalSide() == EntryDirection.CREDIT
                    ? creditMinusDebit
                    : creditMinusDebit.negate();
            if (inNormalSide.signum() != 0) {
                balances.add(new ExerciseClosingCalculator.AccountBalance(account.getId(),
                        account.getNature().normalSide(), inNormalSide));
            }
        }
        // Ordre stable : l'écriture de clôture doit se relire deux fois pareil.
        balances.sort(Comparator.comparing(balance -> balance.accountId().asUuid()));
        return balances;
    }

    private static boolean isIncomeStatement(LedgerAccount account) {
        return account.getNature() == AccountNature.EXPENSE || account.getNature() == AccountNature.INCOME;
    }

    /**
     * Rien à solder : un exercice sans produit ni charge se clôture quand même,
     * sans écriture. C'est le cas d'une copropriété qui vient d'ouvrir.
     */
    private JournalEntryId postClosingEntry(CloseAccountingExerciseCommand command, AccountingExercise exercise,
                                             List<JournalEntryLine> lines) {
        if (lines.isEmpty()) {
            return null;
        }
        Period lastPeriod = periodRepository
                .findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.from(exercise.getEndDate()))
                .orElseThrow(() -> new IllegalStateException(
                        "L'exercice " + exercise.getId() + " n'a pas de période pour son dernier mois"));
        JournalEntry draft = JournalEntry.draft(JournalEntryId.newId(), command.propertyId(), exercise.getId(),
                lastPeriod.getId(), JournalCode.OD, null, exercise.getEndDate(),
                "CLOTURE-" + exercise.getLabel(), command.closedByUserId(), lines);
        JournalEntryId draftId = journalEntryRepository.save(draft).getId();
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
