package com.architek.oikos.installment.domain.valueobject;

/**
 * Fund-call lifecycle (spec &sect;4.1/P1): DRAFT (ventilation editable) -&gt;
 * ISSUED ("emission", ventilation frozen) -&gt; POSTED ("comptabilisation",
 * journal entry generated) ; CANCELLED from DRAFT/ISSUED directly, or only
 * via contre-passation once POSTED (a later phase).
 */
public enum InstallmentCallStatus {
    DRAFT,
    ISSUED,
    POSTED,
    CANCELLED
}
