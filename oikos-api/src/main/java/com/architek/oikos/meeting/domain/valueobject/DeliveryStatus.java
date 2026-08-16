package com.architek.oikos.meeting.domain.valueobject;

/**
 * Where a convocation stands on its way out.
 *
 * <p>Read at two levels, and they do not carry the same values. On a
 * {@link com.architek.oikos.meeting.domain.model.ConvocationDelivery} it is the
 * outcome of one attempt, so only SENT or FAILED. On the convocation itself it
 * is derived from those attempts, and TO_SEND is what their absence means -
 * never a stored value, and never a row.
 */
public enum DeliveryStatus {

    /** No attempt yet. Only ever derived, never the status of a delivery row. */
    TO_SEND,
    SENT,
    FAILED
}
