package com.architek.oikos.meeting.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.meeting.application.port.in.ListConvocationChannelsUseCase;
import com.architek.oikos.meeting.web.response.ConvocationChannelResponse;

/**
 * The channel catalog, read-only.
 *
 * <p>No @PreAuthorize beyond the authentication every endpoint requires: this
 * is a reference list of the product's own, carrying no copropriété's data and
 * naming nobody. Gating it per property would mean a permission check on a list
 * of five constants.
 *
 * <p>Read-only on purpose. Adding a channel is an INSERT precisely so that it
 * stays a deliberate act - an automated one needs an emitter written for it,
 * and a screen that could create one would let anyone promise a send that never
 * happens.
 */
@RestController
public class ConvocationChannelController {

    private final ListConvocationChannelsUseCase listConvocationChannelsUseCase;

    public ConvocationChannelController(ListConvocationChannelsUseCase listConvocationChannelsUseCase) {
        this.listConvocationChannelsUseCase = listConvocationChannelsUseCase;
    }

    @GetMapping("/convocation-channels")
    public List<ConvocationChannelResponse> list() {
        return listConvocationChannelsUseCase.listChannels().stream().map(ConvocationChannelResponse::from).toList();
    }
}
