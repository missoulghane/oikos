package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * RG : l'etage d'un lot se situe dans l'immeuble qui le contient - de 0 (rez-de-chaussee)
 * au nombre d'etages declare sur le Building. Un lot au 5e dans un immeuble qui en compte
 * 3 est une faute de saisie, pas une donnee a corriger plus tard.
 */
public class UnitFloorOutOfBuildingRangeException extends BusinessException {

    public UnitFloorOutOfBuildingRangeException(int floor, int buildingFloorCount) {
        super("Floor " + floor + " is outside this building, which has " + buildingFloorCount
                + " floor(s) above the ground floor");
    }
}
