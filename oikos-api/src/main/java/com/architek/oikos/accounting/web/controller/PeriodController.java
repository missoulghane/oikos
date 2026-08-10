package com.architek.oikos.accounting.web.controller;

import java.time.YearMonth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.command.ClosePeriodCommand;
import com.architek.oikos.accounting.application.port.in.ClosePeriodUseCase;
import com.architek.oikos.accounting.web.response.PeriodResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** P8 (spec &sect;6): monthly period closing. */
@RestController
public class PeriodController {

    private final ClosePeriodUseCase closePeriodUseCase;

    public PeriodController(ClosePeriodUseCase closePeriodUseCase) {
        this.closePeriodUseCase = closePeriodUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/periods/{period}/close")
    public PeriodResponse close(@PathVariable String propertyId, @PathVariable String period,
                                 Authentication authentication) {
        return PeriodResponse.from(closePeriodUseCase.close(new ClosePeriodCommand(EntityId.of(propertyId),
                YearMonth.parse(period), EntityId.of(authentication.getName()))));
    }
}
