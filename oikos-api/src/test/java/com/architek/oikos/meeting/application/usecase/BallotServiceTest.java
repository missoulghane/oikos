package com.architek.oikos.meeting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.command.RecordShowOfHandsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.MeetingNotInProgressException;
import com.architek.oikos.meeting.domain.exception.UnitNotCheckedInException;
import com.architek.oikos.meeting.domain.exception.UnitNotConvokedException;
import com.architek.oikos.meeting.domain.exception.VoteSessionNotOpenException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.VoteRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.AttendanceMode;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.meeting.domain.valueobject.VoteSessionStatus;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The three guards a vote has to pass - session under way, ballot open, lot
 * present - and the show-of-hands rule that only the room votes.
 */
@ExtendWith(MockitoExtension.class)
class BallotServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T18:00:00Z"), ZoneOffset.UTC);
    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers");

    @Mock
    private AgendaItemRepository agendaItemRepository;

    @Mock
    private GeneralMeetingRepository generalMeetingRepository;

    @Mock
    private ConvocationRepository convocationRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PropertyUnitDirectoryPort propertyUnitDirectoryPort;

    private final EntityId propertyId = EntityId.newId();
    private final EntityId presentUnitId = EntityId.newId();
    private final EntityId absentUnitId = EntityId.newId();
    private GeneralMeeting inProgress;
    private AgendaItem openItem;

    @BeforeEach
    void setUp() {
        inProgress = GeneralMeeting.createDraft(GeneralMeetingId.newId(), propertyId, MeetingType.ORDINARY, "AG",
                        null, null, QuorumPercentage.none(), VotingWeightMode.SHARES)
                .schedule(SESSION_DATE, VENUE).convene().open(true, false);
        openItem = AgendaItem.create(AgendaItemId.newId(), inProgress.getId(), "Travaux", null, 0, MajorityRule.SIMPLE)
                .openVoteSession();
    }

    private BallotContext context() {
        return new BallotContext(agendaItemRepository, generalMeetingRepository, convocationRepository, voteRepository);
    }

    private Convocation present(EntityId unitId, int weight) {
        return Convocation.generate(ConvocationId.newId(), inProgress.getId(), unitId,
                        VotingWeight.of(BigDecimal.valueOf(weight)), "token-1")
                .checkIn(AttendanceMode.ON_SITE, null, CLOCK.instant());
    }

    private Convocation absent(EntityId unitId, int weight) {
        return Convocation.generate(ConvocationId.newId(), inProgress.getId(), unitId,
                VotingWeight.of(BigDecimal.valueOf(weight)), "token-2");
    }

    private void givenRoom(AgendaItem item, List<Convocation> convocations) {
        when(agendaItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(generalMeetingRepository.findById(inProgress.getId())).thenReturn(Optional.of(inProgress));
        lenient().when(convocationRepository.findByGeneralMeetingId(inProgress.getId())).thenReturn(convocations);
        lenient().when(propertyUnitDirectoryPort.listUnits(propertyId)).thenReturn(List.of(
                new UnitInfo(presentUnitId, "Appartement 1", "Bâtiment A", BigDecimal.valueOf(100), List.of()),
                new UnitInfo(absentUnitId, "Appartement 2", "Bâtiment A", BigDecimal.valueOf(100), List.of())));
    }

    private CastVoteService castService() {
        return new CastVoteService(context(), voteRepository, propertyUnitDirectoryPort, CLOCK);
    }

    @Test
    void a_lot_that_signed_in_may_vote() {
        givenRoom(openItem, List.of(present(presentUnitId, 100)));
        when(voteRepository.findByAgendaItemIdAndUnitId(openItem.getId(), presentUnitId)).thenReturn(Optional.empty());
        when(voteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(castService().cast(new CastVoteCommand(openItem.getId(), presentUnitId, VoteChoice.FOR,
                EntityId.newId())).choice()).isEqualTo(VoteChoice.FOR);
    }

    @Test
    void a_lot_that_did_not_sign_in_may_not_vote() {
        // Presence is the same fact the quorum was computed from: letting it vote would put
        // weight in the result that never counted towards opening the session.
        givenRoom(openItem, List.of(present(presentUnitId, 100), absent(absentUnitId, 100)));

        assertThatThrownBy(() -> castService().cast(new CastVoteCommand(openItem.getId(), absentUnitId,
                VoteChoice.FOR, EntityId.newId())))
                .isInstanceOf(UnitNotCheckedInException.class)
                .hasMessageContaining("Appartement 2");
        verify(voteRepository, never()).save(any());
    }

    @Test
    void a_lot_of_another_copropriete_has_no_convocation_and_cannot_vote() {
        givenRoom(openItem, List.of(present(presentUnitId, 100)));

        assertThatThrownBy(() -> castService().cast(new CastVoteCommand(openItem.getId(), EntityId.newId(),
                VoteChoice.FOR, EntityId.newId()))).isInstanceOf(UnitNotConvokedException.class);
    }

    @Test
    void nothing_is_accepted_before_the_ballot_is_opened() {
        AgendaItem notOpened = AgendaItem.create(AgendaItemId.newId(), inProgress.getId(), "Travaux", null, 0,
                MajorityRule.SIMPLE);
        givenRoom(notOpened, List.of(present(presentUnitId, 100)));

        assertThatThrownBy(() -> castService().cast(new CastVoteCommand(notOpened.getId(), presentUnitId,
                VoteChoice.FOR, EntityId.newId())))
                .isInstanceOf(VoteSessionNotOpenException.class)
                .hasMessageContaining("NOT_OPENED");
    }

    @Test
    void nothing_is_accepted_once_the_ballot_is_closed() {
        AgendaItem closed = openItem.closeVoteSession();
        givenRoom(closed, List.of(present(presentUnitId, 100)));

        assertThatThrownBy(() -> castService().cast(new CastVoteCommand(closed.getId(), presentUnitId,
                VoteChoice.FOR, EntityId.newId()))).isInstanceOf(VoteSessionNotOpenException.class);
    }

    @Test
    void a_ballot_cannot_be_opened_before_the_chair_opens_the_session() {
        GeneralMeeting convened = GeneralMeeting.createDraft(GeneralMeetingId.newId(), propertyId,
                        MeetingType.ORDINARY, "AG", null, null, QuorumPercentage.none(), VotingWeightMode.SHARES)
                .schedule(SESSION_DATE, VENUE).convene();
        AgendaItem item = AgendaItem.create(AgendaItemId.newId(), convened.getId(), "Travaux", null, 0,
                MajorityRule.SIMPLE);
        when(agendaItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(generalMeetingRepository.findById(convened.getId())).thenReturn(Optional.of(convened));

        OpenVoteSessionService service = new OpenVoteSessionService(context(), agendaItemRepository);

        assertThatThrownBy(() -> service.open(new OpenVoteSessionCommand(item.getId())))
                .isInstanceOf(MeetingNotInProgressException.class);
        verify(agendaItemRepository, never()).save(any());
    }

    @Test
    void voting_again_replaces_the_previous_choice_on_the_same_row() {
        Vote existing = Vote.cast(com.architek.oikos.meeting.domain.valueobject.VoteId.newId(), openItem.getId(),
                presentUnitId, VoteChoice.FOR, CLOCK.instant(), null);
        givenRoom(openItem, List.of(present(presentUnitId, 100)));
        when(voteRepository.findByAgendaItemIdAndUnitId(openItem.getId(), presentUnitId))
                .thenReturn(Optional.of(existing));
        when(voteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        castService().cast(new CastVoteCommand(openItem.getId(), presentUnitId, VoteChoice.AGAINST, EntityId.newId()));

        ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
        verify(voteRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(existing.getId());
        assertThat(captor.getValue().getChoice()).isEqualTo(VoteChoice.AGAINST);
    }

    private RecordShowOfHandsService showOfHandsService() {
        return new RecordShowOfHandsService(context(), voteRepository, CLOCK);
    }

    @Test
    void a_show_of_hands_only_covers_the_lots_in_the_room() {
        // Giving the default to absent lots would invent voices - and under an ABSOLUTE
        // majority, measured against the whole copropriété, decide items nobody carried.
        givenRoom(openItem, List.of(present(presentUnitId, 100), absent(absentUnitId, 100)));
        when(voteRepository.findByAgendaItemId(openItem.getId())).thenReturn(List.of());
        when(voteRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        showOfHandsService().record(new RecordShowOfHandsCommand(openItem.getId(), VoteChoice.FOR, Map.of(),
                EntityId.newId()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Vote>> captor = ArgumentCaptor.forClass(List.class);
        verify(voteRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Vote::getUnitId).containsExactly(presentUnitId);
    }

    @Test
    void an_exception_overrides_the_default_for_the_lot_it_names() {
        EntityId secondPresent = EntityId.newId();
        givenRoom(openItem, List.of(present(presentUnitId, 100), present(secondPresent, 100)));
        when(voteRepository.findByAgendaItemId(openItem.getId())).thenReturn(List.of());
        when(voteRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        showOfHandsService().record(new RecordShowOfHandsCommand(openItem.getId(), VoteChoice.FOR,
                Map.of(secondPresent, VoteChoice.AGAINST), EntityId.newId()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Vote>> captor = ArgumentCaptor.forClass(List.class);
        verify(voteRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).filteredOn(vote -> vote.getUnitId().equals(secondPresent))
                .singleElement().extracting(Vote::getChoice).isEqualTo(VoteChoice.AGAINST);
    }

    @Test
    void an_exception_naming_an_absent_lot_is_refused_rather_than_dropped() {
        // It means the secretary is reading a different list from the one in the room.
        givenRoom(openItem, List.of(present(presentUnitId, 100), absent(absentUnitId, 100)));

        assertThatThrownBy(() -> showOfHandsService().record(new RecordShowOfHandsCommand(openItem.getId(),
                VoteChoice.FOR, Map.of(absentUnitId, VoteChoice.AGAINST), EntityId.newId())))
                .isInstanceOf(UnitNotCheckedInException.class);
        verify(voteRepository, never()).saveAll(any());
    }

    @Test
    void closing_the_ballot_returns_the_result_the_chair_announces() {
        givenRoom(openItem, List.of(present(presentUnitId, 100), absent(absentUnitId, 100)));
        when(agendaItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(voteRepository.findByAgendaItemId(openItem.getId())).thenReturn(List.of(
                Vote.cast(com.architek.oikos.meeting.domain.valueobject.VoteId.newId(), openItem.getId(),
                        presentUnitId, VoteChoice.FOR, CLOCK.instant(), null)));

        AgendaItemResultView result = new CloseVoteSessionService(context(), agendaItemRepository)
                .close(new com.architek.oikos.meeting.application.command.CloseVoteSessionCommand(openItem.getId()));

        assertThat(result.voteSessionStatus()).isEqualTo(VoteSessionStatus.CLOSED);
        assertThat(result.forCount()).isEqualTo(1);
        assertThat(result.forWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.presentWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.totalWeight()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.outcome()).isEqualTo(VoteOutcome.ADOPTED);
    }
}
