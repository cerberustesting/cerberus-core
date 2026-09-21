/*
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

/* =============================================================================
 * Cerberus Monitoring - V2.
 *
 * Same single endpoint as V1 (ReadCerberusDetailInformation), no Java change.
 *
 * WHY THE PAGE WAS REBUILT RATHER THAN RESTYLED
 * --------------------------------------------
 * V1 rendered twelve hand-built <table>s. Nine of them had exactly ONE row: a
 * table whose only job is to caption a single value. That choice is what made
 * the page unreadable - the instance's heap pressure, its schema version and its
 * build number all arrived at identical visual weight, so nothing could be found
 * at a glance, and the widest of those tables overflowed its card and silently
 * clipped the Keycloak URL column off the right edge.
 *
 * Here the split is explicit and it is the whole structure:
 *   - LIVE STATE  (queue, engine, heap, schema drift, sessions) -> .crb_stat
 *     tiles at the top, the answer to "is this instance healthy".
 *   - LISTS       (running executions, scheduler triggers) -> js/global/crbTable.js
 *     in client mode, so both get search / sort / paging / an empty state.
 *   - STATIC FACTS (versions, drivers, URLs) -> .crb_spec definition lists.
 *
 * FIVE THINGS V1 DROPPED ON THE FLOOR
 * -----------------------------------
 * The endpoint returns them; the page never rendered them. On a monitoring page
 * these are not details:
 *   1. `queueStats`  - running / queueSize / globalLimit. The execution queue.
 *   2. `sessions`    - simultaneous_session and the list of connected users.
 *   3. `creditLimit` - numberOfExecution / durationOfExecutionInSecond.
 *   4. `cache`       - the parameter cache entries, their age and its TTL.
 *   5. `saasinfo.isSaaS` was shown, but V1 also read `.saaS` and `.saasInstance`,
 *      which the endpoint does not send - so that table always rendered two
 *      blank cells. Same for the three Keycloak columns when auth is not
 *      Keycloak. Nothing here renders a field that is absent; it says so.
 * `monitorExecutions` is deliberately still not shown: it is the live monitor's
 * websocket bookkeeping (push timings and box positions), consumed by another
 * page, not a fact about this instance.
 *
 * TWO INJECTIONS CLOSED
 * ---------------------
 * V1 built its links by concatenation, unescaped and un-encoded:
 *     "<a href='TestCaseScript.jsp?test=" + obj.test + "&testcase=" + ...
 *     "<a href='ReportingExecutionByTag.jsp?Tag=" + tag + "'>"
 * A tag or test name holding an apostrophe closed the attribute; one holding `&`
 * silently truncated the query string. Every value here goes through
 * encodeURIComponent for the URL and crbTableEscape for the text.
 * (V1 also returned the string "undefined" as a tag when a running execution had
 * none, because `FormatedTag` guarded on `undefined` and then returned it.)
 *
 * AND IT REFRESHES ITSELF
 * -----------------------
 * V1 had a single Refresh button on a page whose whole subject is live state.
 * Auto-refresh is on by default at 30s, with a visible toggle and a last-updated
 * stamp so a stale reading can never be mistaken for a current one.
 * ========================================================================== */

var CRB_INFO_EXE_TABLE = "infoRunningExeTable";
var CRB_INFO_TRIGGER_TABLE = "infoTriggerTable";
var CRB_INFO_AUTO_REFRESH_MS = 30000;

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

function initPage() {
    var doc = new Doc();
    $("#pageTitle").html("Cerberus Monitoring");
    displayFooter(doc);
    crbInfoBuildTables();
}

/* -----------------------------------------------------------------------------
 * The two real lists. Client mode: this page already holds the whole payload
 * from its single request, so there is nothing to page server-side - but the
 * component still gives both tables a search box, sortable columns, a row count
 * and an empty state, none of which the hand-built <tbody> had.
 * -------------------------------------------------------------------------- */
