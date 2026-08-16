package com.architek.oikos.meeting.application.dto;

/**
 * One owner of the convoked lot, as the tracking screens show them. email is
 * null when the party has none recorded - which is precisely what a syndic
 * needs to see to understand a FAILED row, rather than being left to guess.
 */
public record ConvocationRecipient(String fullName, String email) {
}
