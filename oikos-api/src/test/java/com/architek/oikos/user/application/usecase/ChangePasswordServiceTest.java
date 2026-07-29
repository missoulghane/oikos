package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.ChangePasswordCommand;
import com.architek.oikos.user.domain.exception.InvalidCurrentPasswordException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Test
    void wrong_current_password_is_rejected_and_nothing_is_saved() {
        UserId userId = UserId.newId();
        User user = User.register(userId, EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("hashed"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(any(), any())).thenReturn(false);

        ChangePasswordService service = new ChangePasswordService(userRepository, passwordEncoderPort);
        ChangePasswordCommand command = new ChangePasswordCommand(userId, RawPassword.of("wrongwrongwrong"), RawPassword.of("newpassword1"));

        assertThatThrownBy(() -> service.changePassword(command)).isInstanceOf(InvalidCurrentPasswordException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void unknown_user_is_rejected() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ChangePasswordService service = new ChangePasswordService(userRepository, passwordEncoderPort);
        ChangePasswordCommand command = new ChangePasswordCommand(userId, RawPassword.of("currentpassword"), RawPassword.of("newpassword1"));

        assertThatThrownBy(() -> service.changePassword(command)).isInstanceOf(UserNotFoundException.class);
    }
}
