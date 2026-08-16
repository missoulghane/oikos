package com.architek.oikos.meeting.domain.valueobject;

/**
 * The synthetic status of the SFD (statut_global), derived from the three
 * groups of fields a Convocation carries - never stored, exactly like
 * InstallmentStatus. Deriving it means the tracking table can never disagree
 * with the underlying facts.
 */
public enum ConvocationStatus {
    TO_SEND,
    SENT,
    CONFIRMED,
    CHECKED_IN
}
