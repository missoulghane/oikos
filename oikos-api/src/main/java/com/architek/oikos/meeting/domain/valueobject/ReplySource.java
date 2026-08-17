package com.architek.oikos.meeting.domain.valueobject;

/**
 * How the presence confirmation was obtained.
 *
 * <p>An enum and not a catalog, unlike ConvocationChannel and
 * {@link ReplyMediumCode}: each value is a distinct code path on the server, not
 * a piece of data. Adding one is not a new row, it is a new way of answering,
 * and it has to be written.
 *
 * <p>Always derived from the caller, never accepted from the request body.
 * A client able to state its own source could write "the owner confirmed from
 * the app" about an answer nobody gave - which would make the field worse than
 * useless in a contested meeting.
 *
 * <p>Read the three as an exhaustive answer to "what does the server actually
 * know": the owner was authenticated, the owner held the link, or neither -
 * somebody at the office typed it in. That last case is where
 * {@link ReplyMediumCode} takes over and says by what means it arrived, which
 * is a declaration rather than something observed. Keeping the two apart is the
 * whole point: promoting "by phone" into this enum would let a declaration
 * masquerade as a fact the server established.
 */
public enum ReplySource {

    /** Answered from the owner's own authenticated space, web or mobile. */
    OWNER_APP,

    /** Answered through the confirmation link received with the convocation, without an account. */
    OWNER_LINK,

    /**
     * Reached the office some other way - phone, post, email, in person - and
     * was entered by the syndic. Named OTHER rather than the office it arrived
     * at: what the server can vouch for is only that neither of the two paths
     * above was taken.
     */
    OTHER
}
