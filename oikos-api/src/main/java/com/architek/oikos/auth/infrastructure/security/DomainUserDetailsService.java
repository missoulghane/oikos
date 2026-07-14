package com.architek.oikos.auth.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.architek.oikos.user.application.port.in.LoadUserByIdentifierUseCase;
import com.architek.oikos.user.domain.model.User;

/**
 * Only auth.infrastructure component allowed to depend on the user feature: it goes
 * through user's public port-in (LoadUserByIdentifierUseCase), never through its
 * repository directly. auth.application stays fully decoupled from user.
 */
@Component
public class DomainUserDetailsService implements UserDetailsService {

    private final LoadUserByIdentifierUseCase loadUserByIdentifierUseCase;

    public DomainUserDetailsService(LoadUserByIdentifierUseCase loadUserByIdentifierUseCase) {
        this.loadUserByIdentifierUseCase = loadUserByIdentifierUseCase;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = loadUserByIdentifierUseCase.loadByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for identifier: " + identifier));
        return UserPrincipal.of(user);
    }
}
