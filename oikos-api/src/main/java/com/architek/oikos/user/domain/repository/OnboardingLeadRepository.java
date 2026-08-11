package com.architek.oikos.user.domain.repository;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.user.domain.model.OnboardingLead;

public interface OnboardingLeadRepository {

    OnboardingLead save(OnboardingLead lead);

    Optional<OnboardingLead> findByEmail(EmailVO email);
}
