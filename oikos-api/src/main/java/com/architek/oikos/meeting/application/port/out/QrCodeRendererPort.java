package com.architek.oikos.meeting.application.port.out;

/**
 * Renders a QR code for the convocation letter.
 *
 * <p>A port rather than a direct call, for the reason every renderer here is
 * one: the application layer states what the letter needs, and zxing stays in
 * infrastructure with openhtmltopdf and Thymeleaf.
 */
public interface QrCodeRendererPort {

    /**
     * The encoded content as a {@code data:} URI, ready to drop into an
     * {@code <img src>}.
     *
     * <p>A data URI and not a URL: the PDF renderer resolves no external
     * resource (it is given no base URI at all), and a letter whose QR code
     * depends on a server being reachable at print time is a letter that
     * sometimes prints a broken image.
     */
    String renderAsDataUri(String content);
}
