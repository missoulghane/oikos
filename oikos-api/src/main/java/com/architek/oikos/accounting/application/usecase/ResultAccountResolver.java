package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Le compte où la clôture déverse le résultat de l'exercice, créé au premier
 * appel plutôt qu'à la création de la copropriété : tant qu'aucun exercice n'est
 * clos, il n'aurait rien à porter, et un plan de comptes se lit mieux sans
 * comptes vides.
 *
 * <p>Propre à la copropriété, jamais partagé - contrairement aux produits
 * d'appels de fonds : un résultat est le sien, et un compte commun mélangerait
 * ceux de toutes les copropriétés gérées par le même cabinet.
 *
 * <p>Passif de bilan : un résultat bénéficiaire est une dette de la copropriété
 * envers ses copropriétaires, un déficit se lit du côté opposé - le calcul par
 * direction s'en charge sans supposer de signe.
 */
@Component
public class ResultAccountResolver {

    private static final String RESULT_NUMBER_PREFIX = "120000";

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerAccountNumberSequenceRepository sequenceRepository;

    public ResultAccountResolver(LedgerAccountRepository ledgerAccountRepository,
                                  LedgerAccountNumberSequenceRepository sequenceRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.sequenceRepository = sequenceRepository;
    }

    public LedgerAccountId resolve(EntityId propertyId) {
        return ledgerAccountRepository.findByPropertyIdAndRole(propertyId, AccountRole.RESULT)
                .map(LedgerAccount::getId)
                .orElseGet(() -> provision(propertyId));
    }

    private LedgerAccountId provision(EntityId propertyId) {
        int increment = sequenceRepository.allocateNextIncrement(propertyId, RESULT_NUMBER_PREFIX);
        AccountNumber accountNumber = AccountNumber.forSequence(RESULT_NUMBER_PREFIX, increment);
        LedgerAccount account = LedgerAccount.create(LedgerAccountId.newId(), propertyId, null, accountNumber,
                "Resultat de l'exercice", 1, AccountNature.BALANCE_LIABILITY, false, AccountRole.RESULT);
        return ledgerAccountRepository.save(account).getId();
    }
}
