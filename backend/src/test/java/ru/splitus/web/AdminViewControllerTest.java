package ru.splitus.web;

import java.time.OffsetDateTime;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import ru.splitus.admin.AdminCheckCommandService;
import ru.splitus.admin.AdminCheckDetails;
import ru.splitus.admin.AdminCheckSummary;
import ru.splitus.admin.AdminExpenseView;
import ru.splitus.admin.AdminParticipantMergeView;
import ru.splitus.admin.AdminParticipantView;
import ru.splitus.admin.AdminReadService;
import ru.splitus.config.AdminSecurityProperties;
import ru.splitus.check.ParticipantType;
import ru.splitus.expense.ExpenseStatus;
import ru.splitus.settlement.SettlementBalance;
import ru.splitus.settlement.SettlementPlan;
import ru.splitus.settlement.SettlementResult;

/**
 * Tests admin view controller.
 */
class AdminViewControllerTest {

    @Test
    void dashboardShowsReadOnlyResults() {
        Fixture fixture = new Fixture();
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = fixture.controller.dashboard("trip", model, new StaticPrincipal("admin"));

        Assertions.assertEquals("admin-home", viewName);
        Assertions.assertEquals("local", model.get("environmentName"));
        Assertions.assertEquals("spring-security", model.get("authMode"));
        Assertions.assertEquals("trip", model.get("query"));
        Assertions.assertEquals("admin", model.get("currentAdminLogin"));
        List<?> checks = (List<?>) model.get("checks");
        Assertions.assertEquals(1, checks.size());
    }

    @Test
    void checkDetailsShowsDetailView() {
        Fixture fixture = new Fixture();
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = fixture.controller.checkDetails(fixture.checkId, model, new StaticPrincipal("admin"));

        Assertions.assertEquals("admin-check-detail", viewName);
        AdminCheckDetails details = (AdminCheckDetails) model.get("details");
        Assertions.assertNotNull(details);
        Assertions.assertEquals("Trip to Kazan", details.getSummary().getTitle());
        Assertions.assertEquals(1, details.getExpenses().size());
        Assertions.assertEquals(2, details.getSettlement().getBalances().size());
        Assertions.assertEquals("admin", model.get("currentAdminLogin"));
    }

    @Test
    void checkDetailsThrowsNotFoundForUnknownCheck() {
        Fixture fixture = new Fixture();

        ResponseStatusException exception = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> fixture.controller.checkDetails(UUID.randomUUID(), new ExtendedModelMap(), new StaticPrincipal("admin"))
        );

