package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;

public record UpdatePropertyCommand(PropertyId id, String name, String address, String city) {
}
