package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.RegisterPropertyManagerCommand;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

@ExtendWith(MockitoExtension.class)
class RegisterPropertyManagerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactDirectoryPort contactDirectoryPort;

    @Mock
    private PropertyProvisioningPort propertyProvisioningPort;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private RegisterPropertyManagerService newService() {
        return new RegisterPropertyManagerService(userRepository, contactDirectoryPort, propertyProvisioningPort,
                verificationTokenRepository, passwordEncoderPort, emailSenderPort, new VerificationTokenGenerator(),
                new VerificationEmailComposer("http://localhost/verify"), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    @Test
    void registering_a_property_manager_creates_the_user_with_the_property_manager_role_and_provisions_the_property() {
        EntityId contactId = EntityId.newId();
        when(contactDirectoryPort.createContact(any())).thenReturn(contactId);
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterPropertyManagerCommand command = new RegisterPropertyManagerCommand(
                "Doe", "Jane", EmailVO.of("manager@oikos.com"), null, null, RawPassword.of("password123"),
                "Residence A", "1 rue de Paris");

        newService().register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).containsExactly(Role.ROLE_PROPERTY_MANAGER);

        ArgumentCaptor<PropertyProvisioningDetails> detailsCaptor = ArgumentCaptor.forClass(PropertyProvisioningDetails.class);
        verify(propertyProvisioningPort).provisionProperty(detailsCaptor.capture());
        assertThat(detailsCaptor.getValue().name()).isEqualTo("Residence A");
        assertThat(detailsCaptor.getValue().managerContactId()).isEqualTo(contactId);

        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }
}
