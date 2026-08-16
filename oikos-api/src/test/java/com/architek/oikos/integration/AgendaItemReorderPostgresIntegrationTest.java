package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.DeleteAgendaItemCommand;
import com.architek.oikos.meeting.application.command.ReorderAgendaItemsCommand;
import com.architek.oikos.meeting.application.dto.AgendaItemView;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.DeleteAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.ListAgendaItemsUseCase;
import com.architek.oikos.meeting.application.port.in.ReorderAgendaItemsUseCase;
import com.architek.oikos.meeting.application.query.ListAgendaItemsQuery;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MajorityRule;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Reordering an agenda has to run against a real PostgreSQL to mean anything.
 * uk_agenda_item_position is unique per meeting, and any permutation passes
 * through a duplicate position halfway; only PostgreSQL's DEFERRABLE INITIALLY
 * DEFERRED clause (V4) makes that legal by checking at commit. H2 - which the
 * rest of the suite and the dev profile run on - never creates that constraint
 * at all (see AgendaItemEntity's javadoc for why it is deliberately not
 * declared on the entity), so a plain unit test would pass while production
 * rejected every reordering.
 */
class AgendaItemReorderPostgresIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CreateGeneralMeetingUseCase createGeneralMeetingUseCase;

    @Autowired
    private AddAgendaItemUseCase addAgendaItemUseCase;

    @Autowired
    private ReorderAgendaItemsUseCase reorderAgendaItemsUseCase;

    @Autowired
    private DeleteAgendaItemUseCase deleteAgendaItemUseCase;

    @Autowired
    private ListAgendaItemsUseCase listAgendaItemsUseCase;

    private GeneralMeetingId meetingId;

    @BeforeEach
    void createMeetingWithThreeAgendaItems() {
        EntityId propertyId = EntityId.of(UUID.randomUUID());
        // OffsetDateTime and not Instant: the PostgreSQL driver cannot infer a SQL type for the
        // latter through a plain setObject, which is all JdbcTemplate does with a varargs value.
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        jdbcTemplate.update("insert into property (id, name, address, created_date, last_modified_date, version, "
                        + "dues_calculation_mode) values (?, ?, ?, ?, ?, 0, 'SHARES')",
                propertyId.value(), "Résidence Al Amal", "12 rue des Orangers", now, now);

        meetingId = createGeneralMeetingUseCase.create(
                new CreateGeneralMeetingCommand(propertyId, MeetingType.ORDINARY, "AG ordinaire 2026", null, null));
        addItem("Comptes");
        addItem("Travaux");
        addItem("Divers");
    }

    private AgendaItemId addItem(String label) {
        return addAgendaItemUseCase.add(new AddAgendaItemCommand(meetingId, label, null, MajorityRule.SIMPLE)).id();
    }

    private List<AgendaItemView> currentAgenda() {
        return listAgendaItemsUseCase.listAgendaItems(new ListAgendaItemsQuery(meetingId));
    }

    @Test
    void a_permutation_passing_through_a_duplicate_position_commits() {
        List<AgendaItemView> before = currentAgenda();
        assertThat(before).extracting(AgendaItemView::label).containsExactly("Comptes", "Travaux", "Divers");

        // Reversal: every single item changes position, and the first UPDATE alone already
        // collides with a position still held by another row.
        reorderAgendaItemsUseCase.reorder(new ReorderAgendaItemsCommand(meetingId,
                List.of(before.get(2).id(), before.get(1).id(), before.get(0).id())));

        assertThat(currentAgenda()).extracting(AgendaItemView::label)
                .containsExactly("Divers", "Travaux", "Comptes");
        assertThat(currentAgenda()).extracting(AgendaItemView::position).containsExactly(0, 1, 2);
    }

    @Test
    void swapping_two_neighbours_commits() {
        List<AgendaItemView> before = currentAgenda();

        reorderAgendaItemsUseCase.reorder(new ReorderAgendaItemsCommand(meetingId,
                List.of(before.get(1).id(), before.get(0).id(), before.get(2).id())));

        assertThat(currentAgenda()).extracting(AgendaItemView::label)
                .containsExactly("Travaux", "Comptes", "Divers");
    }

    @Test
    void deleting_a_point_renumbers_the_survivors_without_colliding() {
        List<AgendaItemView> before = currentAgenda();

        deleteAgendaItemUseCase.delete(new DeleteAgendaItemCommand(before.get(0).id()));

        assertThat(currentAgenda()).extracting(AgendaItemView::label).containsExactly("Travaux", "Divers");
        assertThat(currentAgenda()).extracting(AgendaItemView::position).containsExactly(0, 1);
    }
}
