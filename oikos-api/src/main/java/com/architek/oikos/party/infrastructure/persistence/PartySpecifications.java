package com.architek.oikos.party.infrastructure.persistence;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.architek.oikos.party.domain.valueobject.PartySearchCriteria;

public final class PartySpecifications {

    private PartySpecifications() {
    }

    public static Specification<PartyEntity> matching(PartySearchCriteria criteria) {
        Specification<PartyEntity> spec = hasPropertyId(criteria.propertyId().value());
        if (criteria.search() != null && !criteria.search().isBlank()) {
            spec = spec.and(searchText(criteria.search()));
        }
        return spec;
    }

    private static Specification<PartyEntity> hasPropertyId(UUID propertyId) {
        return (root, query, cb) -> cb.equal(root.get("propertyId"), propertyId);
    }

    /**
     * Le téléphone entre dans la recherche libre au même titre que le nom et
     * l'email : c'est souvent la seule coordonnée qu'un syndic a sous la main, et
     * l'écran de rattachement d'un lot s'en sert pour reconnaître un contact déjà
     * enregistré avant d'en créer un doublon.
     *
     * <p>La comparaison porte sur la valeur stockée, au format international.
     * Chercher « 0612345678 » ne trouve donc pas « +212612345678 » - c'est le
     * formulaire qui compose le numéro avant de chercher.
     */
    private static Specification<PartyEntity> searchText(String search) {
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern));
    }
}
