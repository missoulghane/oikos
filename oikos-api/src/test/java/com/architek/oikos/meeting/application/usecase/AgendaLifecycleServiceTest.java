package com.architek.oikos.meeting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.ReorderAgendaItemsCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.PropertyInfo;
import com.architek.oikos.meeting.domain.exception.EmptyAgendaException;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;

/**
 * The rules that live in the services rather than in the aggregate: "no
 * scheduling with an empty agenda", "the reordering must be exhaustive", and
 * the freeze that follows scheduling.
 */
@ExtendWith(MockitoExtension.class)
class AgendaLifecycleServiceTest {

    private static final Instant SESSION_DATE = Instant.parse("2026-09-15T17:00:00Z");
    private static final MeetingVenue VENUE = MeetingVenue.onSite("12 rue des Orangers, Casablanca");

    @Mock
    private GeneralMeetingRepository generalMeetingRepository;

    @Mock
    private AgendaItemRepository agendaItemRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private final EntityId propertyId = EntityId.newId();
    private GeneralMeeting draft;

    @BeforeEach
    void setUp() {
        draft = GeneralMeeting.createDraft(GeneralMeetingId.newId(), propertyId, MeetingType.ORDINARY,
                "AG ordinaire 2026", null, null, QuorumPercentage.none(), VotingWeightMode.PER_UNIT, ShortCode.of("agre01"));
    }

    private GeneralMeetingViewAssembler assembler() {
        lenient().when(propertyDirectoryPort.getProperty(propertyId))
                .thenReturn(new PropertyInfo(propertyId, "Résidence Al Amal", VotingWeightMode.PER_UNIT));
        return new GeneralMeetingViewAssembler(propertyDirectoryPort, agendaItemRepository);
    }

    private AgendaItem itemAt(int position, String label) {
        return AgendaItem.create(AgendaItemId.newId(), draft.getId(), label, null, position, MajorityRule.SIMPLE);
    }

    @Test
    void a_meeting_with_an_empty_agenda_cannot_be_scheduled() {
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.countByGeneralMeetingId(draft.getId())).thenReturn(0L);

        ScheduleGeneralMeetingService service =
                new ScheduleGeneralMeetingService(generalMeetingRepository, agendaItemRepository, assembler());

