package com.architek.oikos.property.application.command;

import java.util.List;

public record ConfigurePropertyCommand(String name, String address, List<BuildingConfiguration> buildings) {
}
