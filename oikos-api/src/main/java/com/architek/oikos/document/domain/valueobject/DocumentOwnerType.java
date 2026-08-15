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
    PAYMENT
}
