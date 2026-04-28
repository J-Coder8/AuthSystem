package com.authsystem.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.authsystem.dto.AuthResponse;
import com.authsystem.entity.User;
import com.authsystem.exception.UserNotFoundException;
import com.authsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaceService {

    private static final Logger logger = LoggerFactory.getLogger(FaceService.class);

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${face.service.url:}")
    private String faceServiceUrl;

    private static final double MATCH_THRESHOLD = 0.58;  // More forgiving threshold for the lightweight matcher

    /**
     * Detect face in image and return encoding
     * Uses a simplified approach based on image metrics
     */
    public String detectFace(String base64Image) throws Exception {
        try {
            String external = fetchExternalEncoding(base64Image);
            if (external != null) {
                return external;
            }

            // Extract actual base64 data from data URL format
            String imageData = base64Image;
            
            // Check for data URL prefix (e.g., "data:image/png;base64,..." or "data:image/jpeg;base64,...")
            if (base64Image != null && base64Image.startsWith("data:")) {
                int commaIndex = base64Image.indexOf(",");
                if (commaIndex > 0) {
                    imageData = base64Image.substring(commaIndex + 1);
                    logger.debug("Extracted base64 data from data URL format");
                }
            } else if (base64Image != null && base64Image.contains(",")) {
                // Fallback for malformed data URLs
                imageData = base64Image.split(",")[1];
                logger.debug("Extracted base64 data from comma-separated format");
            }
            
            // Clean up whitespace and newlines from base64 string
            imageData = imageData.trim().replaceAll("\\s+", "");
            
            // Decode base64 image
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            
            if (imageBytes == null || imageBytes.length == 0) {
                throw new Exception("Decoded base64 resulted in empty byte array");
            }
            
            logger.debug("Decoded image bytes: {} bytes", imageBytes.length);
            
            // Try to read image with ImageIO, trying multiple formats
            BufferedImage img = decodeImage(imageBytes);
            
            if (img == null) {
                throw new Exception("Error reading PNG image data - could not decode image. Supported formats: PNG, JPEG, BMP, GIF");
            }
            
            logger.debug("Successfully decoded image: {}x{}", img.getWidth(), img.getHeight());

            // Convert to grayscale and resize to standard size
            BufferedImage processedImage = processImage(img);
            
            // Create encoding from processed image
            return createFaceEncoding(processedImage);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64 image data: {}", e.getMessage());
            throw new Exception("Invalid base64 image data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error detecting face: {}", e.getMessage());
            throw e;
        }
    }

    private String fetchExternalEncoding(String base64Image) {
        if (faceServiceUrl == null || faceServiceUrl.isBlank()) {
            return null;
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of("base64Image", base64Image));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(faceServiceUrl + "/encode"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                Object encoding = result.get("encoding");
                if (encoding != null) {
                    return encoding.toString();
                }
            } else {
                logger.warn("Face service returned status {}", response.statusCode());
            }
        } catch (Exception e) {
            logger.warn("Face service error: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Decode image from byte array with support for multiple formats
     */
    private BufferedImage decodeImage(byte[] imageBytes) {
        // Try with ByteArrayInputStream first
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage img;
        try {
            img = ImageIO.read(bais);
        } catch (IOException e) {
            logger.debug("ImageIO.read failed: {}", e.getMessage());
            img = null;
        }
        
        if (img == null) {
            // Try different image formats
            String[] formats = {"PNG", "JPEG", "BMP", "GIF"};
            for (String format : formats) {
                bais = new ByteArrayInputStream(imageBytes);
                try {
                    img = ImageIO.read(bais);
                    if (img != null) {
                        logger.debug("Successfully decoded image as {}", format);
                        break;
                    }
                } catch (IOException e) {
                    logger.debug("Failed to decode as {}: {}", format, e.getMessage());
                }
            }
        }
        
        return img;
    }

    /**
     * Process image: convert to grayscale and resize to standard size
     */
    private BufferedImage processImage(BufferedImage original) {
        int cropSize = Math.min(original.getWidth(), original.getHeight());
        int offsetX = Math.max((original.getWidth() - cropSize) / 2, 0);
        int offsetY = Math.max((original.getHeight() - cropSize) / 2, 0);
        BufferedImage centered = original.getSubimage(offsetX, offsetY, cropSize, cropSize);

        int targetSize = 64;
        BufferedImage resized = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_BYTE_GRAY);

        java.awt.Graphics2D g = resized.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(centered, 0, 0, targetSize, targetSize, null);
        g.dispose();

        normalizeImageLighting(resized);
        return resized;
    }

    private void normalizeImageLighting(BufferedImage image) {
        double sum = 0;
        double count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                sum += getGrayValue(image.getRGB(x, y));
                count++;
            }
        }

        double mean = count > 0 ? sum / count : 128.0;
        double variance = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double gray = getGrayValue(image.getRGB(x, y));
                variance += Math.pow(gray - mean, 2);
            }
        }
        double stdDev = count > 0 ? Math.sqrt(variance / count) : 1.0;
        if (stdDev < 1.0) {
            stdDev = 1.0;
        }

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double gray = getGrayValue(image.getRGB(x, y));
                int normalized = clampToByte((int) Math.round(128 + ((gray - mean) * 42.0 / stdDev)));
                int rgb = (normalized << 16) | (normalized << 8) | normalized;
                image.setRGB(x, y, rgb);
            }
        }
    }

    private int clampToByte(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Create face encoding from processed image
     * Uses a grid-based approach to capture face features
     */
    private String createFaceEncoding(BufferedImage image) {
        List<Double> features = new ArrayList<>();
        
        int gridSize = 8;  // 8x8 grid
        int cellWidth = image.getWidth() / gridSize;
        int cellHeight = image.getHeight() / gridSize;
        
        // Extract features from each grid cell
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                // Calculate average intensity in this cell
                double sum = 0;
                int count = 0;
                
                for (int y = row * cellHeight; y < (row + 1) * cellHeight && y < image.getHeight(); y++) {
                    for (int x = col * cellWidth; x < (col + 1) * cellWidth && x < image.getWidth(); x++) {
                        int pixel = image.getRGB(x, y);
                        // Get grayscale value
                        int gray = (pixel >> 16) & 0xFF;  // Since it's grayscale, R=G=B
                        sum += gray;
                        count++;
                    }
                }
                
                double avg = count > 0 ? sum / count : 0;
                features.add(avg / 255.0);  // Normalize to 0-1
            }
        }
        
        // Add some derived features for better discrimination
        // Horizontal and vertical symmetry
        double horizontalSymmetry = calculateSymmetry(image, true);
        double verticalSymmetry = calculateSymmetry(image, false);
        features.add(horizontalSymmetry);
        features.add(verticalSymmetry);
        
        // Center vs edge brightness ratio
        double centerBrightness = calculateCenterBrightness(image);
        features.add(centerBrightness);
        
        return features.toString();
    }

    /**
     * Calculate horizontal or vertical symmetry
     */
    private double calculateSymmetry(BufferedImage image, boolean horizontal) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        double sumDiff = 0;
        int count = 0;
        
        if (horizontal) {
            // Compare top half with bottom half
            for (int y = 0; y < height / 2; y++) {
                for (int x = 0; x < width; x++) {
                    int topPixel = image.getRGB(x, y);
                    int bottomPixel = image.getRGB(x, height - 1 - y);
                    sumDiff += Math.abs(getGrayValue(topPixel) - getGrayValue(bottomPixel));
                    count++;
                }
            }
        } else {
            // Compare left half with right half
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width / 2; x++) {
                    int leftPixel = image.getRGB(x, y);
                    int rightPixel = image.getRGB(width - 1 - x, y);
                    sumDiff += Math.abs(getGrayValue(leftPixel) - getGrayValue(rightPixel));
                    count++;
                }
            }
        }
        
        return count > 0 ? 1.0 - (sumDiff / count / 255.0) : 0;
    }

    /**
     * Calculate center brightness relative to overall brightness
     */
    private double calculateCenterBrightness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        int centerX = width / 4;
        int centerY = height / 4;
        int centerW = width / 2;
        int centerH = height / 2;
        
        double centerSum = 0;
        int centerCount = 0;
        
        for (int y = centerY; y < centerY + centerH && y < height; y++) {
            for (int x = centerX; x < centerX + centerW && x < width; x++) {
                centerSum += getGrayValue(image.getRGB(x, y));
                centerCount++;
            }
        }
        
        double centerAvg = centerCount > 0 ? centerSum / centerCount : 0;
        
        // Compare to overall average
        double overallSum = 0;
        int overallCount = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                overallSum += getGrayValue(image.getRGB(x, y));
                overallCount++;
            }
        }
        
        double overallAvg = overallCount > 0 ? overallSum / overallCount : 0;
        
        return overallAvg > 0 ? centerAvg / overallAvg : 0;
    }

    private int getGrayValue(int pixel) {
        // For grayscale images, R=G=B
        return pixel & 0xFF;
    }

    /**
     * Register user's face
     */
    public AuthResponse registerFace(String username, String base64Image) throws Exception {
        User user = findUserByLogin(username);

        // Detect face and get encoding
        String encoding = detectFace(base64Image);
        
        // Save encoding to user
        user.setFaceEncoding(encoding);
        user.setFaceRegisteredAt(LocalDateTime.now());
        user.setFaceEnabled(true);
        userRepository.save(user);

        logger.info("Face registered successfully for user: {}", username);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Face registered",
                    "Face recognition was registered for your account.",
                    "FACE", "INFO");
        }

        return AuthResponse.builder()
                .username(username)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .requiresOtp(false)
                .message("Face registered successfully")
                .build();
    }

    /**
     * Verify face for login - returns the similarity score
     * The controller will handle sending OTP after verification
     */
    public double verifyFaceAndGetSimilarity(String username, String base64Image) throws Exception {
        User user = findUserByLogin(username);

        if (user.getFaceEncoding() == null || user.getFaceEncoding().isEmpty()) {
            throw new Exception("No face registered for this user");
        }

        // Get encoding from provided image
        String providedEncoding = detectFace(base64Image);
        
        // Compare encodings and return similarity
        double similarity = compareEncodings(user.getFaceEncoding(), providedEncoding);
        
        logger.info("Face similarity for user {}: {}", username, similarity);
        
        return similarity;
    }

    /**
     * Check if face matches based on similarity
     */
    public boolean isFaceMatch(String username, String base64Image) throws Exception {
        double similarity = verifyFaceAndGetSimilarity(username, base64Image);
        return similarity >= MATCH_THRESHOLD;
    }

    /**
     * Enable face login for user
     */
    public AuthResponse enableFace(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if (user.getFaceEncoding() == null || user.getFaceEncoding().isEmpty()) {
            throw new RuntimeException("No face registered. Please register your face first.");
        }

        user.setFaceEnabled(true);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Face login enabled",
                    "Face recognition login was enabled for your account.",
                    "FACE", "INFO");
        }

        return AuthResponse.builder()
                .username(username)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Face login enabled successfully")
                .build();
    }

    /**
     * Disable face login for user
     */
    public AuthResponse disableFace(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        user.setFaceEnabled(false);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Face login disabled",
                    "Face recognition login was disabled for your account.",
                    "FACE", "WARNING");
        }

        return AuthResponse.builder()
                .username(username)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Face login disabled successfully")
                .build();
    }

    /**
     * Delete face data for user
     */
    public AuthResponse deleteFace(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        user.setFaceEncoding(null);
        user.setFaceEnabled(false);
        user.setFaceRegisteredAt(null);
        user.setFaceImagePath(null);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Face data removed",
                    "Stored face recognition data was removed from your account.",
                    "FACE", "WARNING");
        }

        return AuthResponse.builder()
                .username(username)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Face data deleted successfully")
                .build();
    }

    /**
     * Check if user has face registered
     */
    public boolean hasFaceRegistered(String username) {
        return findUserOptionalByLogin(username)
                .map(user -> user.getFaceEncoding() != null && !user.getFaceEncoding().isEmpty())
                .orElse(false);
    }

    /**
     * Check if user has face enabled
     */
    public boolean isFaceEnabled(String username) {
        return findUserOptionalByLogin(username)
                .map(User::isFaceEnabled)
                .orElse(false);
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return findUserByLogin(username);
    }

    private User findUserByLogin(String login) {
        String input = login == null ? "" : login.trim();
        if (input.isEmpty()) {
            throw new UserNotFoundException("User not found: " + login);
        }
        return userRepository.findByUsernameOrEmail(input, input)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + input));
    }

    private java.util.Optional<User> findUserOptionalByLogin(String login) {
        String input = login == null ? "" : login.trim();
        if (input.isEmpty()) {
            return java.util.Optional.empty();
        }
        return userRepository.findByUsernameOrEmail(input, input);
    }

    /**
     * Compare two face encodings and return similarity score (0-1)
     */
    private double compareEncodings(String encoding1, String encoding2) {
        try {
            List<Double> features1 = parseEncoding(encoding1);
            List<Double> features2 = parseEncoding(encoding2);

            if (features1.size() != features2.size()) {
                logger.warn("Encoding sizes don't match: {} vs {}", features1.size(), features2.size());
                return 0.0;
            }

            double dotProduct = 0;
            double norm1 = 0;
            double norm2 = 0;
            double absDiff = 0;

            for (int i = 0; i < features1.size(); i++) {
                double x = features1.get(i);
                double y = features2.get(i);
                dotProduct += x * y;
                norm1 += x * x;
                norm2 += y * y;
                absDiff += Math.abs(x - y);
            }

            double cosine = (norm1 > 0 && norm2 > 0)
                    ? dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2))
                    : 0.0;
            double cosineScore = (cosine + 1) / 2;
            double averageDifference = features1.isEmpty() ? 1.0 : absDiff / features1.size();
            double distanceScore = Math.max(0.0, 1.0 - averageDifference);
            double blendedScore = (cosineScore * 0.65) + (distanceScore * 0.35);
            double finalScore = Math.max(cosineScore, blendedScore);

            logger.debug("Face comparison scores cosine={}, distance={}, blended={}, final={}",
                    cosineScore, distanceScore, blendedScore, finalScore);
            return finalScore;

        } catch (Exception e) {
            logger.error("Error comparing encodings: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Parse encoding string back to list of doubles
     */
    private List<Double> parseEncoding(String encoding) {
        List<Double> result = new ArrayList<>();
        try {
            String content = encoding.trim();
            if (content.startsWith("[")) {
                content = content.substring(1);
            }
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }
            
            String[] parts = content.split(",");
            for (String part : parts) {
                try {
                    result.add(Double.parseDouble(part.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing encoding: " + e.getMessage());
        }
        return result;
    }
}
