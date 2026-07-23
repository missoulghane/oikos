package com.architek.oikos.property.application.command;

import java.util.List;

public record BuildingConfiguration(String name, Integer floorCount, List<UnitTypeConfiguration> unitTypes) {
}