function crbInfoBuildTables() {

    createCerberusTable({
        id: CRB_INFO_EXE_TABLE,
        mount: "#runningExecutionsTable",
        clientRows: [],
        embedded: true,
        hasPermissions: true,
        rowKey: "id",
        defaultSort: {field: "id", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search running executions...",
        emptyMessage: "No execution is running on this instance right now",
        columns: [
            {
                field: "id", prop: "id", title: "Execution", width: "110px",
                render: function (row) {
                    // id 0 means "not persisted yet" - V1 rendered it as a plain
                    // 0, and so does this, rather than linking to nothing.
                    if (!row.id) {
                        return '<span class="crb_table_muted">0</span>';
                    }
                    return '<a class="crb_table_link" href="./TestCaseExecution.jsp?executionId='
                        + encodeURIComponent(row.id) + '">' + crbTableEscape(row.id) + '</a>';
                }
            },
            {
                field: "start", prop: "start", title: "Started", width: "170px",
                render: function (row) { return crbTableEscape(crbInfoDate(row.start)); }
            },
            {field: "system", prop: "system", title: "System", width: "110px"},
            {field: "application", prop: "application", title: "Application", width: "130px"},
            {field: "test", prop: "test", title: "Test folder", width: "160px"},
            {
                field: "testcase", prop: "testcase", title: "Test case", width: "130px",
                render: function (row) {
                    if (!row.testcase) {
                        return "";
                    }
                    return '<a class="crb_table_link" href="./TestCaseScript.jsp?test='
                        + encodeURIComponent(row.test || "") + "&testcase="
                        + encodeURIComponent(row.testcase) + '">'
                        + crbTableEscape(row.testcase) + "</a>";
                }
            },
            {field: "environment", prop: "environment", title: "Env.", width: "80px"},
            {field: "country", prop: "country", title: "Country", width: "80px"},
            {field: "robotIP", prop: "robotIP", title: "Robot", width: "140px"},
            {
                field: "tag", prop: "tag", title: "Tag", width: "180px",
                render: function (row) {
                    if (isEmpty(row.tag)) {
                        return "";
                    }
                    return '<a class="crb_table_link" href="./ReportingExecutionByTag.jsp?Tag='
                        + encodeURIComponent(row.tag) + '">' + crbTableEscape(row.tag) + "</a>";
                }
            }
        ]
    });

    createCerberusTable({
        id: CRB_INFO_TRIGGER_TABLE,
        mount: "#schedulerTriggersTable",
        clientRows: [],
        embedded: true,
        hasPermissions: true,
        rowKey: "triggerName",
        defaultSort: {field: "triggerNextFiretime", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search triggers...",
        emptyMessage: "No scheduled trigger on this instance",
        columns: [
            {field: "triggerType", prop: "triggerType", title: "Type", width: "110px"},
            {field: "triggerName", prop: "triggerName", title: "Name", width: "200px",
             className: "font-medium"},
            {
                field: "triggerNextFiretime", prop: "triggerNextFiretime",
                title: "Next fire time", width: "180px",
                render: function (row) {
                    if (!row.triggerNextFiretime) {
                        return '<span class="crb_table_muted">-</span>';
                    }
                    return crbTableEscape(new Date(row.triggerNextFiretime).toLocaleString());
                }
            },
            {
                // V1 put the cron on a SECOND <tr> spanning two columns, with the
                // type and name rowspan'd over both - which is why that table
                // could not be sorted or searched. It is a column.
                field: "triggerCronDefinition", prop: "triggerCronDefinition",
                title: "Cron", width: "160px",
                render: function (row) {
                    if (isEmpty(row.triggerCronDefinition)) {
                        return '<span class="crb_table_muted">-</span>';
                    }
                    return '<code class="crb_code">' + crbTableEscape(row.triggerCronDefinition) + "</code>";
                }
            },
            {field: "triggerUserCreated", prop: "triggerUserCreated", title: "Created by", width: "130px"}
        ]
    });
}

/**
 * The page component. Holds the whole payload and renders it through Alpine
 * bindings - no value is ever concatenated into markup, which is what makes the
 * link fields above the only places escaping has to be thought about at all.
 */
function cerberusInfo() {
    return {
        data: null,
        loading: false,
        error: "",
        lastUpdated: "",
        auto: true,
        timer: null,

        init: function () {
            var self = this;
            this.load();
            this.$watch("auto", function (on) { self.schedule(on); });
            this.schedule(true);
            // Polling a backgrounded tab is pure waste, and worse, the reading is
            // stale the moment it is looked at. Reload on the way back instead.
            document.addEventListener("visibilitychange", function () {
                if (!document.hidden && self.auto) {
                    self.load();
                }
            });
        },

        schedule: function (on) {
            var self = this;
            if (this.timer) {
                clearInterval(this.timer);
                this.timer = null;
            }
            if (on) {
                this.timer = setInterval(function () {
                    if (!document.hidden) {
                        self.load();
                    }
                }, CRB_INFO_AUTO_REFRESH_MS);
            }
        },

        load: function () {
            var self = this;
            this.loading = true;
            this.error = "";
            return $.getJSON("ReadCerberusDetailInformation").then(function (payload) {
                self.data = payload || {};
                self.lastUpdated = new Date().toLocaleTimeString();
                crbTableSetRows(CRB_INFO_EXE_TABLE, self.runningExecutions);
                crbTableSetRows(CRB_INFO_TRIGGER_TABLE, self.triggers);
                self.$nextTick(function () {
                    if (window.lucide) {
                        lucide.createIcons();
                    }
                });
            }).fail(function () {
                self.error = "Could not read the instance information.";
            }).always(function () {
                self.loading = false;
            });
        },

        /* ---------------------------------------------------------------
         * Safe readers. Every one tolerates a missing branch: this payload
         * legitimately omits whole objects depending on how the instance is
         * configured, and V1's habit of reaching straight into them is what
         * printed blank cells and the literal string "undefined".
         * ------------------------------------------------------------ */
        get d() { return this.data || {}; },
        get cerberus() { return this.d.cerberus || {}; },
        get java() { return this.d.java || {}; },
        get db() { return this.d.database || {}; },
        get auth() { return this.d.authentification || {}; },
        get saas() { return this.d.saasinfo || {}; },
        get scheduler() { return this.d.scheduler || {}; },
        get queue() { return this.d.queueStats || {}; },
        get sessions() { return this.d.sessions || {}; },
        get credit() { return this.d.creditLimit || {}; },
        get cache() { return this.d.cache || {}; },

        get runningExecutions() {
            return Array.isArray(this.d.runningExecutionsList) ? this.d.runningExecutionsList : [];
        },

        get triggers() {
            return Array.isArray(this.scheduler.schedulerTriggers) ? this.scheduler.schedulerTriggers : [];
        },

        get activeUsers() {
            return Array.isArray(this.sessions.active_users) ? this.sessions.active_users : [];
        },

        get cacheEntries() {
            return Array.isArray(this.cache.cacheParameterEntries) ? this.cache.cacheParameterEntries : [];
        },

        /* ---------------------------------------------------------------
         * Live state
         * ------------------------------------------------------------ */
        get engineActive() {
            return this.d.executionThreadPoolInstanceActive === true;
        },

        get runningCount() {
            // queueStats.running is the instance-wide figure; the list is what
            // this node is executing. They can differ, so the tile shows the
            // list (what the table below it holds) and names the other in its
            // footnote rather than silently picking one.
            return this.runningExecutions.length;
        },

        get queueSize() { return Number(this.queue.queueSize) || 0; },

        get globalLimit() { return Number(this.queue.globalLimit) || 0; },

        get heapUsed() { return Number(this.java.javaUsedMemory) || 0; },
        get heapMax() { return Number(this.java.javaMaxMemory) || 0; },

        get heapPercent() {
            if (!this.heapMax) {
                return 0;
            }
            return Math.round((this.heapUsed / this.heapMax) * 100);
        },

        get heapTone() {
            var p = this.heapPercent;
            if (p >= 85) { return "danger"; }
            if (p >= 70) { return "warn"; }
            return "ok";
        },

        get heapColor() {
            return {ok: "#10b981", warn: "#f59e0b", danger: "#f43f5e"}[this.heapTone];
        },

        get schemaCurrent() { return this.db.databaseCerberusCurrentVersion; },
        get schemaTarget() { return this.db.databaseCerberusTargetVersion; },

        /**
         * True when the database schema is not at the version this build expects.
         * V1 printed the two numbers in adjacent cells and left the comparison to
         * the reader - which is the one thing on this page that must not be
         * missed, since a lagging schema breaks features silently.
         */
        get schemaDrift() {
            return this.schemaCurrent !== undefined && this.schemaTarget !== undefined
                && Number(this.schemaCurrent) !== Number(this.schemaTarget);
        },

        get isKeycloak() { return this.auth.isKeycloak === true; },
        get isSaaS() { return this.saas.isSaaS === true; },

        /* ---------------------------------------------------------------
         * Formatting
         * ------------------------------------------------------------ */
        /** A value, or an em dash - never a blank cell and never "undefined". */
        val: function (v) {
            if (v === undefined || v === null || v === "") {
                return "—";
            }
            return String(v);
        },

        isSet: function (v) {
            return v !== undefined && v !== null && v !== "";
        },

        bool: function (v) {
            return v === true || v === "true" ? "Yes" : "No";
        },

        num: function (v) {
            var n = Number(v);
            return isNaN(n) ? "—" : n.toLocaleString();
        },

        date: function (v) { return crbInfoDate(v); },

        /** Seconds as a short human duration, for cache ages. */
        age: function (seconds) {
            var s = Number(seconds);
            if (isNaN(s) || s < 0) {
                return "—";
            }
            if (s < 60) { return s + "s"; }
            if (s < 3600) { return Math.floor(s / 60) + "m " + (s % 60) + "s"; }
            return Math.floor(s / 3600) + "h " + Math.floor((s % 3600) / 60) + "m";
        },

        /**
         * The whole diagnostic as plain text, on the clipboard.
         * This page exists to be reported to someone; retyping a build number and
         * five version strings out of a screenshot is how support tickets get
         * them wrong.
         */
        copyDiagnostics: function () {
            var d = this.d;
            var lines = [
                "Cerberus " + this.val(this.cerberus.projectVersion)
                    + " build " + this.val(this.cerberus.projectBuild)
                    + " (" + this.val(this.cerberus.environment) + ")",
                "Schema        : " + this.val(this.schemaCurrent) + " / target " + this.val(this.schemaTarget),
                "Java          : " + this.val(this.java.javaVersion),
                "App server    : " + this.val(this.java.applicationServerInfo),
                "Heap          : " + this.heapUsed + " / " + this.heapMax + " MB (" + this.heapPercent + "%)",
                "Database      : " + this.val(this.db.databaseProductName) + " " + this.val(this.db.databaseProductVersion),
                "Driver        : " + this.val(this.db.driverName) + " " + this.val(this.db.driverVersion),
                "JDBC          : " + this.val(this.db.jDBCMajorVersion) + "." + this.val(this.db.jDBCMinorVersion),
                "Auth          : " + this.val(this.auth.authentification) + (this.isKeycloak ? " (Keycloak)" : ""),
                "SaaS          : " + this.bool(this.saas.isSaaS),
                "Engine active : " + this.bool(d.executionThreadPoolInstanceActive),
                "Queue         : " + this.runningCount + " running, " + this.queueSize + " queued",
                "Scheduler     : " + this.val(this.scheduler.schedulerInstanceVersion),
                "Server time   : " + this.val(this.scheduler.serverDate) + " " + this.val(this.scheduler.serverTimeZone),
                "Read at       : " + this.lastUpdated
            ];
            var text = lines.join("\n");

            function done(ok) {
                showMessageMainPage(ok ? "success" : "danger",
                    ok ? "Diagnostics copied to the clipboard."
                       : "Could not reach the clipboard - select the text manually.", false);
            }

            // navigator.clipboard is unavailable on a plain-http origin, which is
            // exactly how most Cerberus instances are reached internally.
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(text).then(function () { done(true); },
                        function () { crbInfoLegacyCopy(text, done); });
            } else {
                crbInfoLegacyCopy(text, done);
            }
        }
    };
}

/**
 * Clipboard fallback for non-secure origins: a detached textarea + execCommand.
 * Deprecated, and the only thing that works over http.
 */
function crbInfoLegacyCopy(text, done) {
    var area = document.createElement("textarea");
    area.value = text;
    area.setAttribute("readonly", "");
    area.style.position = "fixed";
    area.style.top = "-1000px";
    document.body.appendChild(area);
    area.select();
    var ok = false;
    try {
        ok = document.execCommand("copy");
    } catch (e) {
        ok = false;
    }
    document.body.removeChild(area);
    done(ok);
}

/**
 * The payload mixes formats: `start` on a running execution is already a
 * formatted string, `serverDate` is an ISO-like stamp. Parse only what parses,
 * and hand back the original otherwise rather than printing "Invalid Date".
 */
function crbInfoDate(value) {
    if (value === undefined || value === null || value === "") {
        return "";
    }
    var d = new Date(value);
    if (isNaN(d.getTime())) {
        return String(value);
    }
    return d.toLocaleString();
}
