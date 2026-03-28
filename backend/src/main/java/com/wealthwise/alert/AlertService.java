package com.wealthwise.alert;

import com.wealthwise.common.exception.ResourceNotFoundException;
import com.wealthwise.user.User;
import com.wealthwise.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertLogRepository alertLogRepository;
    private final UserRepository userRepository;

    public List<AlertLog> list(String email) {
        User user = requireUser(email);
        return alertLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<AlertLog> unread(String email) {
        User user = requireUser(email);
        return alertLogRepository.findByUserIdAndReadOrderByCreatedAtDesc(user.getId(), false);
    }

    public void markRead(String email, String id) {
        User user = requireUser(email);
        AlertLog log = alertLogRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        if (!log.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("Alert not found");
        }
        log.setRead(true);
        alertLogRepository.save(log);
    }

    private User requireUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
