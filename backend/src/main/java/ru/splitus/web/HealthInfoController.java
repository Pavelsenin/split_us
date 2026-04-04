package ru.splitus.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/health")
public class HealthInfoController {

    @GetMapping("/live")
    public Map<String, Object> live() {
        return buildResponse("UP", "Application process is alive");
    }

    @GetMapping("/ready")
    public Map<String, Object> ready() {
        return buildResponse("UP", "Application is ready to accept traffic");
    }

    private Map<String, Object> buildResponse(String status, String details) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", status);
        response.put("details", details);
        return response;
    }
}

