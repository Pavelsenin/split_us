package ru.splitus.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.config.TelegramWebhookProperties;

@RestController
@RequestMapping("/api/telegram/webhook")
public class TelegramWebhookController {

    private final TelegramWebhookProperties properties;

    public TelegramWebhookController(TelegramWebhookProperties properties) {
        this.properties = properties;
    }

    @PostMapping("/{alias}")
    public ResponseEntity<Map<String, Object>> acceptUpdate(
            @PathVariable String alias,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretToken,
            @RequestBody(required = false) Map<String, Object> updateBody) {

        if (!properties.getPathAlias().equals(alias)) {
            return response(HttpStatus.NOT_FOUND, "unknown webhook alias");
        }

        if (!properties.getSecretToken().equals(secretToken)) {
            return response(HttpStatus.UNAUTHORIZED, "invalid webhook secret");
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("accepted", Boolean.TRUE);
        payload.put("payloadPresent", Boolean.valueOf(updateBody != null && !updateBody.isEmpty()));
        return ResponseEntity.accepted().body(payload);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("status", Integer.valueOf(status.value()));
        payload.put("message", message);
        return ResponseEntity.status(status).body(payload);
    }
}
