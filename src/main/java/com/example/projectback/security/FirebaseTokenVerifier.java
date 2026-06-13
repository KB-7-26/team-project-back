package com.example.projectback.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

    private final FirebaseAppProvider firebaseAppProvider;

    public FirebaseToken verify(String idToken) {
        try {
            return FirebaseAuth.getInstance(firebaseAppProvider.getFirebaseApp()).verifyIdToken(idToken);
        } catch (FirebaseAuthException | IOException | IllegalArgumentException | IllegalStateException exception) {
            throw new FirebaseAuthenticationException("Firebase ID 토큰 검증에 실패했습니다.", exception);
        }
    }
}
