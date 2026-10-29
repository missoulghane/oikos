package com.architek.oikos.installment.domain.valueobject;

/**
 * Fund-call lifecycle (spec &sect;4.1/P1): DRAFT (ventilation editable) -&gt;
 * ISSUED ("emission", ventilation frozen) -&gt; POSTED ("comptabilisation",
 * journal entry generated) ; CANCELLED from DRAFT/ISSUED directly, or via
 * contre-passation of the journal entry once POSTED.
 */
public enum InstallmentCallStatus {
    DRAFT,
    ISSUED,
    POSTED,
    CANCELLED
}
