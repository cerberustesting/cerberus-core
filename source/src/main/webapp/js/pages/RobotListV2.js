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
 * Robot list - V2 pilot, built on js/global/crbTable.js instead of DataTables.
 *
 * Deliberately a SEPARATE page (RobotListV2.jsp) rather than a rewrite of
 * RobotList.jsp: the V1 page keeps running untouched, the two can be compared
 * side by side on the same data, and abandoning V2 costs nothing but deleting
 * two files.
 *
 * Action-button behaviour is a 1:1 port of RobotList.js:190-257 - with
 * permission: edit + duplicate + delete; without: a single view button opening
 * the same modal in the same 'EDIT' mode (the modal enforces read-only itself).
 * The gates are now declared instead of being rebuilt from an if/else around
 * hand-written HTML.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_ROBOT_TABLE_ID = "robotsTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    initModalRobot();
    $('#editRobotModal').data("initLabel", true);

    $("[name=screensize]").append(
        $('<option></option>').text(doc.getDocLabel("page_runtest", "default_full_screen")).val("")
    );

    createCerberusTable({
        id: CRB_ROBOT_TABLE_ID,
        mount: "#robotList",
        endpoint: "./api/robots/read",
        // Named explicitly rather than derived from `endpoint`: legacy table-filter.js
        // guesses it by appending "?columnName=" to the ajax source, which only works
        // for the old servlets - on this Spring controller the distinct values live on
        // a separate path, so that guess would silently return the row list instead.
        distinctEndpoint: "./api/robots/readDistinctValueOfColumn",
        rowKey: "robotID",
        // V1 sorted on the "robot" column too, but its column carried no sName, so
        // the server received an empty column name and RobotDAO emitted no ORDER BY
        // at all - the arrow moved, the rows did not. Naming the field fixes it.
        defaultSort: {field: "robot", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search robots...",
        emptyMessage: "No robot matches your search",

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            return "<button id='createRobotButton' type='button' onclick=\"openModalRobot(undefined,'ADD')\" " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10 w-auto'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i>" +
                "<span>" + doc.getDocLabel("page_robot", "button_create") + "</span></button>";
        },

        columns: [
            {field: "robot", title: "Robot", width: "200px", className: "font-mono font-medium", filterable: true},
            {field: "type", title: "Type", visible: false, width: "80px", filterable: true},
            {
                field: "platform", title: "Platform", width: "110px", className: "text-center", filterable: true,
                render: function (row) {
                    if (!row.platform) { return ""; }
                    return '<div class="flex flex-col items-center justify-center gap-1">' +
                        '<img src="./images/platform-' + crbTableEscape(row.platform) + '.png" class="h-5" ' +
                        'alt="' + crbTableEscape(row.platform) + '"/>' +
                        '<span class="text-xs font-medium text-gray-600 dark:text-gray-400">' +
                        crbTableEscape(row.platform) + '</span></div>';
                }
            },
            {
                field: "browser", title: "Browser", width: "110px", className: "text-center", filterable: true,
                render: function (row) {
                    if (!row.browser) { return ""; }
                    return '<div class="flex flex-col items-center justify-center gap-1">' +
                        '<img src="./images/browser-' + crbTableEscape(row.browser) + '.png" class="h-5" ' +
                        'alt="' + crbTableEscape(row.browser) + '"/>' +
                        '<span class="text-xs font-medium text-gray-600 dark:text-gray-400">' +
                        crbTableEscape(row.browser) + '</span></div>';
                }
            },
            {
                // No `field`: this is computed from the executors array, so the server
                // cannot order by it. Declaring it without a field makes it explicitly
                // non-sortable rather than sortable-but-silently-broken.
                title: "Executors", sortable: false, width: "170px",
                render: function (row) {
                    var total = (row.executors || []).length;
                    var running = (row.executors || []).filter(function (e) { return e.isActive; }).length;
                    var percent = total > 0 ? Math.round((running / total) * 100) : 0;
                    return '<div class="flex items-center gap-3">' +
                        '<span class="font-mono text-sm whitespace-nowrap">' +
                        '<span class="font-semibold text-emerald-600 dark:text-emerald-400">' + running + '</span>' +
                        '<span class="text-slate-400"> / ' + total + '</span></span>' +
                        '<div class="w-16 h-1.5 rounded-full overflow-hidden bg-slate-200 dark:bg-slate-700">' +
                        '<div class="h-full rounded-full bg-gradient-to-r from-teal-500 to-green-500 transition-all duration-500" ' +
                        'style="width:' + percent + '%"></div></div></div>';
                }
            },
            {field: "version", title: doc.getDocOnline("robot", "version"), visible: false, width: "90px"},
            {
                field: "isActive", title: "Active", width: "100px", filterable: true,
                render: function (row) {
                    // Charter status chip.
                    if (row.isActive === true) {
                        return '<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs ' +
                            'font-semibold ring-1 ring-inset bg-emerald-50 text-emerald-700 ring-emerald-600/20 ' +
                            'dark:bg-emerald-500/10 dark:text-emerald-400 dark:ring-emerald-500/30">Active</span>';
                    }
                    return '<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs ' +
                        'font-semibold ring-1 ring-inset bg-slate-100 text-slate-600 ring-slate-500/20 ' +
                        'dark:bg-slate-500/10 dark:text-slate-400 dark:ring-slate-500/30">Inactive</span>';
                }
            },
            {field: "userAgent", title: doc.getDocOnline("robot", "useragent"), visible: false},
            {field: "screenSize", title: doc.getDocOnline("robot", "screensize"), visible: false},
            {field: "robotDecli", title: doc.getDocOnline("robot", "robotdecli"), visible: false},
            {field: "lbexemethod", title: doc.getDocOnline("robot", "lbexemethod"), visible: false},
            {field: "description", title: "Description", width: "280px"}
        ],

        actions: [
            {
                key: "edit", icon: "pencil", gate: "permission",
                title: doc.getDocLabel("page_robot", "button_edit"),
                onClick: function (row) { openModalRobot(row.robot, "EDIT"); }
            },
            {
                key: "duplicate", icon: "copy", gate: "permission",
                title: doc.getDocLabel("page_robot", "button_duplicate"),
                onClick: function (row) { openModalRobot(row.robot, "DUPLICATE"); }
            },
            {
                key: "delete", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_robot", "button_delete"),
                onClick: function (row) { robotV2DeleteEntry(row.robotID, row.robot); }
            },
            {
                // V1 shows exactly this one button, opening the same modal in the same
                // mode, when the user lacks permission. Ported unchanged.
                key: "view", icon: "eye", gate: "no-permission",
                title: doc.getDocLabel("page_robot", "button_view"),
                onClick: function (row) { openModalRobot(row.robot, "EDIT"); }
            }
        ],

        rowDetail: function (row) {
            return formatExecutors(row);
        }
    });
}

