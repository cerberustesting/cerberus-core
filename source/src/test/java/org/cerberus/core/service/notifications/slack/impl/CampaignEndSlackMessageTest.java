/**
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
package org.cerberus.core.service.notifications.slack.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampaignEndSlackMessageTest {

    private static final String URL = "https://cerberus.test/";
    private static final String CROSS = "⊗";
    private static final String WARNING = "⚠︎";
    private static final String OTHER = "⊘";

    private static Tag.TagBuilder tag(int total, int ok, int ko, int fa, String ci) {
        return Tag.builder().tag("Adtech_Monitoring_DEV2.20261029-110000").campaign("Adtech Monitoring").ciResult(ci).ciScore(ko > 0 ? 200 : 0).ciScoreThreshold(100)
                .nbExeUsefull(total).nbOK(ok).nbKO(ko).nbFA(fa)
                .dateCreated(Timestamp.valueOf(LocalDateTime.of(2026, 10, 29, 11, 0)))
                .dateEndQueue(Timestamp.valueOf(LocalDateTime.of(2026, 10, 29, 11, 39, 26)))
                .systemList("[\"MYTF1\"]").environmentList("[\"DEV2\"]").countryList("[\"FR\"]").robotDecliList("[\"Chrome\"]");
    }

    private static TestCaseExecution execution(int id, String testcase, String status, String description, String message) {
        TestCaseExecution e = new TestCaseExecution();
        e.setId(id);
        e.setTest("Monitoring");
        e.setTestCase(testcase);
        e.setControlStatus(status);
        e.setDescription(description);
        e.setControlMessage(message);
        e.setBrowser("Chrome");
        return e;
    }

    private static JSONObject attachment(JSONObject message) {
        // No text of its own : it would be displayed above the blocks.
        assertFalse(message.has("text"));
        assertFalse(message.has("blocks"));
        return message.getJSONArray("attachments").getJSONObject(0);
    }

    private static JSONArray blocks(JSONObject message) {
        return attachment(message).getJSONArray("blocks");
    }

    private static String types(JSONObject message) {
        List<String> types = new ArrayList<>();
        blocks(message).forEach(b -> types.add(((JSONObject) b).getString("type")));
        return String.join(",", types);
    }

    private static String text(JSONObject message, int block) {
        return blocks(message).getJSONObject(block).getJSONObject("text").getString("text");
    }

    // Text of a context block.
    private static String context(JSONObject message, int block) {
        return blocks(message).getJSONObject(block).getJSONArray("elements").getJSONObject(0).getString("text");
    }

    @Test
    void allOkSuccess() throws Exception {
        JSONObject m = CampaignEndSlackMessage.build(tag(17, 17, 0, 0, "OK").build(), List.of(execution(1, "0001A", "OK", "", "")), URL, "#qa");
        assertEquals("section,context,section,actions", types(m));
        // Campaign, verdict and CI score on one line.
        assertEquals("*Adtech Monitoring succeeded* · CI score 0 < 100", text(m, 0));
        // Each element is highlighted on its own, not the separators. The date and the time are grouped.
        assertEquals("`Oct 29 · 11:00` · `39 min` · `DEV2` · `FR` · `Chrome`", context(m, 1));
        assertEquals("*17 executions*\n17 OK", text(m, 2));
        JSONObject button = blocks(m).getJSONObject(3).getJSONArray("elements").getJSONObject(0);
        assertEquals("View report", button.getJSONObject("text").getString("text"));
        assertEquals(URL + "ReportingExecutionByTag.jsp?Tag=Adtech_Monitoring_DEV2.20261029-110000", button.getString("url"));
        assertEquals("#00d27a", attachment(m).getString("color"));
        assertEquals("#qa", m.getString("channel"));
        // The word of the status is kept for the notifications.
        assertEquals("Success — Adtech Monitoring : 17 executions · 17 OK", attachment(m).getString("fallback"));
        write("slack-campaign-end-success.json", m);
    }

    @Test
    void severalEnvironmentsCountriesAndBrowsersAreCounted() throws Exception {
        TestCaseExecution firefox = execution(2, "0002A", "OK", "", "");
        firefox.setBrowser("Firefox");
        TestCaseExecution noBrowser = execution(3, "0003A", "OK", "", "");
        noBrowser.setBrowser("");
        Tag tag = tag(3, 3, 0, 0, "OK").environmentList("[\"DEV2\",\"UAT\"]").countryList("[\"FR\",\"BE\",\"ES\"]").build();
        JSONObject m = CampaignEndSlackMessage.build(tag, List.of(execution(1, "0001A", "OK", "", ""), firefox, noBrowser), URL, null);
        assertEquals("`Oct 29 · 11:00` · `39 min` · `2 environments` · `3 countries` · `2 browsers`", context(m, 1));
        // The same browser on several executions is counted once.
        JSONObject one = CampaignEndSlackMessage.build(tag, List.of(execution(1, "0001A", "OK", "", ""), execution(2, "0002A", "OK", "", "")), URL, null);
        assertTrue(context(one, 1).endsWith("`3 countries` · `Chrome`"));
    }

    @Test
    void flakyAndOtherStatusesAreInTheResults() throws Exception {
        Tag tag = tag(377, 140, 110, 126, "KO").nbQE(1).nbFlaky(3).build();
        assertEquals("*377 executions*\n140 OK · 237 Non OK (110 KO · 126 FA · 1 QE) · 3 flaky", text(CampaignEndSlackMessage.build(tag, List.of(), URL, null), 2));
        // Nothing for the statuses and the flaky that are absent. One execution : singular.
        assertEquals("*2 executions*\n2 Non OK (1 NA · 1 CA)", text(CampaignEndSlackMessage.build(tag(2, 0, 0, 0, "KO").nbNA(1).nbCA(1).build(), List.of(), URL, null), 2));
        assertEquals("*1 execution*\n1 OK", text(CampaignEndSlackMessage.build(tag(1, 1, 0, 0, "OK").build(), List.of(), URL, null), 2));
    }

    @Test
    void koAndFaWithExpectedAndActual() throws Exception {
        List<TestCaseExecution> executions = List.of(
                execution(11, "0005A", "KO", "Validation de contenu", "'pub_pre_1||vod_extrait' is not equal to 'vod_extrait||vod_extrait' (case sensitive)."),
                execution(12, "0013A", "FA", "", ""));
        JSONObject m = CampaignEndSlackMessage.build(tag(17, 15, 1, 1, "KO").build(), executions, URL, null);
        assertFalse(m.has("channel"));
        assertEquals("#e63757", attachment(m).getString("color"));
        assertTrue(attachment(m).getString("fallback").startsWith("Failure — Adtech Monitoring : 17 executions · 15 OK"));
        // Each anomaly : a section (testcase and description, normal size), then its detail in a context block (small, grey).
        assertEquals("section,context,section,section,context,section,actions", types(m));
        assertEquals("*Adtech Monitoring failed* · CI score 200 ≥ 100", text(m, 0));
        assertEquals("*17 executions*\n15 OK · 2 Non OK (1 KO · 1 FA)", text(m, 2));
        assertEquals("*Anomalies detected*\n" + CROSS + " <https://cerberus.test/TestCaseExecution.jsp?executionId=11|*0005A*> · Validation de contenu", text(m, 3));
        assertEquals("Expected: `vod_extrait||vod_extrait` · Actual: `pub_pre_1||vod_extrait`", context(m, 4));
        // No detail : no context block. No description : the status.
        assertEquals(WARNING + " <https://cerberus.test/TestCaseExecution.jsp?executionId=12|*0013A*> · Failed FA", text(m, 5));
        assertEquals("View full report", blocks(m).getJSONObject(6).getJSONArray("elements").getJSONObject(0).getJSONObject("text").getString("text"));
        write("slack-campaign-end-failure.json", m);
    }

    @Test
    void otherStatusesHaveAGenericIcon() throws Exception {
        List<TestCaseExecution> executions = List.of(execution(1, "A", "NA", "d", ""), execution(2, "B", "QU", "d", ""));
        JSONObject m = CampaignEndSlackMessage.build(tag(2, 0, 0, 0, "KO").build(), executions, URL, null);
        assertTrue(text(m, 3).contains("\n" + OTHER + " <"));
        assertTrue(text(m, 4).startsWith(OTHER + " <"));
    }

    @Test
    void moreThanThreeAnomaliesAreCounted() throws Exception {
        List<TestCaseExecution> executions = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            executions.add(execution(i, "TC" + i, "KO", "d", ""));
        }
        JSONObject m = CampaignEndSlackMessage.build(tag(8, 0, 8, 0, "KO").build(), executions, URL, null);
        assertEquals("section,context,section,section,section,section,context,actions", types(m));
        assertTrue(text(m, 5).contains("*TC3*"));
        assertFalse(m.toString().contains("*TC4*"));
        assertTrue(context(m, 6).startsWith("+ 5 more anomalies — <" + URL + "ReportingExecutionByTag.jsp?Tag="));
        assertTrue(context(CampaignEndSlackMessage.build(tag(4, 0, 4, 0, "KO").build(), executions.subList(0, 4), URL, null), 6).startsWith("+ 1 more anomaly —"));
    }

    @Test
    void noExecution() throws Exception {
        JSONObject m = CampaignEndSlackMessage.build(tag(0, 0, 0, 0, null).build(), List.of(), URL, null);
        // No verdict without execution.
        assertEquals("*Adtech Monitoring*", text(m, 0));
        assertEquals("*No execution*", text(m, 2));
        assertEquals("section,context,section,actions", types(m));
        assertEquals("#cbd5e1", attachment(m).getString("color"));
        // No execution : no browser.
        assertEquals("`Oct 29 · 11:00` · `39 min` · `DEV2` · `FR`", context(m, 1));
    }

    @Test
    void ciFailureWithAllExecutionsOkIsAFailure() throws Exception {
        JSONObject m = CampaignEndSlackMessage.build(tag(17, 17, 0, 0, "KO").build(), List.of(execution(1, "0001A", "OK", "", "")), URL, null);
        assertEquals("#e63757", attachment(m).getString("color"));
        assertTrue(attachment(m).getString("fallback").startsWith("Failure"));
        assertEquals("*Adtech Monitoring failed* · CI score 0 < 100", text(m, 0));
        assertEquals("*17 executions*\n17 OK", text(m, 2));
        assertEquals("section,context,section,actions", types(m));
    }

    @Test
    void withoutCiVerdictTheExecutionsDecide() throws Exception {
        // No CI score when there is no CI verdict.
        assertEquals("*Adtech Monitoring succeeded*", text(CampaignEndSlackMessage.build(tag(2, 2, 0, 0, null).build(), List.of(), URL, null), 0));
        assertEquals("#00d27a", attachment(CampaignEndSlackMessage.build(tag(2, 2, 0, 0, null).build(), List.of(), URL, null)).getString("color"));
        assertEquals("#e63757", attachment(CampaignEndSlackMessage.build(tag(2, 1, 1, 0, "").build(), List.of(execution(1, "A", "KO", "", "")), URL, null)).getString("color"));
    }

    @Test
    void longAndSpecialMessages() throws Exception {
        String longMessage = "Exception <b>&amp;</b> " + "x".repeat(2000) + "\n\tat org.Foo(Foo.java:1)";
        JSONObject m = CampaignEndSlackMessage.build(tag(1, 0, 1, 0, "KO").build(), List.of(execution(1, "TC<1>", "KO", "Desc & <b> " + "d".repeat(300), longMessage)), URL, null);
        String anomaly = text(m, 3);
        assertTrue(anomaly.contains("*TC&lt;1&gt;*"));
        assertTrue(anomaly.contains("Desc &amp; &lt;b&gt; ddd"));
        assertTrue(anomaly.endsWith("…"));
        // The description is limited to 100 characters, the ellipsis included.
        assertFalse(anomaly.contains("d".repeat(100)));
        String detail = context(m, 4);
        assertTrue(detail.startsWith("Exception &lt;b&gt;&amp;amp;&lt;/b&gt; xxx"));
        assertTrue(detail.endsWith("…"));
        assertFalse(detail.contains("x".repeat(121)));
        assertFalse(detail.contains("\t") || detail.contains("\n"));

        String special = "'a `tick` <b> & more' is not equal to 'e|x' (case sensitive).";
        String values = context(CampaignEndSlackMessage.build(tag(1, 0, 1, 0, "KO").build(), List.of(execution(1, "TC", "KO", "d", special)), URL, null), 4);
        assertEquals("Expected: `e|x` · Actual: `a 'tick' &lt;b&gt; &amp; more`", values);
        String longValues = context(CampaignEndSlackMessage.build(tag(1, 0, 1, 0, "KO").build(),
                List.of(execution(1, "TC", "KO", "d", "'" + "o".repeat(500) + "' is not equal to '" + "e".repeat(500) + "'")), URL, null), 4);
        assertTrue(longValues.length() < 150);

        // Special characters in the context line : escaped, and no back quote that would end the highlight.
        Tag tag = tag(1, 1, 0, 0, "OK").environmentList("[\"D<EV`2\"]").build();
        assertTrue(context(CampaignEndSlackMessage.build(tag, List.of(), URL, null), 1).contains("`D&lt;EV'2`"));
    }

    @Test
    void missingMetadata() throws Exception {
        Tag bare = Tag.builder().tag("t1").nbExeUsefull(1).nbOK(1).ciResult("OK").build();
        JSONObject m = CampaignEndSlackMessage.build(bare, List.of(execution(0, "TC", "OK", null, null)), URL, null);
        // No date, environment nor country : only the browser of the executions.
        assertEquals("section,context,section,actions", types(m));
        assertEquals("`Chrome`", context(m, 1));
        assertEquals("*t1 succeeded* · CI score 0 ≥ 0", text(m, 0));
        TestCaseExecution noBrowser = execution(0, "TC", "OK", null, null);
        noBrowser.setBrowser(null);
        assertEquals("section,section,actions", types(CampaignEndSlackMessage.build(bare, List.of(noBrowser), URL, null)));
        // Execution not stored yet (no id) : no link. No description nor message : nothing invented.
        Tag ko = Tag.builder().tag("t1").nbExeUsefull(1).nbKO(1).ciResult("KO").build();
        TestCaseExecution failure = execution(0, "TC", "KO", null, null);
        failure.setBrowser(null);
        JSONObject failed = CampaignEndSlackMessage.build(ko, List.of(failure), URL, null);
        assertEquals("section,section,section,actions", types(failed));
        assertEquals("*Anomalies detected*\n" + CROSS + " *TC* · Failed KO", text(failed, 2));
    }

    private static void write(String name, JSONObject message) throws Exception {
        Path dir = Path.of("target", "slack-samples");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name), message.toString(2));
    }
}
