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
 * crbTable - V2 list table (Alpine.js), replacing jQuery DataTables + Bootstrap.
 *
 * WHY THIS EXISTS, AND WHAT IT DELIBERATELY DOES NOT DO
 * -----------------------------------------------------
 * The legacy stack (js/global/cerberusDatatable.js + global.js + table-filter.js,
 * on top of the vendored DataTables 1.10.12) stays in place, untouched. Pages are
 * migrated one at a time and can be reverted by pointing the page back at the old
 * createDataTableWithPermissions[New]() call - nothing here modifies the old path,
 * so a rollback is a one-line change on a single page, never a global one.
 *
 * The server contract is IDENTICAL to the legacy one: this sends the same
 * DataTables-1.9-style parameters the Java side already parses in
 * org.cerberus.core.util.datatable.DataTableInformation (iDisplayStart,
 * iDisplayLength, sSearch, sColumns, iSortCol_0, sSortDir_0, sLike, sSearch_N)
 * and reads the same response shape ({contentTable, iTotalRecords,
 * iTotalDisplayRecords, hasPermissions}). No Java change is required to migrate a
 * page, and both versions of a page can hit the same endpoint interchangeably.
 *
 * FOUR LEGACY DEFECTS THIS FIXES BY CONSTRUCTION
 * ---------------------------------------------
 * 1. Silent dead sort. Legacy builds `sColumns` from each column's optional
 *    `sName`, including presentational columns, so it contains empty slots (on
 *    RobotList: ",,,type,platform,..."). `iSortCol_0` indexes into that list, so
 *    sorting a column whose sName was never set sends an empty column name, and
 *    RobotDAO's `if (!isEmptyOrNull(column))` guard then emits no ORDER BY at all.
 *    The arrow flips, the rows do not move. Here, only real data columns go into
 *    `sColumns`, a sortable column without a `field` is a startup error rather
 *    than a silent no-op, and `iSortCol_0` indexes that clean list.
 * 2. Permission gates that are easy to forget. Legacy leaves each page to decide
 *    whether to read the permission flag at all; an audit of ~37 tables found 13
 *    that request the flag and never consult it, so every action renders for
 *    everyone. Here `gate` is MANDATORY on every action: a missing gate logs an
 *    error and the action is not rendered (fail closed). "Everyone may do this"
 *    is still available, but only by writing `gate: 'always'` on purpose.
 * 3. A blocking busy-wait. Legacy's fnStateLoadCallback spins in
 *    `while (user === null) {}` waiting on getUser(), freezing the UI thread if
 *    that call is ever not warm-cached. Nothing here blocks the main thread.
 * 4. Column filters that silently do nothing. Legacy decides filterability from
 *    a column being visible AND DataTables-searchable, then derives the distinct-
 *    values URL by appending "?columnName=" to the table's own ajax source - which
 *    only works for the old servlets, not for the /api/ Spring controllers where
 *    that lives on a separate readDistinctValueOfColumn path. Here a column opts
 *    in with `filterable: true` and the endpoint is named explicitly, so a filter
 *    either works or fails loudly at configuration time.
 *
 * WHAT IS DELIBERATELY NOT PORTED
 * ------------------------------
 * (Mass selection checkboxes and client-side mode WERE on this list and have
 * since landed - see `selection` and `clientRows` below. Kept accurate here
 * because this block is what the next migration reads before deciding whether a
 * page can move.)
 *
 * 1. Column WIDTH resizing. Legacy loads a colResize plugin ("Z" in its dom
 *    string). Reordering and visibility live in the Config panel here; width
 *    does not, and the columns keep the widths their definitions declare.
 * 2. Per-column LIKE box. Legacy gives a column flagged `like` a free-text
 *    popover instead of the distinct-values checklist. Here `filterable` always
 *    means the checklist; four columns are declared both `like` and `filterable`
 *    (TestCaseList Testcase, ApplicationObjectList Object, TestDataLib Group and
 *    Method) and get the checklist. Their values are short and enumerable so the
 *    checklist reads better, but per-column substring typing is gone; the global
 *    search still covers it.
 * 3. The filter popover's All / None buttons, and Tab/Enter chaining from one
 *    column's filter to the next.
 * 4. FULL STATE PERSISTENCE - the significant one. Legacy runs stateSave:true,
 *    which keeps search, sort, page, page length, column visibility AND the
 *    per-column filters; and updateUserPreferences() pushes the whole
 *    localStorage to UpdateMyUser (it fires from bindToggleCollapse on any panel
 *    expand/collapse), so fnStateLoadCallback restores all of it on any browser
 *    the user signs into. persistColumnPrefs() below keeps COLUMN VISIBILITY
 *    ONLY, and only in localStorage. Reopening a V2 page therefore resets sort,
 *    page length, search and filters to their defaults.
 *
 * NOT a gap, though legacy appears to offer them:
 *  - Multi-column sort. DataTables allows shift-click, but
 *    util/datatable/DataTableInformation reads only iSortCol_0 / sSortDir_0, so
 *    it never reached the server for a server-side table anyway.
 *  - deferLoading and createdRowCallback: config options no page passes.
 * ========================================================================== */

/**
 * Registry so injected markup can reference its config by table id without
 * serialising functions (render/onClick/gate) through an HTML attribute.
 */
window.crbTableRegistry = window.crbTableRegistry || {};

/**
 * Builds a V2 table into `mountSelector` and returns the config it registered.
 *
 * @param {Object} config
 * @param {String} config.id                unique table id (also the DOM id)
 * @param {String} config.mount             selector of the container to render into
 * @param {String} config.endpoint          POST url, e.g. "./api/robots/read"
 * @param {String} [config.dataProp]        response property holding rows (default "contentTable")
 * @param {String} [config.distinctEndpoint] url returning {distinctValues:[...]} for
 *                                          `columnName=<field>`; REQUIRED for filterable columns
 * @param {String} [config.distinctMethod]  "GET" (default) or "POST" for endpoints that
 *                                          refuse GET
 * @param {Array}  config.columns           column definitions (see below)
 * @param {Array}  [config.actions]         row action definitions (see below)
 * @param {Object} [config.defaultSort]     {field: "robot", dir: "asc"}
 * @param {Number} [config.pageLength]      default rows per page (default 10)
 * @param {Array}  [config.lengthMenu]      page size options (default [10,15,20,30,50,100])
 * @param {String} [config.rowKey]          row property uniquely identifying a row
 * @param {Function} [config.rowDetail]     row => HTML string, enables the expand chevron
 * @param {Function} [config.toolbar]       ({hasPermissions}) => HTML string for page-level buttons
 * @param {Boolean} [config.sendDefaultSystems] append user's default systems (default true)
 * @param {String} [config.searchPlaceholder]
 * @param {String} [config.emptyMessage]
 * @param {Object} [config.initialFilters] {serverColumnName: [values]} applied to the
 *                                          first request (deep links)
 * @param {String} [config.initialSearch]  free-text search applied to the first request
 * @param {Array}  [config.clientRows]     CLIENT MODE: the rows themselves, instead of
 *                                          `endpoint`. Search / sort / filter / paging all
 *                                          run locally; use crbTableSetRows(id, rows) to
 *                                          replace the dataset. `field` is then just a key
 *                                          into the row, not a server column name.
 * @param {Boolean} [config.hasPermissions] CLIENT MODE only: what permission gates see
 *                                          (server mode reads it from the response)
 * @param {Function} [config.onRefresh]    CLIENT MODE only: what the refresh button does
 *                                          (re-run the page's own query). Without it the
 *                                          button is hidden.
 * @param {Boolean} [config.embedded]      render with NO card chrome, for a table that
 *                                          sits inside a card the page already draws
 *                                          (reporting sections). The host card should be
 *                                          padding-free and overflow-hidden.
 *
 * Column definition:
 *   {
 *     field:     "robot",       // server column name - REQUIRED when sortable/filterable
 *     title:     "Robot",
 *     sortable:  true,          // default true when `field` is present
 *     filterable:true,          // opt-in per-column value filter (needs distinctEndpoint)
 *     visible:   true,
 *     configurable: true,       // false hides it from the Config panel (machinery columns)
 *     subtitle:  "MyRobot",     // optional second line in the header
 *     like:      false,         // server-side substring match instead of exact IN-list
 *     className: "font-mono",
 *     width:     "200px",
 *     render:    (row) => "<b>x</b>"
 *   }
 *
 * Action definition (gate is MANDATORY - see header comment):
 *   {
 *     key: "edit", title: "Edit", icon: "pencil",
 *     gate: "permission",     // 'always' | 'permission' | 'no-permission' | fn(row, ctx)
 *     onClick: (row) => openModalRobot(row.robot, 'EDIT'),
 *     danger: true
 *   }
 */
function createCerberusTable(config) {
    var cfg = normalizeCerberusTableConfig(config);
    if (!cfg) {
        return null;
    }
    window.crbTableRegistry[cfg.id] = cfg;

    var $mount = $(cfg.mount);
    if (!$mount.length) {
        console.error("[crbTable] mount container not found: " + cfg.mount);
        return null;
    }
    $mount.html(crbTableMarkup(cfg));
    return cfg;
}

/**
 * Validates and fills in defaults. Returns null (and logs) when the config is
 * unusable, so a mistake surfaces immediately at startup instead of becoming a
 * subtly dead control later - which is exactly how the legacy sort defect and the
 * unenforced permission gates survived unnoticed for so long.
 */
/**
 * Normalises one column set: doc-online titles reduced to text, prop defaulting to
 * field, and the sortable/filterable rules that refuse to reproduce the legacy
 * silent-dead-sort. Extracted so crbTableSetColumns() can put a runtime column set
 * through exactly the same checks as a configured one.
 */
function crbTableNormalizeColumns(columns, tableId, canListFilterValues) {
    return (columns || []).map(function (col, index) {
        var c = $.extend({visible: true, like: false, className: "", width: null, filterable: false}, col);
        // Column titles often come from doc.getDocOnline(), which returns the label
        // WITH a trailing "?" help anchor as raw HTML. The header renders titles as
        // text (deliberately - a title is not a place to inject markup), so that
        // HTML would be shown literally: "LABEL <A TABINDEX='1' CLASS="DOCONLINE"...".
        // Reduce any such title to its text, and keep the doc link's href so the
        // help affordance can be restored later if wanted.
        if (typeof c.title === "string" && c.title.indexOf("<") !== -1) {
            var $t = $("<div>").html(c.title);
            var $a = $t.find("a").first();
            if ($a.length) {
                c.docHref = $a.attr("href") || null;
                $a.remove();
            }
            c.title = $t.text().replace(/\s+/g, " ").trim();
        }
        // `field` is the SERVER column name (used for sColumns / sorting / filtering);
        // `prop` is the key in the returned row object. They coincide on some
        // endpoints (robots) but not on most (test cases sort on "tec.test" while
        // the row carries "test"), so they are separate concepts with `prop`
        // defaulting to `field` for the simple case.
        c.prop = col.prop || col.field;
        // Sortable only makes sense for a column the server can order by, so it
        // defaults to true exactly when a server field name is present.
        c.sortable = (col.sortable === undefined) ? Boolean(c.field) : Boolean(col.sortable);
        if (c.sortable && !c.field) {
            console.error("[crbTable:" + tableId + "] column #" + index + " (\"" + (c.title || "") +
                "\") is sortable but has no `field`. The server cannot order by an unnamed column " +
                "(this is the legacy silent-dead-sort defect) - sorting disabled for it.");
            c.sortable = false;
        }
        if (c.filterable && !c.field) {
            console.error("[crbTable:" + tableId + "] column #" + index + " (\"" + (c.title || "") +
                "\") is filterable but has no `field` - filter disabled.");
            c.filterable = false;
        }
        // Client mode reads the distinct values off the dataset, so it needs no endpoint.
        if (c.filterable && canListFilterValues === false) {
            console.error("[crbTable:" + tableId + "] column \"" + c.field + "\" is filterable but the table " +
                "has no `distinctEndpoint` to list its values - filter disabled.");
            c.filterable = false;
        }
        return c;
    });
}

