package com.architek.oikos.user.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.user.application.command.CaptureOnboardingLeadCommand;
import com.architek.oikos.user.application.port.in.CaptureOnboardingLeadUseCase;
import com.architek.oikos.user.domain.model.OnboardingLead;
import com.architek.oikos.user.domain.repository.OnboardingLeadRepository;
import com.architek.oikos.user.domain.repository.UserRepository;

/**
 * Records the email typed on step 1 of the volunteer-syndic wizard so an
 * abandoned funnel is still reachable (an account can only exist from step 2
 * on, see OnboardingLead).
 *
 * <p>Deliberately silent about what it found: this endpoint is public and
 * unauthenticated, so telling the caller whether an address is already known
 * would turn it into an email enumeration oracle. An address that already has
 * an account needs no lead at all - the funnel it would have measured is
 * already closed - so it is skipped rather than recorded.
 */
@Component
public class CaptureOnboardingLeadService implements CaptureOnboardingLeadUseCase {

    private final OnboardingLeadRepository onboardingLeadRepository;
    private final UserRepository userRepository;

    public CaptureOnboardingLeadService(OnboardingLeadRepository onboardingLeadRepository,
                                         UserRepository userRepository) {
        this.onboardingLeadRepository = onboardingLeadRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void capture(CaptureOnboardingLeadCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            return;
        }
        OnboardingLead lead = onboardingLeadRepository.findByEmail(command.email())
                .map(existing -> existing.withFullName(command.fullName()))
                .orElseGet(() -> OnboardingLead.capture(command.email(), command.fullName()));
        onboardingLeadRepository.save(lead);
    }
}
