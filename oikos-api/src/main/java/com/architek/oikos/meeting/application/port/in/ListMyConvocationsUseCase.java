package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.dto.MyConvocationView;
import com.architek.oikos.meeting.application.query.ListMyConvocationsQuery;

public interface ListMyConvocationsUseCase {

    List<MyConvocationView> listMyConvocations(ListMyConvocationsQuery query);
}
