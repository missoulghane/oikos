package com.architek.oikos.meeting.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.port.in.GetConvocationDocumentUseCase;
import com.architek.oikos.meeting.application.port.out.ConvocationDocumentPort;
import com.architek.oikos.meeting.application.port.out.ConvocationRendererPort;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.domain.exception.ConvocationNotFoundException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

/**
 * The convocation letter of one lot, as a PDF.
 *
 * <p>Two sources, and the distinction matters. Once a convocation has been
 * sent, this returns the very file that was filed at that moment: it is the
 * record of what the copropriétaire received, and it must not silently follow
 * a later correction of the date. Before the first send there is nothing
 * filed, so the letter is rendered on the fly - a preview of what will go out,
 * and the file a syndic prints when convoking by post.
 *
 * <p>Rendering on demand is cheap enough to do per request (one Thymeleaf pass
 * and one PDF), and far cheaper than rendering every lot's letter at
 * generation time for a hundred-lot copropriété when most will never be
 * printed.
 */
@Component
public class GetConvocationDocumentService implements GetConvocationDocumentUseCase {

    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationDocumentPort documentPort;
    private final ConvocationDocumentComposer documentComposer;
    private final ConvocationRendererPort rendererPort;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler viewAssembler;

    public GetConvocationDocumentService(ConvocationRepository convocationRepository,
                                          GeneralMeetingRepository generalMeetingRepository,
                                          ConvocationDocumentPort documentPort,
                                          ConvocationDocumentComposer documentComposer,
                                          ConvocationRendererPort rendererPort,
                                          PropertyDirectoryPort propertyDirectoryPort,
                                          ConvocationViewAssembler viewAssembler) {
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.documentPort = documentPort;
        this.documentComposer = documentComposer;
        this.rendererPort = rendererPort;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocationDocument getDocument(GetConvocationQuery query) {
        Convocation convocation = convocationRepository.findById(query.id())
                .orElseThrow(() -> new ConvocationNotFoundException(query.id()));

        Optional<ConvocationDocumentPort.StoredDocument> filed =
                documentPort.findConvocationDocument(convocation.getId());
        if (filed.isPresent()) {
            return new ConvocationDocument(filed.get().fileName(), filed.get().content(), true);
        }

        GeneralMeeting meeting = generalMeetingRepository.findById(convocation.getGeneralMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(convocation.getGeneralMeetingId()));
        UnitInfo unit = viewAssembler.unitsByIdOf(meeting.getPropertyId()).get(convocation.getUnitId());
        String propertyName = propertyDirectoryPort.getProperty(meeting.getPropertyId()).name();

        byte[] pdf = rendererPort.render(documentComposer.compose(convocation, meeting, unit, propertyName));
        return new ConvocationDocument(ConvocationDocumentComposer.fileNameFor(unit), pdf, false);
    }
}
