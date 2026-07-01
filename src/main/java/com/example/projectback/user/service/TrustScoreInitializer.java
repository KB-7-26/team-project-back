package com.example.projectback.user.service;

import com.example.projectback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TrustScoreInitializer {

    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultTrustScore() {
        userRepository.initializeDefaultTrustScoreForUsersWithoutReviews();
    }
}
