package com.architek.oikos.property.application.command;

public record CreatePropertyCommand(String name, String address, String firstBuildingName,
                                        Integer firstBuildingFloorCount) {
}
