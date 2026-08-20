package com.architek.oikos.accounting.web.controller;

import java.time.YearMonth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.accounting.application.command.ClosePeriodCommand;
import com.architek.oikos.accounting.application.command.ReopenPeriodCommand;
import com.architek.oikos.accounting.application.port.in.ClosePeriodUseCase;
import com.architek.oikos.accounting.application.port.in.ReopenPeriodUseCase;
import com.architek.oikos.accounting.web.response.PeriodResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** P8 (spec &sect;6): monthly period closing, and its réouverture. */
@RestController
public class PeriodController {

    private final ClosePeriodUseCase closePeriodUseCase;
    private final ReopenPeriodUseCase reopenPeriodUseCase;

    public PeriodController(ClosePeriodUseCase closePeriodUseCase, ReopenPeriodUseCase reopenPeriodUseCase) {
        this.closePeriodUseCase = closePeriodUseCase;
        this.reopenPeriodUseCase = reopenPeriodUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteAccounting(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/periods/{period}/close")
    public PeriodResponse close(@PathVariable String propertyId, @PathVariable String period,
                                 Authentication authentication) {
        return PeriodResponse.from(closePeriodUseCase.close(new ClosePeriodCommand(EntityId.of(propertyId),
                YearMonth.parse(period), EntityId.of(authentication.getName()))));
    }

    /**
     * Garde plus étroite que la clôture : {@code managesProperty} plutôt que
     * {@code canWriteAccounting}. Clôturer est un geste de tenue de comptes, que
     * fait le trésorier ; rouvrir défait ce que la clôture avait arrêté, et cela
     * relève de qui administre la copropriété.
     */
    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/accounting/periods/{period}/reopen")
    public PeriodResponse reopen(@PathVariable String propertyId, @PathVariable String period,
                                  Authentication authentication) {
        return PeriodResponse.from(reopenPeriodUseCase.reopen(new ReopenPeriodCommand(EntityId.of(propertyId),
                YearMonth.parse(period), EntityId.of(authentication.getName()))));
    }
}
