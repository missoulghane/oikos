package com.architek.oikos.invitation.application.usecase;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;
import com.architek.oikos.invitation.application.dto.MembershipRequestSortField;
import com.architek.oikos.invitation.application.port.in.ListMembershipRequestsUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.application.query.ListMembershipRequestsQuery;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Le tableau « Demandes d'adhésion » du syndic : uniquement de vraies demandes
 * (les invitations privées encore en attente ne sont plus fondues dedans, voir
 * MembershipRequestOverviewStatus), résolues côté serveur, triées par défaut de
 * la plus récente à la plus ancienne.
 *
 * <p>Chargement complet puis filtre, tri et pagination en mémoire : c'est déjà
 * ce que faisait cette classe pour fusionner deux sources, et c'est ce que
 * réclame un tri sur des colonnes qui n'existent pas en base (le nom du
 * demandeur vit dans user, le numéro de lot dans property). Le volume attendu
 * par copropriété se compte en dizaines, pas en milliers - à revoir seulement
 * si cela cesse d'être vrai.
 *
 * <p>Les identités demandeur/décideur sont mises en cache par requête : deux
 * demandes du même candidat, ou dix tranchées par le même gestionnaire, ne
 * valent qu'un appel chacune.
 */
@Component
public class ListMembershipRequestsService implements ListMembershipRequestsUseCase {

    private final MembershipRequestRepository membershipRequestRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;

    public ListMembershipRequestsService(MembershipRequestRepository membershipRequestRepository,
                                          UnitDirectoryPort unitDirectoryPort, AccountDirectoryPort accountDirectoryPort) {
        this.membershipRequestRepository = membershipRequestRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipRequestOverviewView> listMembershipRequests(ListMembershipRequestsQuery query) {
        Map<EntityId, AccountInfo> accountCache = new HashMap<>();
        Map<EntityId, Optional<UnitBasicInfo>> unitCache = new HashMap<>();

        List<MembershipRequestOverviewView> views = membershipRequestRepository.findAllByPropertyId(query.propertyId())
                .stream()
                .map(request -> toView(request, accountCache, unitCache))
                .filter(view -> matchesStatus(view, query.status()))
                .filter(view -> matchesSearch(view, query.search()))
                .sorted(comparator(query.sortBy(), query.sortDirection()))
                .toList();

        return paginate(views, query.pageRequest());
    }

    private MembershipRequestOverviewView toView(MembershipRequest request, Map<EntityId, AccountInfo> accountCache,
                                                  Map<EntityId, Optional<UnitBasicInfo>> unitCache) {
        AccountInfo requester = account(request.getUserId(), accountCache);
        Optional<UnitBasicInfo> unit = unitCache.computeIfAbsent(request.getUnitId(), unitDirectoryPort::findBasicInfo);
        AccountInfo decidedBy = request.getDecidedByUserId() == null ? null
                : account(request.getDecidedByUserId(), accountCache);

        return MembershipRequestOverviewView.of(request,
                requester == null ? null : requester.fullName(),
                requester == null ? null : requester.email().value(),
                requester != null && requester.verified(),
                unit.map(UnitBasicInfo::unitNumber).orElse(null),
                unit.map(UnitBasicInfo::unitTypeName).orElse(null),
                decidedBy == null ? null : decidedBy.fullName());
    }

    /**
     * Un compte supprimé entre-temps ne doit pas faire disparaître la demande
     * de la liste ni casser la page : la ligne s'affiche sans identité plutôt
     * que de propager une UserNotFoundException. La demande, elle, reste un
     * fait dont le syndic a besoin.
     */
    private AccountInfo account(EntityId userId, Map<EntityId, AccountInfo> cache) {
        return cache.computeIfAbsent(userId, id -> {
            try {
                return accountDirectoryPort.getAccountInfo(id);
            } catch (RuntimeException e) {
                return null;
            }
        });
    }

    private static boolean matchesStatus(MembershipRequestOverviewView view, MembershipRequestOverviewStatus status) {
        return status == null || view.status() == status;
    }

    private static boolean matchesSearch(MembershipRequestOverviewView view, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = fold(search);
        return fold(view.requesterFullName()).contains(needle)
                || fold(view.requesterEmail()).contains(needle)
                || fold(view.unitNumber()).contains(needle);
    }

    /**
     * Recherche insensible à la casse et aux accents : « Benali » doit trouver
     * « Bénali », faute de quoi le champ paraît cassé à qui tape sans accent.
     */
    private static String fold(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static Comparator<MembershipRequestOverviewView> comparator(MembershipRequestSortField sortBy,
                                                                         SortDirection direction) {
        Comparator<MembershipRequestOverviewView> comparator = switch (sortBy == null ? MembershipRequestSortField.SUBMITTED_AT : sortBy) {
            case REQUESTER -> Comparator.comparing(view -> fold(view.requesterFullName()));
            case UNIT -> Comparator.comparing(view -> fold(view.unitNumber()));
            case STATUS -> Comparator.comparing(MembershipRequestOverviewView::status);
            // submittedAt est nul tant que JPA n'a pas horodaté la ligne, ce qui
            // n'arrive qu'à une demande écrite dans la transaction en cours :
            // nullsLast en ASC, donc en tête une fois inversé - où le lecteur
            // s'attend précisément à trouver la plus récente.
            case SUBMITTED_AT -> Comparator.comparing(MembershipRequestOverviewView::submittedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
        // Comparateurs naturellement croissants ; c'est la direction qui décide.
        // Le défaut de l'écran (SUBMITTED_AT, DESC - voir MembershipRequestController)
        // donne donc bien la demande la plus récente en premier.
        return direction == SortDirection.DESC ? comparator.reversed() : comparator;
    }

    private static Page<MembershipRequestOverviewView> paginate(List<MembershipRequestOverviewView> views, PageRequest pageRequest) {
        int fromIndex = Math.min(pageRequest.pageNumber() * pageRequest.pageSize(), views.size());
        int toIndex = Math.min(fromIndex + pageRequest.pageSize(), views.size());
        return Page.of(views.subList(fromIndex, toIndex), pageRequest.pageNumber(), pageRequest.pageSize(), views.size());
    }
}
