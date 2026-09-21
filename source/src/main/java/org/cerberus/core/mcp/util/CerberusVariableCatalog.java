/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.mcp.util;

import java.util.List;

/**
 * The catalogue of {@code %system.*} variables the execution engine substitutes.
 *
 * <p>This list exists nowhere else. The variables are implemented as literal replacements inside
 * {@code VariableService.decodeStringWithSystemVariable}, are not stored in any table, and the
 * documentation only mentions them one at a time in the changelog of the release that introduced
 * them. Reading the engine source is currently the only way to know what may be written in a value
 * field, which is why this catalogue is transcribed here rather than derived from data.</p>
 *
 * <p>Each entry records <em>when</em> the variable resolves, not only what it holds. That is the
 * part that actually costs time: a variable used too early does not raise an error, it silently
 * substitutes an empty string or the literal {@code <null>}, and the failure surfaces much later as
 * an action that acted on the wrong value.</p>
 *
 * <p>Kept in sync by hand with {@code VariableService}. When a variable is added there, add it
 * here; a missing entry costs an agent the same time this catalogue is meant to save.</p>
 */
public final class CerberusVariableCatalog {

    private CerberusVariableCatalog() {
    }

    /** Value the engine substitutes when a variable exists but has nothing to resolve to. */
    public static final String NULL_MARKER = "<null>";

    public static final String FAMILY_APPLICATION = "application";
    public static final String FAMILY_ENVIRONMENT = "environment";
    public static final String FAMILY_COUNTRY = "country";
    public static final String FAMILY_ROBOT = "robot";
    public static final String FAMILY_TESTCASE = "testcase";
    public static final String FAMILY_EXECUTION = "execution";
    public static final String FAMILY_STEP = "step";
    public static final String FAMILY_SERVICE = "service";
    public static final String FAMILY_PROXY = "proxy";
    public static final String FAMILY_DATE = "date";

    public static final List<String> FAMILIES = List.of(
            FAMILY_APPLICATION, FAMILY_ENVIRONMENT, FAMILY_COUNTRY, FAMILY_ROBOT,
            FAMILY_TESTCASE, FAMILY_EXECUTION, FAMILY_STEP, FAMILY_SERVICE,
            FAMILY_PROXY, FAMILY_DATE);

    /**
     * One variable an agent may write in a value field.
     *
     * @param name        the variable exactly as it must be typed, percent signs included.
     * @param family      grouping used to narrow a search.
     * @param description what the engine substitutes.
     * @param availability when it resolves, and what it yields when it cannot.
     */
    public record Variable(String name, String family, String description, String availability) {
    }

    private static final String ALWAYS = "Resolved on every execution.";
    private static final String NEEDS_SERVICE =
            "Only after the testcase has called a service. Before that the engine substitutes " + NULL_MARKER + ".";
    private static final String NEEDS_STEP =
            "Only inside a step, once at least one step has started.";
    private static final String NEEDS_ROBOT =
            "Only when the execution is driven by a robot. Empty for SRV, BAT and NONE applications.";

