package com.architek.oikos.meeting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.ReplyMedium;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;

/**
 * The catalog of means by which an answer reaches the office. Read-only from
 * the application's side: rows are seeded by migration and edited by whoever
 * operates the product, not by a use case.
 */
public interface ReplyMediumRepository {

    /** Every medium still offered, in display order. */
    List<ReplyMedium> findAllActive();

    /** Including the deactivated ones - an answer recorded years ago still names its medium. */
    Optional<ReplyMedium> findByCode(ReplyMediumCode code);

    List<ReplyMedium> findAll();
}
