package com.authsystem.service;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private static final int TIME_STEP = 30;
    private static final int CODE_DIGITS = 6;
    private static final String ALGORITHM = "HmacSHA1";

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base32 base32 = new Base32();

    /**
     * Generate a new secret key for TOTP (Base32 encoded for Google Authenticator)
     */
    public String generateSecret() {
        byte[] buffer = new byte[20];
        secureRandom.nextBytes(buffer);
        String secret = base32.encodeToString(buffer);
        return secret.replace("=", "");
    }

    /**
     * Generate current TOTP code from secret
     * Returns the current 6-digit code that would be shown in Google Authenticator
     */
    public String generateCurrentCode(String secret) {
        long time = getCurrentTimeStep();
        return generateTotp(secret, time);
    }

    /**
     * Verify a TOTP code against the secret
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.isBlank()) {
            return false;
        }
        long time = getCurrentTimeStep();

        // Allow small clock skew in either direction (previous, current, next)
        for (int offset = -1; offset <= 1; offset++) {
            String expectedCode = generateTotp(secret, time + offset);
            if (code.equals(expectedCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get remaining seconds until next TOTP code (for countdown display)
     */
    public int getRemainingSeconds() {
        long currentTime = System.currentTimeMillis() / 1000;
        return TIME_STEP - (int)(currentTime % TIME_STEP);
    }

    /**
     * Get the current time step (for display purposes)
     */
    public long getCurrentTimeStep() {
        return System.currentTimeMillis() / 1000 / TIME_STEP;
    }

    /**
     * Generate TOTP code using HMAC-SHA1
     */
    private String generateTotp(String secret, long time) {
        try {
            byte[] key = decodeSecret(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(time).array();
            
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            byte[] hash = mac.doFinal(data);
            
            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }

    public String buildOtpAuthUri(String issuer, String accountName, String secret) {
        String label = urlEncode(issuer + ":" + accountName);
        String issuerEncoded = urlEncode(issuer);
        return "otpauth://totp/" + label
                + "?secret=" + secret
                + "&issuer=" + issuerEncoded
                + "&algorithm=SHA1&digits=" + CODE_DIGITS
                + "&period=" + TIME_STEP;
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null) {
            return new byte[0];
        }
        String normalized = secret.trim().toUpperCase();
        if (normalized.matches("^[A-Z2-7]+=*$")) {
            return base32.decode(normalized);
        }
        return Base64.getDecoder().decode(secret);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
