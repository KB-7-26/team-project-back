package com.example.projectback.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class FirebaseAppProvider {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    private FirebaseApp firebaseApp;

    public synchronized FirebaseApp getFirebaseApp() throws IOException {
        if (firebaseApp != null) {
            return firebaseApp;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            firebaseApp = FirebaseApp.getInstance();
            return firebaseApp;
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(loadCredentials())
                .build();
        firebaseApp = FirebaseApp.initializeApp(options);
        return firebaseApp;
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (StringUtils.hasText(serviceAccountJson)) {
            String trimmedJson = serviceAccountJson.trim();
            byte[] jsonBytes = trimmedJson.startsWith("{")
                    ? trimmedJson.getBytes(StandardCharsets.UTF_8)
                    : Base64.getDecoder().decode(trimmedJson);
            return GoogleCredentials.fromStream(new ByteArrayInputStream(jsonBytes));
        }

        if (StringUtils.hasText(serviceAccountPath)) {
            return GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath.trim()));
        }

        return GoogleCredentials.getApplicationDefault();
    }
}
