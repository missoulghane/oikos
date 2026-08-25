package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;

/**
 * What one sweep did, for the log line it ends on: how many copropriétés were
 * visited, how many of them could not be swept (their failure is logged
 * individually), and what was actually imputed.
 */
public record RegularizeDueInstallmentsResult(int propertiesVisited, int propertiesFailed, int unitsRegularized,
                                               BigDecimal totalAmountApplied) {
}