function normalizeCerberusTableConfig(config) {
    // Exactly one data source: an endpoint (server mode) or clientRows (client mode).
    var hasEndpoint = Boolean(config && config.endpoint);
    var hasClientRows = Boolean(config && Array.isArray(config.clientRows));
    if (!config || !config.id || !Array.isArray(config.columns) || (!hasEndpoint && !hasClientRows)) {
        console.error("[crbTable] config requires {id, columns} plus either `endpoint` " +
            "(server mode) or `clientRows` (client mode)", config);
        return null;
    }
    if (hasEndpoint && hasClientRows) {
        console.error("[crbTable:" + config.id + "] both `endpoint` and `clientRows` given - " +
            "a table has one data source. Using clientRows.");
    }

    var cfg = $.extend({
        dataProp: "contentTable",
        pageLength: 10,
        lengthMenu: [10, 15, 20, 30, 50, 100],
        actions: [],
        sendDefaultSystems: true,
        searchPlaceholder: "Search...",
        emptyMessage: "No data to display",
        distinctEndpoint: null,
        distinctMethod: "GET",
        selection: null,
        persistColumns: true,
        // Kept as the single opt-out: a page that already said persistColumns:false
        // meant "remember nothing about this table", which is still what it gets.
        persistState: true,
        // Marks the search term wherever it appears in a visible cell. On by
        // default: a result list that does not say WHY a row is in it makes the
        // user re-scan every column by eye.
        highlightSearch: true,
        // Shortest search that gets marked - see CRB_TABLE_HIGHLIGHT_MIN_LENGTH.
        highlightMinLength: CRB_TABLE_HIGHLIGHT_MIN_LENGTH,
        // Column filters to apply on the very first load, e.g. from a deep link
        // (ApplicationObjectList.jsp?application=X). Must be config rather than a
        // post-creation call: createCerberusTable() only injects the markup, and
        // Alpine has not built the component yet when it returns - so reaching for
        // crbTableInstance() on the next line always finds nothing.
        initialFilters: null,
        initialSearch: "",
        // Client-side mode: pass the rows instead of an endpoint. See the block
        // comment above fetchClient().
        clientRows: null,
        hasPermissions: false,
        onRefresh: null,
        // Embedded: the table lives INSIDE a card the page already draws (a
        // reporting section with its own titled head), so it renders no card
        // chrome of its own - one card, not two stacked ones.
        embedded: false,
        mount: "#" + config.id + "_mount"
    }, config);

    // persistColumns predates persistState and meant "remember nothing about this
    // table"; honour it as the opt-out for the whole snapshot so no page has to
    // be revisited.
    if (config.persistColumns === false) {
        cfg.persistState = false;
    }

    // Multi-row selection for mass actions. `gate` follows the same contract as
    // action gates and is equally mandatory when selection is enabled: on
    // TestCaseList the legacy code only renders a row checkbox when that row's
    // own hasPermissionsUpdate is true, which is what keeps its four mass actions
    // (export / update / label / delete) limited to permitted rows.
    if (cfg.selection) {
        if (cfg.selection.gate === undefined || cfg.selection.gate === null) {
            console.error("[crbTable:" + cfg.id + "] selection needs a `gate` saying which rows may be " +
                "selected - use 'always' if every row is selectable. Selection disabled.");
            cfg.selection = null;
        } else if (!cfg.rowKey) {
            console.error("[crbTable:" + cfg.id + "] selection needs `rowKey` to identify rows across " +
                "reloads. Selection disabled.");
            cfg.selection = null;
        }
    }

    cfg.columns = crbTableNormalizeColumns(cfg.columns, cfg.id,
        Boolean(cfg.distinctEndpoint) || Boolean(cfg.clientRows));

    cfg.actions = cfg.actions.filter(function (action, index) {
        if (!action || action.gate === undefined || action.gate === null) {
            console.error("[crbTable:" + cfg.id + "] action #" + index + " (\"" +
                ((action && (action.key || action.title)) || "?") + "\") has no `gate`. Every action must " +
                "declare who may see it - use 'always' if it is genuinely public. Not rendered.");
            return false;
        }
        var g = action.gate;
        if (typeof g !== "function" && ["always", "permission", "no-permission"].indexOf(g) === -1) {
            console.error("[crbTable:" + cfg.id + "] action \"" + (action.key || action.title) +
                "\" has an unknown gate: " + g + ". Not rendered.");
            return false;
        }
        return true;
    });

    cfg.defaultSort = cfg.defaultSort || {};
    if (cfg.defaultSort.field) {
        var known = crbTableServerColumns(cfg).indexOf(cfg.defaultSort.field);
        if (known === -1) {
            console.error("[crbTable:" + cfg.id + "] defaultSort.field \"" + cfg.defaultSort.field +
                "\" does not match any column `field`. Falling back to no explicit sort.");
            cfg.defaultSort = {};
        }
    }
    return cfg;
}

/**
 * The ordered list of server column names, i.e. the value sent as `sColumns`.
 * Only real data columns are included - unlike legacy, which also emitted an
 * empty slot for every presentational column (action buttons, expand chevron)
 * and thereby shifted `iSortCol_0`/`sSearch_N` onto meaningless names.
 * @returns {Array<String>}
 */
function crbTableServerColumns(cfg) {
    return cfg.columns.filter(function (c) { return Boolean(c.field); })
                      .map(function (c) { return c.field; });
}

