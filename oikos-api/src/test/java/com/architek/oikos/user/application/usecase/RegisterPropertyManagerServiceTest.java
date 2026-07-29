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
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.model.PropertyRole;
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
    private PartyProvisioningPort partyProvisioningPort;

    @Mock
    private PropertyProvisioningPort propertyProvisioningPort;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    private RegisterPropertyManagerService newService() {
        return new RegisterPropertyManagerService(userRepository, partyProvisioningPort, propertyProvisioningPort,
                verificationTokenRepository, passwordEncoderPort, emailSenderPort, new VerificationTokenGenerator(),
                new VerificationEmailComposer("http://localhost/verify"), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 24L);
    }

    @Test
    void registering_a_property_manager_provisions_the_property_first_then_a_scoped_party_and_grants_the_role_through_it() {
        EntityId propertyId = EntityId.newId();
        EntityId partyId = EntityId.newId();
        when(propertyProvisioningPort.provisionProperty(any())).thenReturn(propertyId);
        when(partyProvisioningPort.createParty(any())).thenReturn(partyId);
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterPropertyManagerCommand command = new RegisterPropertyManagerCommand(
                "Jane Doe", EmailVO.of("manager@oikos.com"), null, RawPassword.of("password123"),
                "Residence A", "1 rue de Paris");

        newService().register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRoles()).containsExactly(Role.ROLE_USER);
        assertThat(userCaptor.getValue().getLinkedPartyIds()).containsExactly(partyId);
        assertThat(userCaptor.getValue().getPropertyRoleGrants())
                .extracting("role").containsExactly(PropertyRole.ROLE_PROPERTY_MANAGER);

        ArgumentCaptor<PropertyProvisioningDetails> propertyDetailsCaptor =
                ArgumentCaptor.forClass(PropertyProvisioningDetails.class);
        verify(propertyProvisioningPort).provisionProperty(propertyDetailsCaptor.capture());
        assertThat(propertyDetailsCaptor.getValue().name()).isEqualTo("Residence A");

        ArgumentCaptor<PartyProvisioningDetails> partyDetailsCaptor = ArgumentCaptor.forClass(PartyProvisioningDetails.class);
        verify(partyProvisioningPort).createParty(partyDetailsCaptor.capture());
        assertThat(partyDetailsCaptor.getValue().propertyId()).isEqualTo(propertyId);

        verify(propertyProvisioningPort).assignPropertyManager(propertyId, partyId);
        verify(verificationTokenRepository).save(any());
        verify(emailSenderPort).send(any(), any(), any());
    }
}
