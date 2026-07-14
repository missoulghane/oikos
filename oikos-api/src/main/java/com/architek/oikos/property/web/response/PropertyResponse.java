package com.architek.oikos.property.web.response;

import com.architek.oikos.property.application.dto.PropertyView;

public record PropertyResponse(String id, String name, String address) {

    public static PropertyResponse from(PropertyView view) {
        return new PropertyResponse(view.id().toString(), view.name(), view.address());
    }
}
