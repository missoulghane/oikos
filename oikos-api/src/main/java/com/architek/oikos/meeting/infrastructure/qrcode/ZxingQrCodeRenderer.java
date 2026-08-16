package com.architek.oikos.meeting.infrastructure.qrcode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.QrCodeRendererPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * QR codes for the convocation letter, via zxing.
 *
 * <p>Kept behind QrCodeRendererPort so nothing else in the module imports the
 * library - same arrangement as ThymeleafConvocationRenderer for openhtmltopdf.
 *
 * <p>Error correction level M rather than the default L: this code is printed,
 * folded into an envelope, and scanned off paper that may be creased or poorly
 * lit. M tolerates about 15% damage against L's 7%, for a modestly denser
 * symbol - the right trade when the alternative is a copropriétaire typing 43
 * characters.
 */
@Component
public class ZxingQrCodeRenderer implements QrCodeRendererPort {

    private static final int SIZE_PX = 240;

    @Override
    public String renderAsDataUri(String content) {
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    // Without this, zxing pads the symbol with a 4-module quiet zone that the
                    // letter's own layout already provides - it only shrinks the useful pixels.
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", png);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
        } catch (WriterException | IOException e) {
            throw new QrCodeRenderingException("Failed to render the convocation QR code", e);
        }
    }
}
