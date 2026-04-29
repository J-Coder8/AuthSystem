package com.authsystem.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class WebcamUtil {

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private boolean running = false;

    public boolean initialize() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            return false;
        }
        webcam.setViewSize(new java.awt.Dimension(640, 480));
        webcam.open();
        return true;
    }

    public void startPreview(ImageView imageView) {
        if (webcam == null || !webcam.isOpen()) {
            return;
        }
        running = true;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (!running) return;
            BufferedImage bufferedImage = webcam.getImage();
            if (bufferedImage != null) {
                WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                Platform.runLater(() -> imageView.setImage(fxImage));
            }
        }, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS
    }

    public void stopPreview() {
        running = false;
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }

    public String captureBase64() {
        if (webcam == null || !webcam.isOpen()) {
            return null;
        }
        BufferedImage image = webcam.getImage();
        if (image == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void dispose() {
        stopPreview();
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            webcam = null;
        }
    }

    public boolean isAvailable() {
        return Webcam.getDefault() != null;
    }
}
