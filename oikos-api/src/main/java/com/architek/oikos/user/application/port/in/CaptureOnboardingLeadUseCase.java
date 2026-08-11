package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.CaptureOnboardingLeadCommand;

public interface CaptureOnboardingLeadUseCase {

    void capture(CaptureOnboardingLeadCommand command);
}