        assertThatThrownBy(() -> service.schedule(new ScheduleGeneralMeetingCommand(draft.getId(), SESSION_DATE, VENUE)))
                .isInstanceOf(EmptyAgendaException.class);
        verify(generalMeetingRepository, never()).save(any());
    }

    @Test
    void scheduling_succeeds_as_soon_as_the_agenda_has_one_point() {
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.countByGeneralMeetingId(draft.getId())).thenReturn(1L);
        when(generalMeetingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        new ScheduleGeneralMeetingService(generalMeetingRepository, agendaItemRepository, assembler())
                .schedule(new ScheduleGeneralMeetingCommand(draft.getId(), SESSION_DATE, VENUE));

        ArgumentCaptor<GeneralMeeting> captor = ArgumentCaptor.forClass(GeneralMeeting.class);
        verify(generalMeetingRepository).save(captor.capture());
        assertThat(captor.getValue().getScheduledAt()).isEqualTo(SESSION_DATE);
    }

    @Test
    void a_new_point_is_appended_after_the_last_one() {
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.findMaxPosition(draft.getId())).thenReturn(Optional.of(2));
        when(agendaItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AgendaItemView added = new AddAgendaItemService(generalMeetingRepository, agendaItemRepository)
                .add(new AddAgendaItemCommand(draft.getId(), "Travaux", null, MajorityRule.ABSOLUTE));

        assertThat(added.position()).isEqualTo(3);
    }

    @Test
    void the_first_point_of_an_empty_agenda_takes_position_zero() {
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.findMaxPosition(draft.getId())).thenReturn(Optional.empty());
        when(agendaItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AgendaItemView added = new AddAgendaItemService(generalMeetingRepository, agendaItemRepository)
                .add(new AddAgendaItemCommand(draft.getId(), "Comptes", null, MajorityRule.ABSOLUTE));

        assertThat(added.position()).isZero();
    }

    @Test
    void a_point_can_still_be_added_after_the_owners_have_been_convoked() {
        // The agenda is deliberately never frozen for the time being (ADR 0002 §8) - the
        // strict rule (an owner is convoked on the strength of a fixed list) is deferred.
        GeneralMeeting scheduled = draft.schedule(SESSION_DATE, VENUE);
        when(generalMeetingRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));
        when(agendaItemRepository.findMaxPosition(scheduled.getId())).thenReturn(Optional.of(0));
        when(agendaItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AgendaItemView added = new AddAgendaItemService(generalMeetingRepository, agendaItemRepository)
                .add(new AddAgendaItemCommand(scheduled.getId(), "Ajout tardif", null, MajorityRule.SIMPLE));

        assertThat(added.label()).isEqualTo("Ajout tardif");
    }

    @Test
    void reordering_assigns_dense_positions_in_the_order_requested() {
        AgendaItem first = itemAt(0, "Comptes");
        AgendaItem second = itemAt(1, "Travaux");
        AgendaItem third = itemAt(2, "Divers");
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.findByGeneralMeetingId(draft.getId())).thenReturn(List.of(first, second, third));
        when(agendaItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<AgendaItemView> reordered = new ReorderAgendaItemsService(agendaItemRepository, generalMeetingRepository)
                .reorder(new ReorderAgendaItemsCommand(draft.getId(),
                        List.of(third.getId(), first.getId(), second.getId())));

        assertThat(reordered).extracting(AgendaItemView::label).containsExactly("Divers", "Comptes", "Travaux");
        assertThat(reordered).extracting(AgendaItemView::position).containsExactly(0, 1, 2);
    }

    @Test
    void a_partial_reordering_is_refused_rather_than_silently_dropping_points() {
        AgendaItem first = itemAt(0, "Comptes");
        AgendaItem second = itemAt(1, "Travaux");
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.findByGeneralMeetingId(draft.getId())).thenReturn(List.of(first, second));

        ReorderAgendaItemsService service = new ReorderAgendaItemsService(agendaItemRepository, generalMeetingRepository);

        assertThatThrownBy(() -> service.reorder(new ReorderAgendaItemsCommand(draft.getId(), List.of(first.getId()))))
                .isInstanceOf(BusinessException.class);
        verify(agendaItemRepository, never()).saveAll(any());
    }

    @Test
    void a_reordering_listing_the_same_point_twice_is_refused() {
        AgendaItem first = itemAt(0, "Comptes");
        AgendaItem second = itemAt(1, "Travaux");
        when(generalMeetingRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(agendaItemRepository.findByGeneralMeetingId(draft.getId())).thenReturn(List.of(first, second));

        ReorderAgendaItemsService service = new ReorderAgendaItemsService(agendaItemRepository, generalMeetingRepository);

        assertThatThrownBy(() -> service.reorder(new ReorderAgendaItemsCommand(draft.getId(),
                List.of(first.getId(), first.getId())))).isInstanceOf(BusinessException.class);
    }

    @Test
    void deleting_a_point_closes_the_gap_it_leaves() {
        AgendaItem first = itemAt(0, "Comptes");
        AgendaItem third = itemAt(2, "Divers");
        when(agendaItemRepository.findById(third.getId())).thenReturn(Optional.of(third));
        // What the repository returns after the delete: the survivors, still holding positions 0 and 2.
        when(agendaItemRepository.findByGeneralMeetingId(draft.getId()))
                .thenReturn(List.of(first, itemAt(2, "Travaux")));

        new DeleteAgendaItemService(agendaItemRepository)
                .delete(new com.architek.oikos.meeting.application.command.DeleteAgendaItemCommand(third.getId()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AgendaItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(agendaItemRepository).deleteById(third.getId());
        verify(agendaItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(AgendaItem::getPosition).containsExactly(0, 1);
    }
}
