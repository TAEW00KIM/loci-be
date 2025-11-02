package com.teamfiv5.fiv5.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerId"}), // (provider, providerId) 조합이 고유해야 함
        @UniqueConstraint(columnNames = {"nickname"}) // 닉네임도 고유해야 함
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Apple 로그인은 이메일을 숨길 수 있으므로 nullable = true
    @Column(nullable = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = true)
    private String profileUrl;

    @Column(nullable = false)
    private String provider; // "apple"

    @Column(nullable = false)
    private String providerId; // Apple에서 제공하는 고유 sub (subject)

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime createdAt;

    @Builder
    public User(String email, String nickname, String profileUrl, String provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.provider = provider;
        this.providerId = providerId;
    }

    // (참고) Apple은 최초 로그인 시에만 이메일, 이름을 제공합니다.
    // 최초 가입 시 닉네임을 설정하는 로직이 필요합니다.
    // 여기서는 providerId를 임시 닉네임으로 사용합니다.
    public User updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }
}