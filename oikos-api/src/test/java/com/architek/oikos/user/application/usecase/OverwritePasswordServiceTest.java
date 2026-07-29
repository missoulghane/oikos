package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class OverwritePasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void overwrites_the_password_without_checking_the_previous_one() {
        UserId userId = UserId.newId();
        User user = User.register(userId, EmailVO.of("jane@doe.com"), "Jane Doe", HashedPassword.of("old-hash"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        new OverwritePasswordService(userRepository).overwritePassword(userId, HashedPassword.of("new-hash"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword().value()).isEqualTo("new-hash");
    }

    @Test
    void unknown_user_is_rejected() {
        UserId userId = UserId.newId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new OverwritePasswordService(userRepository).overwritePassword(userId, HashedPassword.of("new-hash")))
                .isInstanceOf(UserNotFoundException.class);
    }
}
