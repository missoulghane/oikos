package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.ProvisionInvitedAccountCommand;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProvisionInvitedAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private ProvisionInvitedAccountService newService() {
        return new ProvisionInvitedAccountService(userRepository, passwordEncoderPort);
    }

    @Test
    void provisioning_a_new_email_persists_an_already_verified_user() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().provision(new ProvisionInvitedAccountCommand(
                EmailVO.of("jane.doe@example.com"), "Jane Doe", RawPassword.of("password123")));

        org.mockito.ArgumentCaptor<User> captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isVerified()).isTrue();
        assertThat(captor.getValue().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void provisioning_an_already_used_email_is_rejected_without_creating_an_account() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> newService().provision(new ProvisionInvitedAccountCommand(
                EmailVO.of("taken@example.com"), "Someone", RawPassword.of("password123"))))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
    }
}
