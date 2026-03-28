package com.wealthwise.alert;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertLogRepository extends MongoRepository<AlertLog, String> {

    boolean existsByUserIdAndAlertTypeAndCategoryAndCreatedAtAfter(
            Long userId, String alertType, String category, LocalDateTime after);

    List<AlertLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AlertLog> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read);
}
