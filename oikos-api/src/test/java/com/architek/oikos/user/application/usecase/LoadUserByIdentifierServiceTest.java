package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Verifies identifier resolution against the account's own email.
 */
@ExtendWith(MockitoExtension.class)
class LoadUserByIdentifierServiceTest {

    @Mock
    private UserRepository userRepository;

    private LoadUserByIdentifierService newService() {
        return new LoadUserByIdentifierService(userRepository);
    }

    @Test
    void resolves_by_the_accounts_own_email() {
        User user = User.register(UserId.newId(), EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
        when(userRepository.findByEmail("jane@doe.com")).thenReturn(Optional.of(user));

        var result = newService().loadByIdentifier("jane@doe.com");

        assertThat(result).contains(user);
    }

    @Test
    void returns_empty_when_the_identifier_matches_nothing() {
        when(userRepository.findByEmail("nobody")).thenReturn(Optional.empty());

        assertThat(newService().loadByIdentifier("nobody")).isEmpty();
    }
}
