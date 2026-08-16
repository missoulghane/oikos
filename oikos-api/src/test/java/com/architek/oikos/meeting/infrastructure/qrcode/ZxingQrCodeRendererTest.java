package com.architek.oikos.meeting.infrastructure.qrcode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/**
 * A QR code nobody can scan is worse than no QR code: it is printed, posted,
 * and fails silently in the hands of a copropriétaire who has no other way to
 * confirm. So the assertion is not "an image came out" - it is that the image
 * decodes back to what went in.
 */
class ZxingQrCodeRendererTest {

    private final ZxingQrCodeRenderer renderer = new ZxingQrCodeRenderer();

    private static String decode(String dataUri) throws Exception {
        String base64 = dataUri.substring(dataUri.indexOf(',') + 1);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new QRCodeReader().decode(bitmap).getText();
    }

    @Test
    void the_rendered_code_scans_back_to_the_link_it_encodes() throws Exception {
        String link = "https://oikos.ma/convocations/confirmation?token=Gg3kP1qz-4Xa_9bYc7dEf2hJkLmNoPqRsTuVwXyZ012";

        assertThat(decode(renderer.renderAsDataUri(link))).isEqualTo(link);
    }

    @Test
    void it_comes_out_as_a_self_contained_png_data_uri() {
        // The PDF renderer is given no base URI and reaches no external resource: a QR code
        // pointing at a hosted image would print as a broken box.
        String dataUri = renderer.renderAsDataUri("https://oikos.ma/x");

        assertThat(dataUri).startsWith("data:image/png;base64,");
        assertThat(Base64.getDecoder().decode(dataUri.substring(dataUri.indexOf(',') + 1))).isNotEmpty();
    }

    @Test
    void an_accented_link_survives_the_round_trip() throws Exception {
        // The base URL is configuration and nothing forbids a host or path with accents;
        // UTF-8 is set explicitly on the encoder for exactly this.
        String link = "https://oikos.ma/confirmation?ag=référence";

        assertThat(decode(renderer.renderAsDataUri(link))).isEqualTo(link);
    }
}
