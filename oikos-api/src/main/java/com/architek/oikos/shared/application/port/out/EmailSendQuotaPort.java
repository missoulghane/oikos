package com.architek.oikos.shared.application.port.out;

/**
 * Plafonne le nombre d'emails qu'un tiers peut faire envoyer à une adresse
 * donnée. C'est le pendant, côté application, du plafond par IP tenu par le
 * filtre : ici la victime n'est pas le serveur mais le titulaire de la boîte,
 * qui reçoit les messages sans avoir rien demandé, et changer d'adresse IP ne
 * change rien à ce qu'il subit.
 *
 * <p>Un port et non un appel direct au limiteur : les services applicatifs ne
 * connaissent pas l'infrastructure, ici pas plus qu'ailleurs.
 */
public interface EmailSendQuotaPort {

    /**
     * Compte un envoi vers {@code emailAddress} et laisse passer, ou lève
     * {@link com.architek.oikos.shared.exception.TooManyRequestsException} si le
     * plafond est atteint.
     *
     * <p>À appeler avant de chercher si un compte existe pour cette adresse :
     * appliqué après, le refus ne surviendrait que pour les adresses connues et
     * répondrait précisément à la question que ces endpoints refusent de traiter.
     */
    void requireQuota(String emailAddress);
}