function displayPageLabel() {
    var doc = new Doc();
    displayGlobalLabel(doc);
    $("#pageTitle").html(doc.getDocLabel("page_robot", "title"));
    $("#title").html(doc.getDocOnline("page_robot", "title"));
    displayFooter(doc);
}

/**
 * Same confirm-then-DELETE flow as RobotList.js:119-159; only the table refresh
 * differs (component reload instead of DataTables' fnDraw + manual page-back,
 * which the component handles by re-fetching the current page).
 */
async function robotV2DeleteEntry(entry, name) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_global", "message_delete");
    messageComplete = messageComplete.replace("%TABLE%", doc.getDocLabel("robot", "robot"));
    messageComplete = messageComplete.replace("%ENTRY%", name);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_robot", "button_delete"),
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await fetch("DeleteRobot", {
                    method: "POST",
                    headers: {"Content-Type": "application/x-www-form-urlencoded"},
                    body: "robotid=" + entry
                });
                const data = await resp.json();
                if (getAlertType(data.messageType) !== "success") {
                    Swal.showValidationMessage(data.message || "Delete failed");
                    return null;
                }
                return data;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        var table = crbTableInstance(CRB_ROBOT_TABLE_ID);
        if (table) {
            // Stay on a valid page if the last row of the last page just went away.
            if (table.rows.length === 1 && table.page > 1) {
                table.goToPage(table.page - 1);
            } else {
                table.reload();
            }
        }
        showMessageMainPage("success", result.value.message || "Robot deleted successfully", false);
    }
}

/**
 * Executor detail panel.
 *
 * `host` and `port` are optional in the API payload (the DTO omits them when
 * unset), which V1 never accounted for: its template interpolates them raw, so
 * an executor without a host renders the literal string "undefined:undefined"
 * to the user - reproducible on any robot whose executor has no host, e.g.
 * AAZDD in local data. Here the executor's own name is the primary label (it is
 * always present) and the address is shown only when there actually is one.
 */
function formatExecutors(robot) {
    if (!robot.executors || robot.executors.length === 0) {
        return '<div class="p-4 pl-12 text-sm text-slate-500">No executor</div>';
    }

    var html = '<div class="p-4 pl-12">' +
        '<h4 class="text-sm font-medium mb-3 text-slate-500">Executors (' + robot.executors.length + ')</h4>' +
        '<div class="grid gap-2">';

    robot.executors.forEach(function (exe) {
        var isActive = exe.isActive === true;
        var hasAddress = Boolean(exe.host);
        var address = hasAddress
            ? ('<span class="font-mono text-sm text-slate-600 dark:text-slate-300">' +
               crbTableEscape(exe.host) + (exe.port ? ':' + crbTableEscape(exe.port) : '') + '</span>')
            : '<span class="text-xs italic text-slate-400">no address configured</span>';

        html += '<div class="flex items-center justify-between p-3 rounded-lg border ' +
            'border-slate-200 dark:border-slate-700">' +
            '<div class="flex items-center gap-4 min-w-0">' +
            '<div class="w-2.5 h-2.5 rounded-full shrink-0 ' +
            (isActive ? 'bg-emerald-500' : 'bg-slate-300 dark:bg-slate-600') + '"></div>' +
            '<span class="font-mono text-sm font-medium truncate">' + crbTableEscape(exe.executor || '-') + '</span>' +
            address +
            '</div>' +
            '<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ' +
            'ring-1 ring-inset capitalize shrink-0 ' +
            (isActive
                ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20 dark:bg-emerald-500/10 dark:text-emerald-400 dark:ring-emerald-500/30'
                : 'bg-slate-100 text-slate-600 ring-slate-500/20 dark:bg-slate-500/10 dark:text-slate-400 dark:ring-slate-500/30') +
            '">' + (isActive ? 'active' : 'inactive') + '</span>' +
            '</div>';
    });

    return html + '</div></div>';
}
