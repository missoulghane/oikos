package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One lot's vote on one agenda item. The lot votes, not the copropriétaire
 * (ADR 0002 §2): a lot in indivision casts a single voice, an owner of three
 * lots casts three, and the unicity is (agenda item, lot).
 *
 * <p>The vote carries no weight of its own. Its weight is the one snapshotted
 * on the lot's Convocation when the meeting was convened - so a sale or a
 * correction of tantièmes after the session cannot restate a result. Storing
 * it twice would be storing a chance to disagree.
 *
 * <p>castByUserId records who entered the vote - the copropriétaire, or the
 * syndic entering a show of hands - never who holds the voice. Nullable: a
 * lot whose owners have no account is voted for by the syndic, and a bulk
 * entry made from the chair belongs to nobody in particular.
 *
 * <p>Immutable: recasting produces a new instance, which replaces the previous
 * one under the unicity constraint while the ballot is open. Once the ballot
 * closes nothing more is written - reopening is refused by AgendaItem.
 */
public final class Vote {

    private final VoteId id;
    private final AgendaItemId agendaItemId;
    private final EntityId unitId;
    private final VoteChoice choice;
    private final Instant castAt;
    private final EntityId castByUserId;
    private final Instant createdDate;

    private Vote(VoteId id, AgendaItemId agendaItemId, EntityId unitId, VoteChoice choice, Instant castAt,
                  EntityId castByUserId, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.agendaItemId = Objects.requireNonNull(agendaItemId, "agendaItemId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.choice = Objects.requireNonNull(choice, "choice must not be null");
        this.castAt = Objects.requireNonNull(castAt, "castAt must not be null");
        this.castByUserId = castByUserId;
        this.createdDate = createdDate;
    }

    public static Vote cast(VoteId id, AgendaItemId agendaItemId, EntityId unitId, VoteChoice choice, Instant castAt,
                             EntityId castByUserId) {
        return new Vote(id, agendaItemId, unitId, choice, castAt, castByUserId, null);
    }

    public static Vote reconstruct(VoteId id, AgendaItemId agendaItemId, EntityId unitId, VoteChoice choice,
                                    Instant castAt, EntityId castByUserId, Instant createdDate) {
        return new Vote(id, agendaItemId, unitId, choice, castAt, castByUserId, createdDate);
    }

    /** Changing one's mind while the ballot is open keeps the same row, so the unicity holds. */
    public Vote recast(VoteChoice newChoice, Instant at, EntityId byUserId) {
        return new Vote(id, agendaItemId, unitId, newChoice, at, byUserId, createdDate);
    }

    public VoteId getId() {
        return id;
    }

    public AgendaItemId getAgendaItemId() {
        return agendaItemId;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public VoteChoice getChoice() {
        return choice;
    }

    public Instant getCastAt() {
        return castAt;
    }

    public EntityId getCastByUserId() {
        return castByUserId;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Vote other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
