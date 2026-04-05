package ru.splitus.web;

import java.util.UUID;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.splitus.admin.AdminCheckCommandService;
import ru.splitus.admin.AdminCheckDetails;
import ru.splitus.admin.AdminReadService;
import ru.splitus.config.AdminSecurityProperties;
import ru.splitus.error.ApiException;

/**
 * Handles admin view web requests.
 */
@Controller
public class AdminViewController {

    private final AdminReadService adminReadService;
    private final AdminCheckCommandService adminCheckCommandService;
    private final AdminSecurityProperties adminSecurityProperties;

    /**
     * Creates a new admin view controller instance.
     */
    public AdminViewController(
            AdminReadService adminReadService,
            AdminCheckCommandService adminCheckCommandService,
            AdminSecurityProperties adminSecurityProperties) {
        this.adminReadService = adminReadService;
        this.adminCheckCommandService = adminCheckCommandService;
        this.adminSecurityProperties = adminSecurityProperties;
    }

    /**
     * Executes login.
     */
    @GetMapping("/admin/login")
    public String login(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "logout", required = false) String logout,
            Model model,
            Principal principal) {
        if (principal != null) {
            return "redirect:/admin";
        }
        model.addAttribute("environmentName", adminSecurityProperties.getEnvironmentName());
        model.addAttribute("hasError", Boolean.valueOf(error != null));
        model.addAttribute("loggedOut", Boolean.valueOf(logout != null));
        return "admin-login";
    }

    /**
     * Executes dashboard.
     */
    @GetMapping("/admin")
    public String dashboard(@RequestParam(name = "q", required = false) String query, Model model, Principal principal) {
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", adminSecurityProperties.getEnvironmentName());
        model.addAttribute("authMode", "spring-security");
        model.addAttribute("currentAdminLogin", principal == null ? null : principal.getName());
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("checks", adminReadService.searchChecks(query));
        return "admin-home";
    }

    /**
     * Executes check detail view.
     */
    @GetMapping("/admin/checks/{checkId}")
    public String checkDetails(@PathVariable("checkId") UUID checkId, Model model, Principal principal) {
        AdminCheckDetails details = adminReadService.findCheckDetails(checkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check not found"));
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", adminSecurityProperties.getEnvironmentName());
        model.addAttribute("currentAdminLogin", principal == null ? null : principal.getName());
        model.addAttribute("details", details);
        return "admin-check-detail";
    }

    /**
     * Deletes a check from the admin panel.
     */
    @PostMapping("/admin/checks/{checkId}/delete")
    public String deleteCheck(
            @PathVariable("checkId") UUID checkId,
            @RequestParam("confirmationTitle") String confirmationTitle,
            RedirectAttributes redirectAttributes) {
        try {
            String title = adminCheckCommandService.deleteCheck(checkId, confirmationTitle);
            redirectAttributes.addFlashAttribute("successMessage", "Чек \"" + title + "\" удалён");
            return "redirect:/admin";
        } catch (ApiException exception) {
            if (exception.getStatus() == HttpStatus.NOT_FOUND) {
                redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                return "redirect:/admin";
            }
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/admin/checks/" + checkId;
        }
    }
}




