package com.architek.oikos.meeting.application.query;

import com.architek.oikos.meeting.domain.valueobject.ShortCode;

/**
 * The pair a copropriétaire types off their letter. callerId identifies who is
 * trying, for the attempt cap - it is the request's IP, supplied by the web
 * layer, never anything the caller states about itself.
 */
public record GetConvocationByCodeQuery(ShortCode meetingReference, ShortCode confirmationCode, String callerId) {
}
