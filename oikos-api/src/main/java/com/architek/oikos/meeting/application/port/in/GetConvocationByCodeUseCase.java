package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.query.GetConvocationByCodeQuery;

/** Anonymous read behind the pair of short codes printed on the letter. */
public interface GetConvocationByCodeUseCase {

    ConvocationConfirmationView getByCode(GetConvocationByCodeQuery query);
}
