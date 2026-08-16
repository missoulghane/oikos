package com.architek.oikos.meeting.application.dto;

import java.time.Instant;
import java.util.List;

import com.architek.oikos.meeting.domain.valueobject.VenueType;

/**
 * Everything the convocation letter prints, already resolved - the renderer
 * does formatting and nothing else. agendaLabels is the agenda in reading
 * order: an owner is convoked on the strength of that list, which is why it
 * cannot change afterwards.
 *
 * <p>confirmationLink is printed on the letter, not only sent by email: the
 * postal path is exactly the one whose recipients have no account, so a link
 * that existed only in an email would miss them entirely.
 *
 * <p>commentParagraphs is the meeting's note of intent, already flattened to
 * plain text: the PDF renderer needs well-formed XML and a rich-text editor
 * does not produce any (see RichTextToParagraphs).
 *
 * <p>Three ways in, on one sheet, because a paper letter is read by people in
 * very different situations: confirmationQrCode (a data: URI) for whoever has a
 * phone to point at it, confirmationLink for whoever reads the PDF on screen,
 * and the reference/code pair for whoever has neither and will type six
 * characters rather than forty-three.
 */
public record ConvocationDocumentView(String propertyName, String meetingTitle, String meetingType,
                                       Instant scheduledAt, VenueType venueType, String venueAddress, String venueLink,
                                       String unitNumber, String buildingName, List<String> ownerNames,
                                       List<String> agendaLabels, String quorumPercentage,
                                       String confirmationLink, String confirmationQrCode,
                                       String meetingPublicReference, String confirmationCode,
                                       List<String> commentParagraphs) {
}
