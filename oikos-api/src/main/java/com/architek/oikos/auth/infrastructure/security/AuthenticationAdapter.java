package com.architek.oikos.auth.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.architek.oikos.auth.application.dto.AuthenticatedPrincipal;
import com.architek.oikos.auth.application.port.out.AuthenticationPort;
import com.architek.oikos.auth.domain.exception.AccountNotActivatedException;
import com.architek.oikos.auth.domain.exception.InvalidCredentialsException;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

@Component
public class AuthenticationAdapter implements AuthenticationPort {

    private final AuthenticationManager authenticationManager;

    public AuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticatedPrincipal authenticate(String identifier, RawPassword rawPassword) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, rawPassword.value()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Set<String> authorities = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            return new AuthenticatedPrincipal(EntityId.of(principal.getUserId()), authorities);
        } catch (DisabledException e) {
            // Ne remonte qu'après un mot de passe correct - voir
            // PostPasswordUserDetailsChecker, qui est ce qui rend ce message
            // racontable sans ouvrir un annuaire des comptes.
            throw new AccountNotActivatedException();
        } catch (AuthenticationException e) {
            // Tout le reste (mot de passe faux, identifiant inconnu, compte
            // verrouillé) se répond d'une seule et même façon.
            throw new InvalidCredentialsException();
        }
    }
}
