package com.architek.oikos.invitation.application.query;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestSortField;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * {@code search} porte sur le nom et l'adresse email du demandeur ainsi que
 * sur le numéro de lot ; nul ou vide, il ne filtre rien. {@code status} nul
 * laisse passer tous les statuts.
 */
public record ListMembershipRequestsQuery(EntityId propertyId, String search, MembershipRequestOverviewStatus status,
                                           MembershipRequestSortField sortBy, SortDirection sortDirection,
                                           PageRequest pageRequest) {
}
