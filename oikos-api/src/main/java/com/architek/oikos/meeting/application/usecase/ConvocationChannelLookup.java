package com.architek.oikos.meeting.application.usecase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.domain.exception.ConvocationChannelNotFoundException;
import com.architek.oikos.meeting.domain.exception.ConvocationNotSendableException;
import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.repository.ConvocationChannelRepository;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;

/**
 * Resolves channel codes against the catalog, for the four services that all
 * need the same two answers: does this code exist, and may the application
 * send by it.
 *
 * <p>The second question has an answer the catalog alone cannot give. A row
 * flagged automated only means somebody intended it to be automatic; whether
 * an emitter exists for it is a fact about the code, and it lives in
 * {@link #EMITTED_CODES}. Since a channel is now an INSERT away, that list is
 * what stands between "we added SMS to the catalog" and convocations silently
 * going nowhere.
 */
@Component
public class ConvocationChannelLookup {

    /**
     * The codes SendConvocationService actually implements. Adding a row to the
     * catalog does not add anything here - writing the emitter does.
     */
    private static final List<ChannelCode> EMITTED_CODES = List.of(ChannelCode.EMAIL, ChannelCode.APP);

    private final ConvocationChannelRepository channelRepository;

    public ConvocationChannelLookup(ConvocationChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public ConvocationChannel require(ChannelCode code) {
        return channelRepository.findByCode(code).orElseThrow(() -> new ConvocationChannelNotFoundException(code));
    }

    /**
     * The channel to send by, or an explanation of why the application will not
     * do it. Refuses a manual channel outright, and refuses an automated one it
     * has no emitter for rather than pretend the convocation went out.
     */
    public ConvocationChannel requireSendable(ChannelCode code) {
        ConvocationChannel channel = require(code);
        if (!channel.isAutomated()) {
            throw new ConvocationNotSendableException(code);
        }
        if (!EMITTED_CODES.contains(channel.getCode())) {
            throw ConvocationNotSendableException.noEmitter(code);
        }
        return channel;
    }

    /**
     * The whole catalog by code, for the read paths that decorate deliveries
     * with their label. Deactivated rows included: a delivery recorded before a
     * channel was retired still has to display as something other than a code.
     */
    public Map<ChannelCode, ConvocationChannel> byCode() {
        Map<ChannelCode, ConvocationChannel> byCode = new LinkedHashMap<>();
        for (ConvocationChannel channel : channelRepository.findAll()) {
            byCode.put(channel.getCode(), channel);
        }
        return byCode;
    }
}
