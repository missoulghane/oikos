package com.architek.oikos.meeting.domain.valueobject;

/**
 * How the presence confirmation was obtained.
 *
 * <p>An enum and not a catalog, unlike ConvocationChannel: each value is a
 * distinct code path on the server, not a piece of data. Adding one is not a
 * new row, it is a new way of answering, and it has to be written.
 *
 * <p>Always derived from the caller, never accepted from the request body.
 * A client able to state its own source could write "the owner confirmed from
 * the app" about an answer nobody gave - which would make the field worse than
 * useless in a contested meeting.
 */
public enum ReplySource {

    /** Answered from the owner's own authenticated space, web or mobile. */
    OWNER_APP,

    /** Answered through the confirmation link received with the convocation, without an account. */
    OWNER_LINK,

    /** Answered to the syndic's office - by phone, in person, by message - and entered by the syndic. */
    SYNDIC_OFFICE
}
