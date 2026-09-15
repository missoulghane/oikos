package com.architek.oikos.auth.infrastructure.security;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Les contrôles d'état du compte (activé, non verrouillé, non expiré), rebranchés
 * APRÈS la vérification du mot de passe.
 *
 * <p>Spring Security les exécute par défaut avant : une adresse au hasard et un
 * mot de passe au hasard suffisaient alors à faire répondre « ce compte n'est pas
 * activé » plutôt que « identifiants invalides ». Cet écart de réponse transforme
 * la page de connexion en annuaire - on y teste des adresses jusqu'à savoir
 * lesquelles ont un compte ici, et lesquelles sont des cibles de phishing
 * crédibles (« votre compte n'est pas encore activé, cliquez ici »).
 *
 * <p>En post-vérification, l'écart subsiste mais ne se produit que face à
 * quelqu'un qui a déjà donné le bon mot de passe : il n'apprend rien qu'il ne
 * sache déjà, et le vrai titulaire du compte apprend enfin pourquoi il n'entre
 * pas.
 *
 * <p>Le contrôle « mot de passe expiré » reste ici lui aussi : le déplacer ne
 * change rien à sa sémantique et laisse un seul endroit qui décide de l'état d'un
 * compte au moment de la connexion.
 */
public final class PostPasswordUserDetailsChecker implements UserDetailsChecker {

    @Override
    public void check(UserDetails user) {
        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }
        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }
    }
}
