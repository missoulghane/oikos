package com.architek.oikos.meeting.infrastructure.qrcode;

/** Wraps zxing's and ImageIO's checked failures - neither is actionable by a caller. */
public class QrCodeRenderingException extends RuntimeException {

    public QrCodeRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
