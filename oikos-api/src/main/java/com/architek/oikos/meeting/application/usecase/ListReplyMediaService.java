package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.port.in.ListReplyMediaUseCase;
import com.architek.oikos.meeting.domain.model.ReplyMedium;
import com.architek.oikos.meeting.domain.repository.ReplyMediumRepository;

/**
 * Active rows only, in display order. A deactivated medium still resolves by
 * code - answers recorded years ago reference it - but it is not offered for a
 * new one.
 */
@Component
public class ListReplyMediaService implements ListReplyMediaUseCase {

    private final ReplyMediumRepository replyMediumRepository;

    public ListReplyMediaService(ReplyMediumRepository replyMediumRepository) {
        this.replyMediumRepository = replyMediumRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReplyMedium> listMedia() {
        return replyMediumRepository.findAllActive();
    }
}
