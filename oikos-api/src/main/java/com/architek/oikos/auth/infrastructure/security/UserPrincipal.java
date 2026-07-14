package com.architek.oikos.auth.infrastructure.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.architek.oikos.user.domain.model.User;

/**
 * Bridges the user domain aggregate into Spring Security without the domain User
 * ever implementing UserDetails itself. getUsername() intentionally returns the
 * user's id (not the email) so that Authentication#getName() can be used everywhere
 * else in the application as the JWT subject / current-user id, without any other
 * feature needing a compile-time dependency on this class.
 */
public final class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String passwordHash;
    private final Set<GrantedAuthority> authorities;
    private final boolean enabled;

    private UserPrincipal(UUID userId, String passwordHash, Set<GrantedAuthority> authorities, boolean enabled) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    public static UserPrincipal of(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
        return new UserPrincipal(user.getId().asUuid(), user.getPassword().value(), authorities,
                user.isVerified() && user.isEnabled());
    }

    public static UserPrincipal fromClaims(UUID userId, Set<String> authorities) {
        Set<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority))
                .collect(Collectors.toSet());
        return new UserPrincipal(userId, "", grantedAuthorities, true);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
