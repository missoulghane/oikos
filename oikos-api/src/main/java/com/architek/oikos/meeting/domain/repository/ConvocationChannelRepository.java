package com.architek.oikos.meeting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * The channel catalog. Read-only from the application's side: rows are seeded
 * by migration and edited by whoever operates the product, not by a use case -
 * an automated channel needs an emitter written for it, so creating one from a
 * screen would let anybody promise a send that never happens.
 */
public interface ConvocationChannelRepository {

    /**
     * Every channel still offered, in display order. Deactivated rows stay in
     * the table because past deliveries reference them.
     */
    List<ConvocationChannel> findAllActive();

    /** Including the deactivated ones - a delivery recorded years ago still names its channel. */
    Optional<ConvocationChannel> findByCode(ChannelCode code);

    List<ConvocationChannel> findAll();
}
