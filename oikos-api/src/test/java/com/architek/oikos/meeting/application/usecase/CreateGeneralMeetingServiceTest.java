package com.architek.oikos.meeting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.PropertyInfo;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.MeetingQuorumSettingRepository;
import com.architek.oikos.meeting.domain.valueobject.MeetingQuorumSettingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateGeneralMeetingServiceTest {

    @Mock
    private GeneralMeetingRepository generalMeetingRepository;

    @Mock
    private MeetingQuorumSettingRepository quorumSettingRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers, Casablanca");

    private final EntityId propertyId = EntityId.newId();

    private CreateGeneralMeetingService newService() {
        return new CreateGeneralMeetingService(generalMeetingRepository, quorumSettingRepository, propertyDirectoryPort);
    }

    private GeneralMeeting create(VotingWeightMode mode, Optional<MeetingQuorumSetting> setting) {
        when(propertyDirectoryPort.getProperty(propertyId))
                .thenReturn(new PropertyInfo(propertyId, "Résidence Al Amal", mode));
        when(quorumSettingRepository.findByPropertyAndType(propertyId, MeetingType.ORDINARY)).thenReturn(setting);
        when(generalMeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG ordinaire 2026",
                SESSION_DATE, VENUE));

        ArgumentCaptor<GeneralMeeting> captor = ArgumentCaptor.forClass(GeneralMeeting.class);
        verify(generalMeetingRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void a_meeting_is_created_as_a_draft() {
        GeneralMeeting created = create(VotingWeightMode.SHARES, Optional.empty());

        assertThat(created.getStatus()).isEqualTo(MeetingStatus.DRAFT);
        assertThat(created.getTitle()).isEqualTo("AG ordinaire 2026");
        assertThat(created.getPropertyId()).isEqualTo(propertyId);
    }

    @Test
    void the_date_and_venue_given_at_creation_are_carried_onto_the_draft() {
        // The creation screen asks for them, and they are shown at the top of every one of
        // the meeting's tabs afterwards - creating an empty shell and updating it a second
        // later would only be a round trip.
        GeneralMeeting created = create(VotingWeightMode.SHARES, Optional.empty());

        assertThat(created.getScheduledAt()).isEqualTo(SESSION_DATE);
        assertThat(created.getVenue()).isEqualTo(VENUE);
        // Still a draft: having a date is not being scheduled, which is what freezes the agenda.
        assertThat(created.getStatus()).isEqualTo(MeetingStatus.DRAFT);
    }

    @Test
    void a_draft_may_still_be_created_without_a_date() {
        when(propertyDirectoryPort.getProperty(propertyId))
                .thenReturn(new PropertyInfo(propertyId, "Résidence Al Amal", VotingWeightMode.SHARES));
        when(quorumSettingRepository.findByPropertyAndType(propertyId, MeetingType.ORDINARY))
                .thenReturn(Optional.empty());
        when(generalMeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().create(new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG", null, null));

        ArgumentCaptor<GeneralMeeting> captor = ArgumentCaptor.forClass(GeneralMeeting.class);
        verify(generalMeetingRepository).save(captor.capture());
        assertThat(captor.getValue().getScheduledAt()).isNull();
        assertThat(captor.getValue().getVenue()).isNull();
    }

    @Test
    void the_configured_quorum_for_this_meeting_type_is_snapshotted() {
        MeetingQuorumSetting setting = MeetingQuorumSetting.create(MeetingQuorumSettingId.newId(), propertyId,
                MeetingType.ORDINARY, QuorumPercentage.of(BigDecimal.valueOf(60)));

        GeneralMeeting created = create(VotingWeightMode.SHARES, Optional.of(setting));

        assertThat(created.getQuorumPercentage()).isEqualTo(QuorumPercentage.of(BigDecimal.valueOf(60)));
    }

    @Test
    void an_unconfigured_quorum_becomes_none_rather_than_an_invented_legal_default() {
        GeneralMeeting created = create(VotingWeightMode.SHARES, Optional.empty());

        assertThat(created.getQuorumPercentage()).isEqualTo(QuorumPercentage.none());
        assertThat(created.getQuorumPercentage().isRequired()).isFalse();
    }

    @Test
    void a_property_billing_by_tantiemes_votes_by_tantiemes() {
        assertThat(create(VotingWeightMode.SHARES, Optional.empty()).getVotingWeightMode())
                .isEqualTo(VotingWeightMode.SHARES);
    }

    @Test
    void a_property_billing_a_flat_rate_votes_one_voice_per_lot() {
        assertThat(create(VotingWeightMode.PER_UNIT, Optional.empty()).getVotingWeightMode())
                .isEqualTo(VotingWeightMode.PER_UNIT);
    }
}
