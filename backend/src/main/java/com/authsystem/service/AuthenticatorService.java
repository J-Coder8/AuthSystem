package com.authsystem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.authsystem.dto.AuthenticatorAccountDto;
import com.authsystem.dto.AuthenticatorAccountRequest;
import com.authsystem.entity.AuthenticatorAccount;
import com.authsystem.entity.User;
import com.authsystem.exception.UserNotFoundException;
import com.authsystem.repository.AuthenticatorAccountRepository;
import com.authsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticatorService {

    private static final int MAX_ISSUER_LENGTH = 80;
    private static final int MAX_ACCOUNT_LENGTH = 120;
    private static final int MAX_SECRET_LENGTH = 255;

    private final AuthenticatorAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TotpService totpService;

    public List<AuthenticatorAccountDto> listAccounts(String username) {
        User user = getUser(username);
        return accountRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(account -> toDto(account, null, null))
                .collect(Collectors.toList());
    }

    public List<AuthenticatorAccountDto> listCodes(String username) {
        User user = getUser(username);
        int remaining = totpService.getRemainingSeconds();
        return accountRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(account -> toDto(account, safeGenerateCode(account.getSecret()), remaining))
                .collect(Collectors.toList());
    }

    public AuthenticatorAccountDto addAccount(String username, AuthenticatorAccountRequest request) {
        if (request == null) {
            throw new RuntimeException("Request is required");
        }
        String issuer = normalize(request.getIssuer());
        String accountName = normalize(request.getAccountName());
        String secret = normalizeSecret(request.getSecret());

        if (issuer.isEmpty()) {
            throw new RuntimeException("Issuer is required");
        }
        if (accountName.isEmpty()) {
            throw new RuntimeException("Account name is required");
        }
        if (secret.isEmpty()) {
            throw new RuntimeException("Secret is required");
        }
        if (issuer.length() > MAX_ISSUER_LENGTH) {
            throw new RuntimeException("Issuer must be 80 characters or less");
        }
        if (accountName.length() > MAX_ACCOUNT_LENGTH) {
            throw new RuntimeException("Account name must be 120 characters or less");
        }
        if (secret.length() > MAX_SECRET_LENGTH) {
            throw new RuntimeException("Secret is too long");
        }

        User user = getUser(username);

        // Validate secret by generating a code
        String code = safeGenerateCode(secret);
        int remaining = totpService.getRemainingSeconds();

        AuthenticatorAccount account = new AuthenticatorAccount();
        account.setUser(user);
        account.setIssuer(issuer);
        account.setAccountName(accountName);
        account.setSecret(secret);
        AuthenticatorAccount saved = accountRepository.save(account);

        return toDto(saved, code, remaining);
    }

    @SuppressWarnings("nullness")
    public void deleteAccount(String username, Long id) {
        User user = getUser(username);
        @SuppressWarnings("nullness")
        AuthenticatorAccount account = accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Authenticator account not found"));
        accountRepository.delete(account);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeSecret(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replace(" ", "").replace("-", "");
        return normalized.toUpperCase();
    }

    private String safeGenerateCode(String secret) {
        try {
            return totpService.generateCurrentCode(secret);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Invalid secret. Please check your authenticator key.");
        }
    }

    private AuthenticatorAccountDto toDto(AuthenticatorAccount account, String code, Integer remainingSeconds) {
        return new AuthenticatorAccountDto(
                account.getId(),
                account.getIssuer(),
                account.getAccountName(),
                account.getCreatedAt(),
                code,
                remainingSeconds
        );
    }
}
