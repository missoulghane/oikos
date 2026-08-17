package com.architek.oikos.meeting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationChannelRepository;
import com.architek.oikos.meeting.domain.repository.ReplyMediumRepository;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.service.ConvocationTokenGenerator;
import com.architek.oikos.meeting.domain.service.ShortCodeGenerator;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GenerateConvocationsServiceTest {

    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers");

    @Mock
    private GeneralMeetingRepository generalMeetingRepository;

    @Mock
    private ConvocationRepository convocationRepository;

    @Mock
    private PropertyUnitDirectoryPort propertyUnitDirectoryPort;

    @Mock
    private ConvocationChannelRepository convocationChannelRepository;

    @Mock
    private ReplyMediumRepository replyMediumRepository;

    private final EntityId propertyId = EntityId.newId();
    private final EntityId ownedUnitId = EntityId.newId();
    private final EntityId unownedUnitId = EntityId.newId();
    private GeneralMeeting scheduled;

    @BeforeEach
    void setUp() {
        scheduled = GeneralMeeting.createDraft(GeneralMeetingId.newId(), propertyId, MeetingType.ORDINARY,
                        "AG ordinaire 2026", null, null, QuorumPercentage.of(BigDecimal.valueOf(50)), VotingWeightMode.SHARES, ShortCode.of("agre01"))
                .schedule(SESSION_DATE, VENUE);
    }

    private GenerateConvocationsService newService() {
        return new GenerateConvocationsService(generalMeetingRepository, convocationRepository,
                new ConvocationViewAssembler(propertyUnitDirectoryPort,
                        new ConvocationChannelLookup(convocationChannelRepository), replyMediumRepository,
                        generalMeetingRepository),
                new ConvocationTokenGenerator(), new ShortCodeGenerator());
    }

    private void givenTwoLotsOneOfThemUnowned() {
        when(propertyUnitDirectoryPort.listUnits(propertyId)).thenReturn(List.of(
                new UnitInfo(ownedUnitId, "Appartement 1", "Bâtiment A", BigDecimal.valueOf(120),
                        List.of(new OwnerInfo(EntityId.newId(), "Karim Benali", "karim@example.com"))),
                new UnitInfo(unownedUnitId, "Appartement 2", "Bâtiment A", BigDecimal.valueOf(80), List.of())));
    }

    private List<Convocation> capturedSavedConvocations() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Convocation>> captor = ArgumentCaptor.forClass(List.class);
        verify(convocationRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void a_lot_with_no_owner_is_convoked_like_any_other() {
        // It cannot answer or sign in, but it exists and it weighs in the totals the quorum
        // and an absolute majority are measured against - dropping it would lower both bars.
        givenTwoLotsOneOfThemUnowned();
        when(generalMeetingRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of());
        when(convocationRepository.findByGeneralMeetingId(scheduled.getId())).thenReturn(List.of());

        newService().generate(new GenerateConvocationsCommand(scheduled.getId()));

        assertThat(capturedSavedConvocations()).extracting(Convocation::getUnitId)
                .containsExactlyInAnyOrder(ownedUnitId, unownedUnitId);
    }

    @Test
    void the_voting_weight_is_snapshotted_from_the_meetings_own_mode() {
        givenTwoLotsOneOfThemUnowned();
        when(generalMeetingRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of());
        when(convocationRepository.findByGeneralMeetingId(scheduled.getId())).thenReturn(List.of());

        newService().generate(new GenerateConvocationsCommand(scheduled.getId()));

        assertThat(capturedSavedConvocations()).extracting(convocation -> convocation.getVotingWeight().value())
                .containsExactlyInAnyOrder(new BigDecimal("120.00"), new BigDecimal("80.00"));
    }

    @Test
    void a_flat_rate_property_gives_every_lot_one_voice_whatever_its_size() {
        GeneralMeeting perUnitMeeting = GeneralMeeting.createDraft(GeneralMeetingId.newId(), propertyId,
                        MeetingType.ORDINARY, "AG", null, null, QuorumPercentage.none(), VotingWeightMode.PER_UNIT, ShortCode.of("agre02"))
                .schedule(SESSION_DATE, VENUE);
        givenTwoLotsOneOfThemUnowned();
        when(generalMeetingRepository.findById(perUnitMeeting.getId())).thenReturn(Optional.of(perUnitMeeting));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of());
        when(convocationRepository.findByGeneralMeetingId(perUnitMeeting.getId())).thenReturn(List.of());

        newService().generate(new GenerateConvocationsCommand(perUnitMeeting.getId()));

        assertThat(capturedSavedConvocations()).extracting(convocation -> convocation.getVotingWeight().value())
                .containsExactly(new BigDecimal("1.00"), new BigDecimal("1.00"));
    }

    @Test
    void generating_moves_the_meeting_to_convened() {
        givenTwoLotsOneOfThemUnowned();
        when(generalMeetingRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of());
        when(convocationRepository.findByGeneralMeetingId(scheduled.getId())).thenReturn(List.of());

        newService().generate(new GenerateConvocationsCommand(scheduled.getId()));

        ArgumentCaptor<GeneralMeeting> captor = ArgumentCaptor.forClass(GeneralMeeting.class);
        verify(generalMeetingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MeetingStatus.CONVENED);
    }

    @Test
    void re_running_convokes_only_the_lots_that_were_missing_and_does_not_transition_again() {
        // Idempotence: a double click, a retry after a timeout, or a lot added to the
        // copropriété after the first run - none of them may duplicate or re-transition.
        givenTwoLotsOneOfThemUnowned();
        GeneralMeeting convened = scheduled.convene();
        Convocation existing = Convocation.generate(ConvocationId.newId(), convened.getId(), ownedUnitId,
                VotingWeight.of(BigDecimal.valueOf(120)), "token-1", ShortCode.of("code01"));
        when(generalMeetingRepository.findById(convened.getId())).thenReturn(Optional.of(convened));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of(existing));
        when(convocationRepository.findByGeneralMeetingId(convened.getId())).thenReturn(List.of(existing));

        newService().generate(new GenerateConvocationsCommand(convened.getId()));

        assertThat(capturedSavedConvocations()).extracting(Convocation::getUnitId).containsExactly(unownedUnitId);
        verify(generalMeetingRepository, never()).save(any());
    }

    @Test
    void the_returned_rows_carry_the_lot_labels_the_tracking_table_shows() {
        givenTwoLotsOneOfThemUnowned();
        Convocation existing = Convocation.generate(ConvocationId.newId(), scheduled.getId(), ownedUnitId,
                VotingWeight.of(BigDecimal.valueOf(120)), "token-2", ShortCode.of("code02"));
        when(generalMeetingRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));
        when(convocationRepository.findByGeneralMeetingIdAndUnitIds(any(), any())).thenReturn(List.of());
        when(convocationRepository.findByGeneralMeetingId(scheduled.getId())).thenReturn(List.of(existing));

        List<ConvocationView> views = newService().generate(new GenerateConvocationsCommand(scheduled.getId()));

        assertThat(views).singleElement().satisfies(view -> {
            assertThat(view.unitNumber()).isEqualTo("Appartement 1");
            assertThat(view.buildingName()).isEqualTo("Bâtiment A");
            assertThat(view.recipients()).singleElement().satisfies(recipient -> {
                assertThat(recipient.fullName()).isEqualTo("Karim Benali");
                // The email is carried through so a FAILED row can say why it failed.
                assertThat(recipient.email()).isEqualTo("karim@example.com");
            });
        });
    }
}
