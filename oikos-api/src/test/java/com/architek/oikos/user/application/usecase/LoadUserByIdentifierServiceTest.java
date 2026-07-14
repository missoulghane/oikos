package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Verifies the fixed login-identifier resolution priority: 1) account login,
 * 2) linked contact's email, 3) linked contact's phone.
 */
@ExtendWith(MockitoExtension.class)
class LoadUserByIdentifierServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactDirectoryPort contactDirectoryPort;

    private LoadUserByIdentifierService newService() {
        return new LoadUserByIdentifierService(userRepository, contactDirectoryPort);
    }

    @Test
    void resolves_by_login_first_without_consulting_contact_at_all() {
        User user = User.register(UserId.newId(), EntityId.newId(), HashedPassword.of("hashed"), "jdoe");
        when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(user));

        var result = newService().loadByIdentifier("jdoe");

        assertThat(result).contains(user);
        verify(contactDirectoryPort, never()).findIdByEmail(org.mockito.ArgumentMatchers.any());
        verify(contactDirectoryPort, never()).findIdByPhone(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void falls_back_to_the_linked_contacts_email_when_no_login_matches() {
        EntityId contactId = EntityId.newId();
        User user = User.register(UserId.newId(), contactId, HashedPassword.of("hashed"), null);
        when(userRepository.findByLogin("jane@doe.com")).thenReturn(Optional.empty());
        when(contactDirectoryPort.findIdByEmail(EmailVO.of("jane@doe.com"))).thenReturn(Optional.of(contactId));
        when(userRepository.findByContactId(contactId)).thenReturn(Optional.of(user));

        var result = newService().loadByIdentifier("jane@doe.com");

        assertThat(result).contains(user);
    }

    @Test
    void falls_back_to_the_linked_contacts_phone_when_identifier_is_not_an_email_and_no_login_matches() {
        EntityId contactId = EntityId.newId();
        User user = User.register(UserId.newId(), contactId, HashedPassword.of("hashed"), null);
        when(userRepository.findByLogin("0600000000")).thenReturn(Optional.empty());
        when(contactDirectoryPort.findIdByPhone("0600000000")).thenReturn(Optional.of(contactId));
        when(userRepository.findByContactId(contactId)).thenReturn(Optional.of(user));

        var result = newService().loadByIdentifier("0600000000");

        assertThat(result).contains(user);
        verify(contactDirectoryPort, never()).findIdByEmail(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returns_empty_when_the_identifier_matches_nothing() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());
        when(contactDirectoryPort.findIdByPhone("nobody")).thenReturn(Optional.empty());

        assertThat(newService().loadByIdentifier("nobody")).isEmpty();
    }
}