    private static final List<Variable> VARIABLES = List.of(
            // ---- application -------------------------------------------------------------
            new Variable("%system.SYSTEM%", FAMILY_APPLICATION,
                    "System the application under test belongs to.", ALWAYS),
            new Variable("%system.APPLI%", FAMILY_APPLICATION,
                    "Application under test.", ALWAYS),
            new Variable("%system.APP_HOST%", FAMILY_APPLICATION,
                    "Host configured for this application on the country and environment being run.",
                    "Resolved from the country/environment parameters of the application. Empty when that "
                    + "combination is not configured — the same misconfiguration that makes a run queue nothing."),
            new Variable("%system.APP_CONTEXTROOT%", FAMILY_APPLICATION,
                    "Context root configured for this application on the country and environment being run.", ALWAYS),
            new Variable("%system.APP_DOMAIN%", FAMILY_APPLICATION,
                    "First domain configured for the application, before any comma.", ALWAYS),
            new Variable("%system.APP_VAR1%", FAMILY_APPLICATION,
                    "Free variable 1 of the application's country/environment parameters.", ALWAYS),
            new Variable("%system.APP_VAR2%", FAMILY_APPLICATION,
                    "Free variable 2 of the application's country/environment parameters.", ALWAYS),
            new Variable("%system.APP_VAR3%", FAMILY_APPLICATION,
                    "Free variable 3 of the application's country/environment parameters.", ALWAYS),
            new Variable("%system.APP_VAR4%", FAMILY_APPLICATION,
                    "Free variable 4 of the application's country/environment parameters.", ALWAYS),
            new Variable("%system.APP_SECRET1%", FAMILY_APPLICATION,
                    "Secret 1 of the application's country/environment parameters. Masked in execution records.",
                    "Resolved like the other application variables, but never echoed back: prefer it to writing "
                    + "a credential into a testcase."),
            new Variable("%system.APP_SECRET2%", FAMILY_APPLICATION,
                    "Secret 2 of the application's country/environment parameters. Masked in execution records.",
                    "Same handling as APP_SECRET1."),

            // ---- environment -------------------------------------------------------------
            new Variable("%system.ENV%", FAMILY_ENVIRONMENT,
                    "Environment the data is taken from, which is the run environment unless overridden.", ALWAYS),
            new Variable("%system.ENVGP%", FAMILY_ENVIRONMENT,
                    "Group of the environment, from the ENVIRONMENT invariant.", ALWAYS),

            // ---- country -----------------------------------------------------------------
            new Variable("%system.COUNTRY%", FAMILY_COUNTRY,
                    "Country the testcase is running for.", ALWAYS),
            new Variable("%system.COUNTRYGP1%", FAMILY_COUNTRY,
                    "Group 1 of the country, from the COUNTRY invariant. Groups let a testcase branch on a set "
                    + "of countries without listing them.", ALWAYS),
            new Variable("%system.COUNTRYGP2%", FAMILY_COUNTRY, "Group 2 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP3%", FAMILY_COUNTRY, "Group 3 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP4%", FAMILY_COUNTRY, "Group 4 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP5%", FAMILY_COUNTRY, "Group 5 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP6%", FAMILY_COUNTRY, "Group 6 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP7%", FAMILY_COUNTRY, "Group 7 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP8%", FAMILY_COUNTRY, "Group 8 of the country.", ALWAYS),
            new Variable("%system.COUNTRYGP9%", FAMILY_COUNTRY, "Group 9 of the country.", ALWAYS),

            // ---- robot -------------------------------------------------------------------
            new Variable("%system.ROBOT%", FAMILY_ROBOT, "Robot the execution runs on.", NEEDS_ROBOT),
            new Variable("%system.ROBOTDECLI%", FAMILY_ROBOT,
                    "Robot declination, which falls back to the robot name when none is set.", NEEDS_ROBOT),
            new Variable("%system.ROBOTHOST%", FAMILY_ROBOT,
                    "Host of the robot executor actually selected for this run.", NEEDS_ROBOT),
            new Variable("%system.BROWSER%", FAMILY_ROBOT, "Browser used.", NEEDS_ROBOT),
            new Variable("%system.SCREENSIZE%", FAMILY_ROBOT, "Screen size used.", NEEDS_ROBOT),
            new Variable("%system.ROBOTSESSIONID%", FAMILY_ROBOT,
                    "Session id of the robot, useful to correlate with the robot's own logs.",
                    "Only once the robot session is open."),
            new Variable("%system.ROBOTPROVIDERSESSIONID%", FAMILY_ROBOT,
                    "Session id on the robot provider side, for a cloud provider.",
                    "Only when a robot provider is used, once its session is open."),
            new Variable("%system.SSIP%", FAMILY_ROBOT, "Selenium server host.", NEEDS_ROBOT),
            new Variable("%system.SSPORT%", FAMILY_ROBOT, "Selenium server port.", NEEDS_ROBOT),

            // ---- testcase ----------------------------------------------------------------
            new Variable("%system.TEST%", FAMILY_TESTCASE, "Test folder of the running testcase.", ALWAYS),
            new Variable("%system.TESTCASE%", FAMILY_TESTCASE, "Identifier of the running testcase.", ALWAYS),
            new Variable("%system.TESTCASEDESCRIPTION%", FAMILY_TESTCASE,
                    "Description of the running testcase.", ALWAYS),

            // ---- execution ---------------------------------------------------------------
            new Variable("%system.EXECUTIONID%", FAMILY_EXECUTION,
                    "Numeric id of the running execution, the same one cerberus_testcase_execution_get takes.",
                    ALWAYS),
            new Variable("%system.EXEURL%", FAMILY_EXECUTION,
                    "URL the execution started on.", ALWAYS),
            new Variable("%system.TAG%", FAMILY_EXECUTION,
                    "Tag the execution was queued under.", ALWAYS),
            new Variable("%system.EXESTART%", FAMILY_EXECUTION,
                    "Timestamp the execution started at.", ALWAYS),
            new Variable("%system.EXEELAPSEDMS%", FAMILY_EXECUTION,
                    "Milliseconds elapsed since the execution started.",
                    "Recomputed at every substitution, so two uses in the same step yield different values. "
                    + "Use it to measure, never as a stable identifier."),
            new Variable("%system.EXESTORAGEURL%", FAMILY_EXECUTION,
                    "URL of the folder where this execution's recorded files are stored.", ALWAYS),

            // ---- step --------------------------------------------------------------------
            new Variable("%system.CURRENTSTEP_SORT%", FAMILY_STEP,
                    "Sort of the step being executed.", NEEDS_STEP),
            new Variable("%system.CURRENTSTEP_INDEX%", FAMILY_STEP,
                    "Index of the step being executed, which differs from the sort when the step loops.",
                    NEEDS_STEP),
            new Variable("%system.CURRENTSTEP_STARTISO%", FAMILY_STEP,
                    "Timestamp the current step started at.", NEEDS_STEP),
            new Variable("%system.CURRENTSTEP_ELAPSEDMS%", FAMILY_STEP,
                    "Milliseconds elapsed since the current step started.",
                    NEEDS_STEP + " Recomputed at every substitution."),
            new Variable("%system.STEP.<sort>.<index>.RETURNCODE%", FAMILY_STEP,
                    "Return code of an already executed step, addressed by its sort and index — for example "
                    + "%system.STEP.10.1.RETURNCODE%. Use it to branch on what an earlier step did.",
                    "Only for a step that has already run in this execution. A step that has not run yet leaves "
                    + "the variable untouched, so the literal text reaches the action."),

            // ---- service -----------------------------------------------------------------
            new Variable("%system.LASTSERVICE_RESPONSE%", FAMILY_SERVICE,
                    "Body of the last service response, the usual way to control a service answer from a GUI, "
                    + "APK or IPA testcase.", NEEDS_SERVICE),
            new Variable("%system.LASTSERVICE_HTTPCODE%", FAMILY_SERVICE,
                    "HTTP status code of the last service call.", NEEDS_SERVICE),
            new Variable("%system.LASTSERVICE_RESPONSETIME%", FAMILY_SERVICE,
                    "Duration of the last service call, in milliseconds.", NEEDS_SERVICE),
            new Variable("%system.LASTSERVICE_CALL%", FAMILY_SERVICE,
                    "Full last service call serialised as JSON: request, response, headers.", NEEDS_SERVICE),

            // ---- proxy -------------------------------------------------------------------
            new Variable("%system.REMOTEPROXYUUID%", FAMILY_PROXY,
                    "Identifier of the recording proxy for this execution.",
                    "Only when the executor records network traffic through its proxy."),
            new Variable("%system.REMOTEPROXY_HAR_URL%", FAMILY_PROXY,
                    "URL of the HAR file captured by the recording proxy.",
                    "Only when the executor records network traffic through its proxy."),

            // ---- date --------------------------------------------------------------------
            new Variable("%system.TODAY-<format>%", FAMILY_DATE,
                    "Today's date rendered with a Java date pattern: %system.TODAY-yyyy%, %system.TODAY-MM%, "
                    + "%system.TODAY-dd%, %system.TODAY-yyyy.MM.dd%.", ALWAYS),
            new Variable("%system.TOMORROW-<format>%", FAMILY_DATE,
                    "Tomorrow's date, same formats as TODAY.", ALWAYS),
            new Variable("%system.YESTERDAY-<format>%", FAMILY_DATE,
                    "Yesterday's date, same formats as TODAY.", ALWAYS),
            new Variable("%system.<UNIT><+|-><n>-<format>%", FAMILY_DATE,
                    "Date shifted by an offset, for example %system.DAY+3-dd% or %system.MONTH-1-yyyy.MM%. "
                    + "UNIT is YEAR, MONTH, WEEK, DAY, HOUR or MINUTE.", ALWAYS)
    );

