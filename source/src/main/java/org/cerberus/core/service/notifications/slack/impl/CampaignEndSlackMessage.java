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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.service.notifications.common.CampaignEndFigures;
import org.cerberus.core.service.notifications.email.impl.EmailStatusPalette;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builds the end of campaign Slack message (Block Kit) : compact, readable in a few seconds, even on a phone.
 * Title, context line, one line of results, the first anomalies and a button to the campaign report.
 * Every text of the message is defined here.
 */
final class CampaignEndSlackMessage {

    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";
    private static final String NO_EXECUTION = "No execution";

    // Discreet icons : Unicode symbols in text presentation (U+FE0E), not the big colored emojis.
    // Slack cannot size nor color them, they take the color of the text.
    private static final String ICON_KO = "\u2297";
    private static final String ICON_FA = "\u26A0\uFE0E";
    private static final String ICON_OTHER = "\u2298";

    private static final int MAX_ANOMALIES = 3;
    private static final int DESCRIPTION_MAX_LENGTH = 100;
    private static final int VALUE_MAX_LENGTH = 60;
    private static final int MESSAGE_MAX_LENGTH = 120;
    private static final String ELLIPSIS = "…";

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String NOT_EQUAL_SEPARATOR = "' is not equal to '";

    private CampaignEndSlackMessage() {
    }

    /**
     * @param tag the campaign execution
     * @param executions the executions of the campaign (the OK ones are ignored)
     * @param cerberusUrl the URL of Cerberus, ending with /
     * @param channel the Slack channel, null for the one of the webhook
     */
    static JSONObject build(Tag tag, List<TestCaseExecution> executions, String cerberusUrl, String channel) throws Exception {
        CampaignEndFigures figures = new CampaignEndFigures(tag);
        String reportUrl = cerberusUrl + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(tag.getTag());
        List<TestCaseExecution> anomalies = executions.stream()
                .filter(e -> !TestCaseExecution.CONTROLSTATUS_OK.equals(e.getControlStatus())).toList();

        // The global status is the CI verdict of the campaign : all executions OK is not a success when the CI rules say so.
        boolean ciKnown = !StringUtil.isEmptyOrNull(tag.getCiResult());
        boolean success = ciKnown ? "OK".equalsIgnoreCase(tag.getCiResult()) : figures.total() > 0 && figures.notOk() == 0 && figures.pending() == 0;
        boolean noExecution = figures.total() == 0 && !ciKnown;
        String status = noExecution ? NO_EXECUTION : success ? SUCCESS : FAILURE;
        // The status is the color of the bar, the word is kept in the fallback of the notifications.

        String campaign = StringUtil.isEmptyOrNull(tag.getCampaign()) ? tag.getTag() : tag.getCampaign();
        String title = escape(campaign);
        String results = results(tag, figures);
        // Title : the campaign, its verdict and the CI score, on one line : "Adtech_Monitoring_Prod succeeded · CI score 0 < 100".
        // Cerberus rule : the CI is OK when the score is under the threshold.
        String heading = "*" + title + (noExecution ? "" : success ? " succeeded" : " failed") + "*"
                + (ciKnown ? " · CI score " + tag.getCiScore() + (tag.getCiScore() < tag.getCiScoreThreshold() ? " < " : " ≥ ") + tag.getCiScoreThreshold() : "");

        JSONArray blocks = new JSONArray();
        blocks.put(section(heading));
        String context = context(tag, executions);
        if (!context.isEmpty()) {
            blocks.put(context(context));
        }
        blocks.put(section(results));
        addAnomalies(blocks, anomalies, cerberusUrl, reportUrl);
        blocks.put(new JSONObject().put("type", "actions").put("elements", new JSONArray().put(new JSONObject()
                .put("type", "button").put("text", new JSONObject().put("type", "plain_text").put("text", success ? "View report" : "View full report"))
                .put("url", reportUrl))));

        // Bar on the left of the message : green or red like the status. The blocks are in the attachment and the message has no
        // text of its own : it would be displayed above them. The fallback of the attachment is used by the notifications.
        JSONObject attachment = new JSONObject()
                .put("color", noExecution ? EmailStatusPalette.color("NE") : success ? TestCaseExecution.CONTROLSTATUS_OK_COL_EXT : TestCaseExecution.CONTROLSTATUS_KO_COL_EXT)
                .put("fallback", escape(status + " — " + campaign) + " : " + results.replace("*", "").replace("\n", " · "))
                .put("blocks", blocks);
        JSONObject message = new JSONObject()
                .put("attachments", new JSONArray().put(attachment))
                .put("username", "Cerberus");
        if (!StringUtil.isEmptyOrNull(channel)) {
            message.put("channel", channel);
        }
        return message;
    }

    // Two lines : the total in bold, then the detail in plain text (the bold total stands out from it) :
    // 377 executions
    // 140 OK · 237 Non OK (110 KO · 126 FA · 1 QE) · 3 flaky
    // Only the statuses present, all the statuses other than OK are grouped under "Non OK".
    private static String results(Tag tag, CampaignEndFigures figures) {
        if (figures.total() == 0) {
            return "*" + NO_EXECUTION + "*";
        }
        String total = "*" + figures.total() + (figures.total() == 1 ? " execution" : " executions") + "*";
        List<String> details = new ArrayList<>();
        if (figures.count("OK") > 0) {
            details.add(figures.count("OK") + " OK");
        }
        int nonOk = 0;
        List<String> nonOkDetails = new ArrayList<>();
        for (String status : EmailStatusPalette.ORDER) {
            if (!"OK".equals(status) && figures.count(status) > 0) {
                nonOk += figures.count(status);
                nonOkDetails.add(figures.count(status) + " " + status);
            }
        }
        if (nonOk > 0) {
            details.add(nonOk + " Non OK (" + String.join(" · ", nonOkDetails) + ")");
        }
        if (tag.getNbFlaky() > 0) {
            details.add(tag.getNbFlaky() + " flaky");
        }
        return details.isEmpty() ? total : total + "\n" + String.join(" · ", details);
    }

