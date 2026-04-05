package ru.splitus.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles admin view web requests.
 */
@Controller
public class AdminViewController {

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
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Split Us Admin");
        model.addAttribute("environmentName", "local");
        model.addAttribute("authMode", "stub");
        return "admin-home";
    }
}




