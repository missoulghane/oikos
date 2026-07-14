package com.architek.oikos.auth.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.architek.oikos.auth.application.dto.AuthenticatedPrincipal;
import com.architek.oikos.auth.application.port.out.AuthenticationPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.shared.exception.UnauthorizedException;

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
            throw new UnauthorizedException("Account is not verified or has been disabled");
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }
}
