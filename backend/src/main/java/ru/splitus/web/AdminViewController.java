package ru.splitus.web;

import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.splitus.admin.AdminAuthService;
import ru.splitus.admin.AdminCheckCommandService;
import ru.splitus.admin.AdminCheckDetails;
import ru.splitus.admin.AdminCheckSummary;
import ru.splitus.admin.AdminUser;
import ru.splitus.admin.AdminReadService;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Handles admin view web requests.
 */
@Controller
public class AdminViewController {

    private final AdminReadService adminReadService;
    private final AdminAuthService adminAuthService;
    private final AdminCheckCommandService adminCheckCommandService;
    private final AdminSecurityProperties adminSecurityProperties;

    /**
     * Creates a new admin view controller instance.
     */
    public AdminViewController(
            AdminReadService adminReadService,
            AdminAuthService adminAuthService,
            AdminCheckCommandService adminCheckCommandService,
            AdminSecurityProperties adminSecurityProperties) {
        this.adminReadService = adminReadService;
        this.adminAuthService = adminAuthService;
        this.adminCheckCommandService = adminCheckCommandService;
        this.adminSecurityProperties = adminSecurityProperties;
    }

    /**
     * Executes login.
     */
    @GetMapping("/admin/login")
    public String login() {
        return "admin-login";
    }

    /**
     * Executes login submit.
     */
    @PostMapping("/admin/login")
    public String loginSubmit(
            @RequestParam("login") String login,
            @RequestParam("password") String password,
            HttpSession session) {
        java.util.Optional<AdminUser> adminUser = adminAuthService.authenticate(login, password);
        if (!adminUser.isPresent()) {
            return "redirect:/admin/login?error";
        }
        adminAuthService.signIn(session, adminUser.get().getLogin());
        return "redirect:/admin";
    }

    /**
     * Executes logout.
     */
    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        adminAuthService.signOut(session);
        return "redirect:/admin/login?logout";
    }

    /**
     * Executes dashboard.
     */
    @GetMapping("/admin")
    public String dashboard(
            @RequestParam(value = "q", required = false) String query,
            HttpSession session,
            Model model) {
        model.addAttribute("checks", adminReadService.searchChecks(query));
        model.addAttribute("searchQuery", query == null ? "" : query.trim());
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", adminSecurityProperties.getEnvironmentName());
        model.addAttribute("currentLogin", adminAuthService.currentLogin(session));
        return "admin-home";
    }

    /**
     * Returns the check details page.
     */
    @GetMapping("/admin/checks/{checkId}")
    public String checkDetails(@PathVariable UUID checkId, HttpSession session, Model model) {
        AdminCheckDetails checkDetails = adminReadService.findCheckDetails(checkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check not found"));
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", adminSecurityProperties.getEnvironmentName());
        model.addAttribute("currentLogin", adminAuthService.currentLogin(session));
        model.addAttribute("checkDetails", checkDetails);
        return "admin-check-detail";
    }

    /**
     * Deletes check from the admin panel.
     */
    @PostMapping("/admin/checks/{checkId}/delete")
    public String deleteCheck(@PathVariable UUID checkId, RedirectAttributes redirectAttributes) {
        AdminCheckSummary summary = adminReadService.findCheckSummary(checkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check not found"));
        adminCheckCommandService.deleteCheck(checkId);
        redirectAttributes.addFlashAttribute("flashMessage", "Чек \"" + summary.getTitle() + "\" удалён.");
        return "redirect:/admin";
    }
}
