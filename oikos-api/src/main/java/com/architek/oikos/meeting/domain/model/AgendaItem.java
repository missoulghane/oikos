package com.architek.oikos.meeting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.meeting.domain.exception.InvalidVoteSessionTransitionException;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;

/**
 * One point of the agenda: what is being put to the meeting, and under which
 * majority it is decided.
 *
 * <p>No result is stored here. The tally is recomputed from the votes at read
 * time and frozen exactly once, in the minutes - the convention the whole
 * codebase follows for anything derivable (InstallmentStatus, OwnershipStatus,
 * unread counts). voteSessionStatus is the exception that proves it: whether
 * the chair has opened the ballot cannot be deduced from the votes cast, so it
 * is a real state and it is persisted.
 *
 * <p>position is dense and zero-based within a meeting, and unique
 * (uk_agenda_item_position, DEFERRABLE so that a reordering may pass through
 * a transient duplicate mid-transaction).
 *
 * <p>Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class AgendaItem {

    private static final int MAX_LABEL_LENGTH = 200;

    private final AgendaItemId id;
    private final GeneralMeetingId generalMeetingId;
    private final String label;
    private final String description;
    private final int position;
    private final MajorityRule majorityRule;
    private final VoteSessionStatus voteSessionStatus;
    private final Instant createdDate;

    private AgendaItem(AgendaItemId id, GeneralMeetingId generalMeetingId, String label, String description,
                        int position, MajorityRule majorityRule, VoteSessionStatus voteSessionStatus,
                        Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.generalMeetingId = Objects.requireNonNull(generalMeetingId, "generalMeetingId must not be null");
        this.label = requireValidLabel(label);
        this.description = description == null || description.isBlank() ? null : description.trim();
        if (position < 0) {
            throw new IllegalArgumentException("position must be >= 0");
        }
        this.position = position;
        this.majorityRule = Objects.requireNonNull(majorityRule, "majorityRule must not be null");
        this.voteSessionStatus = Objects.requireNonNull(voteSessionStatus, "voteSessionStatus must not be null");
        this.createdDate = createdDate;
    }

    public static AgendaItem create(AgendaItemId id, GeneralMeetingId generalMeetingId, String label,
                                     String description, int position, MajorityRule majorityRule) {
        return new AgendaItem(id, generalMeetingId, label, description, position, majorityRule,
                VoteSessionStatus.NOT_OPENED, null);
    }

    public static AgendaItem reconstruct(AgendaItemId id, GeneralMeetingId generalMeetingId, String label,
                                          String description, int position, MajorityRule majorityRule,
                                          VoteSessionStatus voteSessionStatus, Instant createdDate) {
        return new AgendaItem(id, generalMeetingId, label, description, position, majorityRule, voteSessionStatus,
                createdDate);
    }

    public AgendaItem update(String newLabel, String newDescription, MajorityRule newMajorityRule) {
        return new AgendaItem(id, generalMeetingId, newLabel, newDescription, position, newMajorityRule,
                voteSessionStatus, createdDate);
    }

    public AgendaItem moveTo(int newPosition) {
        return new AgendaItem(id, generalMeetingId, label, description, newPosition, majorityRule, voteSessionStatus,
                createdDate);
    }

    /**
     * Opening and closing the ballot (lot 4). A point whose ballot has been
     * closed never reopens: reopening would let a result already announced to
     * the room be changed afterwards.
     */
    public AgendaItem openVoteSession() {
        requireVoteSessionIs(VoteSessionStatus.NOT_OPENED, VoteSessionStatus.OPEN);
        return withVoteSessionStatus(VoteSessionStatus.OPEN);
    }

    public AgendaItem closeVoteSession() {
        requireVoteSessionIs(VoteSessionStatus.OPEN, VoteSessionStatus.CLOSED);
        return withVoteSessionStatus(VoteSessionStatus.CLOSED);
    }

    public boolean acceptsVotes() {
        return voteSessionStatus == VoteSessionStatus.OPEN;
    }

    private AgendaItem withVoteSessionStatus(VoteSessionStatus newStatus) {
        return new AgendaItem(id, generalMeetingId, label, description, position, majorityRule, newStatus, createdDate);
    }

    private void requireVoteSessionIs(VoteSessionStatus expected, VoteSessionStatus target) {
        if (voteSessionStatus != expected) {
            throw new InvalidVoteSessionTransitionException(voteSessionStatus, target);
        }
    }

    private static String requireValidLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        String trimmed = label.trim();
        if (trimmed.length() > MAX_LABEL_LENGTH) {
            throw new IllegalArgumentException("label must be at most " + MAX_LABEL_LENGTH + " characters");
        }
        return trimmed;
    }

    public AgendaItemId getId() {
        return id;
    }

    public GeneralMeetingId getGeneralMeetingId() {
        return generalMeetingId;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }

    public MajorityRule getMajorityRule() {
        return majorityRule;
    }

    public VoteSessionStatus getVoteSessionStatus() {
        return voteSessionStatus;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof AgendaItem other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