function crbTableEscape(value) {
    return String(value === undefined || value === null ? "" : value)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

/* -----------------------------------------------------------------------------
 * Markup. Rendered once; everything dynamic is Alpine-bound, so there is no
 * post-hoc DOM rewriting of the kind the legacy engine needs (it re-skins
 * DataTables' own generated pagination/length/header nodes on every draw).
 *
 * Layout follows the newer Cerberus chrome (ReportingAutomateScore /
 * TestCaseExecutionV2): one compact horizontal control bar rather than stacked
 * blocks, pills for counters, and paging kept ABOVE the table where the legacy
 * DataTables pages put it.
 * -------------------------------------------------------------------------- */
function crbTableMarkup(cfg) {
    return `
<!-- No x-init="init()": Alpine 3 already calls a component's init() method on
     its own. Declaring both ran the whole initialisation twice - two listeners
     registered and two identical requests fired on every page load. -->
<div id="${cfg.id}" class="crb_table_root${cfg.embedded ? ' crb_table_root--embedded' : ''}" x-data="crbTable(window.crbTableRegistry['${cfg.id}'])">

  <div class="crb_table_bar">
    <div class="crb_table_bar_row">
      <div id="${cfg.id}_toolbar" class="crb_table_bar_actions" x-html="toolbarHtml"></div>

      <div class="crb_table_bar_search">
        <i data-lucide="search" class="crb_table_search_icon w-4 h-4"></i>
        <input type="search" x-model="search" @input="onSearchInput()"
               class="crb_table_search_input"
               placeholder="${crbTableEscape(cfg.searchPlaceholder)}" aria-label="Search table">
      </div>

      <!-- The spin class goes on a wrapper, never on the <i> itself: lucide
           REPLACES that <i> with a generated <svg>, which drops any class Alpine
           had bound onto it. Binding :class there leaves whatever value was set
           at replacement time frozen on the svg, so the refresh icon kept
           spinning forever after the first load. -->
      <!-- Client mode has no endpoint to re-query, so refreshing means asking the
           PAGE for a new dataset. A client table without an onRefresh hook hides
           the button rather than offering one that re-sorts the same rows. -->
      <button type="button" @click="refresh()" title="Refresh" class="crb_table_iconbtn"
              x-show="!isClient || hasRefreshHook">
        <span :class="loading ? 'crb_table_spin' : ''" class="inline-flex">
          <i data-lucide="refresh-cw" class="w-4 h-4"></i>
        </span>
      </button>

      <div>
        <button type="button" @click="toggleConfig($event)" title="Columns"
                class="crb_table_iconbtn crb_table_iconbtn--labelled">
          <i data-lucide="sliders" class="w-4 h-4"></i><span>Config</span>
        </button>
        <!-- Positioned in fixed coordinates, not absolutely inside the toolbar:
             an absolute popover is clipped by any ancestor with overflow set, and
             a card holding a table legitimately does that. See crbTablePopoverStyle(). -->
        <div x-show="configOpen" x-cloak x-ref="configPopover" :style="configStyle"
             @click.outside="closeConfig()" class="crb_table_popover crb_table_popover--config">
          <div class="crb_table_popover_head">
            <span class="crb_table_popover_title">Columns</span>
            <span class="crb_table_popover_quick">
              <button type="button" @click="resetView()"
                      title="Back to the default order, columns, sort and filters">Reset view</button>
            </span>
          </div>
          <p class="crb_table_popover_hint">Drag to reorder. Saved automatically.</p>
          <div class="crb_table_popover_list">
            <!-- configurable:false hides a column from this list. For sort-key or
                 other machinery columns: offering to "show" one is offering noise. -->
            <template x-for="(col, i) in columns" :key="colKey(col)">
              <div class="crb_table_popover_item crb_table_popover_item--draggable"
                   x-show="col.configurable !== false"
                   draggable="true"
                   @dragstart="onColumnDragStart(i, $event)"
                   @dragover.prevent
                   @drop.prevent="onColumnDrop(i)">
                <!-- The handle is a real button so the reorder is reachable from
                     the keyboard with Up/Down, not only by dragging. -->
                <button type="button" class="crb_table_drag"
                        :aria-label="'Move ' + col.title"
                        @keydown="onColumnKey(i, $event)"
                        x-html="dragIcon"></button>
                <label class="crb_table_popover_itemlabel">
                  <input type="checkbox" :checked="col.visible" @change="toggleColumn(i)">
                  <span x-text="col.title"></span>
                </label>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div class="crb_table_bar_row crb_table_bar_row--meta">
      <div class="crb_table_bar_left">
        <span class="crb_table_meta_label">Show</span>
        <!-- Options are emitted here, not by an x-for.
             Alpine applies x-model when it walks the <select>, and an x-for
             inside it has not produced its <option>s yet - so the browser has
             nothing matching to select and falls back to the first entry. That
             went unnoticed while length always started at lengthMenu[0]; once a
             restored state opens the table on 30 rows, the select said 10 while
             the table paged by 30. cfg.lengthMenu is known when this markup is
             built, so the options can simply be static. -->
        <select class="crb_table_select" x-model.number="length" @change="setLength()">
          ${cfg.lengthMenu.map(function (n) {
              return '<option value="' + Number(n) + '">' + Number(n) + '</option>';
          }).join("")}
        </select>
        <span class="crb_table_meta_label">entries</span>
        <span class="crb_table_count" x-text="infoText"></span>
        <!-- Selection count sits with the row counters it qualifies, not among
             the action buttons - it reports state, it is not something to click. -->
        <span class="crb_table_selcount" x-show="selectedCount" x-cloak>
          <span x-text="selectedCount"></span>&nbsp;selected
          <button type="button" @click="clearSelection()" title="Clear selection" aria-label="Clear selection">
            <i data-lucide="x" class="w-3 h-3"></i>
          </button>
        </span>
      </div>

      <div class="crb_table_pager">
        <button type="button" class="crb_table_page" @click="goToPage(1)" :disabled="page === 1" title="First">
          <i data-lucide="chevrons-left" class="w-4 h-4"></i>
        </button>
        <button type="button" class="crb_table_page" @click="goToPage(page - 1)" :disabled="page === 1" title="Previous">
          <i data-lucide="chevron-left" class="w-4 h-4"></i>
        </button>
        <template x-for="(p, pi) in pageButtons" :key="pi">
          <button type="button" class="crb_table_page"
                  :class="p === page ? 'crb_table_page--active' : ''"
                  :disabled="p === '…'" @click="p !== '…' && goToPage(p)" x-text="p"></button>
        </template>
        <button type="button" class="crb_table_page" @click="goToPage(page + 1)"
                :disabled="page >= pageCount" title="Next">
          <i data-lucide="chevron-right" class="w-4 h-4"></i>
        </button>
        <button type="button" class="crb_table_page" @click="goToPage(pageCount)"
                :disabled="page >= pageCount" title="Last">
          <i data-lucide="chevrons-right" class="w-4 h-4"></i>
        </button>
      </div>
    </div>

    <div class="crb_table_activefilters" x-show="activeFilterList.length" x-cloak>
      <span class="crb_table_meta_label">Filtered by</span>
      <template x-for="f in activeFilterList" :key="f.field">
        <span class="crb_table_chip">
          <span class="crb_table_chip_key" x-text="f.title + ':'"></span>
          <span x-text="f.values.join(', ')"></span>
          <button type="button" @click="clearFilter(f.field)" :aria-label="'Remove filter on ' + f.title">
            <i data-lucide="x" class="w-3 h-3"></i>
          </button>
        </span>
      </template>
      <button type="button" class="crb_table_clearall" @click="clearAllFilters()">Clear all</button>
    </div>
  </div>

  <div class="crb_table_scroll">
    <table class="crb_table" :aria-busy="loading">
      <thead>
        <tr>
          ${cfg.selection ? `<th class="crb_table_th crb_table_th--tiny">
            <input type="checkbox" class="crb_table_checkbox" @change="toggleAllOnPage()"
                   :checked="allSelectableOnPageSelected" :indeterminate="someSelectedOnPage"
                   :disabled="!selectableRowsOnPage.length" aria-label="Select all rows on this page">
          </th>` : ''}
          ${cfg.rowDetail ? '<th class="crb_table_th crb_table_th--tiny"><span class="sr-only">Details</span></th>' : ''}
          ${cfg.actions.length ? '<th class="crb_table_th crb_table_th--actions">Actions</th>' : ''}
          <template x-for="(col, i) in columns" :key="i">
            <template x-if="col.visible">
              <th class="crb_table_th" :style="col.width ? ('width:' + col.width) : ''"
                  :aria-sort="ariaSortFor(col)">
                <div class="crb_table_th_inner">
                  <template x-if="col.sortable">
                    <button type="button" class="crb_table_sortbtn" @click="toggleSort(col)"
                            :title="'Sort by ' + col.title">
                      <span class="crb_table_label" x-text="col.title" :title="col.title"></span>
                      <span class="crb_table_sorticon" x-html="sortIconFor(col)"></span>
                    </button>
                  </template>
                  <template x-if="!col.sortable">
                    <span class="crb_table_label" x-text="col.title" :title="col.title"></span>
                  </template>
                  <!-- Optional second line under the title, for a column whose header
                       carries a qualifier: the campaign report's combination columns
                       show "PROD FR" over the robot they ran on. -->
                  <template x-if="col.subtitle">
                    <span class="crb_table_sublabel" x-text="col.subtitle" :title="col.subtitle"></span>
                  </template>
                  <template x-if="col.filterable">
                    <!-- data-filter-field is how applyFilterAndOpenNext() finds
                         the next column's anchor to position the popover over. -->
                    <button type="button" class="crb_table_filterbtn"
                            :data-filter-field="col.field"
                            :class="isFilterActive(col.field) ? 'crb_table_filterbtn--active' : ''"
                            @click.stop="openFilter(col, $event)"
                            :title="'Filter ' + col.title">
                      <i data-lucide="filter" class="w-3 h-3"></i>
                    </button>
                  </template>
                </div>
              </th>
            </template>
          </template>
        </tr>
      </thead>

        <template x-if="error">
          <tbody><tr><td :colspan="totalColumnCount" class="crb_table_state crb_table_state--error">
            <i data-lucide="alert-circle" class="w-4 h-4"></i>
            <span x-text="error"></span>
            <button type="button" class="crb_table_retry" @click="refresh()">Retry</button>
          </td></tr></tbody>
        </template>

        <template x-if="!error && loading && rows.length === 0">
          <tbody>
            <template x-for="s in skeletonRows" :key="'sk' + s">
              <tr class="crb_table_tr">
                <template x-for="c in totalColumnCount" :key="'skc' + c">
                  <td class="crb_table_td"><span class="crb_table_skeleton"></span></td>
                </template>
              </tr>
            </template>
          </tbody>
        </template>

        <template x-if="!error && !loading && rows.length === 0">
          <tbody><tr><td :colspan="totalColumnCount" class="crb_table_state">
            <i data-lucide="inbox" class="w-5 h-5 opacity-60"></i>
            <span>${crbTableEscape(cfg.emptyMessage)}</span>
          </td></tr></tbody>
        </template>

        <template x-for="(row, r) in rows" :key="rowKeyFor(row, r)">
          <tbody class="crb_table_rowgroup">
            <tr class="crb_table_tr group" :class="isRowSelected(rowKeyFor(row, r)) ? 'crb_table_tr--selected' : ''">
              <template x-if="hasSelection">
                <td class="crb_table_td crb_table_td--tiny">
                  <input type="checkbox" class="crb_table_checkbox"
                         x-show="isRowSelectable(row)"
                         :checked="isRowSelected(rowKeyFor(row, r))"
                         @change="toggleRow(rowKeyFor(row, r))"
                         :aria-label="'Select row ' + rowKeyFor(row, r)">
                </td>
              </template>

              <template x-if="hasRowDetail">
                <td class="crb_table_td crb_table_td--tiny">
                  <button type="button" class="crb_table_expand" @click="toggleRowDetail(rowKeyFor(row, r))"
                          :aria-expanded="isExpanded(rowKeyFor(row, r))" aria-label="Toggle details">
                    <i data-lucide="chevron-right" class="w-4 h-4"
                       :class="isExpanded(rowKeyFor(row, r)) ? 'crb_table_expand--open' : ''"></i>
                  </button>
                </td>
              </template>

              <template x-if="actions.length">
                <td class="crb_table_td crb_table_td--actions" data-label="Actions">
                  <div class="crb_table_actions" x-html="actionsHtmlFor(row, r)"></div>
                </td>
              </template>

              <template x-for="(col, i) in columns" :key="i">
                <template x-if="col.visible">
                  <td class="crb_table_td" :class="col.className" :data-label="col.title"
                      x-html="cellHtml(col, row, r)"></td>
                </template>
              </template>
            </tr>

            <template x-if="hasRowDetail && isExpanded(rowKeyFor(row, r))">
              <tr class="crb_table_detailrow">
                <td :colspan="totalColumnCount" x-html="detailHtml(row)"></td>
              </tr>
            </template>
          </tbody>
        </template>
    </table>
  </div>

  <!-- Column-filter popover: rendered once at root level and positioned fixed,
       because the header it belongs to lives inside an overflow:auto scroller
       that would otherwise clip it. -->
  <!-- One popover, repositioned over whichever column asked for it.
       Two modes: a column declared with like:true gets a free-text box (the
       server does a LIKE on it, so an arbitrary substring is the useful input
       and a list of
       every distinct value is not); every other filterable column gets the
       distinct-value checklist. -->
  <div class="crb_table_popover crb_table_popover--filter" x-show="openFilterField" x-cloak
       x-ref="filterPopover" @click.outside="closeFilter()" :style="filterStyle">

    <div class="crb_table_popover_head">
      <span class="crb_table_popover_title" x-text="openFilterTitle"></span>
      <!-- All / None act on what the search box currently shows, not on the whole
           list: with 300 values, "All" after typing three letters is the fast way
           to pick a family, and that is the behaviour it replaces. -->
      <span class="crb_table_popover_quick" x-show="!openFilterIsLike">
        <button type="button" @click="checkAllVisible()">All</button>
        <button type="button" @click="uncheckAllVisible()">None</button>
      </span>
    </div>

    <!-- Enter applies, Tab applies and moves to the next filterable column,
         Escape abandons. Same keys as the widget this replaces, because filling
         three column filters in a row without touching the mouse is how this
         gets used. -->
    <input type="search" class="crb_table_popover_search" x-model="filterSearch"
           x-ref="filterSearchInput"
           :placeholder="openFilterIsLike ? 'Contains...' : 'Search values...'"
           :aria-label="openFilterIsLike ? 'Filter value' : 'Search filter values'"
           @keydown.enter.prevent="applyFilter()"
           @keydown.tab.prevent="applyFilterAndOpenNext()"
           @keydown.escape.prevent="closeFilter()">

    <div class="crb_table_popover_list" x-show="!openFilterIsLike">
      <template x-if="filterLoading">
        <div class="crb_table_popover_empty">Loading…</div>
      </template>
      <template x-if="!filterLoading && visibleFilterValues.length === 0">
        <div class="crb_table_popover_empty">No value</div>
      </template>
      <template x-for="v in visibleFilterValues" :key="v">
        <label class="crb_table_popover_item">
          <input type="checkbox" :checked="isValueChecked(v)" @change="toggleFilterValue(v)">
          <span x-text="v === '' ? '(empty)' : v"></span>
        </label>
      </template>
    </div>

    <div class="crb_table_popover_hint" x-show="openFilterIsLike">
      Matches any value containing what you type.
    </div>

    <div class="crb_table_popover_footer">
      <button type="button" class="crb_table_popover_btn" @click="clearFilter(openFilterField)">Clear</button>
      <button type="button" class="crb_table_popover_btn crb_table_popover_btn--primary"
              @click="applyFilter()">Apply</button>
    </div>
  </div>
</div>`;
}

/* -----------------------------------------------------------------------------
 * The Alpine component itself. Registered as a plain global so it can be used as
 * `x-data="crbTable(...)"`, matching how the rest of this codebase declares its
 * Alpine components (see include/transversal/*.html).
 * -------------------------------------------------------------------------- */
function crbTable(cfg) {
    return {
        cfg: cfg,
        columns: cfg.columns.map(function (c) { return $.extend({}, c); }),
        actions: cfg.actions,
        lengthMenu: cfg.lengthMenu,
        hasRowDetail: typeof cfg.rowDetail === "function",
        hasSelection: Boolean(cfg.selection),
        selected: [],

        // Client mode: the dataset lives here and fetchClient() derives `rows` from it.
        isClient: Array.isArray(cfg.clientRows),
        clientRows: Array.isArray(cfg.clientRows) ? cfg.clientRows.slice() : null,

        rows: [],
        totalRecords: 0,
        filteredRecords: 0,
        start: 0,
        length: cfg.pageLength,
        // Seeded from cfg.initialSearch for the same reason as initialFilters: a
        // page deep-linked with a search term must send it with the FIRST request,
        // not set it afterwards and fetch twice.
        search: cfg.initialSearch || "",
        sortField: cfg.defaultSort.field || null,
        sortDir: cfg.defaultSort.dir || "asc",
        loading: false,
        error: null,
        // Server mode overwrites this from the response; client mode keeps what the
        // page declared, since there is no response to read it from.
        hasPermissions: Boolean(cfg.hasPermissions),
        response: {},
        configOpen: false,
        dragIcon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" ' +
            'class="w-3.5 h-3.5" aria-hidden="true"><circle cx="9" cy="6" r="1.6"/><circle cx="15" cy="6" r="1.6"/>' +
            '<circle cx="9" cy="12" r="1.6"/><circle cx="15" cy="12" r="1.6"/>' +
            '<circle cx="9" cy="18" r="1.6"/><circle cx="15" cy="18" r="1.6"/></svg>',
        configStyle: "",
        _configAnchor: null,
        _dragFrom: null,
        expanded: [],
        toolbarHtml: "",

        // Column filters. Seeded from cfg.initialFilters so a deep-linked filter
        // is part of the first request rather than a second one.
        activeFilters: $.extend({}, cfg.initialFilters || {}),
        draftFilter: [],        // checkbox state of the open popover, applied on Apply
        openFilterField: null,
        openFilterTitle: "",
        // A `like` column filters on a typed substring instead of a value list;
        // the popover swaps its whole body on this.
        openFilterIsLike: false,
        filterStyle: "",
        filterValues: {},       // {field: [distinct values]} cache
        filterLoading: false,
        filterSearch: "",

        _searchTimer: null,
        _requestSeq: 0,
        _filterAnchor: null,
        _filterScrollHandler: null,
        _dataChangedHandler: null,

        init: function () {
            // Before the first fetch, so a restored sort or filter travels with
            // that request instead of causing a second one.
            this.restoreState();
            this.listenForDataChanges();
            this.fetch();
        },

        /**
         * Reload when something outside the table changes its data.
         *
         * The shared modals (include/transversal/*.html) are used by both V1 and
         * V2 pages, and they refresh the list by calling
         * $("#<legacy table id>").dataTable().fnDraw() directly. On a migrated
         * page that id no longer exists, so the call quietly hits an empty jQuery
         * set and the grid keeps showing stale rows until a manual reload - the
         * user creates an entry and appears to have lost it.
         *
         * Rather than teach every modal about the component, they now also emit
         * a plain `crb-data-changed` event; V1 pages ignore it (they still have
         * their fnDraw), and every V2 table on the page picks it up. A table can
         * narrow what it reacts to with cfg.refreshOn = ["testdatalib", ...],
         * matched against event.detail.source.
         */
        listenForDataChanges: function () {
            var self = this;
            // A client-mode table has no endpoint to re-query: its dataset is
            // owned by the page, which pushes a new one with crbTableSetRows().
            // Re-running the local pipeline on a data-changed event would only
            // re-sort the same stale rows and look like a refresh that did nothing.
            if (this.isClient) {
                return;
            }
            this._dataChangedHandler = function (e) {
                var wanted = self.cfg.refreshOn;
                var source = e && e.detail && e.detail.source;
                if (wanted && wanted.length && wanted.indexOf(source) === -1) {
                    return;
                }
                self.fetch();
            };
            window.addEventListener("crb-data-changed", this._dataChangedHandler);
        },

        /* ---------------------------------------------------------------
         * STATE PERSISTENCE.
         *
         * Restores what legacy's `stateSave: true` kept and this component had
         * narrowed to column visibility: search, sort, page, page length,
         * per-column filters and hidden columns.
         *
         * Two stores, newest wins:
         *  - localStorage, written synchronously on every change. Fast, and the
         *    only one that works when the user record is not loaded yet.
         *  - the user's server-side `userPreferences` blob, pushed debounced.
         *    This is what carries the state to another browser or machine, which
         *    is the half a pure-localStorage scheme cannot do.
         *
         * The blob format is deliberately the legacy one - a JSON dump of the
         * whole localStorage, key by key - so this shares the field with the
         * DataTables_* entries the not-yet-migrated pages still write, and
         * neither side clobbers the other.
         *
         * A `ts` stamp on each snapshot is what settles the two stores: legacy
         * let the server copy always win, which silently discarded a change made
         * seconds earlier on the machine you were sitting at.
         * ------------------------------------------------------------ */
        prefsKey: function () {
            return "crbTable:" + cfg.id + ":" + location.pathname;
        },

        /** Key used before this stored anything but column visibility. */
        legacyPrefsKey: function () {
            return this.prefsKey() + ":columns";
        },

        /**
         * Stable identity for a column across reloads.
         *
         * `field` where there is one; a presentational column (actions, a chip
         * built purely by render) has none, so it falls back to its title. Two
         * untitled presentational columns would collide - they are also the two
         * that nobody reorders or hides, and `configurable: false` keeps them out
         * of the panel entirely.
         */
        colKey: function (col) {
            return col.field || ("#" + (col.title || ""));
        },

        captureState: function () {
            var filters = {};
            var self = this;
            Object.keys(this.activeFilters).forEach(function (f) {
                var v = self.activeFilters[f];
                if (v && v.length) {
                    filters[f] = v.slice();
                }
            });
            return {
                v: 1,
                ts: Date.now(),
                hidden: this.columns.filter(function (c) { return c.field && !c.visible; })
                                    .map(function (c) { return c.field; }),
                order: this.columns.map(function (c) { return self.colKey(c); }),
                search: this.search,
                sortField: this.sortField,
                sortDir: this.sortDir,
                length: this.length,
                start: this.start,
                filters: filters
            };
        },

        applyState: function (state) {
            if (!state || typeof state !== "object") {
                return;
            }
            var self = this;

            if (Array.isArray(state.hidden)) {
                this.columns.forEach(function (c) {
                    if (c.field && state.hidden.indexOf(c.field) !== -1) {
                        c.visible = false;
                    }
                });
            }

            // Saved column order. Anything the snapshot does not mention keeps
            // its declared position at the end, so a column added to the page
            // since the view was saved appears instead of vanishing.
            if (Array.isArray(state.order) && state.order.length) {
                var known = {};
                state.order.forEach(function (k, i) { known[k] = i; });
                var placed = [];
                var rest = [];
                this.columns.forEach(function (c) {
                    if (known[self.colKey(c)] !== undefined) {
                        placed.push(c);
                    } else {
                        rest.push(c);
                    }
                });
                placed.sort(function (a, b) { return known[self.colKey(a)] - known[self.colKey(b)]; });
                this.columns = placed.concat(rest);
            }

            // A deep link is an instruction for THIS visit and outranks anything
            // remembered: following "show the test cases labelled X" must not land
            // on last week's filter instead.
            var deepLinked = Boolean(cfg.initialSearch) ||
                    Boolean(cfg.initialFilters && Object.keys(cfg.initialFilters).length);

            if (!deepLinked) {
                if (typeof state.search === "string") {
                    this.search = state.search;
                }
                if (state.filters && typeof state.filters === "object") {
                    Object.keys(state.filters).forEach(function (f) {
                        // Drop a filter on a column this table no longer has - the
                        // reporting grids rebuild their column set from the data.
                        if (self.cfg.columns.some(function (c) { return c.field === f; })) {
                            self.activeFilters[f] = state.filters[f];
                        }
                    });
                }
                if (typeof state.start === "number" && state.start >= 0) {
                    this.start = state.start;
                }
            }

            // Sort and page length are display preferences, not a view of the
            // data, so a deep link has no opinion on them.
            if (state.sortField && this.cfg.columns.some(function (c) {
                return c.field === state.sortField && c.sortable;
            })) {
                this.sortField = state.sortField;
                this.sortDir = (state.sortDir === "desc") ? "desc" : "asc";
            }
            if (typeof state.length === "number" && this.lengthMenu.indexOf(state.length) !== -1) {
                this.length = state.length;
            }
        },

        /**
         * Reads both stores and applies the newer snapshot. Called once, before
         * the first fetch, so a restored sort or filter is part of that request
         * instead of causing a second one.
         */
        restoreState: function () {
            if (!cfg.persistState) {
                return;
            }
            var local = null;
            var remote = null;

            try {
                var raw = localStorage.getItem(this.prefsKey());
                if (raw) {
                    local = JSON.parse(raw);
                } else {
                    // Upgrade in place from the column-visibility-only key.
                    var legacyRaw = localStorage.getItem(this.legacyPrefsKey());
                    if (legacyRaw) {
                        var hidden = JSON.parse(legacyRaw);
                        if (Array.isArray(hidden)) {
                            local = {v: 1, ts: 0, hidden: hidden};
                        }
                        localStorage.removeItem(this.legacyPrefsKey());
                    }
                }
            } catch (e) {
                /* private mode / disabled storage - fall through to the server copy */
            }

            remote = crbTableReadServerPrefs(this.prefsKey());
            if (!remote) {
                // Same upgrade as above, for a user whose only copy of the
                // column-visibility-only key is the one on the server - i.e. it
                // was saved from another machine before this stored full state.
                // Without this that preference is simply never seen again.
                var legacyHidden = crbTableReadServerPrefs(this.legacyPrefsKey());
                if (Array.isArray(legacyHidden)) {
                    remote = {v: 1, ts: 0, hidden: legacyHidden};
                }
            }

            var chosen = local;
            if (remote && (!local || (remote.ts || 0) > (local.ts || 0))) {
                chosen = remote;
            }
            this.applyState(chosen);
        },

        /**
         * Writes the snapshot to localStorage now and schedules the server push.
         * Called from the user actions that change state, never from init() or a
         * crb-data-changed reload: bumping `ts` on a page load alone would make
         * whichever browser was opened last always win the comparison above.
         */
        persistState: function () {
            if (!cfg.persistState) {
                return;
            }
            try {
                localStorage.setItem(this.prefsKey(), JSON.stringify(this.captureState()));
            } catch (e) {
                return; // storage unavailable: nothing to push either
            }
            crbTableSchedulePrefsPush();
        },

        /* ---------------------------------------------------------------
         * Data loading. Sends the legacy DataTables-1.9 parameter set the
         * Java DataTableInformation parser already understands, so the
         * endpoint is byte-for-byte compatible with the V1 page.
         * ------------------------------------------------------------ */
        buildParams: function () {
            var self = this;
            var serverCols = crbTableServerColumns(this.cfg);
            var params = {
                iDisplayStart: this.start,
                iDisplayLength: this.length,
                sSearch: this.search,
                sColumns: serverCols.join(","),
                iSortingCols: 1,
                sLike: this.cfg.columns.filter(function (c) { return c.like && c.field; })
                                       .map(function (c) { return c.field; }).join(",")
            };

            // iSortCol_0 indexes into sColumns; because sColumns holds only real
            // data columns here, this always resolves to a genuine column name.
            if (this.sortField) {
                var idx = serverCols.indexOf(this.sortField);
                if (idx !== -1) {
                    params.iSortCol_0 = idx;
                    params.sSortDir_0 = this.sortDir;
                }
            }

            // Per-column filters use the same index space as sColumns.
            Object.keys(this.activeFilters).forEach(function (field) {
                var values = self.activeFilters[field];
                if (!values || !values.length) {
                    return;
                }
                var i = serverCols.indexOf(field);
                if (i !== -1) {
                    params["sSearch_" + i] = values.join(",");
                }
            });

            if (this.cfg.sendDefaultSystems && typeof getUser === "function") {
                var user = getUser();
                if (user && user.defaultSystems) {
                    params.system = user.defaultSystems;
                }
            }
            return params;
        },

        fetch: function () {
            if (this.isClient) {
                this.fetchClient();
                return;
            }
            var self = this;
            var seq = ++this._requestSeq;
            this.loading = true;
            this.error = null;

            $.ajax({
                url: this.cfg.endpoint,
                type: "POST",
                dataType: "json",
                traditional: true,
                data: this.buildParams()
            }).done(function (json) {
                // Drop responses that a newer request has already superseded, so
                // fast typing in the search box can never leave stale rows on screen.
                if (seq !== self._requestSeq) {
                    return;
                }
                self.rows = (json && json[self.cfg.dataProp]) || [];
                self.totalRecords = (json && json.iTotalRecords) || 0;
                self.filteredRecords = (json && json.iTotalDisplayRecords !== undefined)
                    ? json.iTotalDisplayRecords : self.totalRecords;
                self.hasPermissions = Boolean(json && json.hasPermissions);
                // Endpoints do not agree on which permission flags they send:
                // /api/robots/read returns `hasPermissions`, ReadTestCase returns
                // `hasPermissionsCreate` and nothing else. Keeping the whole
                // response available means a page can read whichever flag its own
                // servlet actually sends instead of the component guessing.
                self.response = json || {};
                self.loading = false;

                // A restored page number can outlive the rows it pointed at (the
                // list shrank, or a filter came back with it). Landing on an empty
                // page 7 with no explanation is worse than landing on page 1.
                if (self.rows.length === 0 && self.start > 0 && self.filteredRecords > 0) {
                    self.start = 0;
                    self.fetch();
                    return;
                }

                self.renderToolbar();
                self.refreshIcons();
            }).fail(function (xhr) {
                if (seq !== self._requestSeq) {
                    return;
                }
                self.loading = false;
                self.rows = [];
                // Legacy routed every failure into a generic global handler, which
                // in practice left the table silently empty. Surfacing it in-place
                // with a retry keeps the failure attributable to this table.
                self.error = "Could not load data (" + (xhr && xhr.status ? xhr.status : "network error") + ")";
                self.refreshIcons();
            });
        },

        /* ---------------------------------------------------------------
         * CLIENT MODE.
         *
         * The reporting pages compute their rows in the browser (a period's
         * campaigns, a run-by-run list, an execution history): there is no
         * paginated endpoint to talk to, the whole dataset is already in memory.
         * Everything above the data layer - columns, actions and their gates,
         * the config panel, column persistence, the responsive card mode, the
         * filter popovers - is identical; only where the rows come from changes.
         *
         * `field` is then simply the key to read, sort and filter on. The
         * "sortable needs a server field" rule still applies and still means the
         * same thing: a column with nothing to sort by cannot be sorted.
         * ------------------------------------------------------------ */
        fetchClient: function () {
            var self = this;
            var rows = Array.isArray(this.clientRows) ? this.clientRows.slice() : [];

            // 1. free-text search, over every searchable column's rendered value
            var term = (this.search || "").trim().toLowerCase();
            if (term) {
                var searchCols = this.cfg.columns.filter(function (c) { return c.searchable !== false && c.prop; });
                rows = rows.filter(function (row) {
                    return searchCols.some(function (c) {
                        var v = row[c.prop];
                        return v !== undefined && v !== null && String(v).toLowerCase().indexOf(term) !== -1;
                    });
                });
            }

            // 2. per-column filters. An IN-list, same semantics as sSearch_N -
            //    except on a `like` column, where the server would run a LIKE and
            //    the popover collects a substring, so the local pipeline has to
            //    match on containment or the two modes would disagree.
            Object.keys(this.activeFilters).forEach(function (field) {
                var values = self.activeFilters[field];
                if (!values || !values.length) {
                    return;
                }
                var col = self.cfg.columns.filter(function (c) { return c.field === field; })[0];
                var prop = col ? col.prop : field;
                if (col && col.like) {
                    var needle = String(values[0] || "").toLowerCase();
                    rows = rows.filter(function (row) {
                        return crbTableFilterValue(row[prop]).toLowerCase().indexOf(needle) !== -1;
                    });
                    return;
                }
                rows = rows.filter(function (row) {
                    return values.indexOf(crbTableFilterValue(row[prop])) !== -1;
                });
            });

            // 3. sort. Numbers and dates compare as numbers, everything else as a
            //    locale-aware string, so "10" does not sort before "9".
            if (this.sortField) {
                var scol = this.cfg.columns.filter(function (c) { return c.field === self.sortField; })[0];
                var sprop = scol ? scol.prop : this.sortField;
                var dir = this.sortDir === "desc" ? -1 : 1;
                rows.sort(function (a, b) {
                    var va = a[sprop];
                    var vb = b[sprop];
                    var ea = (va === undefined || va === null || va === "");
                    var eb = (vb === undefined || vb === null || vb === "");
                    if (ea && eb) {
                        return 0;
                    }
                    // Empties always last, whichever way the column is sorted -
                    // a blank cell is not "the smallest value".
                    if (ea) {
                        return 1;
                    }
                    if (eb) {
                        return -1;
                    }
                    if (typeof va === "number" && typeof vb === "number") {
                        return (va - vb) * dir;
                    }
                    var na = Number(va);
                    var nb = Number(vb);
                    if (!isNaN(na) && !isNaN(nb) && String(va).trim() !== "" && String(vb).trim() !== "") {
                        return (na - nb) * dir;
                    }
                    return String(va).localeCompare(String(vb), undefined, {numeric: true, sensitivity: "base"}) * dir;
                });
            }

            this.filteredRecords = rows.length;
            this.totalRecords = Array.isArray(this.clientRows) ? this.clientRows.length : 0;

            // 4. page. Guard the offset: deleting or filtering rows can leave
            //    `start` past the end, which would show an empty page with a
            //    pager saying there is data.
            if (this.start >= rows.length) {
                this.start = Math.max(0, (Math.ceil(rows.length / this.length) - 1) * this.length);
            }
            this.rows = rows.slice(this.start, this.start + this.length);

            this.loading = false;
            this.error = null;
            this.renderToolbar();
            this.refreshIcons();
        },

        /** Replaces the dataset in client mode and re-renders. */
        setRows: function (rows) {
            this.clientRows = Array.isArray(rows) ? rows : [];
            this.start = 0;
            this.selected = [];
            this.fetch();
        },

        get hasRefreshHook() {
            return typeof this.cfg.onRefresh === "function";
        },

        refresh: function () {
            if (this.isClient) {
                if (typeof this.cfg.onRefresh === "function") {
                    this.cfg.onRefresh(this);
                }
                return;
            }
            this.fetch();
        },

        renderToolbar: function () {
            if (typeof this.cfg.toolbar === "function") {
                this.toolbarHtml = this.cfg.toolbar({
                    hasPermissions: this.hasPermissions,
                    response: this.response,
                    selectedCount: this.selected.length,
                    selectedKeys: this.selected.slice(),
                    table: this
                }) || "";
            }
        },

        /**
         * Lucide replaces <i data-lucide> with generated <svg>, so any markup
         * Alpine has just (re)inserted needs another pass.
         */
        refreshIcons: function () {
            this.$nextTick(function () {
                if (window.lucide) {
                    lucide.createIcons();
                }
            });
        },

        /* ---------------------------------------------------------------
         * Sorting / paging / searching
         * ------------------------------------------------------------ */
        toggleSort: function (col) {
            if (!col.sortable) {
                return;
            }
            if (this.sortField === col.field) {
                this.sortDir = (this.sortDir === "asc") ? "desc" : "asc";
            } else {
                this.sortField = col.field;
                this.sortDir = "asc";
            }
            this.start = 0;
            this.persistState();
            this.fetch();
        },

        sortIconFor: function (col) {
            if (!col.sortable) {
                return "";
            }
            var name = "chevrons-up-down";
            if (this.sortField === col.field) {
                name = (this.sortDir === "asc") ? "arrow-up" : "arrow-down";
            }
            var active = (this.sortField === col.field) ? " crb_table_sorticon--active" : "";
            return '<i data-lucide="' + name + '" class="w-3.5 h-3.5' + active + '"></i>';
        },

        ariaSortFor: function (col) {
            if (!col.sortable || this.sortField !== col.field) {
                return "none";
            }
            return (this.sortDir === "asc") ? "ascending" : "descending";
        },

        onSearchInput: function () {
            var self = this;
            clearTimeout(this._searchTimer);
            this._searchTimer = setTimeout(function () {
                self.start = 0;
                self.persistState();
                self.fetch();
            }, 400);
        },

        setLength: function () {
            this.start = 0;
            this.persistState();
            this.fetch();
        },

        goToPage: function (p) {
            if (p < 1 || p > this.pageCount || p === this.page) {
                return;
            }
            this.start = (p - 1) * this.length;
            this.persistState();
            this.fetch();
        },

        get page() {
            return Math.floor(this.start / this.length) + 1;
        },

        get pageCount() {
            return Math.max(1, Math.ceil(this.filteredRecords / this.length));
        },

        /** Page numbers with ellipses, so a 400-page table stays one row of buttons. */
        get pageButtons() {
            var total = this.pageCount, current = this.page, out = [];
            if (total <= 7) {
                for (var i = 1; i <= total; i++) { out.push(i); }
                return out;
            }
            out.push(1);
            var from = Math.max(2, current - 1);
            var to = Math.min(total - 1, current + 1);
            if (from > 2) { out.push("…"); }
            for (var p = from; p <= to; p++) { out.push(p); }
            if (to < total - 1) { out.push("…"); }
            out.push(total);
            return out;
        },

        get infoText() {
            if (this.filteredRecords === 0) {
                return "No entries";
            }
            var first = this.start + 1;
            var last = Math.min(this.start + this.length, this.filteredRecords);
            var base = first + "-" + last + " of " + this.filteredRecords;
            if (this.filteredRecords !== this.totalRecords) {
                base += " (of " + this.totalRecords + ")";
            }
            return base;
        },

        get totalColumnCount() {
            var n = this.columns.filter(function (c) { return c.visible; }).length;
            if (this.hasSelection) { n++; }
            if (this.hasRowDetail) { n++; }
            if (this.actions.length) { n++; }
            return n;
        },

        /** Placeholder row count while the first page loads, capped so a page size
         *  of 1000 doesn't paint a thousand shimmer bars. */
        get skeletonRows() {
            return Math.min(this.length, 8);
        },

        toggleColumn: function (i) {
            this.columns[i].visible = !this.columns[i].visible;
            this.persistState();
            this.refreshIcons();
        },

        /* ---------------------------------------------------------------
         * Column order.
         *
         * Legacy had this through DataTables' colReorder (drag a header). Here
         * it lives in the Config panel instead: dragging a header would have to
         * share that surface with the sort button and the filter funnel already
         * on it, and a drag that starts on a sort control is a coin toss between
         * reordering and sorting.
         *
         * Only the DISPLAY order changes. buildParams() derives sColumns from
         * cfg.columns, which is untouched, so iSortCol_0 and sSearch_N keep
         * indexing the same list and the server contract is unaffected.
         * ------------------------------------------------------------ */
        moveColumn: function (from, to) {
            if (to < 0 || to >= this.columns.length || from === to) {
                return;
            }
            var next = this.columns.slice();
            var moved = next.splice(from, 1)[0];
            next.splice(to, 0, moved);
            this.columns = next;
            this.persistState();
            this.refreshIcons();
        },

        onColumnDragStart: function (i, event) {
            this._dragFrom = i;
            if (event.dataTransfer) {
                event.dataTransfer.effectAllowed = "move";
                // Firefox refuses to start a drag unless some data is set.
                event.dataTransfer.setData("text/plain", String(i));
            }
        },

        onColumnDrop: function (i) {
            if (this._dragFrom !== null && this._dragFrom !== undefined) {
                this.moveColumn(this._dragFrom, i);
            }
            this._dragFrom = null;
        },

        /**
         * Keyboard equivalent of the drag, on the handle itself: a reorder that
         * only works with a mouse is not a reorder for everyone.
         */
        onColumnKey: function (i, event) {
            if (event.key === "ArrowUp") {
                event.preventDefault();
                this.moveColumn(i, i - 1);
            } else if (event.key === "ArrowDown") {
                event.preventDefault();
                this.moveColumn(i, i + 1);
            }
        },

        /**
         * Back to the definitions the page declares: order, visibility, sort,
         * page length, search and filters. Clears the saved view in BOTH stores,
         * otherwise the next load would restore what was just discarded.
         *
         * Legacy offered Save / Reload / Reset in a drawer; saving is automatic
         * here and reloading is what the browser's own reload does, so Reset is
         * the one of the three that still has a job.
         */
        resetView: function () {
            try {
                localStorage.removeItem(this.prefsKey());
                localStorage.removeItem(this.legacyPrefsKey());
            } catch (e) {
                /* storage unavailable - the server copy below still needs clearing */
            }
            crbTableSchedulePrefsPush();

            this.columns = this.cfg.columns.map(function (c) { return $.extend({}, c); });
            this.search = this.cfg.initialSearch || "";
            this.activeFilters = $.extend({}, this.cfg.initialFilters || {});
            this.sortField = this.cfg.defaultSort.field || null;
            this.sortDir = this.cfg.defaultSort.dir || "asc";
            this.length = this.cfg.pageLength;
            this.start = 0;
            this.closeConfig();
            this.fetch();
            this.refreshIcons();
        },

        /* ---------------------------------------------------------------
         * Multi-row selection (mass actions)
         * ------------------------------------------------------------ */
        isRowSelectable: function (row) {
            if (!this.hasSelection) {
                return false;
            }
            var gate = cfg.selection.gate;
            if (typeof gate === "function") {
                return Boolean(gate(row, {hasPermissions: this.hasPermissions}));
            }
            if (gate === "always") {
                return true;
            }
            if (gate === "permission") {
                return this.hasPermissions === true;
            }
            return false;
        },

        isRowSelected: function (key) {
            return this.selected.indexOf(key) !== -1;
        },

        toggleRow: function (key) {
            var at = this.selected.indexOf(key);
            if (at === -1) {
                this.selected.push(key);
            } else {
                this.selected.splice(at, 1);
            }
            this.renderToolbar();
            this.refreshIcons();
        },

        get selectableRowsOnPage() {
            var self = this;
            return this.rows.filter(function (r) { return self.isRowSelectable(r); });
        },

        get allSelectableOnPageSelected() {
            var self = this;
            var sel = this.selectableRowsOnPage;
            return sel.length > 0 && sel.every(function (r) {
                return self.isRowSelected(self.rowKeyFor(r, self.rows.indexOf(r)));
            });
        },

        get someSelectedOnPage() {
            var self = this;
            var sel = this.selectableRowsOnPage;
            var n = sel.filter(function (r) {
                return self.isRowSelected(self.rowKeyFor(r, self.rows.indexOf(r)));
            }).length;
            return n > 0 && n < sel.length;
        },

        toggleAllOnPage: function () {
            var self = this;
            var keys = this.selectableRowsOnPage.map(function (r) {
                return self.rowKeyFor(r, self.rows.indexOf(r));
            });
            if (this.allSelectableOnPageSelected) {
                this.selected = this.selected.filter(function (k) { return keys.indexOf(k) === -1; });
            } else {
                keys.forEach(function (k) {
                    if (self.selected.indexOf(k) === -1) {
                        self.selected.push(k);
                    }
                });
            }
            this.renderToolbar();
            this.refreshIcons();
        },

        /**
         * Rows currently selected AND present on the current page. Selection
         * survives paging (keys are kept), but only loaded rows can be returned
         * as objects - a page acting on a cross-page selection should use
         * `selectedKeys` and resolve them server-side.
         */
        get selectedRows() {
            var self = this;
            return this.rows.filter(function (r, i) { return self.isRowSelected(self.rowKeyFor(r, i)); });
        },

        get selectedKeys() {
            return this.selected.slice();
        },

        get selectedCount() {
            return this.selected.length;
        },

        clearSelection: function () {
            this.selected = [];
            this.renderToolbar();
            this.refreshIcons();
        },

        /* ---------------------------------------------------------------
         * Per-column filters
         * ------------------------------------------------------------ */
        /* ---------------------------------------------------------------
         * Column config panel. Same fixed-positioning treatment as the filter
         * popover below, for the same reason - see crbTablePopoverStyle().
         * ------------------------------------------------------------ */
        toggleConfig: function (event) {
            if (this.configOpen) {
                this.closeConfig();
                return;
            }
            var self = this;
            this.configOpen = true;
            this._configAnchor = event.currentTarget;
            this.trackPopoverScroll();
            // Two passes: the first places it so it can be measured, the second
            // uses that measurement to decide whether it has to flip above.
            this.positionConfig();
            this.$nextTick(function () { self.positionConfig(); });
        },

        positionConfig: function () {
            if (!this.configOpen) {
                return;
            }
            var style = crbTablePopoverStyle(this._configAnchor, this.$refs.configPopover, "right");
            if (!style) {
                this.closeConfig();
                return;
            }
            this.configStyle = style;
        },

        closeConfig: function () {
            this.configOpen = false;
            this._configAnchor = null;
            this.releasePopoverScroll();
        },

        /**
         * A fixed popover does not travel with its anchor, so both of them track
         * the page and the table's own scroller until closed. One listener pair
         * serves whichever is open.
         */
        trackPopoverScroll: function () {
            if (this._filterScrollHandler) {
                return;
            }
            var self = this;
            this._filterScrollHandler = function () {
                self.positionFilter();
                self.positionConfig();
            };
            window.addEventListener("scroll", this._filterScrollHandler, true);
            window.addEventListener("resize", this._filterScrollHandler);
        },

        releasePopoverScroll: function () {
            if (this.openFilterField || this.configOpen) {
                return; // the other one is still open and still needs it
            }
            if (this._filterScrollHandler) {
                window.removeEventListener("scroll", this._filterScrollHandler, true);
                window.removeEventListener("resize", this._filterScrollHandler);
                this._filterScrollHandler = null;
            }
        },

        isFilterActive: function (field) {
            return Boolean(this.activeFilters[field] && this.activeFilters[field].length);
        },

        get activeFilterList() {
            var self = this;
            return Object.keys(this.activeFilters)
                .filter(function (f) { return self.activeFilters[f] && self.activeFilters[f].length; })
                .map(function (f) {
                    var col = self.columns.filter(function (c) { return c.field === f; })[0];
                    return {field: f, title: (col && col.title) || f, values: self.activeFilters[f]};
                });
        },

        openFilter: function (col, event) {
            var self0 = this;
            if (this.openFilterField === col.field) {
                this.closeFilter();
                return;
            }
            this.openFilterField = col.field;
            this.openFilterTitle = col.title;
            this.openFilterIsLike = Boolean(col.like);
            this.draftFilter = (this.activeFilters[col.field] || []).slice();
            // In like mode the search box IS the filter input, so it opens holding
            // the active value rather than empty.
            this.filterSearch = this.openFilterIsLike ? (this.draftFilter[0] || "") : "";
            this._filterAnchor = event.currentTarget;
            this.trackPopoverScroll();
            this.positionFilter();

            this.$nextTick(function () {
                // Second pass with the popover measured, so it can flip above the
                // header when there is no room below.
                self0.positionFilter();
                // Keyboard-first: the box is focused on open, so Tab-chaining
                // across several columns never needs the mouse.
                var input = self0.$refs.filterSearchInput;
                if (input) {
                    input.focus();
                    input.select();
                }
            });

            if (!this.filterValues[col.field]) {
                this.loadFilterValues(col.field);
            }
            this.refreshIcons();
        },

        /** Keeps the popover glued under the funnel button it belongs to. */
        positionFilter: function () {
            if (!this.openFilterField || !this._filterAnchor) {
                return;
            }
            // Close rather than float away once the anchor scrolls out of view.
            var style = crbTablePopoverStyle(this._filterAnchor, this.$refs.filterPopover, "left");
            if (!style) {
                this.closeFilter();
                return;
            }
            this.filterStyle = style;
        },

        closeFilter: function () {
            this.openFilterField = null;
            this.openFilterIsLike = false;
            this._filterAnchor = null;
            this.releasePopoverScroll();
        },

        loadFilterValues: function (field) {
            var self = this;
            // A like column has no value list to offer - and asking a distinct
            // endpoint for every value of a free-text column is a slow request
            // whose answer would be thrown away.
            if (this.openFilterIsLike) {
                this.filterValues[field] = [];
                this.filterLoading = false;
                return;
            }
            this.filterLoading = true;

            // Client mode: the values ARE the dataset's distinct values. Computing
            // them from the full set (not the filtered view) keeps a filter's own
            // options stable while it is being edited.
            if (this.isClient) {
                var col = this.cfg.columns.filter(function (c) { return c.field === field; })[0];
                var prop = col ? col.prop : field;
                var seen = {};
                (this.clientRows || []).forEach(function (row) {
                    var v = crbTableFilterValue(row[prop]);
                    if (v !== "") {
                        seen[v] = true;
                    }
                });
                this.filterValues[field] = Object.keys(seen).sort(function (a, b) {
                    return a.localeCompare(b, undefined, {numeric: true, sensitivity: "base"});
                });
                this.filterLoading = false;
                return;
            }

            // Most distinct endpoints are legacy servlets that answer either verb,
            // so a GET with the column in the query string is enough. A few only
            // accept POST (api/testcases/objects is a Spring @PostMapping) and
            // answer 405 to a GET, which used to surface as an empty, silent filter
            // list. Those pages set distinctMethod:"POST" and get the same params as
            // the main fetch, so the values offered match the current search.
            var req;
            if (String(this.cfg.distinctMethod || "GET").toUpperCase() === "POST") {
                var params = this.buildParams();
                params.columnName = field;
                req = $.ajax({
                    url: this.cfg.distinctEndpoint, type: "POST", dataType: "json",
                    traditional: true, data: params
                });
            } else {
                var sep = this.cfg.distinctEndpoint.indexOf("?") > -1 ? "&" : "?";
                req = $.ajax({
                    url: this.cfg.distinctEndpoint + sep + "columnName=" + encodeURIComponent(field),
                    type: "GET", dataType: "json"
                });
            }

            req
                .done(function (json) {
                    self.filterValues[field] = (json && json.distinctValues) || [];
                    self.filterLoading = false;
                })
                .fail(function () {
                    self.filterValues[field] = [];
                    self.filterLoading = false;
                });
        },

        get visibleFilterValues() {
            var all = this.filterValues[this.openFilterField] || [];
            var q = (this.filterSearch || "").toLowerCase();
            if (!q) {
                return all;
            }
            return all.filter(function (v) { return String(v).toLowerCase().indexOf(q) !== -1; });
        },

        isValueChecked: function (v) {
            return this.draftFilter.indexOf(v) !== -1;
        },

        /** Checks everything the popover's own search currently lists. */
        checkAllVisible: function () {
            var self = this;
            this.visibleFilterValues.forEach(function (v) {
                if (self.draftFilter.indexOf(v) === -1) {
                    self.draftFilter.push(v);
                }
            });
        },

        uncheckAllVisible: function () {
            var visible = this.visibleFilterValues;
            this.draftFilter = this.draftFilter.filter(function (v) {
                return visible.indexOf(v) === -1;
            });
        },

        toggleFilterValue: function (v) {
            var at = this.draftFilter.indexOf(v);
            if (at === -1) {
                this.draftFilter.push(v);
            } else {
                this.draftFilter.splice(at, 1);
            }
        },

        applyFilter: function () {
            if (!this.openFilterField) {
                return;
            }
            var field = this.openFilterField;

            if (this.openFilterIsLike) {
                // Like mode: the search box is the value.
                var text = (this.filterSearch || "").trim();
                if (text) {
                    this.activeFilters[field] = [text];
                } else {
                    delete this.activeFilters[field];
                }
            } else if (!this.draftFilter.length) {
                delete this.activeFilters[field];
            } else if (this.filterValues[field]
                    && this.draftFilter.length === this.filterValues[field].length) {
                // Everything ticked is the same view as nothing ticked. Recording
                // it as a filter would send every value of the column in
                // sSearch_N and light the funnel icon for a filter that excludes
                // nothing - so "All" then "Apply" clears instead.
                delete this.activeFilters[field];
            } else {
                this.activeFilters[field] = this.draftFilter.slice();
            }

            this.closeFilter();
            this.start = 0;
            this.persistState();
            this.fetch();
        },

        /**
         * Apply, then open the next filterable column's popover - the Tab key.
         *
         * Columns are walked in display order from the current one, skipping
         * hidden ones; the anchor is found by the data-filter-field attribute the
         * header buttons carry, so the popover positions itself over the right
         * header even after the column set has been rebuilt at runtime.
         */
        applyFilterAndOpenNext: function () {
            var field = this.openFilterField;
            if (!field) {
                return;
            }
            var at = this.columns.findIndex(function (c) { return c.field === field; });
            var next = null;
            for (var i = at + 1; i < this.columns.length; i++) {
                if (this.columns[i].visible && this.columns[i].filterable && this.columns[i].field) {
                    next = this.columns[i];
                    break;
                }
            }

            this.applyFilter();

            if (!next) {
                return;
            }
            var root = document.getElementById(cfg.id);
            var self = this;
            this.$nextTick(function () {
                var btn = root && root.querySelector('[data-filter-field="' + next.field + '"]');
                if (btn) {
                    // Bring the column into view first: a wide table scrolls
                    // horizontally, and Tabbing onto a header that is off to the
                    // right would open the popover clamped to the viewport edge,
                    // detached from any visible column. Instant, not smooth - the
                    // rect has to be final before positionFilter() reads it.
                    if (typeof btn.scrollIntoView === "function") {
                        btn.scrollIntoView({block: "nearest", inline: "nearest"});
                    }
                    // Going through the button's own click keeps openFilter's
                    // anchor and positioning path identical to a mouse open.
                    btn.click();
                }
            });
        },

        clearFilter: function (field) {
            delete this.activeFilters[field];
            this.persistState();
            if (this.openFilterField === field) {
                this.draftFilter = [];
                this.closeFilter();
            }
            this.start = 0;
            this.fetch();
        },

        clearAllFilters: function () {
            this.activeFilters = {};
            this.persistState();
            this.draftFilter = [];
            this.closeFilter();
            this.start = 0;
            this.fetch();
        },

        /* ---------------------------------------------------------------
         * Rows
         * ------------------------------------------------------------ */
        /**
         * Identifies a row across reloads. Accepts a property name or a function,
         * because several entities are keyed by a composite (a test case is
         * test + testcase, not one id column).
         * Falls back to the row index, which is enough for rendering but NOT for
         * selection - hence selection requires an explicit rowKey at config time.
         */
        rowKeyFor: function (row, index) {
            var k = this.cfg.rowKey;
            if (typeof k === "function") {
                return String(k(row));
            }
            if (k && row[k] !== undefined) {
                return String(row[k]);
            }
            return "idx:" + index;
        },

        isExpanded: function (key) {
            return this.expanded.indexOf(key) !== -1;
        },

        toggleRowDetail: function (key) {
            var at = this.expanded.indexOf(key);
            if (at === -1) {
                this.expanded.push(key);
            } else {
                this.expanded.splice(at, 1);
            }
            this.refreshIcons();
        },

        detailHtml: function (row) {
            return this.hasRowDetail ? (this.cfg.rowDetail(row) || "") : "";
        },

        /**
         * `render` receives the row index as well as the row, so a cell that needs
         * an interactive control can address the row by INDEX rather than by
         * interpolating its data into an inline handler.
         *
         * That distinction is a security boundary, not a style preference:
         * crbTableEscape() makes a value safe as HTML *text*, but an onclick
         * attribute is decoded from HTML entities and THEN evaluated as
         * JavaScript, so an escaped apostrophe (&#39;) still closes the JS string
         * it sits in. A test folder named  x'); doSomething(); //  therefore runs
         * arbitrary code from an "escaped" handler. Verified as exploitable before
         * this note was written. Use crbTableRowAction()/the row index instead of
         * building handlers out of row values.
         */
        /**
         * The term to mark in cells, or "" when there is nothing to mark.
         *
         * Deliberately the free-text search only, not the per-column filters: a
         * filter's matches are already obvious from the column it sits on, and
         * marking every cell of a filtered column would be noise.
         */
        get highlightTerm() {
            if (this.cfg.highlightSearch === false) {
                return "";
            }
            var term = String(this.search || "").trim();
            var min = this.cfg.highlightMinLength;
            if (typeof min !== "number") {
                min = CRB_TABLE_HIGHLIGHT_MIN_LENGTH;
            }
            return (term.length < min) ? "" : term;
        },

        cellHtml: function (col, row, rowIndex) {
            var term = this.highlightTerm;

            // A column excluded from the search cannot be the reason a row is
            // here, so a coincidental match in it would point at the wrong cell.
            if (term && col.searchable === false) {
                term = "";
            }

            if (typeof col.render === "function") {
                var html = col.render(row, rowIndex);
                return term ? crbTableHighlightHtml(html, term) : html;
            }

            var value = col.prop ? row[col.prop] : "";
            value = (value === undefined || value === null) ? "" : value;
            return term ? crbTableHighlight(value, term) : crbTableEscape(value);
        },

        /* ---------------------------------------------------------------
         * Actions and their permission gates.
         *
         * This is the single place in the V2 engine where "may this user see
         * this button" is decided, replacing the six independent conventions
         * the legacy pages grew (table-wide DOM attribute, per-row JSON field,
         * permission-plus-row-state, one-shot disable on load, requested-but-
         * never-read, and never-requested-at-all).
         * ------------------------------------------------------------ */
        isActionVisible: function (action, row) {
            var gate = action.gate;
            if (typeof gate === "function") {
                return Boolean(gate(row, {hasPermissions: this.hasPermissions}));
            }
            if (gate === "always") {
                return true;
            }
            if (gate === "permission") {
                return this.hasPermissions === true;
            }
            if (gate === "no-permission") {
                return this.hasPermissions !== true;
            }
            return false;
        },

        visibleActionsFor: function (row) {
            var self = this;
            return this.actions.filter(function (a) { return self.isActionVisible(a, row); });
        },

        /**
         * An action may additionally be *disabled* (rendered, dimmed, inert) as
         * opposed to hidden — TestCaseList's "Edit Header" works exactly that way,
         * and losing the distinction would change what users see. `icon` and
         * `title` may be functions of the row for the same reason: several legacy
         * buttons swap pencil/eye per row without changing what they do.
         */
        actionsHtmlFor: function (row, rowIndex) {
            var self = this;
            var ctx = {hasPermissions: this.hasPermissions};
            return this.visibleActionsFor(row).map(function (action, i) {
                var id = self.cfg.id + "_action_" + (action.key || i) + "_" + rowIndex;
                var danger = action.danger ? " crb_table_action--danger" : "";
                var icon = (typeof action.icon === "function") ? action.icon(row, ctx) : action.icon;
                var title = (typeof action.title === "function") ? action.title(row, ctx) : action.title;
                var disabled = (typeof action.disabled === "function")
                    ? Boolean(action.disabled(row, ctx))
                    : Boolean(action.disabled);
                var label = crbTableEscape(title || action.key || "");
                var iconHtml = '<i data-lucide="' + crbTableEscape(icon || "circle") + '" class="w-4 h-4"></i>';

                if (disabled) {
                    return '<button type="button" id="' + crbTableEscape(id) + '" disabled' +
                        ' class="crb_table_action crb_table_action--disabled"' +
                        ' title="' + label + '" aria-label="' + label + '">' + iconHtml + '</button>';
                }

                // A link action navigates instead of running JS, so it must stay a
                // real anchor (middle-click, open-in-new-tab, copy address all work).
                if (typeof action.href === "function") {
                    return '<a href="' + crbTableEscape(action.href(row, ctx)) + '"' +
                        ' id="' + crbTableEscape(id) + '" class="crb_table_action' + danger + '"' +
                        ' title="' + label + '" aria-label="' + label + '">' + iconHtml + '</a>';
                }

                return '<button type="button" id="' + crbTableEscape(id) + '"' +
                    ' class="crb_table_action' + danger + '"' +
                    ' title="' + label + '" aria-label="' + label + '"' +
                    ' onclick="crbTableRunAction(\'' + crbTableEscape(self.cfg.id) + '\',' +
                    (action.key ? ("'" + crbTableEscape(action.key) + "'") : i) + ',' + rowIndex + ')">' +
                    iconHtml + '</button>';
            }).join("");
        },

        runAction: function (keyOrIndex, rowIndex) {
            var row = this.rows[rowIndex];
            if (!row) {
                return;
            }
            var action = (typeof keyOrIndex === "number")
                ? this.actions[keyOrIndex]
                : this.actions.filter(function (a) { return a.key === keyOrIndex; })[0];
            if (!action || typeof action.onClick !== "function") {
                return;
            }
            // Re-check the gate at click time, not just at render time: a stale row
            // (an action fired from markup rendered before a refresh changed the
            // permission flag) must not slip through.
            if (!this.isActionVisible(action, row)) {
                console.warn("[crbTable:" + this.cfg.id + "] action \"" + (action.key || "") +
                    "\" blocked: gate no longer satisfied.");
                return;
            }
            action.onClick(row, {hasPermissions: this.hasPermissions, table: this});
        },

        /** Called by pages after a mutation (create/delete) to reload in place. */
        reload: function () {
            this.fetch();
        }
    };
}

/**
 * Bridge for the inline onclick in generated action markup. Kept as a global
 * (rather than an Alpine @click) because the action cell is injected via x-html,
 * whose contents Alpine does not compile as directives.
 */
function crbTableRunAction(tableId, keyOrIndex, rowIndex) {
    var root = document.getElementById(tableId);
    if (!root || !root._x_dataStack || !root._x_dataStack[0]) {
        console.error("[crbTable] no Alpine component found for table " + tableId);
        return;
    }
    root._x_dataStack[0].runAction(keyOrIndex, rowIndex);
}

/**
 * Page-level helper: gets the live component for a table id, so page code can
 * call e.g. crbTableInstance('robotsTable').reload() after a delete.
 */
/**
 * Normalises a cell value for the client-mode filters, so the popover's list and
 * the comparison agree on one string form (a boolean row value and its "true"
 * label must not be two different options).
 */
function crbTableFilterValue(value) {
    if (value === undefined || value === null) {
        return "";
    }
    return String(value);
}

/**
 * Replaces the COLUMN SET of a table at runtime.
 *
 * For a pivot grid whose columns are data: the campaign report has one column per
 * environment / country / robot combination, and which combinations exist is only
 * known once the report has loaded - and changes with the tag being looked at.
 * The definitions go through the same normalisation and the same validation as
 * those passed to createCerberusTable().
 *
 * Column visibility preferences are re-applied afterwards, so a user who hid a
 * combination keeps it hidden across reloads.
 */
function crbTableSetColumns(tableId, columns) {
    var cfg = window.crbTableRegistry[tableId];
    if (!cfg) {
        console.error("[crbTable] crbTableSetColumns() on an unknown table: " + tableId);
        return false;
    }
    var normalised = crbTableNormalizeColumns(columns, cfg.id,
        Boolean(cfg.distinctEndpoint) || Boolean(cfg.clientRows));
    cfg.columns = normalised;

    // createCerberusTable() only injects markup: Alpine builds the component on its
    // own schedule, so the instance does not exist yet on the line after it. Writing
    // to the registry entry is enough - init() reads cfg - and this stays correct
    // whether the caller is early or late.
    var table = crbTableInstance(tableId);
    if (!table) {
        return true;
    }
    table.columns = normalised;
    table.restoreColumnPrefs();
    // A sort on a column that no longer exists would silently order by nothing.
    var stillThere = normalised.some(function (c) { return c.field === table.sortField; });
    if (!stillThere) {
        table.sortField = cfg.defaultSort.field || null;
        table.sortDir = cfg.defaultSort.dir || "asc";
    }
    table.fetch();
    return true;
}

/**
 * Replaces the dataset of a CLIENT-mode table.
 *
 * The reporting pages fetch a period, compute their rows, then hand them over -
 * this is the equivalent of reload() for a table that has no endpoint.
 */
function crbTableSetRows(tableId, rows) {
    var cfg = window.crbTableRegistry[tableId];
    var table = crbTableInstance(tableId);
    if (!table) {
        // Same race as crbTableSetColumns: the component may not be built yet.
        // Seeding the registry entry means init() starts with these rows.
        if (cfg && Array.isArray(cfg.clientRows)) {
            cfg.clientRows = Array.isArray(rows) ? rows : [];
            return true;
        }
        console.error("[crbTable] crbTableSetRows() on an unknown table: " + tableId);
        return false;
    }
    if (!table.isClient) {
        console.error("[crbTable:" + tableId + "] crbTableSetRows() on a server-mode table - " +
            "use reload() instead.");
        return false;
    }
    table.setRows(rows);
    return true;
}

/* =============================================================================
 * Server-side preference storage.
 *
 * Cerberus already keeps a per-user `userPreferences` field holding a JSON dump
 * of the whole localStorage (global.js#updateUserPreferences, fired from
 * bindToggleCollapse on any panel expand). That is the mechanism the legacy
 * tables used to carry their state to another browser, and this reuses the same
 * field and the same format so the two coexist: the DataTables_* keys the
 * not-yet-migrated pages write and the crbTable:* keys written here live side by
 * side in one blob, and neither push drops the other's entries.
 *
 * The one thing not reused is updateUserPreferences() itself: it is a
 * SYNCHRONOUS XHR (async:false), which freezes the UI thread. Firing that on
 * every sort click is not acceptable, hence the debounced async push below.
 * ========================================================================== */

var CRB_TABLE_PREFS_PUSH_DELAY_MS = 1500;
var crbTablePrefsPushTimer = null;

/**
 * Reads one key out of the user's server-side preference blob.
 *
 * Double-parse is not an accident: the blob is JSON.stringify(localStorage), so
 * every value inside it is itself a JSON string.
 *
 * @param {String} key localStorage key to look up
 * @returns {Object|null} the parsed snapshot, or null if absent/unreadable
 */
function crbTableReadServerPrefs(key) {
    try {
        if (typeof getUser !== "function") {
            return null;
        }
        var user = getUser();
        if (!user || !user.userPreferences) {
            return null;
        }
        var blob = JSON.parse(user.userPreferences);
        var raw = blob && blob[key];
        return raw ? JSON.parse(raw) : null;
    } catch (e) {
        // A malformed or half-written blob must never stop a table from loading.
        return null;
    }
}

/**
 * Pushes the whole localStorage to the user record, debounced and shared by
 * every table on the page so three tables changing at once send one request.
 *
 * Silent by design: this is a preference write the user did not ask for, and a
 * toast on every sort click would be noise. A failure costs nothing beyond the
 * state not following to another browser - localStorage already has it here.
 */
function crbTableSchedulePrefsPush() {
    clearTimeout(crbTablePrefsPushTimer);
    crbTablePrefsPushTimer = setTimeout(function () {
        try {
            $.ajax({
                url: "UpdateMyUser",
                type: "POST",
                data: {column: "userPreferences", value: JSON.stringify(localStorage)},
                async: true
            });
        } catch (e) {
            /* offline / storage unavailable - localStorage is still authoritative here */
        }
    }, CRB_TABLE_PREFS_PUSH_DELAY_MS);
}

/**
 * Fixed-position style for a popover hanging off `anchorEl`.
 *
 * Both popovers in this component are positioned this way rather than with
 * `position:absolute` inside the toolbar, because absolute positioning is
 * clipped by ANY ancestor that sets overflow - and hosting a table in a card
 * with `overflow:hidden` is normal and often required (an embedded table needs
 * the card to clip its corners to the card radius). Homepage's #homeTableDiv did
 * exactly that and cut 72px off the bottom of the column list; ReportingAutomateScore
 * happened not to, which is why the same panel behaved differently on the two
 * pages. Fixed coordinates take the popover out of that flow entirely, so no host
 * can clip it and no page has to know about it.
 *
 * @param {Element} anchorEl        the button the popover belongs to
 * @param {Element} popEl           the popover, for its measured size (may be null
 *                                  on the first pass, before it is rendered)
 * @param {String}  align           "left" (default) or "right", i.e. which edge
 *                                  of the popover lines up with the anchor
 * @returns {String} an inline style, or "" when the anchor has scrolled away
 */
/* =============================================================================
 * Search highlighting.
 *
 * Showing WHERE a row matched, not just that it did. Ported up from
 * ImpactAnalysisV2, which had it for its three value columns, and from the label
 * tree, which had its own copy - one implementation now serves every table.
 *
 * Two entry points because cells arrive in two states:
 *  - crbTableHighlight()     raw text the component is about to escape itself.
 *  - crbTableHighlightHtml() markup a column's render() already produced.
 * The second is the delicate one: a naive replace over rendered HTML would wrap
 * matches inside attributes too, so searching "000" would rewrite
 * href="...testcase=0001A" into href="...testcase=<mark>000</mark>1A" and break
 * the link. It therefore only ever touches text BETWEEN tags.
 * ========================================================================== */

/*
 * Below this many characters, nothing is marked.
 *
 * A single letter matches in almost every cell, so highlighting it turns the
 * whole table into confetti and points at nothing. Two is the floor rather than
 * three because Cerberus is full of meaningful two-character tokens - the
 * country codes (FR, ES) and every execution status (OK, KO, NA, NE, PE, QE, QU,
 * CA, PA, WE) - and those are exactly the searches where seeing WHERE the match
 * landed is worth having.
 *
 * This gates the MARKING only. The search itself still filters from the first
 * character; a one-letter search returns its rows, they are simply not painted.
 */
var CRB_TABLE_HIGHLIGHT_MIN_LENGTH = 2;

/**
 * Wraps every occurrence of `term` in raw `value`, escaping as it goes.
 *
 * Each fragment is escaped separately rather than escaping first and searching
 * the escaped string: that way a search for "amp" cannot land inside an &amp;
 * this function just produced.
 *
 * @param {*} value raw, unescaped cell value
 * @param {String} term what the user typed
 * @returns {String} HTML
 */
/*
 * Two adjacent matches ("ok" twice in "okok") come out as </mark><mark>, and the
 * mark's own horizontal padding then draws a seam through what the user reads as
 * one run. Splicing that boundary out merges them into a single mark.
 */
var CRB_TABLE_MARK_OPEN = '<mark class="crb_table_mark">';
var CRB_TABLE_MARK_SEAM = new RegExp("</mark>" + CRB_TABLE_MARK_OPEN.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "g");

function crbTableMergeMarks(html) {
    return html.indexOf("</mark>") === -1 ? html : html.replace(CRB_TABLE_MARK_SEAM, "");
}

function crbTableHighlight(value, term) {
    var text = (value === undefined || value === null) ? "" : String(value);
    if (!term || !text) {
        return crbTableEscape(text);
    }
    var haystack = text.toLowerCase();
    var needle = String(term).toLowerCase();
    var out = "";
    var from = 0;
    var at = haystack.indexOf(needle, from);

    while (at !== -1) {
        out += crbTableEscape(text.substring(from, at)) +
            '<mark class="crb_table_mark">' +
            crbTableEscape(text.substr(at, needle.length)) + "</mark>";
        from = at + needle.length;
        at = haystack.indexOf(needle, from);
    }
    return crbTableMergeMarks(out + crbTableEscape(text.substring(from)));
}

/**
 * Same, over markup a render() produced.
 *
 * Walks the string tag by tag and only rewrites the text between them, so
 * attributes, urls and class names are never touched. Two things are skipped
 * inside those text runs:
 *  - HTML entities, so searching "amp" cannot split an &amp; into
 *    &<mark>amp</mark>; which then renders as the literal text "&amp;";
 *  - anything already inside a <mark>, so a page that highlights something of
 *    its own does not end up with marks nested in marks.
 *
 * @param {String} html output of a column's render()
 * @param {String} term what the user typed
 * @returns {String} HTML
 */
function crbTableHighlightHtml(html, term) {
    var source = (html === undefined || html === null) ? "" : String(html);
    if (!term || source.indexOf("<") === -1) {
        // No markup at all: the cheap path, and the common one for a render()
        // that just formats a date.
        return term ? crbTableHighlightTextRun(source, term) : source;
    }

    var out = "";
    var i = 0;
    var markDepth = 0;

    while (i < source.length) {
        var lt = source.indexOf("<", i);
        if (lt === -1) {
            out += markDepth ? source.substring(i) : crbTableHighlightTextRun(source.substring(i), term);
            break;
        }
        var run = source.substring(i, lt);
        out += markDepth ? run : crbTableHighlightTextRun(run, term);

        var gt = source.indexOf(">", lt);
        if (gt === -1) {
            // Unterminated tag - hand the rest back untouched rather than guess.
            out += source.substring(lt);
            break;
        }
        var tag = source.substring(lt, gt + 1);
        if (/^<mark[\s>]/i.test(tag)) {
            markDepth++;
        } else if (/^<\/mark\s*>/i.test(tag) && markDepth > 0) {
            markDepth--;
        }
        out += tag;
        i = gt + 1;
    }
    return out;
}

/**
 * Highlights one run of already-escaped text, leaving HTML entities intact.
 */
function crbTableHighlightTextRun(run, term) {
    if (!run) {
        return "";
    }
    // Split on entities and highlight only the parts between them.
    return run.split(/(&[a-zA-Z][a-zA-Z0-9]*;|&#[0-9]+;)/).map(function (part, idx) {
        if (idx % 2 === 1) {
            return part; // an entity, kept whole
        }
        return crbTableHighlightEscapedRun(part, term);
    }).join("");
}

/**
 * Wraps matches in a run that is ALREADY escaped, so nothing is re-escaped.
 */
function crbTableHighlightEscapedRun(run, term) {
    var haystack = run.toLowerCase();
    var needle = String(term).toLowerCase();
    var out = "";
    var from = 0;
    var at = haystack.indexOf(needle, from);

    while (at !== -1) {
        out += run.substring(from, at) + '<mark class="crb_table_mark">' +
            run.substr(at, needle.length) + "</mark>";
        from = at + needle.length;
        at = haystack.indexOf(needle, from);
    }
    return crbTableMergeMarks(out + run.substring(from));
}

function crbTablePopoverStyle(anchorEl, popEl, align) {
    if (!anchorEl) {
        return "";
    }
    var rect = anchorEl.getBoundingClientRect();
    var vw = window.innerWidth;
    var vh = window.innerHeight;

    // A viewport of zero means the page is not being laid out (a background or
    // hidden tab reports 0 for both). Every clamp below would then resolve to the
    // top-left corner, and the "anchor scrolled away" test would fire and close a
    // popover the user had just opened. Place it plainly and let the next scroll
    // or resize event correct it.
    if (!vw || !vh) {
        return "position:fixed;top:" + Math.round(rect.bottom + 6) +
            "px;left:" + Math.round(rect.left) + "px;";
    }

    if (rect.bottom < 0 || rect.top > vh) {
        return "";
    }

    var width = (popEl && popEl.offsetWidth) || 260;
    var height = (popEl && popEl.offsetHeight) || 0;
    var gap = 6;
    var margin = 8;

    var left = (align === "right") ? (rect.right - width) : rect.left;
    left = Math.max(margin, Math.min(left, vw - width - margin));

    // Below the anchor, unless that would run off the bottom and there is more
    // room above - the same content cut off by the viewport instead of by a card
    // is no better.
    var top = rect.bottom + gap;
    if (height && top + height > vh - margin) {
        var above = rect.top - gap - height;
        top = (above >= margin) ? above : Math.max(margin, vh - height - margin);
    }

    return "position:fixed;top:" + Math.round(top) + "px;left:" + Math.round(left) + "px;";
}

function crbTableInstance(tableId) {
    var root = document.getElementById(tableId);
    return (root && root._x_dataStack && root._x_dataStack[0]) ? root._x_dataStack[0] : null;
}

/**
 * Tells every crbTable on the page that server data changed, so it reloads.
 *
 * Call this from anywhere that creates, updates, duplicates or deletes a record -
 * in particular from the shared modals, which cannot know whether the page behind
 * them is a legacy DataTable or a V2 table. Safe to call on V1 pages: nothing
 * listens there, and their own fnDraw still runs.
 *
 * @param {String} [source] optional tag ("testdatalib", "robot", ...) letting a
 *                          table opt into only some events via cfg.refreshOn
 */
function crbNotifyDataChanged(source) {
    window.dispatchEvent(new CustomEvent("crb-data-changed", {detail: {source: source || null}}));
}

/**
 * Safe way for a cell's `render` to build a clickable control.
 *
 * Returns a button whose handler carries ONLY the table id, a page-registered
 * callback name and the row INDEX - never row data. The callback is then invoked
 * with the live row object, so nothing from the server is ever interpolated into
 * an inline handler, which is the one place HTML escaping does not protect you
 * (entities are decoded before the JS is evaluated, so an escaped quote still
 * terminates the string it sits in).
 *
 * Usage in a column definition:
 *   render: function (row, i) {
 *       return crbTableCellButton('myTable', 'onFolderClick', i, row.test, 'crb_tc_pill');
 *   }
 * with a global  function onFolderClick(row, table) { ... }
 *
 * @param {String} tableId
 * @param {String} callbackName  name of a global function taking (row, table)
 * @param {Number} rowIndex
 * @param {String} label         displayed text (escaped here)
 * @param {String} [className]
 * @param {String} [title]
 * @returns {String} HTML
 */
function crbTableCellButton(tableId, callbackName, rowIndex, label, className, title) {
    return '<button type="button" class="' + crbTableEscape(className || "") + '"' +
        (title ? ' title="' + crbTableEscape(title) + '"' : "") +
        ' onclick="crbTableCellCallback(\'' + crbTableEscape(tableId) + '\',\'' +
        crbTableEscape(callbackName) + '\',' + Number(rowIndex) + ')">' +
        crbTableEscape(label) + '</button>';
}

/** Dispatcher for crbTableCellButton. */
function crbTableCellCallback(tableId, callbackName, rowIndex, extra) {
    var table = crbTableInstance(tableId);
    if (!table) {
        return;
    }
    var row = table.rows[rowIndex];
    var fn = window[callbackName];
    if (row && typeof fn === "function") {
        // `extra` addresses something INSIDE the row - which column of a pivot grid
        // was clicked, for instance. Still never a row value: only an index or a key
        // the page itself minted, so nothing from the data reaches the markup.
        fn(row, table, extra);
    }
}
