package com.architek.oikos.meeting.domain.valueobject;

import java.util.Collection;
import java.util.Objects;

import com.architek.oikos.meeting.domain.model.Convocation;

/**
 * What the attendance of a meeting adds up to: how many lots are at each stage
 * of their convocation, and - the part that decides anything - how much voting
 * weight is present against how much exists.
 *
 * <p>Computed from the convocations every time it is asked for, never stored:
 * a stored count is a count that can disagree with its own rows. Both
 * weights are needed downstream, and by two different rules - the quorum
 * compares present against total, and an ABSOLUTE majority is measured against
 * the total whether those lots turned up or not (ADR 0002 §4).
 *
 * <p>A lot counts as present because it signed in, never because it announced
 * it would attend: an owner who confirms and then does not come cannot make
 * the quorum.
 */
public record AttendanceTally(int totalUnits, int sentCount, int attendingCount, int notAttendingCount,
                               int noReplyCount, int checkedInCount, VotingWeight totalWeight,
                               VotingWeight presentWeight) {

    public AttendanceTally {
        Objects.requireNonNull(totalWeight, "totalWeight must not be null");
        Objects.requireNonNull(presentWeight, "presentWeight must not be null");
    }

    public static AttendanceTally of(Collection<Convocation> convocations) {
        int sent = 0;
        int attending = 0;
        int notAttending = 0;
        int noReply = 0;
        int checkedIn = 0;
        VotingWeight total = VotingWeight.zero();
        VotingWeight present = VotingWeight.zero();

        for (Convocation convocation : convocations) {
            total = total.plus(convocation.getVotingWeight());
            // Derived from the convocation's deliveries: a lot reached by any channel counts as
            // reached, and one that was tried and failed does not.
            if (convocation.getDeliveryStatus() == DeliveryStatus.SENT) {
                sent++;
            }
            switch (convocation.getAttendanceReply()) {
                case ATTENDING -> attending++;
                case NOT_ATTENDING -> notAttending++;
                case NO_REPLY -> noReply++;
            }
            if (convocation.isCheckedIn()) {
                checkedIn++;
                present = present.plus(convocation.getVotingWeight());
            }
        }

        return new AttendanceTally(convocations.size(), sent, attending, notAttending, noReply, checkedIn, total,
                present);
    }

    public boolean isQuorumReachedFor(QuorumPercentage quorum) {
        return quorum.isReachedBy(presentWeight.value(), totalWeight.value());
    }
}
