package ru.splitus.acceptance;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.splitus.admin.AdminCheckCommandService;
import ru.splitus.admin.AdminReadService;
import ru.splitus.admin.AdminUser;
import ru.splitus.admin.AdminUserRepository;
import ru.splitus.config.InternalApiSecurityProperties;
import ru.splitus.error.ApiException;
import ru.splitus.settlement.SettlementBalance;
import ru.splitus.settlement.SettlementExecutionService;
import ru.splitus.settlement.SettlementPlan;
import ru.splitus.settlement.SettlementResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies admin and internal API flows against the Spring web stack.
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "splitus.security.service-token=test-service-token",
        "splitus.admin.environment-name=test",
        "splitus.admin.bootstrap-login=",
        "splitus.admin.bootstrap-password=",
        "splitus.admin.bootstrap-password-hash="
})
@AutoConfigureMockMvc
class WebAcceptanceTest {

    private static final String CSRF_SESSION_ATTRIBUTE = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NamedParameterJdbcTemplate jdbcTemplate;

    @MockBean
    private AdminReadService adminReadService;

    @MockBean
    private AdminCheckCommandService adminCheckCommandService;

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private SettlementExecutionService settlementExecutionService;

    @MockBean
    private InternalApiSecurityProperties internalApiSecurityProperties;

    @Test
    void internalSettlementEndpointRequiresValidServiceToken() throws Exception {
        UUID checkId = UUID.randomUUID();
        when(internalApiSecurityProperties.getServiceToken()).thenReturn("test-service-token");
        when(settlementExecutionService.calculateStable(checkId)).thenReturn(new SettlementResult(
                Collections.singletonList(new SettlementBalance("alice", 450L)),
                new SettlementPlan(Collections.singletonList(new SettlementPlan.Transfer("bob", "alice", 450L)))
        ));

        mockMvc.perform(post("/api/internal/checks/" + checkId + "/settlement"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));

        mockMvc.perform(post("/api/internal/checks/" + checkId + "/settlement")
                        .header("X-Service-Token", "test-service-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[0].participant").value("alice"))
                .andExpect(jsonPath("$.transfers[0].fromParticipant").value("bob"))
                .andExpect(jsonPath("$.transfers[0].amountMinor").value(450));
    }

    @Test
    void adminCanLoginAndDeleteCheckThroughWebFlow() throws Exception {
        UUID checkId = UUID.randomUUID();
        when(internalApiSecurityProperties.getServiceToken()).thenReturn("test-service-token");
        when(adminUserRepository.findByLogin("admin")).thenReturn(Optional.of(new AdminUser(
                UUID.randomUUID(),
                "admin",
                new BCryptPasswordEncoder().encode("secret"),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        )));
        when(adminCheckCommandService.deleteCheck(eq(checkId), eq("Trip"))).thenReturn("Trip");

        MockHttpSession authenticatedSession = loginAsAdmin("admin", "secret");

        mockMvc.perform(get("/admin").session(authenticatedSession))
                .andExpect(status().isOk());

        CsrfToken csrfToken = (CsrfToken) authenticatedSession.getAttribute(CSRF_SESSION_ATTRIBUTE);
        Assertions.assertNotNull(csrfToken);

                mockMvc.perform(post("/admin/checks/" + checkId + "/delete")
                        .session(authenticatedSession)
                        .param("confirmationTitle", "Trip")
                        .param(csrfToken.getParameterName(), csrfToken.getToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", Matchers.containsString("Trip")));
    }

    /**
     * Performs form login and returns the authenticated session.
     */
    private MockHttpSession loginAsAdmin(String login, String password) throws Exception {
        MvcResult loginPage = mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginPage.getRequest().getSession(false);
        Assertions.assertNotNull(session);

        CsrfToken csrfToken = (CsrfToken) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        Assertions.assertNotNull(csrfToken);

        MvcResult loginResult = mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", login)
                        .param("password", password)
                        .param(csrfToken.getParameterName(), csrfToken.getToken()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin"))
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
