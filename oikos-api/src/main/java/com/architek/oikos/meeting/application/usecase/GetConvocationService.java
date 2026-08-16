package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;

@Component
public class GetConvocationService implements GetConvocationUseCase {

    private final ConvocationLookup lookup;

    public GetConvocationService(ConvocationLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocationView getConvocation(GetConvocationQuery query) {
        return lookup.toView(lookup.require(query.id()));
    }
}
