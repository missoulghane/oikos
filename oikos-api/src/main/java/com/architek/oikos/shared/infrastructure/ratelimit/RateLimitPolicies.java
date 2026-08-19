package com.architek.oikos.shared.infrastructure.ratelimit;

import java.time.Duration;

/**
 * Les plafonds anti-abus, tels que les lit la configuration
 * ({@code oikos.security.rate-limit}). Une seule fenêtre pour tous : rien ici ne
 * justifie des durées différentes, et deux réglages à comparer suffisent à rendre
 * un plafond incompréhensible.
 *
 * <p>Les endpoints publics sont groupés par ce qu'ils coûtent quand on en abuse :
 * une tentative de mot de passe (login), un email parti (envoi), un jeton deviné
 * (consommation), une ligne créée en base (inscription). Le plafond « par
 * adresse » est le seul qui protège quelqu'un d'autre que le serveur - la victime
 * d'une inondation reçoit les messages sans avoir rien demandé -, d'où un chiffre
 * bien plus bas.
 */
public record RateLimitPolicies(
        boolean enabled,
        Duration window,
        int loginPerIp,
        int emailSendPerIp,
        int tokenAttemptPerIp,
        int registrationPerIp,
        int emailSendPerAddress) {
}
