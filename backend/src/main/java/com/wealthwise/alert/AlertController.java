package com.wealthwise.alert;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertLog>> all(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(alertService.list(user.getUsername()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<AlertLog>> unread(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(alertService.unread(user.getUsername()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal UserDetails user, @PathVariable String id) {
        alertService.markRead(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
