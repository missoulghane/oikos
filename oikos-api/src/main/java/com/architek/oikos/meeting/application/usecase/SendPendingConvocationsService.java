package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.command.SendConvocationCommand;
import com.architek.oikos.meeting.application.command.SendPendingConvocationsCommand;
import com.architek.oikos.meeting.application.port.in.SendConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.DeliveryStatus;

/**
 * Sends in bulk what is still waiting to go out - the counterpart of
 * generating, and a separate act from it: a syndic generates the convocations,
 * looks at the tracking table, and only then sends.
 *
 * <p>"Pending" means no successful delivery yet, on any channel - so a lot whose
 * email bounced is retried and one already reached by post is not. Retrying a
 * failure is the point: the previous attempt stays in the record either way,
 * since deliveries are appended and never replaced.
 *
 * <p>Deliberately NOT transactional at this level. Each convocation is sent by
 * SendConvocationUseCase in its own REQUIRES_NEW transaction, so one that
 * cannot be delivered is recorded as FAILED and the ninety-nine others still
 * go out - a single unreachable lot must not undo the whole run. That is also
 * why the failures are counted and returned rather than thrown: the syndic
 * needs the tracking table, not a stack trace.
 */
@Component
public class SendPendingConvocationsService implements SendPendingConvocationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendPendingConvocationsService.class);

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final SendConvocationUseCase sendConvocationUseCase;
    private final ConvocationChannelLookup channelLookup;

    public SendPendingConvocationsService(GeneralMeetingRepository generalMeetingRepository,
                                           ConvocationRepository convocationRepository,
                                           SendConvocationUseCase sendConvocationUseCase,
                                           ConvocationChannelLookup channelLookup) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.sendConvocationUseCase = sendConvocationUseCase;
        this.channelLookup = channelLookup;
    }

    @Override
    public SendPendingConvocationsResult send(SendPendingConvocationsCommand command) {
        // Checked once here rather than only per convocation: refusing a whole run up front beats
        // failing a hundred times over, and the loop below swallows each failure by design.
        channelLookup.requireSendable(command.channel());
        generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        List<Convocation> pending = convocationRepository.findByGeneralMeetingId(command.generalMeetingId()).stream()
                .filter(convocation -> convocation.getDeliveryStatus() != DeliveryStatus.SENT).toList();

        int sent = 0;
        int failed = 0;
        for (Convocation convocation : pending) {
            try {
                sendConvocationUseCase.send(new SendConvocationCommand(convocation.getId(), command.channel(),
                        command.requestedByUserId()));
                sent++;
            } catch (RuntimeException e) {
                failed++;
                log.error("Convocation {} could not be sent - the convocation itself stands, re-send it with "
                        + "POST /convocations/{}/send", convocation.getId(), convocation.getId(), e);
            }
        }
        log.info("{}/{} pending convocation(s) sent by {} for general meeting {}", sent, pending.size(),
                command.channel(), command.generalMeetingId());
        return new SendPendingConvocationsResult(sent, failed);
    }
}
