package com.authsystem.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BackupCodeService {

    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<String> generateCodes(int count) {
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        while (codes.size() < count) {
            int value = random.nextInt(100_000_000);
            String code = String.format("%04d-%04d", value / 10_000, value % 10_000);
            codes.add(code);
        }
        return new ArrayList<>(codes);
    }

    public List<String> generateCodes() {
        return generateCodes(10);
    }

    public String hashCodes(List<String> codes) {
        return codes.stream()
                .map(this::normalize)
                .map(encoder::encode)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    public int remainingCount(String hashedCodes) {
        if (hashedCodes == null || hashedCodes.isBlank()) {
            return 0;
        }
        return (int) List.of(hashedCodes.split(",")).stream()
                .filter(hash -> !hash.isBlank())
                .count();
    }

    public ConsumeResult consumeCode(String hashedCodes, String rawCode) {
        if (hashedCodes == null || hashedCodes.isBlank()) {
            return new ConsumeResult(false, null);
        }

        String normalized = normalize(rawCode);
        if (normalized.isBlank()) {
            return new ConsumeResult(false, hashedCodes);
        }

        List<String> hashes = new ArrayList<>(List.of(hashedCodes.split(",")));
        hashes.removeIf(String::isBlank);

        for (int i = 0; i < hashes.size(); i++) {
            if (encoder.matches(normalized, hashes.get(i))) {
                hashes.remove(i);
                String updated = hashes.isEmpty() ? null : String.join(",", hashes);
                return new ConsumeResult(true, updated);
            }
        }

        return new ConsumeResult(false, hashedCodes);
    }

    private String normalize(String code) {
        return code.replace("-", "").replace(" ", "").trim();
    }

    public static final class ConsumeResult {
        private final boolean matched;
        private final String updatedHashes;

        public ConsumeResult(boolean matched, String updatedHashes) {
            this.matched = matched;
            this.updatedHashes = updatedHashes;
        }

        public boolean getMatched() {
            return matched;
        }

        public String getUpdatedHashes() {
            return updatedHashes;
        }
    }
}