    /**
     * The whole catalogue, in a reading order that groups related variables together.
     */
    public static List<Variable> all() {
        return VARIABLES;
    }

    /**
     * Variables that exist <strong>only</strong> in the {@code %system.} form.
     *
     * <p>The engine also accepts a legacy {@code %SYS_X%} spelling, but not for every variable:
     * forty-three are substituted under both prefixes and these eight under the new one only.
     * Writing {@code %SYS_ROBOTHOST%} therefore does not fail, it simply stays in the string and
     * reaches the action as literal text — the kind of silence that is expensive to diagnose.</p>
     */
    private static final List<String> NEW_FORM_ONLY = List.of(
            "APP_SECRET1", "APP_SECRET2", "LASTSERVICE_RESPONSETIME", "REMOTEPROXYUUID",
            "REMOTEPROXY_HAR_URL", "ROBOTHOST", "ROBOTPROVIDERSESSIONID", "ROBOTSESSIONID");

    /**
     * Whether a variable also answers to the legacy {@code %SYS_X%} spelling.
     *
     * @param name the variable as written in this catalogue, percent signs included.
     * @return {@code false} for the eight variables that only exist in the {@code %system.} form,
     *         and for the parameterised families, which the legacy prefix never covered.
     */
    public static boolean hasLegacyForm(String name) {
        if (name == null || !name.startsWith("%system.") || name.contains("<")) {
            return false;
        }
        String bare = name.substring("%system.".length(), name.length() - 1);
        return !NEW_FORM_ONLY.contains(bare);
    }

    /**
     * The legacy spelling of a variable, or an empty string when it has none.
     */
    public static String legacyForm(String name) {
        return hasLegacyForm(name)
                ? "%SYS_" + name.substring("%system.".length())
                : "";
    }
}
