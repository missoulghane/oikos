package com.architek.oikos.meeting.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface ConvocationRepository {

    Convocation save(Convocation convocation);

    List<Convocation> saveAll(List<Convocation> convocations);

    Optional<Convocation> findById(ConvocationId id);

    /**
     * Every convocation of one meeting. Unpaged on purpose: the attendance
     * tally and the quorum are sums over the whole set, and a page of it would
     * answer a different question. Bounded by the number of lots in one
     * copropriété.
     */
    List<Convocation> findByGeneralMeetingId(GeneralMeetingId generalMeetingId);

    /** The lots already convoked for this meeting - what makes generation a find-or-create. */
    List<Convocation> findByGeneralMeetingIdAndUnitIds(GeneralMeetingId generalMeetingId, Collection<EntityId> unitIds);

    /** Every convocation addressed to any of the given lots, for the owner's own "my meetings" list. */
    List<Convocation> findByUnitIds(Collection<EntityId> unitIds);

    /**
     * The convocation behind a confirmation link. The only lookup an anonymous
     * caller can reach, and the token is all it has to go on.
     */
    Optional<Convocation> findByConfirmationToken(String confirmationToken);
}
