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

    private ErrorCodes() {
    }
}
