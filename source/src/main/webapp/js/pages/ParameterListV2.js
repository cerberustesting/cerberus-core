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
 * Parameters - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadParameter?system1=<system> (contentTable; the edit permission is
 * PER ROW, as hasPermissionsUpdate, not a table-wide flag).
 *
 * The three tabs (All / AI / SMTP) are three states of the SAME table: AI and
 * SMTP put an explicit list of parameter names into the par.param column filter.
 * V1 did it with DataTables' fnFilter on column index 1 plus a jQuery event
 * handler that hid the resulting filter banner; here it is just the component's
 * own column filter, and the chips it shows are the honest answer to "why am I
 * seeing six rows".
 *
 * Fixed on the way over:
 *   - the Edit handler was built as onclick="openModalParameter('${obj.param}',
 *     '${getSys()}')" with NO escaping at all - a parameter name containing a
 *     quote broke the page, and one crafted to close the string ran. V2 passes
 *     the row to onClick.
 *   - the icon already swapped on the row permission but the tooltip always said
 *     "edit", even on a row the user can only read.
 *   - renderOptionsForApplication() (sic) only injected an empty spacer div.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
        $('[data-toggle="popover"]').popover({placement: 'auto', container: 'body'});
    });
});

var CRB_PARAM_TABLE_ID = "parameterTableV2";

/**
 * The two curated views. Same lists as ParameterList.js:76-99 - keep them in step
 * with the parameters the AI and SMTP features actually read.
 */
var CRB_PARAM_VIEWS = {
    ai: [
        "cerberus_ai_mcp_apikey",
        "cerberus_ai_mcp_host",
        "cerberus_ai_use_mcp",
        "cerberus_log_mcpcalls",
        "cerberus_mcp_enable",
        "cerberus_anthropic_apikey",
        "cerberus_anthropic_defaultmodel",
        "cerberus_anthropic_maxtoken",
        "cerberus_anthropic_price_input_per_million",
        "cerberus_anthropic_price_output_per_million"
    ],
    smtp: [
        "cerberus_smtp_from",
        "cerberus_smtp_host",
        "cerberus_smtp_isSetTls",
        "cerberus_smtp_password",
        "cerberus_smtp_port",
        "cerberus_smtp_username"
    ]
};

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    createCerberusTable({
        id: CRB_PARAM_TABLE_ID,
        mount: "#parameterList",
        endpoint: "ReadParameter?system1=" + encodeURIComponent(getSys()),
        distinctEndpoint: "ReadParameter?system1=" + encodeURIComponent(getSys()),
        rowKey: "param",
        defaultSort: {field: "par.param", dir: "asc"},
        pageLength: 15,
        searchPlaceholder: "Search parameters...",
        emptyMessage: "No parameter matches your search",

        // `field` = server column name (sName in V1), `prop` = key in the row.
        columns: [
            {field: "par.param", prop: "param",
             title: doc.getDocLabel("page_parameter", "parameter_col"),
             width: "300px", className: "font-mono", filterable: true},
            {field: "par.value", prop: "value",
             title: doc.getDocLabel("page_parameter", "cerberus_col"), width: "260px"},
            {field: "par1.value", prop: "system1value",
             title: doc.getDocLabel("page_parameter", "system_col") + " (" + getSys() + ")",
             width: "260px"},
            {field: "par.description", prop: "description",
             title: doc.getDocLabel("page_parameter", "description_col"),
             width: "420px", like: true}
        ],

        actions: [
            {
                // Every row is at least readable, so the button is always there and
                // the icon AND the tooltip follow the row's own permission.
                key: "edit", gate: "always",
                icon: function (row) { return row.hasPermissionsUpdate ? "pencil" : "eye"; },
                title: function (row) {
                    return doc.getDocLabel("page_parameter",
                        row.hasPermissionsUpdate ? "button_edit" : "button_view");
                },
                onClick: function (row) { openModalParameter(row.param, getSys()); }
            }
        ]
    });
}

/**
 * Tab handlers, called from the Alpine buttons in ParameterList.jsp.
 * A view is a filter on the parameter-name column, nothing more.
 */
function displayAllParametersTable() {
    paramV2ApplyView(null);
}

function displayFilteredParametersTable(view) {
    var list = CRB_PARAM_VIEWS[view];
    if (!list) {
        console.warn("Unknown filtered parameters view:", view);
        paramV2ApplyView(null);
        return;
    }
    paramV2ApplyView(list);
}

function paramV2ApplyView(paramNames) {
    var table = crbTableInstance(CRB_PARAM_TABLE_ID);
    if (!table) {
        return;
    }
    if (paramNames) {
        table.activeFilters["par.param"] = paramNames.slice();
    } else {
        delete table.activeFilters["par.param"];
    }
    table.start = 0;
    table.fetch();
}

function displayPageLabel() {
    var doc = new Doc();

    $("#pageTitle").html(doc.getDocLabel("page_parameter", "allParameters"));
    $("#title").html(doc.getDocOnline("page_parameter", "allParameters"));
    displayFooter(doc);
    displayGlobalLabel(doc);
}
