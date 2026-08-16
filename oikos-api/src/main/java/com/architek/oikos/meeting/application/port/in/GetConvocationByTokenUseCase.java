package com.architek.oikos.meeting.application.port.in;

import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;

/** Anonymous read behind a confirmation link. */
public interface GetConvocationByTokenUseCase {

    ConvocationConfirmationView getByToken(GetConvocationByTokenQuery query);
}
