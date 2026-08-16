package com.architek.oikos.meeting.application.port.in;

import java.util.List;

import com.architek.oikos.meeting.application.dto.VoteView;
import com.architek.oikos.meeting.application.query.ListVotesByAgendaItemQuery;

public interface ListVotesByAgendaItemUseCase {

    List<VoteView> listVotes(ListVotesByAgendaItemQuery query);
}
