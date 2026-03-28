package com.wealthwise.alert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "alert_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertLog {

    @Id
    private String id;

    private Long userId;
    private String alertType;
    private String category;
    private String message;
    private BigDecimal triggerAmount;
    private boolean read;
    private LocalDateTime createdAt;
}
