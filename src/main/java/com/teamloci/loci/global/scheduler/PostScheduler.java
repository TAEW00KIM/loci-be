package com.teamloci.loci.global.scheduler;

import com.teamloci.loci.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostScheduler {

    private final PostRepository postRepository;

    private static final int EXPIRATION_DAYS = 30;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void archiveExpiredPosts() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(EXPIRATION_DAYS);

        log.info("[Scheduler] 게시글 자동 보관 작업 시작... (기준: {} 이전 작성)", expiryDate);

        try {
            int count = postRepository.archiveOldPosts(expiryDate);
            log.info("[Scheduler] 총 {}개의 게시글이 보관함으로 이동되었습니다.", count);
        } catch (Exception e) {
            log.error("[Scheduler] 게시글 보관 처리 중 오류 발생", e);
        }
    }
}