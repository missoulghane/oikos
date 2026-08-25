package com.architek.oikos.invitation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.invitation.application.dto.MembershipRequestSortField;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListMembershipRequestsServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();

    @Mock
    private MembershipRequestRepository membershipRequestRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    @Mock
    private AccountDirectoryPort accountDirectoryPort;

    private ListMembershipRequestsService newService() {
        return new ListMembershipRequestsService(membershipRequestRepository, unitDirectoryPort, accountDirectoryPort);
    }

    private static ListMembershipRequestsQuery query(String search, MembershipRequestOverviewStatus status,
                                                      MembershipRequestSortField sortBy, SortDirection direction) {
        return new ListMembershipRequestsQuery(PROPERTY_ID, search, status, sortBy, direction, PageRequest.of(0, 20));
    }

    private static ListMembershipRequestsQuery defaultQuery() {
        return query(null, null, MembershipRequestSortField.SUBMITTED_AT, SortDirection.DESC);
    }

    private MembershipRequest request(EntityId userId, EntityId unitId, MembershipRequestStatus status, Instant submittedAt) {
        return MembershipRequest.reconstruct(MembershipRequestId.newId(), EntityId.newId(), PROPERTY_ID, unitId,
                EntityId.newId(), userId, status, null, null, null, submittedAt);
    }

    private void account(EntityId userId, String fullName, String email, boolean verified) {
        when(accountDirectoryPort.getAccountInfo(userId))
                .thenReturn(new AccountInfo(EmailVO.of(email), fullName, null, verified));
    }

    private void unit(EntityId unitId, String unitNumber) {
        when(unitDirectoryPort.findBasicInfo(unitId))
                .thenReturn(Optional.of(new UnitBasicInfo(PROPERTY_ID, unitNumber, "Appartement", false)));
    }

    /**
     * Le tableau du syndic affichait des identifiants bruts et allait chercher
     * chaque contact et chaque lot depuis le navigateur : le nom, l'adresse et
     * le numéro de lot arrivent désormais résolus avec la ligne.
     */
    @Test
    void a_request_is_returned_with_its_requester_and_its_unit_already_resolved() {
        EntityId userId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        MembershipRequest pending = request(userId, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH);
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(pending));
        account(userId, "Jane Doe", "jane.doe@example.com", true);
        unit(unitId, "A-12");

        Page<MembershipRequestOverviewView> result = newService().listMembershipRequests(defaultQuery());

        assertThat(result.content()).hasSize(1);
        MembershipRequestOverviewView view = result.content().get(0);
        assertThat(view.status()).isEqualTo(MembershipRequestOverviewStatus.PENDING);
        assertThat(view.requesterFullName()).isEqualTo("Jane Doe");
        assertThat(view.requesterEmail()).isEqualTo("jane.doe@example.com");
        assertThat(view.requesterAccountVerified()).isTrue();
        assertThat(view.unitNumber()).isEqualTo("A-12");
        assertThat(view.unitTypeName()).isEqualTo("Appartement");
        assertThat(view.submittedAt()).isEqualTo(Instant.EPOCH);
    }

    /** Une file d'attente se lit par le haut : la demande la plus récente d'abord. */
    @Test
    void the_default_order_is_the_most_recent_request_first() {
        EntityId olderUser = EntityId.newId();
        EntityId newerUser = EntityId.newId();
        EntityId unitId = EntityId.newId();
        MembershipRequest older = request(olderUser, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH);
        MembershipRequest newer = request(newerUser, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH.plusSeconds(3600));
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(older, newer));
        account(olderUser, "Ancien Candidat", "ancien@example.com", true);
        account(newerUser, "Nouveau Candidat", "nouveau@example.com", true);
        unit(unitId, "A-12");

        Page<MembershipRequestOverviewView> result = newService().listMembershipRequests(defaultQuery());

        assertThat(result.content()).extracting(MembershipRequestOverviewView::requesterFullName)
                .containsExactly("Nouveau Candidat", "Ancien Candidat");
    }

    @Test
    void the_status_filter_keeps_only_the_matching_requests() {
        EntityId pendingUser = EntityId.newId();
        EntityId acceptedUser = EntityId.newId();
        EntityId unitId = EntityId.newId();
        MembershipRequest pending = request(pendingUser, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH);
        MembershipRequest accepted = request(acceptedUser, unitId, MembershipRequestStatus.ACCEPTED, Instant.EPOCH);
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(pending, accepted));
        account(pendingUser, "En Attente", "attente@example.com", true);
        account(acceptedUser, "Validé", "valide@example.com", true);
        unit(unitId, "A-12");

        Page<MembershipRequestOverviewView> result = newService().listMembershipRequests(
                query(null, MembershipRequestOverviewStatus.PENDING, MembershipRequestSortField.SUBMITTED_AT, SortDirection.DESC));

        assertThat(result.content()).extracting(MembershipRequestOverviewView::requesterFullName)
                .containsExactly("En Attente");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    /** « Benali » doit trouver « Bénali », faute de quoi le champ paraît cassé à qui tape sans accent. */
    @Test
    void the_search_ignores_case_and_accents_and_covers_email_and_unit_number() {
        EntityId userId = EntityId.newId();
        EntityId otherUserId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        EntityId otherUnitId = EntityId.newId();
        MembershipRequest matching = request(userId, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH);
        MembershipRequest other = request(otherUserId, otherUnitId, MembershipRequestStatus.PENDING, Instant.EPOCH);
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(matching, other));
        account(userId, "Amine Bénali", "amine@example.com", true);
        account(otherUserId, "Sofia Alami", "sofia@example.com", true);
        unit(unitId, "A-12");
        unit(otherUnitId, "B-03");

        assertThat(newService().listMembershipRequests(
                query("benali", null, MembershipRequestSortField.SUBMITTED_AT, SortDirection.DESC)).content())
                .extracting(MembershipRequestOverviewView::requesterFullName).containsExactly("Amine Bénali");
        assertThat(newService().listMembershipRequests(
                query("SOFIA@EXAMPLE.COM", null, MembershipRequestSortField.SUBMITTED_AT, SortDirection.DESC)).content())
                .extracting(MembershipRequestOverviewView::requesterFullName).containsExactly("Sofia Alami");
        assertThat(newService().listMembershipRequests(
                query("b-03", null, MembershipRequestSortField.SUBMITTED_AT, SortDirection.DESC)).content())
                .extracting(MembershipRequestOverviewView::unitNumber).containsExactly("B-03");
    }

    @Test
    void sorting_by_requester_orders_the_names_alphabetically() {
        EntityId first = EntityId.newId();
        EntityId second = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(
                request(second, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH),
                request(first, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH)));
        account(first, "Amine Bénali", "amine@example.com", true);
        account(second, "Sofia Alami", "sofia@example.com", true);
        unit(unitId, "A-12");

        assertThat(newService().listMembershipRequests(
                query(null, null, MembershipRequestSortField.REQUESTER, SortDirection.ASC)).content())
                .extracting(MembershipRequestOverviewView::requesterFullName)
                .containsExactly("Amine Bénali", "Sofia Alami");
    }

    /**
     * Un compte supprimé entre-temps ne doit pas faire disparaître la demande
     * ni casser la page : elle reste un fait dont le syndic a besoin.
     */
    @Test
    void a_request_whose_account_no_longer_exists_is_still_listed_without_an_identity() {
        EntityId userId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID))
                .thenReturn(List.of(request(userId, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH)));
        when(accountDirectoryPort.getAccountInfo(userId)).thenThrow(new IllegalStateException("user is gone"));
        unit(unitId, "A-12");

        Page<MembershipRequestOverviewView> result = newService().listMembershipRequests(defaultQuery());

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).requesterFullName()).isNull();
        assertThat(result.content().get(0).requesterAccountVerified()).isFalse();
        assertThat(result.content().get(0).unitNumber()).isEqualTo("A-12");
    }

    /** Dix demandes du même candidat ne valent qu'une résolution d'identité. */
    @Test
    void the_same_requester_across_several_requests_is_resolved_once() {
        EntityId userId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(
                request(userId, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH),
                request(userId, unitId, MembershipRequestStatus.REJECTED, Instant.EPOCH)));
        account(userId, "Jane Doe", "jane.doe@example.com", true);
        unit(unitId, "A-12");

        newService().listMembershipRequests(defaultQuery());

        verify(accountDirectoryPort, times(1)).getAccountInfo(userId);
        verify(unitDirectoryPort, times(1)).findBasicInfo(any());
    }

    @Test
    void pagination_applies_to_the_filtered_and_sorted_list() {
        EntityId olderUser = EntityId.newId();
        EntityId newerUser = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(membershipRequestRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(
                request(olderUser, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH),
                request(newerUser, unitId, MembershipRequestStatus.PENDING, Instant.EPOCH.plusSeconds(3600))));
        account(olderUser, "Ancien Candidat", "ancien@example.com", true);
        account(newerUser, "Nouveau Candidat", "nouveau@example.com", true);
        unit(unitId, "A-12");

        Page<MembershipRequestOverviewView> firstPage = newService().listMembershipRequests(
                new ListMembershipRequestsQuery(PROPERTY_ID, null, null, MembershipRequestSortField.SUBMITTED_AT,
                        SortDirection.DESC, PageRequest.of(0, 1)));
        Page<MembershipRequestOverviewView> secondPage = newService().listMembershipRequests(
                new ListMembershipRequestsQuery(PROPERTY_ID, null, null, MembershipRequestSortField.SUBMITTED_AT,
                        SortDirection.DESC, PageRequest.of(1, 1)));

        assertThat(firstPage.totalElements()).isEqualTo(2);
        assertThat(firstPage.content()).extracting(MembershipRequestOverviewView::requesterFullName)
                .containsExactly("Nouveau Candidat");
        assertThat(secondPage.content()).extracting(MembershipRequestOverviewView::requesterFullName)
                .containsExactly("Ancien Candidat");
    }
}