        Assertions.assertEquals(404, exception.getStatus().value());
    }

    @Test
    void loginRedirectsAuthenticatedAdminToDashboard() {
        Fixture fixture = new Fixture();

        String viewName = fixture.controller.login(null, null, new ExtendedModelMap(), new StaticPrincipal("admin"));

        Assertions.assertEquals("redirect:/admin", viewName);
    }

    @Test
    void deleteCheckRedirectsWithSuccessMessage() {
        Fixture fixture = new Fixture();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = fixture.controller.deleteCheck(fixture.checkId, "Trip to Kazan", redirectAttributes);

        Assertions.assertEquals("redirect:/admin", viewName);
        Assertions.assertEquals("Чек \"Trip to Kazan\" удалён", redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    void deleteCheckRedirectsBackToDetailsOnValidationError() {
        Fixture fixture = new Fixture();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String viewName = fixture.controller.deleteCheck(fixture.checkId, "wrong", redirectAttributes);

        Assertions.assertEquals("redirect:/admin/checks/" + fixture.checkId, viewName);
        Assertions.assertEquals(
                "Confirmation title does not match the check title",
                redirectAttributes.getFlashAttributes().get("errorMessage")
        );
    }

    /**
     * Test fixture.
     */
    private static class Fixture {
        private final UUID checkId = UUID.randomUUID();
        private final InMemoryCheckBookRepository checkBookRepository = new InMemoryCheckBookRepository(checkId, "Trip to Kazan");
        private final AdminCheckCommandService commandService = new AdminCheckCommandService(checkBookRepository);
        private final AdminViewController controller = new AdminViewController(
                new StubAdminReadService(checkId),
                commandService,
                adminSecurityProperties()
        );

        private static AdminSecurityProperties adminSecurityProperties() {
            AdminSecurityProperties properties = new AdminSecurityProperties();
            properties.setEnvironmentName("local");
            return properties;
        }
    }

    /**
     * Stub admin read service.
     */
    private static class StubAdminReadService implements AdminReadService {
        private final UUID expectedCheckId;

        /**
         * Creates a new stub admin read service instance.
         */
        StubAdminReadService(UUID expectedCheckId) {
            this.expectedCheckId = expectedCheckId;
        }

        /**
         * Searches checks.
         */
        @Override
        public List<AdminCheckSummary> searchChecks(String query) {
            return Collections.singletonList(new AdminCheckSummary(
                    expectedCheckId,
                    "Trip to Kazan",
                    "invite-123",
                    "RUB",
                    Long.valueOf(-1001234567890L),
                    true,
                    501L,
                    "owner",
                    2,
                    1,
                    OffsetDateTime.parse("2026-04-05T11:00:00+03:00")
            ));
        }

        /**
         * Finds check details.
         */
        @Override
        public Optional<AdminCheckDetails> findCheckDetails(UUID checkId) {
            if (!expectedCheckId.equals(checkId)) {
                return Optional.empty();
            }
            AdminCheckSummary summary = searchChecks("").get(0);
            List<AdminParticipantView> participants = Arrays.asList(
                    new AdminParticipantView(
                            UUID.randomUUID(),
                            ParticipantType.REGISTERED,
                            "alice",
                            UUID.randomUUID(),
                            "alice",
                            null,
                            null,
                            OffsetDateTime.parse("2026-04-05T11:01:00+03:00")
                    ),
                    new AdminParticipantView(
                            UUID.randomUUID(),
                            ParticipantType.GUEST,
                            "bob",
                            null,
                            null,
                            null,
                            null,
                            OffsetDateTime.parse("2026-04-05T11:02:00+03:00")
                    )
            );
            List<AdminParticipantMergeView> merges = Collections.emptyList();
            List<AdminExpenseView> expenses = Collections.singletonList(new AdminExpenseView(
                    UUID.randomUUID(),
                    900L,
                    ExpenseStatus.VALID,
                    "alice",
                    "alice",
                    "alice",
                    "Dinner",
                    "/add_expense invite-123 900 alice,bob | Dinner",
                    Long.valueOf(-1001234567890L),
                    Long.valueOf(77L),
                    OffsetDateTime.parse("2026-04-05T11:10:00+03:00"),
                    OffsetDateTime.parse("2026-04-05T11:10:00+03:00"),
                    Collections.emptyList()
            ));
            SettlementResult settlement = new SettlementResult(
                    Arrays.asList(
                            new SettlementBalance("alice", 450L),
                            new SettlementBalance("bob", -450L)
                    ),
                    new SettlementPlan(Collections.singletonList(new SettlementPlan.Transfer("bob", "alice", 450L)))
            );
            return Optional.of(new AdminCheckDetails(summary, participants, merges, expenses, settlement));
        }
    }

    /**
     * Static test principal.
     */
    private static class StaticPrincipal implements Principal {
        private final String name;

        /**
         * Creates a new static principal instance.
         */
        StaticPrincipal(String name) {
            this.name = name;
        }

        /**
         * Returns the principal name.
         */
        @Override
        public String getName() {
            return name;
        }
    }

    /**
     * In-memory check repository for delete flow.
     */
    private static class InMemoryCheckBookRepository implements ru.splitus.check.CheckBookRepository {
        private final UUID expectedCheckId;
        private final String title;
        private boolean deleted;

        /**
         * Creates a new in-memory repository instance.
         */
        InMemoryCheckBookRepository(UUID expectedCheckId, String title) {
            this.expectedCheckId = expectedCheckId;
            this.title = title;
        }

        /**
         * Executes save.
         */
        @Override
        public ru.splitus.check.CheckBook save(ru.splitus.check.CheckBook checkBook) {
            return checkBook;
        }

        /**
         * Finds by id.
         */
        @Override
        public Optional<ru.splitus.check.CheckBook> findById(UUID checkId) {
            if (deleted || !expectedCheckId.equals(checkId)) {
                return Optional.empty();
            }
            return Optional.of(new ru.splitus.check.CheckBook(
                    expectedCheckId,
                    title,
                    UUID.randomUUID(),
                    "invite-123",
                    null,
                    "RUB",
                    true,
                    OffsetDateTime.parse("2026-04-05T11:00:00+03:00")
            ));
        }

        /**
         * Finds by invite token.
         */
        @Override
        public Optional<ru.splitus.check.CheckBook> findByInviteToken(String inviteToken) {
            return Optional.empty();
        }

        /**
         * Counts created by owner since.
         */
        @Override
        public int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since) {
            return 0;
        }

        /**
         * Deletes a check by id.
         */
        @Override
        public boolean deleteById(UUID checkId) {
            if (deleted || !expectedCheckId.equals(checkId)) {
                return false;
            }
            deleted = true;
            return true;
        }
    }
}
