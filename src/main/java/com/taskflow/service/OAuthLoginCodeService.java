
package com.taskflow.service;

import com.taskflow.entity.OAuthLoginCode;
import com.taskflow.entity.User;
import com.taskflow.repository.OAuthLoginCodeRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class OAuthLoginCodeService {

    private final OAuthLoginCodeRepository repository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${oauth.login-code.expiration-seconds:120}")
    private long expirationSeconds;

    public OAuthLoginCodeService(
            OAuthLoginCodeRepository repository) {
        this.repository = repository;
    }

    // Generate and store a temporary login code.
    @Transactional
    public String createCode(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawCode = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String codeHash = hashCode(rawCode);

        OAuthLoginCode loginCode = new OAuthLoginCode();

        loginCode.setCodeHash(codeHash);
        loginCode.setUser(user);
        loginCode.setExpiresAt(
                LocalDateTime.now().plusSeconds(expirationSeconds)
        );

        repository.save(loginCode);

        return rawCode;
    }

    // Validate and consume a login code exactly once.
    @Transactional
    public User consumeCode(String rawCode) {

        if (rawCode == null || rawCode.isBlank()) {
            throw new BadCredentialsException(
                    "Invalid or expired login code"
            );
        }

        String codeHash = hashCode(rawCode);

        OAuthLoginCode loginCode = repository
                .findByCodeHash(codeHash)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid or expired login code"
                        )
                );

        // Initialize the lazy user association before the bulk update.
        User user = loginCode.getUser();
        user.getEmail();

        LocalDateTime now = LocalDateTime.now();

        int updatedRows = repository.consumeCode(codeHash, now);

        if (updatedRows != 1) {
            throw new BadCredentialsException(
                    "Invalid, expired, or already used login code"
            );
        }

        return user;
    }

    // SHA-256 hashing.
    private String hashCode(String rawCode) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawCode.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash login code",
                    exception
            );
        }
    }
}
