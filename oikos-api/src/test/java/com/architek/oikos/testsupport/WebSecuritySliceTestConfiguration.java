package com.architek.oikos.testsupport;

import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.architek.oikos.auth.infrastructure.security.DomainUserDetailsService;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.auth.infrastructure.security.PropertyAccessEvaluator;
import com.architek.oikos.auth.infrastructure.security.RestAccessDeniedHandler;
import com.architek.oikos.auth.infrastructure.security.RestAuthenticationEntryPoint;
import com.architek.oikos.auth.infrastructure.security.SecurityConfiguration;
import com.architek.oikos.document.application.port.in.GetDocumentUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.invitation.application.port.in.GetInvitationUseCase;
import com.architek.oikos.invitation.application.port.in.GetMembershipRequestUseCase;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.messaging.application.port.in.GetConversationUseCase;
import com.architek.oikos.messaging.application.port.in.GetMessageDraftUseCase;
import com.architek.oikos.notification.application.port.in.GetNotificationUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.shared.infrastructure.configuration.PasswordEncoderConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;

/**
 * @WebMvcTest(controllers = ...) only whitelists a narrow set of bean types
 * (@Controller, @ControllerAdvice, WebMvcConfigurer, Filter, ...) - our custom
 * SecurityConfiguration is a plain @Configuration and is excluded from that slice
 * by default, so the real Spring Security filter chain never runs and requests
 * reach controllers with a null Authentication. Import this into any @WebMvcTest
 * that needs genuine authentication/authorization enforcement (401/403, current
 * user id from the JWT subject, @PreAuthorize).
 *
 * <p>Controllers guarded by @PreAuthorize("@propertyAccess...") need the
 * "propertyAccess" bean (PropertyAccessEvaluator) present in the slice's
 * ApplicationContext, or SpEL evaluation fails outright (400, not 403/401).
 * GetUserAccessUseCase is mocked here directly (default answer: an empty,
 * non-admin, no-grants view, so an unstubbed regular user correctly fails
 * every managesX/ownsX check instead of NPE'ing) since every evaluator method
 * calls it; the other collaborators are looked up via ObjectProvider so a
 * test class that already declares its own @MockitoBean for one of them
 * (e.g. GetBuildingUseCase, because its controller uses it directly) reuses
 * that same mock instance instead of colliding with a second one.
 */
@TestConfiguration
@Import({
        PasswordEncoderConfiguration.class,
        SecurityConfiguration.class,
        JwtService.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
public class WebSecuritySliceTestConfiguration {

    @Bean
    DomainUserDetailsService domainUserDetailsService() {
        return Mockito.mock(DomainUserDetailsService.class);
    }

    @Bean
    GetUserAccessUseCase getUserAccessUseCase() {
        return Mockito.mock(GetUserAccessUseCase.class,
                invocation -> new UserAccessView(Set.of(), Map.of(), Map.of(), Set.of(), Set.of()));
    }

    @Bean
    PropertyAccessEvaluator propertyAccess(GetUserAccessUseCase getUserAccessUseCase,
                                            ObjectProvider<GetUnitUseCase> getUnitUseCase,
                                            ObjectProvider<GetBuildingUseCase> getBuildingUseCase,
                                            ObjectProvider<GetInstallmentUseCase> getInstallmentUseCase,
                                            ObjectProvider<GetInstallmentCallUseCase> getInstallmentCallUseCase,
                                            ObjectProvider<GetPaymentUseCase> getPaymentUseCase,
                                            ObjectProvider<ListUnitOwnershipsByUnitUseCase> listUnitOwnershipsByUnitUseCase,
                                            ObjectProvider<GetPartyUseCase> getPartyUseCase,
                                            ObjectProvider<GetInvitationUseCase> getInvitationUseCase,
                                            ObjectProvider<GetMembershipRequestUseCase> getMembershipRequestUseCase,
                                            ObjectProvider<GetConversationUseCase> getConversationUseCase,
                                            ObjectProvider<GetMessageDraftUseCase> getMessageDraftUseCase,
                                            ObjectProvider<GetDocumentUseCase> getDocumentUseCase,
                                            ObjectProvider<GetNotificationUseCase> getNotificationUseCase,
                                            ObjectProvider<GetGeneralMeetingUseCase> getGeneralMeetingUseCase,
                                            ObjectProvider<GetAgendaItemUseCase> getAgendaItemUseCase,
                                            ObjectProvider<GetConvocationUseCase> getConvocationUseCase) {
        return new PropertyAccessEvaluator(getUserAccessUseCase,
                getUnitUseCase.getIfAvailable(() -> Mockito.mock(GetUnitUseCase.class)),
                getBuildingUseCase.getIfAvailable(() -> Mockito.mock(GetBuildingUseCase.class)),
                getInstallmentUseCase.getIfAvailable(() -> Mockito.mock(GetInstallmentUseCase.class)),
                getInstallmentCallUseCase.getIfAvailable(() -> Mockito.mock(GetInstallmentCallUseCase.class)),
                getPaymentUseCase.getIfAvailable(() -> Mockito.mock(GetPaymentUseCase.class)),
                listUnitOwnershipsByUnitUseCase.getIfAvailable(() -> Mockito.mock(ListUnitOwnershipsByUnitUseCase.class)),
                getPartyUseCase.getIfAvailable(() -> Mockito.mock(GetPartyUseCase.class)),
                getInvitationUseCase.getIfAvailable(() -> Mockito.mock(GetInvitationUseCase.class)),
                getMembershipRequestUseCase.getIfAvailable(() -> Mockito.mock(GetMembershipRequestUseCase.class)),
                getConversationUseCase.getIfAvailable(() -> Mockito.mock(GetConversationUseCase.class)),
                getMessageDraftUseCase.getIfAvailable(() -> Mockito.mock(GetMessageDraftUseCase.class)),
                getDocumentUseCase.getIfAvailable(() -> Mockito.mock(GetDocumentUseCase.class)),
                getNotificationUseCase.getIfAvailable(() -> Mockito.mock(GetNotificationUseCase.class)),
                getGeneralMeetingUseCase.getIfAvailable(() -> Mockito.mock(GetGeneralMeetingUseCase.class)),
                getAgendaItemUseCase.getIfAvailable(() -> Mockito.mock(GetAgendaItemUseCase.class)),
                getConvocationUseCase.getIfAvailable(() -> Mockito.mock(GetConvocationUseCase.class)));
    }
}
