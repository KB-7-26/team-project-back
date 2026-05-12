package com.example.projectback.security;

import com.example.projectback.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Value("${jwt.secret:dev-jwt-secret-key-must-be-changed-before-production-1234567890}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms:3600000}")
    private long accessTokenExpirationMs;

    private byte[] secretBytes;

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret은 32바이트 이상으로 설정해야 합니다.");
        }
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getLoginId());
        claims.put("userId", user.getId());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusMillis(accessTokenExpirationMs).getEpochSecond());

        return createToken(claims);
    }

    public String getLoginId(String token) {
        Map<String, Object> claims = parseClaims(token);
        Object subject = claims.get("sub");

        if (!(subject instanceof String loginId) || !StringUtils.hasText(loginId)) {
            throw new JwtAuthenticationException("토큰 subject가 올바르지 않습니다.");
        }

        return loginId;
    }

    private String createToken(Map<String, Object> claims) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(claims);
            String unsignedToken = encodedHeader + "." + encodedPayload;

            return unsignedToken + "." + sign(unsignedToken);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("JWT 생성에 실패했습니다.", exception);
        }
    }

    private Map<String, Object> parseClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtAuthenticationException("JWT 형식이 올바르지 않습니다.");
        }

        verifySignature(parts);

        try {
            String payloadJson = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, CLAIMS_TYPE);
            validateExpiration(claims);
            return claims;
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new JwtAuthenticationException("JWT payload를 읽을 수 없습니다.", exception);
        }
    }

    private void verifySignature(String[] parts) {
        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);

        boolean matches = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII)
        );

        if (!matches) {
            throw new JwtAuthenticationException("JWT 서명이 올바르지 않습니다.");
        }
    }

    private void validateExpiration(Map<String, Object> claims) {
        Object expiration = claims.get("exp");
        long expirationEpochSecond = toLong(expiration);

        if (Instant.now().getEpochSecond() >= expirationEpochSecond) {
            throw new JwtAuthenticationException("JWT가 만료되었습니다.");
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException exception) {
                throw new JwtAuthenticationException("JWT 만료 시간이 올바르지 않습니다.", exception);
            }
        }
        throw new JwtAuthenticationException("JWT 만료 시간이 없습니다.");
    }

    private String encodeJson(Map<String, Object> value) throws JsonProcessingException {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
        return BASE64_URL_ENCODER.encodeToString(jsonBytes);
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretBytes, HMAC_SHA256));
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.US_ASCII));
            return BASE64_URL_ENCODER.encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 서명 생성에 실패했습니다.", exception);
        }
    }
}
