package ru.splitus.web;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.splitus.admin.AdminCheckDetails;
import ru.splitus.admin.AdminReadService;

/**
 * Handles admin view web requests.
 */
@Controller
public class AdminViewController {

    private final AdminReadService adminReadService;
    private final String environmentName;

    /**
     * Creates a new admin view controller instance.
     */
    public AdminViewController(
            AdminReadService adminReadService,
            @Value("${splitus.admin.environment-name:local}") String environmentName) {
        this.adminReadService = adminReadService;
        this.environmentName = environmentName;
    }

    /**
     * Executes login.
     */
    @GetMapping("/admin/login")
    public String login() {
        return "admin-login";
    }

    /**
     * Executes dashboard.
     */
    @GetMapping("/admin")
    public String dashboard(@RequestParam(name = "q", required = false) String query, Model model) {
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", environmentName);
        model.addAttribute("authMode", "disabled");
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("checks", adminReadService.searchChecks(query));
        return "admin-home";
    }

    /**
     * Executes check detail view.
     */
    @GetMapping("/admin/checks/{checkId}")
    public String checkDetails(@PathVariable("checkId") UUID checkId, Model model) {
        AdminCheckDetails details = adminReadService.findCheckDetails(checkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check not found"));
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", environmentName);
        model.addAttribute("details", details);
        return "admin-check-detail";
    }
}




