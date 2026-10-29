package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.exception.InvalidInstallmentCallTransitionException;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class InstallmentCallTest {

    @Test
    void draft_builds_a_installment_call_with_the_given_fields() {
        EntityId propertyId = EntityId.newId();
        YearMonth period = YearMonth.of(2026, 1);
        LocalDate dueDate = LocalDate.of(2026, 2, 5);

        InstallmentCall installmentCall = InstallmentCall.draft(InstallmentCallId.newId(), propertyId, period, dueDate);

        assertThat(installmentCall.getPropertyId()).isEqualTo(propertyId);
        assertThat(installmentCall.getPeriod()).isEqualTo(period);
        assertThat(installmentCall.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void draft_builds_a_draft_installment_call_with_no_journal_entry() {
        InstallmentCall installmentCall = InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(),
                YearMonth.of(2026, 8), LocalDate.of(2026, 9, 5));

        assertThat(installmentCall.getStatus()).isEqualTo(InstallmentCallStatus.DRAFT);
        assertThat(installmentCall.getJournalEntryId()).isEmpty();
    }

    @Test
    void P1_issue_then_post_moves_a_draft_call_through_its_lifecycle() {
        InstallmentCall draft = InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(),
                YearMonth.of(2026, 8), LocalDate.of(2026, 9, 5));
        EntityId journalEntryId = EntityId.newId();

        InstallmentCall issued = draft.issue();
        InstallmentCall posted = issued.post(journalEntryId);

        assertThat(issued.getStatus()).isEqualTo(InstallmentCallStatus.ISSUED);
        assertThat(posted.getStatus()).isEqualTo(InstallmentCallStatus.POSTED);
        assertThat(posted.getJournalEntryId()).contains(journalEntryId);
    }

    @Test
    void a_call_cannot_be_posted_before_being_issued() {
        InstallmentCall draft = InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(),
                YearMonth.of(2026, 8), LocalDate.of(2026, 9, 5));

        assertThatThrownBy(() -> draft.post(EntityId.newId()))
                .isInstanceOf(InvalidInstallmentCallTransitionException.class);
    }

    @Test
    void a_draft_or_issued_call_can_be_cancelled_directly() {
        InstallmentCall draft = InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(),
                YearMonth.of(2026, 8), LocalDate.of(2026, 9, 5));

        assertThat(draft.cancel().getStatus()).isEqualTo(InstallmentCallStatus.CANCELLED);
        assertThat(draft.issue().cancel().getStatus()).isEqualTo(InstallmentCallStatus.CANCELLED);
    }

    @Test
    void a_posted_call_cannot_be_cancelled_directly_it_needs_a_contre_passation() {
        InstallmentCall posted = posted();

        assertThatThrownBy(posted::cancel).isInstanceOf(InvalidInstallmentCallTransitionException.class);
    }

    @Test
    void a_posted_call_is_cancelled_after_its_contre_passation_and_keeps_its_journal_entry() {
        InstallmentCall posted = posted();

        InstallmentCall cancelled = posted.cancelPosted();

        assertThat(cancelled.getStatus()).isEqualTo(InstallmentCallStatus.CANCELLED);
        assertThat(cancelled.isCancelled()).isTrue();
        assertThat(cancelled.getJournalEntryId()).isEqualTo(posted.getJournalEntryId());
    }

    @Test
    void only_a_posted_call_goes_through_contre_passation() {
        InstallmentCall draft = InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(),
                YearMonth.of(2026, 8), LocalDate.of(2026, 9, 5));

        assertThatThrownBy(draft::cancelPosted).isInstanceOf(InvalidInstallmentCallTransitionException.class);
        assertThatThrownBy(() -> posted().cancelPosted().cancelPosted())
                .isInstanceOf(InvalidInstallmentCallTransitionException.class);
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        InstallmentCallId id = InstallmentCallId.newId();
        InstallmentCall a = InstallmentCall.draft(id, EntityId.newId(), YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5)).issue().post(EntityId.newId());
        InstallmentCall b = InstallmentCall.draft(id, EntityId.newId(), YearMonth.of(2026, 3), LocalDate.of(2026, 4, 5)).issue().post(EntityId.newId());

        assertThat(a).isEqualTo(b);
    }

    private static InstallmentCall posted() {
        return InstallmentCall.draft(InstallmentCallId.newId(), EntityId.newId(), YearMonth.of(2026, 8),
                LocalDate.of(2026, 9, 5)).issue().post(EntityId.newId());
    }
}
