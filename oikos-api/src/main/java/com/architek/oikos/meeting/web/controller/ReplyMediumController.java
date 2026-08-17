package com.architek.oikos.meeting.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.meeting.application.port.in.ListReplyMediaUseCase;
import com.architek.oikos.meeting.web.response.ReplyMediumResponse;

/**
 * The catalog of means by which an answer reaches the office, read-only - same
 * stance as ConvocationChannelController, and for the same reasons: a reference
 * list of the product's own, carrying no copropriété's data and naming nobody,
 * so no @PreAuthorize beyond the authentication every endpoint requires.
 */
@RestController
public class ReplyMediumController {

    private final ListReplyMediaUseCase listReplyMediaUseCase;

    public ReplyMediumController(ListReplyMediaUseCase listReplyMediaUseCase) {
        this.listReplyMediaUseCase = listReplyMediaUseCase;
    }

    @GetMapping("/reply-media")
    public List<ReplyMediumResponse> list() {
        return listReplyMediaUseCase.listMedia().stream().map(ReplyMediumResponse::from).toList();
    }
}
