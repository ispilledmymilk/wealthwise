package com.wealthwise.scheduler;

import com.wealthwise.alert.AlertEngine;
import com.wealthwise.insight.SpendingInsightService;
import com.wealthwise.user.User;
import com.wealthwise.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NightlyAnalysisJob {

    private final UserRepository userRepository;
    private final AlertEngine alertEngine;
    private final SpendingInsightService insightService;

    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyAnalysis() {
        log.info("Starting nightly analysis job...");
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            try {
                alertEngine.runForUser(user);
                insightService.generateSnapshot(user);
            } catch (Exception e) {
                log.error("Nightly job failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        });

        log.info("Nightly analysis complete for {} users", users.size());
    }
}