    // Oct 29 · 11:00 | 39 min | DEV2 | FR | Chrome : each element is highlighted on its own (code span), the separators are not.
    // The date and the time are grouped. Several environments, countries or browsers : their number. Only what is available.
    private static String context(Tag tag, List<TestCaseExecution> executions) {
        List<String> chips = new ArrayList<>();
        if (tag.getDateCreated() != null) {
            chips.add(tag.getDateCreated().toLocalDateTime().format(DAY_FORMAT) + " · " + tag.getDateCreated().toLocalDateTime().format(TIME_FORMAT));
            if (tag.getDateEndQueue() != null) {
                chips.add(duration((tag.getDateEndQueue().getTime() - tag.getDateCreated().getTime()) / 60000));
            }
        }
        chips.add(summary(values(tag.getEnvironmentList()), "environments"));
        chips.add(summary(values(tag.getCountryList()), "countries"));
        chips.add(summary(executions.stream().map(TestCaseExecution::getBrowser).filter(b -> !StringUtil.isEmptyOrNull(b)).distinct().toList(), "browsers"));
        return chips.stream().filter(chip -> !chip.isEmpty()).map(chip -> "`" + escape(chip).replace("`", "'") + "`").collect(Collectors.joining(" · "));
    }

    // The value when there is only one, otherwise the number ("2 countries").
    private static String summary(List<String> values, String plural) {
        return values.isEmpty() ? "" : values.size() == 1 ? values.get(0) : values.size() + " " + plural;
    }

    private static String duration(long minutes) {
        if (minutes < 1) {
            return "< 1 min";
        }
        return minutes < 60 ? minutes + " min" : (minutes / 60) + "h " + String.format("%02d", minutes % 60);
    }

    // One section per anomaly (testcase and description, normal size), followed by its detail in a context block (small and grey).
    // The title "Anomalies detected" is in the first section : no block spent on it.
    private static void addAnomalies(JSONArray blocks, List<TestCaseExecution> anomalies, String cerberusUrl, String reportUrl) {
        for (int i = 0; i < Math.min(MAX_ANOMALIES, anomalies.size()); i++) {
            TestCaseExecution execution = anomalies.get(i);
            String id = "*" + escape(execution.getTestCase()) + "*";
            if (execution.getId() > 0) {
                id = "<" + cerberusUrl + "TestCaseExecution.jsp?executionId=" + execution.getId() + "|" + id + ">";
            }
            String description = oneLine(execution.getDescription(), DESCRIPTION_MAX_LENGTH);
            blocks.put(section((i == 0 ? "*Anomalies detected*\n" : "") + statusIcon(execution.getControlStatus()) + " " + id + " · "
                    + (description.isEmpty() ? label(execution.getControlStatus()) : escape(description))));
            String detail = detail(execution.getControlMessage());
            if (!detail.isEmpty()) {
                blocks.put(context(detail));
            }
        }
        if (anomalies.size() > MAX_ANOMALIES) {
            int more = anomalies.size() - MAX_ANOMALIES;
            blocks.put(context("+ " + more + (more > 1 ? " more anomalies" : " more anomaly") + " — <" + reportUrl + "|View report>"));
        }
    }

    // Expected and obtained values when the control message tells them, otherwise a short version of the message : nothing is guessed.
    private static String detail(String controlMessage) {
        String message = controlMessage == null ? "" : controlMessage.trim();
        int separator = message.indexOf(NOT_EQUAL_SEPARATOR);
        int end = message.lastIndexOf('\'');
        if (message.startsWith("'") && separator > 0 && end > separator + NOT_EQUAL_SEPARATOR.length() - 1) {
            String obtained = message.substring(1, separator);
            String expected = message.substring(separator + NOT_EQUAL_SEPARATOR.length(), end);
            return "Expected: " + code(expected) + " · Actual: " + code(obtained);
        }
        return escape(oneLine(message, MESSAGE_MAX_LENGTH));
    }

    // Value in a code span : the span cannot contain a back quote.
    private static String code(String value) {
        return "`" + escape(oneLine(value, VALUE_MAX_LENGTH)).replace("`", "'") + "`";
    }

    private static String statusIcon(String status) {
        return "KO".equals(status) ? ICON_KO : "FA".equals(status) ? ICON_FA : ICON_OTHER;
    }

    private static String label(String status) {
        return "KO".equals(status) || "FA".equals(status) ? "Failed " + status : String.valueOf(status);
    }

    private static JSONObject section(String text) {
        return new JSONObject().put("type", "section").put("text", new JSONObject().put("type", "mrkdwn").put("text", text));
    }

    private static JSONObject context(String text) {
        return new JSONObject().put("type", "context").put("elements", new JSONArray().put(new JSONObject().put("type", "mrkdwn").put("text", text)));
    }

    private static List<String> values(String jsonArray) {
        List<String> values = new ArrayList<>();
        if (!StringUtil.isEmptyOrNull(jsonArray)) {
            try {
                new JSONArray(jsonArray).forEach(value -> values.add(String.valueOf(value)));
            } catch (Exception ex) {
                // Not a list : nothing to display.
            }
        }
        return values;
    }

    // Slack mrkdwn control characters.
    static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // One line (a message can be a stack trace), limited in length.
    private static String oneLine(String text, int maxLength) {
        return shorten(text == null ? "" : text.replaceAll("\\s+", " ").trim(), maxLength);
    }

    private static String shorten(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1).trim() + ELLIPSIS;
    }
}
