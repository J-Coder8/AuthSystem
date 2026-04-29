package com.authsystem.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class QrCodeService {

    public static Image createQrCode(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            BufferedImage original = MatrixToImageWriter.toBufferedImage(matrix);
            // Convert to ARGB to ensure compatibility with JavaFX
            BufferedImage image = new BufferedImage(
                    original.getWidth(),
                    original.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();
            return SwingFXUtils.toFXImage(image, null);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
