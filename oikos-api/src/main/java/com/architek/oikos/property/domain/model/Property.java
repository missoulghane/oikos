package com.architek.oikos.property.domain.model;

import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.ProjectedBudget;
import com.architek.oikos.property.domain.valueobject.PropertyId;

/**
 * Entite racine representant la property geree. Une property peut exister
 * sans building (ex: inscription d'un property manager, voir
 * PropertyProvisioningAdapter) ; des buildings peuvent lui etre rattaches
 * plus tard via AddBuildingUseCase. Le endpoint public POST /properties
 * continue neanmoins d'exiger un premier building a la creation (voir
 * CreatePropertyRequest), par choix de ce point d'entree specifique et non
 * par contrainte de cet agregat.
 * duesCalculationMode/projectedBudget pilotent le calcul des appels a
 * cotisation (voir installment.GenerateInstallmentCallUseCase): FLAT_RATE
 * (defaut) facture le prix du type de lot, SHARES repartit projectedBudget
 * au prorata des tantiemes. projectedBudget reste null tant qu'il n'a pas
 * ete configure - non requis en mode FLAT_RATE, requis pour generer un appel
 * en mode SHARES.
 * Immutable: toute mutation retourne une nouvelle instance. Semantique
 * d'entite: equals/hashCode se basent sur l'identite (id), pas sur les valeurs.
 */
public final class Property {

    private final PropertyId id;
    private final String name;
    private final String address;
    private final String city;
    private final DuesCalculationMode duesCalculationMode;
    private final ProjectedBudget projectedBudget;

    private Property(PropertyId id, String name, String address, String city,
                      DuesCalculationMode duesCalculationMode, ProjectedBudget projectedBudget) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = requireNonBlank(name, "name");
        this.address = requireNonBlank(address, "address");
        this.city = blankToNull(city);
        this.duesCalculationMode = Objects.requireNonNull(duesCalculationMode, "duesCalculationMode must not be null");
        this.projectedBudget = projectedBudget;
    }

    /**
     * Sans ville : c'est l'etat de toutes les coproprietes creees avant que le
     * champ existe, et celui d'une copropriete provisionnee sans qu'on l'ait
     * demandee. La ville reste donc facultative, la ou l'adresse ne l'est pas.
     */
    public static Property create(PropertyId id, String name, String address) {
        return create(id, name, address, null);
    }

    public static Property create(PropertyId id, String name, String address, String city) {
        return new Property(id, name, address, city, DuesCalculationMode.FLAT_RATE, null);
    }

    public static Property reconstruct(PropertyId id, String name, String address, String city,
                                        DuesCalculationMode duesCalculationMode, ProjectedBudget projectedBudget) {
        return new Property(id, name, address, city, duesCalculationMode, projectedBudget);
    }

    public Property withDetails(String newName, String newAddress, String newCity) {
        return new Property(id, newName, newAddress, newCity, duesCalculationMode, projectedBudget);
    }

    public Property withDuesCalculationMode(DuesCalculationMode newMode) {
        return new Property(id, name, address, city, newMode, projectedBudget);
    }

    public Property withProjectedBudget(ProjectedBudget newProjectedBudget) {
        return new Property(id, name, address, city, duesCalculationMode, newProjectedBudget);
    }

    /** Une ville vide et une ville absente sont la meme chose : ne rien savoir. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public PropertyId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Optional<String> getCity() {
        return Optional.ofNullable(city);
    }

    public DuesCalculationMode getDuesCalculationMode() {
        return duesCalculationMode;
    }

    public Optional<ProjectedBudget> getProjectedBudget() {
        return Optional.ofNullable(projectedBudget);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Property other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
