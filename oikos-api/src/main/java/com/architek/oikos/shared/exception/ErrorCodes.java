package com.architek.oikos.shared.exception;

/**
 * Le vocabulaire des codes d'erreur que l'API expose à ses clients, en un seul
 * endroit - son pendant côté web/mobile est le catalogue de messages, qui traduit
 * chaque code en une phrase française.
 *
 * <p>Volontairement court : un code ne se justifie que si un client doit réagir
 * autrement (autre texte, autre bouton) à deux erreurs que le statut HTTP
 * confond. Partout ailleurs, le statut suffit.
 */
public final class ErrorCodes {

    /** Identifiant inconnu ou mot de passe faux - indistinguables, par construction. */
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    /** Mot de passe correct, mais compte jamais activé ou désactivé depuis. */
    public static final String ACCOUNT_NOT_ACTIVATED = "ACCOUNT_NOT_ACTIVATED";

    /**
     * Lien reçu par email (vérification, activation, réinitialisation, invitation)
     * invalide, expiré ou déjà consommé : le client propose d'en redemander un.
     */
    public static final String INVALID_LINK = "INVALID_LINK";


    /** Ce contact est déjà propriétaire de ce lot. */
    public static final String PARTY_ALREADY_OWNS_UNIT = "PARTY_ALREADY_OWNS_UNIT";

    /** Ce type de lot est encore porté par des lots et ne peut pas être retiré. */
    public static final String UNIT_TYPE_IN_USE = "UNIT_TYPE_IN_USE";

    /** Aucun exercice comptable de la copropriété ne couvre la date de l'opération. */
    public static final String NO_EXERCISE_FOR_DATE = "NO_EXERCISE_FOR_DATE";

    /** L'exercice qui couvre la date de l'opération est clôturé. */
    public static final String EXERCISE_CLOSED_FOR_DATE = "EXERCISE_CLOSED_FOR_DATE";

    /** Le nouvel exercice ne commence pas le lendemain de la fin du précédent. */
    public static final String EXERCISE_NOT_CONTIGUOUS = "EXERCISE_NOT_CONTIGUOUS";

    /** Deux exercices sont déjà ouverts : il faut clôturer le plus ancien avant d'en ouvrir un autre. */
    public static final String TOO_MANY_OPEN_EXERCISES = "TOO_MANY_OPEN_EXERCISES";

    /** Un exercice plus ancien est encore ouvert : les exercices se clôturent dans l'ordre. */
    public static final String EXERCISE_NOT_OLDEST_OPEN = "EXERCISE_NOT_OLDEST_OPEN";

    /** L'appel de fonds a déjà reçu des règlements : il ne peut plus être annulé. */
    public static final String INSTALLMENT_CALL_SETTLED = "INSTALLMENT_CALL_SETTLED";

    /** Aucun lot n'a de montant à appeler (ni tarif pour son type, ni tantièmes). */
    public static final String NO_CHARGEABLE_UNIT = "NO_CHARGEABLE_UNIT";

    /** La date d'échéance précède le début de la période appelée. */
    public static final String DUE_DATE_BEFORE_PERIOD = "DUE_DATE_BEFORE_PERIOD";

    /** Le lot n'a pas d'avance disponible, ou pas d'échéance due sur laquelle l'imputer. */
    public static final String NOTHING_TO_REGULARIZE = "NOTHING_TO_REGULARIZE";

    private ErrorCodes() {
    }
}
