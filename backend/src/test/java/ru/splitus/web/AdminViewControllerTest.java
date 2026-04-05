package ru.splitus.web;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import ru.splitus.admin.AdminAuthService;
import ru.splitus.admin.AdminCheckCommandService;
import ru.splitus.admin.AdminCheckDetails;
import ru.splitus.admin.AdminCheckSummary;
import ru.splitus.admin.AdminReadService;
import ru.splitus.admin.AdminUser;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Tests admin view controller.
 */
class AdminViewControllerTest {

    @Test
    void dashboardShowsSearchResults() {
        AdminReadService readService = Mockito.mock(AdminReadService.class);
        AdminAuthService authService = Mockito.mock(AdminAuthService.class);
        Mockito.when(readService.searchChecks("trip")).thenReturn(Collections.singletonList(summary("Trip")));
        Mockito.when(authService.currentLogin(Mockito.any(HttpSession.class))).thenReturn("root-admin");
        AdminViewController controller = new AdminViewController(readService, authService, Mockito.mock(AdminCheckCommandService.class), properties());
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.dashboard("trip", new MockHttpSession(), model);

        Assertions.assertEquals("admin-home", viewName);
        Assertions.assertEquals("prod-eu1", model.get("environmentName"));
        Assertions.assertEquals("root-admin", model.get("currentLogin"));
        Assertions.assertEquals("trip", model.get("searchQuery"));
    }

    @Test
    void checkDetailsShowsRequestedCheck() {
        AdminReadService readService = Mockito.mock(AdminReadService.class);
        AdminAuthService authService = Mockito.mock(AdminAuthService.class);
        AdminCheckDetails checkDetails = new AdminCheckDetails(summary("Weekend"), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Mockito.when(readService.findCheckDetails(checkDetails.getSummary().getId())).thenReturn(Optional.of(checkDetails));
        Mockito.when(authService.currentLogin(Mockito.any(HttpSession.class))).thenReturn("root-admin");
        AdminViewController controller = new AdminViewController(readService, authService, Mockito.mock(AdminCheckCommandService.class), properties());
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = controller.checkDetails(checkDetails.getSummary().getId(), new MockHttpSession(), model);

        Assertions.assertEquals("admin-check-detail", viewName);
        Assertions.assertSame(checkDetails, model.get("checkDetails"));
    }

    @Test
    void deleteCheckRedirectsToDashboard() {
        AdminReadService readService = Mockito.mock(AdminReadService.class);
        AdminAuthService authService = Mockito.mock(AdminAuthService.class);
        AdminCheckCommandService commandService = Mockito.mock(AdminCheckCommandService.class);
        AdminCheckSummary summary = summary("Trip");
        Mockito.when(readService.findCheckSummary(summary.getId())).thenReturn(Optional.of(summary));
        Mockito.when(commandService.deleteCheck(summary.getId())).thenReturn(true);
        AdminViewController controller = new AdminViewController(readService, authService, commandService, properties());
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = controller.deleteCheck(summary.getId(), redirectAttributes);

        Assertions.assertEquals("redirect:/admin", viewName);
        Assertions.assertTrue(String.valueOf(redirectAttributes.getFlashAttributes().get("flashMessage")).contains("Trip"));
    }

    @Test
    void loginSubmitStoresSessionForAuthenticatedAdmin() {
        AdminReadService readService = Mockito.mock(AdminReadService.class);
        AdminAuthService authService = Mockito.mock(AdminAuthService.class);
        AdminCheckSummary summary = summary("Trip");
        Mockito.when(authService.authenticate("admin", "secret")).thenReturn(Optional.of(new AdminUser(
                summary.getId(),
                "admin",
                "hash",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        )));
        MockHttpSession session = new MockHttpSession();
        AdminViewController controller = new AdminViewController(readService, authService, Mockito.mock(AdminCheckCommandService.class), properties());

        String viewName = controller.loginSubmit("admin", "secret", session);

        Assertions.assertEquals("redirect:/admin", viewName);
        Mockito.verify(authService).signIn(session, "admin");
    }

    private AdminSecurityProperties properties() {
        AdminSecurityProperties properties = new AdminSecurityProperties();
        properties.setEnvironmentName("prod-eu1");
        return properties;
    }

    private AdminCheckSummary summary(String title) {
        return new AdminCheckSummary(
                UUID.randomUUID(),
                title,
                "owner",
                "token",
                1L,
                "RUB",
                true,
                3,
                2,
                OffsetDateTime.now()
        );
    }
}
