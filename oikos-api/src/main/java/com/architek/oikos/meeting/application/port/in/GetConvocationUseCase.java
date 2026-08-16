package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;

/** Also how PropertyAccessEvaluator resolves a convocation back to its lot and its property. */
public interface GetConvocationUseCase {

    ConvocationView getConvocation(GetConvocationQuery query);
}
