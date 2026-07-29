package com.architek.oikos.auth.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.property.application.port.in.GetBuildingUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.query.GetBuildingQuery;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * SpEL-callable authorization bean (referenced from @PreAuthorize expressions
 * as {@code @propertyAccess.xxx(...)}). Resolves access per request from the
 * database (no property-scoped claims embedded in the JWT - see
 * GetUserAccessUseCase), so a newly granted/revoked access takes effect
 * immediately without requiring the caller to re-authenticate. Every
 * cross-feature lookup goes through a port-in use case (rule 6), never a
 * repository directly.
 */
@Component("propertyAccess")
public class PropertyAccessEvaluator {

    private final GetUserAccessUseCase getUserAccessUseCase;
    private final GetUnitUseCase getUnitUseCase;
    private final GetBuildingUseCase getBuildingUseCase;
    private final GetInstallmentUseCase getInstallmentUseCase;
    private final GetInstallmentCallUseCase getInstallmentCallUseCase;
    private final ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;
    private final GetPartyUseCase getPartyUseCase;

    public PropertyAccessEvaluator(GetUserAccessUseCase getUserAccessUseCase,
                                    GetUnitUseCase getUnitUseCase,
                                    GetBuildingUseCase getBuildingUseCase,
                                    GetInstallmentUseCase getInstallmentUseCase,
                                    GetInstallmentCallUseCase getInstallmentCallUseCase,
                                    ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase,
                                    GetPartyUseCase getPartyUseCase) {
        this.getUserAccessUseCase = getUserAccessUseCase;
        this.getUnitUseCase = getUnitUseCase;
        this.getBuildingUseCase = getBuildingUseCase;
        this.getInstallmentUseCase = getInstallmentUseCase;
        this.getInstallmentCallUseCase = getInstallmentCallUseCase;
        this.listUnitOwnershipsByUnitUseCase = listUnitOwnershipsByUnitUseCase;
        this.getPartyUseCase = getPartyUseCase;
    }

    /** ADMIN is a global, JWT-embedded authority (same trust boundary as the existing
     * hasAuthority('ROLE_ADMIN') checks elsewhere) - checked directly off the token so
     * admins short-circuit every rule below without a DB round-trip. */
    public boolean isAdmin(Authentication authentication) {
        return isAdminAuthority(authentication);
    }

    public boolean managesProperty(Authentication authentication, String propertyId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesBuilding(Authentication authentication, String buildingId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getBuildingUseCase.getBuilding(new GetBuildingQuery(BuildingId.of(buildingId)))
                .propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesUnit(Authentication authentication, String unitId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getUnitUseCase.getUnit(new GetUnitQuery(UnitId.of(unitId))).propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesInstallment(Authentication authentication, String installmentId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String unitId = getInstallmentUseCase.getInstallment(new GetInstallmentQuery(InstallmentId.of(installmentId)))
                .unitId().toString();
        return managesUnit(authentication, unitId) || ownsUnit(authentication, unitId);
    }

    public boolean managesInstallmentCall(Authentication authentication, String installmentCallId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getInstallmentCallUseCase
                .getInstallmentCall(new GetInstallmentCallQuery(InstallmentCallId.of(installmentCallId)))
                .installmentCall().propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    public boolean managesParty(Authentication authentication, String partyId) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        String propertyId = getPartyUseCase.getParty(new GetPartyQuery(PartyId.of(partyId))).propertyId().toString();
        return access(authentication).managesProperty(propertyId);
    }

    /** True for ADMIN, or for an account already managing at least one property. Used to
     * gate the authenticated "create a property" endpoint: a plain owner-only USER with
     * no property grant at all must not be able to create additional properties this way
     * (the public self-registration bootstrap flow is a separate, unrelated code path). */
    public boolean isManagerOfAny(Authentication authentication) {
        if (isAdminAuthority(authentication)) {
            return true;
        }
        return !access(authentication).managedPropertyIds().isEmpty();
    }

    /** Self-service: the current account's own linked party. */
    public boolean ownsParty(Authentication authentication, String partyId) {
        return access(authentication).ownedPartyIds().contains(partyId);
    }

    /** Read-only access for the owner of the unit, in addition to admin/manager. */
    public boolean ownsUnit(Authentication authentication, String unitId) {
        UserAccessView access = access(authentication);
        if (access.ownedPartyIds().isEmpty()) {
            return false;
        }
        return listUnitOwnershipsByUnitUseCase.listUnitOwnerships(new ListUnitOwnershipsByUnitQuery(UnitId.of(unitId)))
                .stream()
                .anyMatch(ownership -> access.ownedPartyIds().contains(ownership.partyId().toString()));
    }

    private UserAccessView access(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return getUserAccessUseCase.getAccess(new GetUserAccessQuery(UserId.of(principal.getUserId())));
    }

    private static boolean isAdminAuthority(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_MASTER"));
    }
}
