/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.service.notifications.email.impl;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.cerberus.core.service.notifications.email.IEmailThemeService;
import org.cerberus.core.service.notifications.email.entity.BuildContentRow;
import org.cerberus.core.service.notifications.email.entity.EmailTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailRendererTest {

    private EmailRenderer renderer;
    private EmailTheme theme;

    @BeforeEach
    void setUp() throws Exception {
        renderer = new EmailRenderer();
        IEmailThemeService themeService = system -> theme;
        Field f = EmailRenderer.class.getDeclaredField("emailThemeService");
        f.setAccessible(true);
        f.set(renderer, themeService);
        theme = new EmailTheme("Acme QA", "https://acme.test/logo.png", "#ff0000", "#eeeeee", "Acme footer", "https://acme.test");
    }

    @Test
    void rendersThemeInLayout() {
        String html = renderer.render("sample", "Weekly summary", Map.of("message", "Hello", "link", "https://acme.test/x"), Locale.ENGLISH, "");
        assertTrue(html.contains("Weekly summary"));
        assertTrue(html.contains("src=\"cid:icon-mail\""));
        assertTrue(html.contains("src=\"https://acme.test/logo.png\""));
        assertTrue(html.contains("background-color:#ff0000"));
        assertTrue(html.contains("Acme footer"));
        // The footer names the instance that sent the mail.
        assertTrue(html.contains("Acme QA</span>"));
        assertTrue(html.contains("href=\"https://acme.test\""));
        assertTrue(html.contains("Acme QA"));
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("href=\"https://acme.test/x\""));
    }

    @Test
    void escapesVariablesAndOmitsEmptyLogo() {
        theme = new EmailTheme("Acme", "", "#ff0000", "#eeeeee", "", "");
        String html = renderer.render("sample", "T", Map.of("message", "<script>alert(1)</script>", "link", ""), Locale.ENGLISH, "");
        assertFalse(html.contains("<script>"));
        // No logo (the title icon is the only image) and no button.
        assertFalse(html.contains("height=\"28\""));
        assertFalse(html.contains("<a "));
    }

    @Test
    void footerWithoutInstanceUrlOnlyShowsTheName() {
        theme = new EmailTheme("Acme", "", "#ff0000", "#eeeeee", "", "");
        String html = render("sample", Map.of("message", "Hi", "link", ""));
        assertTrue(html.contains("Acme</span>"));
        assertFalse(html.contains("</span> - "));
    }

    private Map<String, Object> campaignEndModel(int notOkTotal, int maxLines) {
        Map<String, Object> model = new HashMap<>();
        model.put("tag", "Tag<1>");
        model.put("campaign", "Nightly");
        model.put("urlReport", "https://acme.test/ReportingExecutionByTag.jsp?Tag=Tag1");
        model.put("start", "2026-10-19 02:00:00");
        model.put("end", "2026-10-19 02:30:00");
        model.put("durationMin", 30L);
        model.put("environments", "QA");
        model.put("countries", "FR");
        model.put("applications", "");
        model.put("systems", "SYS");
        model.put("robots", "");
        model.put("tiles", List.of(
                new EmailGenerationService.StatusTile("10", "Total", "", "#eff6ff", "#bfdbfe", "#2563eb"),
                new EmailGenerationService.StatusTile("8", "OK", "80%", "#f0fdf4", "#bbf7d0", "#16a34a"),
                new EmailGenerationService.StatusTile("2", "Not OK", "20%", "#fef2f2", "#fecaca", "#dc2626")));
        model.put("statusRows", List.of(
                new EmailGenerationService.StatusRow("OK", "OK", "#22c55e", 8, 80),
                new EmailGenerationService.StatusRow("KO", "KO", "#ef4444", 2, 20)));
        model.put("totalExecutions", 10);
        model.put("hasDonut", true);
        model.put("donutSize", 190);
        model.put("executionRows", List.of(
                new EmailGenerationService.ExecutionRow(1, "folder", "TC001", "QA", "FR", "chrome", "KO", "#ef4444", "#ffffff", "https://acme.test/TestCaseExecution.jsp?executionId=5", "<b>desc</b>", "Element <div> not found"),
                new EmailGenerationService.ExecutionRow(2, "folder", "TC002", "QA", "FR", "chrome", "FA", "#f97316", "#ffffff", "", "", "")));
        model.put("notOkTotal", notOkTotal);
        model.put("maxLines", maxLines);
        return model;
    }

    @Test
    void rendersCampaignEnd() {
        String html = renderer.render("campaign-end", "Campaign Nightly finished", campaignEndModel(2, 100), Locale.ENGLISH, "");
        assertTrue(html.contains("Campaign Nightly finished"));
        assertTrue(html.contains("Tag&lt;1&gt;"));
        assertTrue(html.contains("Report by status (10 executions)"));
        assertTrue(html.contains("src=\"cid:img-donut\""));
        assertTrue(html.contains("background-color:#22c55e"));
        assertTrue(html.contains("TC001"));
        assertTrue(html.contains("executionId=5"));
        assertTrue(html.contains("&lt;b&gt;desc&lt;/b&gt;"));
        // The control message is displayed (escaped) with the color of the status, only on the rows that have one.
        assertTrue(html.contains("Element &lt;div&gt; not found"));
        assertTrue(html.contains("border-left:3px solid #ef4444"));
        assertEquals(1, html.split("border-left:3px solid", -1).length - 1);
        assertTrue(html.contains("Open the campaign report"));
        assertFalse(html.contains("Only the first"));
        // Empty lists are not displayed.
        assertFalse(html.contains("Applications"));
        assertFalse(html.contains("Robots"));
    }

    @Test
    void campaignEndMentionsTruncatedList() {
        String html = renderer.render("campaign-end", "T", campaignEndModel(250, 100), Locale.ENGLISH, "");
        assertTrue(html.contains("Only the first"));
    }

    private String render(String template, Map<String, Object> model) {
        return renderer.render(template, "Title", model, Locale.ENGLISH, "");
    }

    @Test
    void rendersAccountCreation() {
        String html = render("account-creation", Map.of("name", "Jo <b>", "login", "jo", "password", "p@ss", "url", "https://acme.test/"));
        assertTrue(html.contains("Jo &lt;b&gt;"));
        assertTrue(html.contains("p@ss"));
        assertTrue(html.contains("href=\"https://acme.test/\""));
        assertTrue(html.contains("Open Acme QA"));
    }

    @Test
    void rendersForgotPassword() {
        String html = render("forgot-password", Map.of("name", "Jo", "login", "jo", "linkUrl", "https://acme.test/ChangePassword.jsp?login=jo&confirmationToken=abc"));
        assertTrue(html.contains("Reset your password"));
        assertTrue(html.contains("confirmationToken=abc"));
    }

    @Test
    void rendersCampaignStart() {
        String html = render("campaign-start", Map.of("tag", "T1", "campaign", "Nightly", "environments", "QA", "countries", "", "urlReport", "https://acme.test/r"));
        assertTrue(html.contains("Nightly"));
        assertTrue(html.contains("on <b>QA</b>"));
        assertFalse(html.contains("for <b>"));
        assertTrue(html.contains("href=\"https://acme.test/r\""));
    }

    @Test
    void rendersExecutionStartAndEnd() {
        Map<String, Object> model = new HashMap<>();
        model.put("executionId", 42L);
        model.put("tag", null);
        model.put("test", "folder");
        model.put("testcase", "TC001");
        model.put("environment", "QA");
        model.put("country", "FR");
        model.put("robot", "");
        model.put("urlExecution", "https://acme.test/TestCaseExecution.jsp?executionId=42");
        String start = render("execution-start", model);
        assertTrue(start.contains("folder - TC001"));
        assertFalse(start.contains(">Robot<"));
        assertFalse(start.contains(">Tag<"));

        model.put("status", "KO");
        model.put("statusColor", "#ef4444");
        model.put("statusTextColor", "#ffffff");
        String end = render("execution-end", model);
        assertTrue(end.contains("KO"));
        assertTrue(end.contains("color:#ffffff;background-color:#ef4444"));
        assertTrue(end.contains("Analyse the result"));
    }

    @Test
    void rendersTestcaseChangeVariants() {
        Map<String, Object> model = new HashMap<>();
        model.put("test", "folder");
        model.put("testcase", "TC001");
        model.put("version", 3);
        model.put("urlTestcase", "https://acme.test/TestCaseScript.jsp?test=folder&testcase=TC001");

        model.put("eventType", "TESTCASE_CREATE");
        assertTrue(render("testcase-change", model).contains("was just created"));
        model.put("eventType", "TESTCASE_UPDATE");
        String updated = render("testcase-change", model);
        assertTrue(updated.contains("updated to version"));
        assertTrue(updated.contains("Open the testcase"));
        model.put("eventType", "TESTCASE_DELETE");
        model.put("urlTestcase", "");
        String deleted = render("testcase-change", model);
        assertTrue(deleted.contains("was just deleted"));
        assertFalse(deleted.contains("Open the testcase"));
    }

    @Test
    void rendersEnvironmentAndChainMails() {
        String disabled = render("disable-environment", Map.of("environment", "QA", "country", "FR", "build", "", "revision", ""));
        assertTrue(disabled.contains("has been disabled"));
        assertFalse(disabled.contains("revision <b"));
        String chain = render("new-chain", Map.of("environment", "QA", "country", "FR", "chain", "NIGHT (Nightly batch)"));
        assertTrue(chain.contains("NIGHT (Nightly batch)"));
    }

    @Test
    void rendersRevisionChangeWithAndWithoutContent() {
        Map<String, Object> model = new HashMap<>();
        model.put("environment", "QA");
        model.put("country", "FR");
        model.put("build", "B1");
        model.put("revision", "R2");
        model.put("lastBuild", "B1");
        model.put("lastRevision", "R1");
        model.put("buildContent", List.of(
                new BuildContentRow("B1", "R2", "App", "Proj", "BUG-1", "https://bugs.test/BUG-1", "T-9", "Jo", "1.2", "", "<i>Fix</i>")));
        String html = render("revision-change", model);
        assertTrue(html.contains("href=\"https://bugs.test/BUG-1\""));
        assertTrue(html.contains("BUG-1"));
        assertTrue(html.contains("&lt;i&gt;Fix&lt;/i&gt;"));
        assertTrue(html.contains("Modifications since the last change"));

        model.put("buildContent", List.of());
        assertFalse(render("revision-change", model).contains("Modifications since the last change"));
    }

    @Test
    void campaignEndUsesSectionIcons() {
        String html = render("campaign-end", campaignEndModel(2, 100));
        assertTrue(html.contains("cid:icon-globe"));
        assertTrue(html.contains("cid:icon-chart-pie"));
        assertTrue(html.contains("cid:icon-circle-x"));
        assertTrue(html.contains("Non OK testcases (2)"));
    }

    /**
     * Writes an HTML preview of the main mails, to open in a browser :
     * mvn test -Dtest=EmailRendererTest -Demail.preview.dir=/tmp/emails
     * The cid: icons are pointed to the icon files so that the browser displays them.
     */
    @Test
    void writesPreviews() throws Exception {
        String dir = System.getProperty("email.preview.dir");
        assumeTrue(dir != null && !dir.isEmpty());
        Path out = Paths.get(dir);
        Files.createDirectories(out);
        String icons = Paths.get("src/main/resources/emails/icons").toAbsolutePath().toUri().toString();

        Map<String, Object> exec = new HashMap<>();
        exec.put("executionId", 42L);
        exec.put("tag", "Nightly.20261019");
        exec.put("test", "folder");
        exec.put("testcase", "TC001");
        exec.put("environment", "QA");
        exec.put("country", "FR");
        exec.put("robot", "chrome");
        exec.put("status", "KO");
        exec.put("statusColor", "#ef4444");
        exec.put("statusTextColor", "#ffffff");
        exec.put("urlExecution", "https://acme.test/e/42");

        Map<String, Object> rev = new HashMap<>();
        rev.put("environment", "QA");
        rev.put("country", "FR");
        rev.put("build", "B1");
        rev.put("revision", "R2");
        rev.put("lastBuild", "B1");
        rev.put("lastRevision", "R1");
        rev.put("buildContent", List.of(new BuildContentRow("B1", "R2", "App", "Proj", "BUG-1", "https://bugs.test/1", "T-9", "Jo", "1.2", "", "Fix the login")));

        Map<String, Map<String, Object>> previews = new java.util.LinkedHashMap<>();
        previews.put("campaign-end", campaignEndModel(2, 100));
        previews.put("execution-end", exec);
        previews.put("revision-change", rev);
        previews.put("account-creation", Map.of("name", "Jo", "login", "jo", "password", "changeme", "url", "https://acme.test/"));
        previews.put("disable-environment", Map.of("environment", "QA", "country", "FR", "build", "B1", "revision", "R2"));
        // Donut of the campaign end preview, written next to the pages and referenced instead of cid:img-donut.
        Files.write(out.resolve("donut.png"), DonutChartRenderer.render(List.of(
                new DonutChartRenderer.Slice("#22c55e", 8), new DonutChartRenderer.Slice("#ef4444", 2)), "80%", "OK RATE", "#d97706"));
        for (Map.Entry<String, Map<String, Object>> e : previews.entrySet()) {
            String html = renderer.render(e.getKey(), "Preview of " + e.getKey(), e.getValue(), Locale.ENGLISH, "").replaceAll("cid:icon-([a-z0-9-]+)", icons + "$1.png").replace("cid:img-donut", "donut.png");
            Files.writeString(out.resolve(e.getKey() + ".html"), html);
        }
    }

    @Test
    void titleIconColorFollowsTheMail() {
        // Default color of the family : blue for a start, green for an end.
        assertTrue(render("campaign-start", Map.of("tag", "T", "campaign", "C", "environments", "", "countries", "", "urlReport", "https://x"))
                .contains("width:32px;height:32px;border-radius:12px;background-color:#3b82f6"));
        Map<String, Object> end = campaignEndModel(0, 100);
        end.remove("titleColor");
        assertTrue(render("campaign-end", end).contains("width:32px;height:32px;border-radius:12px;background-color:#22c55e"));
        // The generation overrides it : by hex or by palette name.
        end.put("titleColor", "#e63757");
        assertTrue(render("campaign-end", end).contains("width:32px;height:32px;border-radius:12px;background-color:#e63757"));
        Map<String, Object> deleted = new HashMap<>();
        deleted.put("eventType", "TESTCASE_DELETE");
        deleted.put("test", "f");
        deleted.put("testcase", "t");
        deleted.put("version", 1);
        deleted.put("urlTestcase", "");
        deleted.put("titleColorName", "red");
        assertTrue(render("testcase-change", deleted).contains("width:32px;height:32px;border-radius:12px;background-color:#ef4444"));
    }
}
