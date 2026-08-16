package com.architek.oikos.document.domain.valueobject;

/**
 * Closed catalog of functional object types a Document can be attached to.
 * Adding a new attachable type is: a new constant here + a new case in
 * DocumentOwnerExistenceAdapter (existence check) + a new resolution branch in
 * PropertyAccessEvaluator.resolveDocumentOwnerPropertyId (authorization) - no other layer
 * of the document module needs to change.
 */
public enum DocumentOwnerType {
    PROPERTY,
    UNIT,
    /** A generated payment receipt, attached to the payment it attests to. */
    PAYMENT,
    /** Supporting documents of one point of a general meeting's agenda. */
    AGENDA_ITEM,
    /** Supporting documents of the assembly as a whole - budget, report - rather than of one point. */
    GENERAL_MEETING,
    /** The generated convocation letter of one lot, and its signed attendance sheet. */
    CONVOCATION,
    /** The final PDF of a general meeting's minutes. */
    MEETING_MINUTES
}
