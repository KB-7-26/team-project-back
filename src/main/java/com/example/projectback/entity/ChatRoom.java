package com.example.projectback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    private LocalDateTime lastMessageAt;

    private LocalDateTime sellerLastReadAt;

    private LocalDateTime buyerLastReadAt;

    private LocalDateTime sellerLeftAt;

    private LocalDateTime buyerLeftAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public boolean isParticipant(Long userId) {
        return seller.getId().equals(userId) || buyer.getId().equals(userId);
    }

    public LocalDateTime getLeftAt(Long userId) {
        if (seller.getId().equals(userId)) return sellerLeftAt;
        if (buyer.getId().equals(userId)) return buyerLeftAt;
        throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
    }

    public boolean isVisibleTo(Long userId) {
        LocalDateTime leftAt = getLeftAt(userId);
        if (leftAt == null) return true;

        LocalDateTime activityAt = lastMessageAt != null ? lastMessageAt : createdAt;
        return activityAt != null && activityAt.isAfter(leftAt);
    }

    public void leave(Long userId) {
        if (seller.getId().equals(userId)) {
            sellerLeftAt = LocalDateTime.now();
            return;
        }
        if (buyer.getId().equals(userId)) {
            buyerLeftAt = LocalDateTime.now();
            return;
        }
        throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
    }

    public void rejoin(Long userId) {
        if (seller.getId().equals(userId)) {
            sellerLeftAt = null;
            return;
        }
        if (buyer.getId().equals(userId)) {
            buyerLeftAt = null;
            return;
        }
        throw new IllegalArgumentException("채팅방 참여자가 아닙니다.");
    }
    public void updateLastReadAt(boolean isSeller) {
        if (isSeller) this.sellerLastReadAt = LocalDateTime.now();
        else this.buyerLastReadAt = LocalDateTime.now();
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}