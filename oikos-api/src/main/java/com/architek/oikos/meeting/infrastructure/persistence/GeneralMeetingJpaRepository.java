package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;

public interface GeneralMeetingJpaRepository extends JpaRepository<GeneralMeetingEntity, UUID> {

    /**
     * A null filter parameter means "no filter", not "matches null" - there is
     * no meeting without a status or a type. Ordered by scheduledAt descending
     * with nulls first so that drafts, which have no date yet, sit at the top
     * where the syndic still has work to do; createdDate breaks ties between
     * several dateless drafts.
     */
    @Query("select m from GeneralMeetingEntity m where m.propertyId = :propertyId "
            + "and (:status is null or m.status = :status) "
            + "and (:meetingType is null or m.meetingType = :meetingType) "
            + "order by case when m.scheduledAt is null then 0 else 1 end, m.scheduledAt desc, m.createdDate desc")
    Page<GeneralMeetingEntity> findByPropertyIdFiltered(@Param("propertyId") UUID propertyId,
                                                          @Param("status") MeetingStatus status,
                                                          @Param("meetingType") MeetingType meetingType,
                                                          Pageable pageable);
}
