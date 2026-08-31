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
 * crbLabelTree - V2 label hierarchy (Alpine.js), replacing bootstrap-treeview
 * for the three browse tabs of Label & Tag and for every label PICKER
 * (Edit Test Case Header > Label, test-list mass action).
 *
 * ONE COMPONENT, TWO MODES, ON PURPOSE
 * ------------------------------------
 * The page and the modal used to be two unrelated widgets that happened to hit
 * the same servlet: the page got server-rendered Bootstrap 3 buttons, the modal
 * got checkbox icons faked through bootstrap-treeview's nodeIcon/selectedIcon
 * options. Same data, two looks, two sets of bugs. Here a row is built by ONE
 * function; `mode` only decides whether the row carries a checkbox (select) or
 * hover actions (browse). Change the row and both follow.
 *
 * WHAT IT NO LONGER DEPENDS ON
 * ----------------------------
 * Nothing here reads the node's `text` property. ReadLabel.java still builds it
 * (LabelV1.jsp / TestCaseListV1.jsp drive bootstrap-treeview and have no other
 * source for a row) but it is a dead field for this component, which renders
 * from the structured `label` / `stats` / counter fields instead. That closes a
 * real injection: `text` concatenated the label NAME straight into inline
 * onclick handlers -
 *     onclick="...deleteEntryClick('5', 'lol');"
 * so a label named  l'ol  ended the JS string early, and one named
 *  ' + fetch(...) + '  ran. HTML-escaping cannot help there: the browser decodes
 * the entity and THEN evaluates the attribute as JavaScript. Rows here are built
 * from data with Alpine bindings; no label text ever reaches a code position.
 *
 * FOUR DEFECTS OF THE OLD WIDGET THIS FIXES BY CONSTRUCTION
 * --------------------------------------------------------
 * 1. Three unexplained pills. Every node showed up to three bare grey badges
 *    (3 / 6 / 1) with no icon, no tooltip and no unit. They are, in order:
 *    test cases carrying the label, the same including sub-labels, and the
 *    number of sub-labels. Here that is two labelled metrics with tooltips, and
 *    the aggregate is shown as "+n" so it reads as an addition, not a second
 *    unrelated number.
 * 2. No hierarchy affordance. Depth was a bare left margin, so a 3-level tree
 *    was unreadable. Rows now draw real guide rails with elbows.
 * 3. No way to find anything. A system with 300 labels offered scrolling. There
 *    is now a search box that keeps matching nodes plus their ancestors and
 *    highlights the hit.
 * 4. Empty meant blank. A type with no labels rendered an empty <div> - the
 *    Requirement tab looked broken rather than empty. There is a real empty
 *    state, with the create button when the user may create.
 *
 * SERVER CONTRACT
 * ---------------
 * Consumes ReadLabel?...&withHierarchy=true, node shape (dto/TreeNode#toJson):
 *   {id, nodes[], counter1, counter1WithChild, nbNodesWithChild, selectable,
 *    state:{selected}, label:{id, system, label, description, type, color,
 *    fontColor, requirementType, requirementStatus, requirementCriticity}}
 * The `label` sub-object gained id/system/requirement* for this component
 * (Label.java#toJsonGUI); everything else was already there.
 * ========================================================================== */

/**
 * Registry so injected markup can reference its config by tree id without
 * serialising functions (onEdit / onDelete / onCreate) through an attribute.
 * Same mechanism as window.crbTableRegistry.
 */
window.crbLabelTreeRegistry = window.crbLabelTreeRegistry || {};

/* Inline SVG rather than <i data-lucide>: rows are rebuilt by x-for on every
   expand, collapse and keystroke, and lucide REPLACES each <i> with a generated
   <svg>, dropping the Alpine class bindings on it (the frozen-spinner bug
   documented in crbTable.js). Inline paths need no re-sync pass and keep
   :class reactive - which the chevron rotation depends on. */
var CRB_LTREE_ICONS = {
    chevron: '<path d="m9 18 6-6-6-6"/>',
    chevronsDown: '<path d="m7 6 5 5 5-5"/><path d="m7 13 5 5 5-5"/>',
    chevronsUp: '<path d="m17 11-5-5-5 5"/><path d="m17 18-5-5-5 5"/>',
    check: '<path d="M20 6 9 17l-5-5"/>',
    pencil: '<path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/>',
    trash: '<path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>' +
           '<path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>',
    list: '<path d="M8 6h13"/><path d="M8 12h13"/><path d="M8 18h13"/>' +
          '<path d="M3 6h.01"/><path d="M3 12h.01"/><path d="M3 18h.01"/>',
    checks: '<path d="m3 17 2 2 4-4"/><path d="m3 7 2 2 4-4"/><path d="M13 6h8"/>' +
            '<path d="M13 12h8"/><path d="M13 18h8"/>',
    branch: '<path d="M15 10l5 5-5 5"/><path d="M4 4v7a4 4 0 0 0 4 4h12"/>',
    eye: '<path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0"/><circle cx="12" cy="12" r="3"/>',
    tag: '<path d="M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z"/><circle cx="7.5" cy="7.5" r=".5" fill="currentColor"/>'
};

/**
 * @param {String} name  key of CRB_LTREE_ICONS
 * @param {String} cls   classes for the <svg>
 * @returns {String} an inline SVG string
 */
function crbLabelTreeIcon(name, cls) {
    return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" ' +
        'stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" ' +
        'class="' + (cls || "w-4 h-4") + '" aria-hidden="true">' + (CRB_LTREE_ICONS[name] || "") + '</svg>';
}

/*
 * The create button's classes, copied VERBATIM from the 14 V2 page toolbars
 * (LabelV2.js:106, TestCaseListV2.js:97, UserManagerV2.js:126, ...). Not a local
 * .crb_ltree_create rule: the ink here is not what the class list says.
 * `text-white` sits in Tailwind's `utilities` LAYER, and Bootstrap ships an
 * UNLAYERED `button,input,optgroup,select,textarea{color:inherit}` - an unlayered
 * rule beats any layered one whatever the specificity - so every one of those
 * buttons actually renders the inherited near-black on sky-400, in both themes.
 * A local unlayered rule of our own would win instead and come out white, which
 * is both off-pattern and the worse read (white on sky-400 is ~2:1, the
 * inherited ink ~8:1). Staying on the same string is what keeps it identical.
 */
var CRB_LTREE_CREATE_CLASS = "bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10";

function crbLabelTreeEscape(value) {
    return String(value === undefined || value === null ? "" : value)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

/**
 * Builds a label tree into `config.mount` and returns the config it registered.
 *
 * @param {Object}   config
 * @param {String}   config.id            unique id (also the DOM id of the root)
 * @param {String}   config.mount         selector of the container to render into
 * @param {String}   [config.mode]        "browse" (default) or "select"
 * @param {Array}    [config.nodes]       initial hierarchy; use crbLabelTreeSetNodes() after
 * @param {String}   [config.type]        label type this tree shows, for the copy
 *                                        ("STICKER" / "REQUIREMENT" / "BATTERY")
 * @param {String}   [config.typeLabel]   singular human name, e.g. "sticker"
 * @param {Boolean}  [config.hasPermissions] gates create/edit/delete (browse mode)
 * @param {Function} [config.onCreate]    () => void; without it no create button
 * @param {Function} [config.onRefresh]   () => void; without it no refresh button
 * @param {Function} [config.onEdit]      (label) => void, label = node.label
 * @param {Function} [config.onDelete]    (label) => void
 * @param {Function} [config.listHref]    (label) => url for the "test cases" link
 * @param {Boolean}  [config.embedded]    no card chrome, for a panel the host already draws
 * @param {Boolean}  [config.showMetrics] default true in browse, true in select too
 * @param {String}   [config.searchPlaceholder]
 * @returns {Object|null} the registered config
 */
function createCerberusLabelTree(config) {
    var cfg = config || {};

    if (!cfg.id) {
        console.error("[crbLabelTree] config.id is required");
        return null;
    }
    if (!cfg.mount) {
        console.error("[crbLabelTree] config.mount is required for " + cfg.id);
        return null;
    }

    cfg.mode = (cfg.mode === "select") ? "select" : "browse";
    cfg.nodes = Array.isArray(cfg.nodes) ? cfg.nodes : [];
    cfg.hasPermissions = !!cfg.hasPermissions;
    cfg.embedded = !!cfg.embedded;
    cfg.showMetrics = (cfg.showMetrics !== false);
    cfg.typeLabel = cfg.typeLabel || "label";
    cfg.searchPlaceholder = cfg.searchPlaceholder || ("Search " + cfg.typeLabel + "s...");

    window.crbLabelTreeRegistry[cfg.id] = cfg;

    var $mount = $(cfg.mount);
    if (!$mount.length) {
        console.error("[crbLabelTree] mount container not found: " + cfg.mount);
        return null;
    }
    $mount.html(crbLabelTreeMarkup(cfg));
    return cfg;
}

/**
 * The Alpine component instance, or null before Alpine builds it.
 *
 * Accepts either the tree id or the MOUNT selector it was built into ("#selectLabelS").
 * The pickers are addressed by their mount everywhere - the three save handlers,
 * on both the V1 and V2 test list, know the container ids and nothing else - so
 * resolving from the mount is what lets those call sites stay one line.
 *
 * @param {String} ref tree id, or a "#mount" selector
 */
function crbLabelTreeInstance(ref) {
    var root = null;
    if (typeof ref === "string" && ref.charAt(0) === "#") {
        var host = document.querySelector(ref);
        if (host) {
            root = host.classList.contains("crb_ltree") ? host : host.querySelector(".crb_ltree");
        }
    } else {
        root = document.getElementById(ref);
    }
    return (root && root._x_dataStack && root._x_dataStack[0]) ? root._x_dataStack[0] : null;
}

/**
 * Replaces the hierarchy, keeping the user's expand/collapse state.
 *
 * Works BEFORE Alpine has built the component: createCerberusLabelTree() only
 * injects markup, and Alpine walks the DOM on its own schedule, so the very next
 * statement usually finds no instance. Seeding the registry entry means init()
 * starts from these nodes. Same contract as crbTableSetRows().
 *
 * @param {String} treeId
 * @param {Array}  nodes
 * @param {Object} [opts] {hasPermissions: Boolean} - the flag travels with the
 *                 same response as the nodes, so it is set here, not at build time.
 */
function crbLabelTreeSetNodes(treeId, nodes, opts) {
    var cfg = window.crbLabelTreeRegistry[treeId];
    var list = Array.isArray(nodes) ? nodes : [];
    var options = opts || {};

    if (!cfg) {
        console.error("[crbLabelTree] crbLabelTreeSetNodes() on an unknown tree: " + treeId);
        return false;
    }
    if (options.hasPermissions !== undefined) {
        cfg.hasPermissions = !!options.hasPermissions;
    }

    var tree = crbLabelTreeInstance(treeId);
    if (!tree) {
        cfg.nodes = list;
        return true;
    }
    if (options.hasPermissions !== undefined) {
        tree.hasPermissions = cfg.hasPermissions;
    }
    tree.setNodes(list);
    return true;
}

/**
 * Ids ticked in a select-mode tree, as Numbers.
 *
 * Replaces $(sel).treeview('getSelected').map(n => n.id) at every call site.
 * Returns [] for an unknown or not-yet-built tree rather than throwing, because
 * the save handlers call it for three trees whether or not the tab was opened.
 */
function crbLabelTreeSelectedIds(treeId) {
    var tree = crbLabelTreeInstance(treeId);
    if (!tree) {
        return [];
    }
    return tree.selectedIds();
}

/**
 * True when the label matches the query, on name or description.
 * Type/system are deliberately not searched: a tree is already filtered to one
 * type, and matching on system would light up every row of a mono-system setup.
 */
function crbLabelTreeMatches(node, query) {
    var label = node.label || {};
    var hay = String(label.label || "") + " " + String(label.description || "");
    return hay.toLowerCase().indexOf(query) !== -1;
}

/**
 * Prunes the hierarchy to nodes matching `query`, keeping their ancestors.
 *
 * A matched node keeps only its MATCHING descendants, not its whole subtree:
 * revealing a full subtree because its root matched buries the other results.
 * Clearing the search brings everything back, so nothing is lost.
 */
function crbLabelTreeFilter(nodes, query) {
    var out = [];
    (nodes || []).forEach(function (n) {
        var keptChildren = crbLabelTreeFilter(n.nodes || [], query);
        if (crbLabelTreeMatches(n, query) || keptChildren.length) {
            var copy = {};
            Object.keys(n).forEach(function (k) { copy[k] = n[k]; });
            copy.nodes = keptChildren;
            out.push(copy);
        }
    });
    return out;
}

/**
 * Counts every node of a hierarchy, at any depth.
 */
function crbLabelTreeCount(nodes) {
    var total = 0;
    (nodes || []).forEach(function (n) {
        total += 1 + crbLabelTreeCount(n.nodes || []);
    });
    return total;
}

function crbLabelTreeMarkup(cfg) {
    var isSelect = (cfg.mode === "select");
    return `
<!-- No x-init="init()": Alpine 3 calls a component's init() itself; declaring
     both runs the whole initialisation twice. -->
<div id="${cfg.id}" class="crb_ltree${cfg.embedded ? ' crb_ltree--embedded' : ''}"
     x-data="crbLabelTree(window.crbLabelTreeRegistry['${cfg.id}'])">

  <div class="crb_table_bar">
    <div class="crb_table_bar_row">
      <!-- Create first, then search, then the icon buttons - the order every
           crbTable toolbar uses (crb_table_bar_actions is emitted before
           crb_table_bar_search in crbTableMarkup). -->
      ${isSelect ? "" : `
      <div class="crb_table_bar_actions">
        <button type="button" class="${CRB_LTREE_CREATE_CLASS}" @click="create()"
                x-show="canCreate" x-cloak>
          <i data-lucide="plus" class="w-4 h-4"></i>
          <span>New ${crbLabelTreeEscape(cfg.typeLabel)}</span>
        </button>
      </div>`}

      <div class="crb_table_bar_search">
        ${crbLabelTreeIcon("tag", "crb_table_search_icon w-4 h-4")}
        <input type="search" x-model="query" class="crb_table_search_input"
               placeholder="${crbLabelTreeEscape(cfg.searchPlaceholder)}"
               aria-label="Search ${crbLabelTreeEscape(cfg.typeLabel)} labels">
      </div>

      <!-- Double chevrons, not the row twisty's single one: two single chevrons
           side by side read as "next / previous", which is not what they do. -->
      <button type="button" class="crb_table_iconbtn" @click="expandAll()"
              title="Expand all" aria-label="Expand all">
        ${crbLabelTreeIcon("chevronsDown", "w-4 h-4")}
      </button>
      <button type="button" class="crb_table_iconbtn" @click="collapseAll()"
              title="Collapse all" aria-label="Collapse all">
        ${crbLabelTreeIcon("chevronsUp", "w-4 h-4")}
      </button>

      <button type="button" class="crb_table_iconbtn" @click="refresh()" x-show="hasRefreshHook"
              title="Refresh" aria-label="Refresh">
        <span :class="loading ? 'crb_table_spin' : ''" class="inline-flex">
          <i data-lucide="refresh-cw" class="w-4 h-4"></i>
        </span>
      </button>
    </div>

    <div class="crb_table_bar_row crb_table_bar_row--meta">
      <div class="crb_table_bar_left">
        <span class="crb_table_meta_label" x-text="countLabel"></span>
        <span class="crb_table_count" x-text="countText"></span>
        ${isSelect ? `
        <span class="crb_table_selcount" x-show="selectedCount" x-cloak>
          <span x-text="selectedCount"></span>&nbsp;selected
          <button type="button" @click="clearSelection()" title="Clear selection" aria-label="Clear selection">
            <i data-lucide="x" class="w-3 h-3"></i>
          </button>
        </span>` : ""}
      </div>
    </div>
  </div>

  <div class="crb_ltree_body" role="tree" :aria-busy="loading"
       aria-label="${crbLabelTreeEscape(cfg.typeLabel)} label hierarchy">

    <template x-if="rows.length === 0">
      <div class="crb_ltree_empty">
        <span class="crb_ltree_empty_icon">${crbLabelTreeIcon("tag", "w-5 h-5")}</span>
        <p class="crb_ltree_empty_title" x-text="emptyTitle"></p>
        <p class="crb_ltree_empty_hint" x-text="emptyHint"></p>
        ${isSelect ? "" : `
        <button type="button" class="${CRB_LTREE_CREATE_CLASS} mt-2" @click="create()"
                x-show="canCreate && !query" x-cloak>
          <i data-lucide="plus" class="w-4 h-4"></i>
          <span>New ${crbLabelTreeEscape(cfg.typeLabel)}</span>
        </button>`}
      </div>
    </template>

    <template x-for="row in rows" :key="row.key">
      <div class="crb_ltree_row group"
           :class="rowClass(row)"
           role="treeitem"
           :aria-level="row.depth + 1"
           :aria-expanded="row.hasChildren ? String(row.open) : null"
           :aria-selected="isSelect ? String(isSelected(row.node)) : null"
           :tabindex="isSelect ? 0 : -1"
           @click="isSelect && toggleSelect(row.node)"
           @keydown.enter.prevent="isSelect && toggleSelect(row.node)"
           @keydown.space.prevent="isSelect && toggleSelect(row.node)">

        <!-- Guide rails. One per ancestor level: a vertical line when that
             ancestor still has siblings below, blank when the branch ended.
             The last one is the elbow into this row. -->
        <template x-for="(g, gi) in row.guides" :key="'g' + gi">
          <span class="crb_ltree_rail" :class="g ? 'crb_ltree_rail--line' : ''"></span>
        </template>
        <template x-if="row.depth > 0">
          <span class="crb_ltree_rail crb_ltree_rail--elbow" :class="row.isLast ? 'is-last' : ''"></span>
        </template>

        <button type="button" class="crb_ltree_twisty" x-show="row.hasChildren"
                :class="row.open ? 'crb_ltree_twisty--open' : ''"
                @click.stop="toggleOpen(row.node)"
                :title="row.open ? 'Collapse' : 'Expand'"
                :aria-label="(row.open ? 'Collapse ' : 'Expand ') + labelName(row.node)"
                x-html="twistyIcon"></button>
        <span class="crb_ltree_leafdot" x-show="!row.hasChildren" aria-hidden="true"></span>

        <template x-if="isSelect">
          <span class="crb_ltree_check" :class="isSelected(row.node) ? 'crb_ltree_check--on' : ''"
                x-html="isSelected(row.node) ? checkIcon : ''" aria-hidden="true"></span>
        </template>

        <span class="crb_ltree_chip" :style="chipStyle(row.node)"
              :title="labelName(row.node)" x-html="labelHtml(row.node)"></span>

        <span class="crb_ltree_desc" x-show="hasDescription(row.node)"
              :title="description(row.node)" x-html="descriptionHtml(row.node)"></span>

        <span class="crb_ltree_spacer"></span>

        <!-- Keyed on title+text: two requirement attributes can hold the same
             value (a "MAJOR" type and a "MAJOR" criticity), and a duplicate
             x-for key drops one of them silently. -->
        <template x-for="pill in pills(row.node)" :key="pill.title + '/' + pill.text">
          <span class="crb_ltree_pill" :class="pill.cls" :title="pill.title" x-text="pill.text"></span>
        </template>

        <template x-if="showMetrics">
          <span class="crb_ltree_metrics">
            <span class="crb_ltree_metric" x-show="row.node.nbNodesWithChild > 0"
                  :title="subLabelTitle(row.node)">
              <span class="crb_ltree_metric_icon" x-html="branchIcon"></span>
              <span x-text="row.node.nbNodesWithChild"></span>
            </span>
            <span class="crb_ltree_metric" x-show="testCaseCount(row.node) > 0 || extraCount(row.node) > 0"
                  :title="testCaseTitle(row.node)">
              <span class="crb_ltree_metric_icon" x-html="checksIcon"></span>
              <span x-text="testCaseCount(row.node)"></span>
              <span class="crb_ltree_metric_more" x-show="extraCount(row.node) > 0"
                    x-text="'+' + extraCount(row.node)"></span>
            </span>
          </span>
        </template>

        <template x-if="!isSelect">
          <span class="crb_ltree_actions">
            <!-- Pencil / eye, gated exactly like the List tab's own edit action
                 (LabelV2.js): the modal opens read-only without the permission,
                 so the icon has to say so rather than promise an edit. -->
            <button type="button" class="crb_table_action" @click.stop="edit(row.node)"
                    :title="canWrite ? 'Edit label' : 'View label'"
                    :aria-label="(canWrite ? 'Edit ' : 'View ') + labelName(row.node)"
                    x-html="canWrite ? editIcon : eyeIcon"></button>
            <button type="button" class="crb_table_action crb_table_action--danger"
                    @click.stop="remove(row.node)" x-show="canWrite"
                    title="Delete label" :aria-label="'Delete ' + labelName(row.node)"
                    x-html="trashIcon"></button>
            <!-- One link, not V1's two buttons ("same tab" + "new tab"): an <a>
                 already gives both, through ctrl/cmd-click and middle-click,
                 and it is what the List tab's own Test Cases action renders. -->
            <a class="crb_table_action" :href="listHref(row.node)" @click.stop
               title="Open the test cases carrying this label"
               :aria-label="'Test cases labelled ' + labelName(row.node)"
               x-html="listIcon"></a>
          </span>
        </template>
      </div>
    </template>
  </div>
</div>`;
}

/**
 * The Alpine component. Registered as a plain global function, the same way
 * crbTable() is, so `x-data="crbLabelTree(...)"` resolves without an
 * alpine:init hook (which would race with markup injected after Alpine start).
 */
function crbLabelTree(cfg) {
    return {
        cfg: cfg,
        nodes: (cfg.nodes || []).slice(),
        query: "",
        loading: false,
        hasPermissions: !!cfg.hasPermissions,
        isSelect: (cfg.mode === "select"),
        showMetrics: cfg.showMetrics !== false,

        /* id -> true. A map, not a Set: Alpine 3's proxy does not track Set
           mutation, so a Set would collapse rows without re-rendering. Storing
           `false` rather than deleting keeps the write reactive on every path. */
        collapsedMap: {},
        selectedMap: {},

        /* Icons are constant strings; exposing them as component properties
           keeps them out of the x-for body, where they would be re-evaluated
           per row per render. */
        twistyIcon: crbLabelTreeIcon("chevron", "w-3.5 h-3.5"),
        checkIcon: crbLabelTreeIcon("check", "w-3 h-3"),
        editIcon: crbLabelTreeIcon("pencil", "w-4 h-4"),
        trashIcon: crbLabelTreeIcon("trash", "w-4 h-4"),
        listIcon: crbLabelTreeIcon("list", "w-4 h-4"),
        eyeIcon: crbLabelTreeIcon("eye", "w-4 h-4"),
        branchIcon: crbLabelTreeIcon("branch", "w-3 h-3"),
        checksIcon: crbLabelTreeIcon("checks", "w-3 h-3"),

        init: function () {
            this.readSelectionFromNodes(this.nodes);
            this.refreshIcons();
        },

        /* ---------------------------------------------------------------
         * Data
         * ------------------------------------------------------------ */

        /**
         * Replaces the hierarchy. Expand/collapse state survives because it is
         * keyed by label id and defaults to "open", so a node absent from the
         * map is expanded - including ids that appear for the first time.
         */
        setNodes: function (list) {
            this.nodes = Array.isArray(list) ? list : [];
            // In select mode the payload is authoritative about what is ticked:
            // ReadLabel marks state.selected from testSelect/testCaseSelect. Not
            // clearing first would carry the previous test case's labels into the
            // next one the modal is opened on - the picker is rebuilt on every
            // open, with the same tree id.
            if (this.isSelect) {
                this.selectedMap = {};
            }
            this.readSelectionFromNodes(this.nodes);
            this.refreshIcons();
        },

        /**
         * Seeds the ticked set from the payload's state.selected, which
         * ReadLabel fills from testSelect/testCaseSelect. Only in select mode:
         * in browse mode the server never marks a node selected and a stray
         * flag would light up rows for no reason.
         */
        readSelectionFromNodes: function (list) {
            if (!this.isSelect) {
                return;
            }
            var self = this;
            (list || []).forEach(function (n) {
                if (n.state && n.state.selected) {
                    self.selectedMap[n.id] = true;
                }
                self.readSelectionFromNodes(n.nodes || []);
            });
        },

        /* ---------------------------------------------------------------
         * Rows: filter, then flatten. Two passes rather than one recursive
         * render, because Alpine has no clean recursive template and a flat
         * x-for is both simpler to reason about and cheaper to re-render.
         * ------------------------------------------------------------ */
        get normQuery() {
            return String(this.query || "").trim().toLowerCase();
        },

        get shownNodes() {
            var q = this.normQuery;
            return q ? crbLabelTreeFilter(this.nodes, q) : this.nodes;
        },

        get rows() {
            var out = [];
            var self = this;
            var searching = !!this.normQuery;

            function walk(list, depth, guides) {
                (list || []).forEach(function (node, i) {
                    var isLast = (i === list.length - 1);
                    var children = node.nodes || [];
                    // A search result is always expanded: hiding the very node
                    // that matched behind a collapsed ancestor is the one thing
                    // a filter must never do.
                    var open = searching ? true : self.isOpen(node);
                    out.push({
                        key: String(node.id) + "@" + depth,
                        node: node,
                        depth: depth,
                        guides: guides,
                        isLast: isLast,
                        hasChildren: children.length > 0,
                        open: open
                    });
                    if (children.length && open) {
                        walk(children, depth + 1, guides.concat([!isLast]));
                    }
                });
            }

            walk(this.shownNodes, 0, []);
            return out;
        },

        get totalCount() {
            return crbLabelTreeCount(this.nodes);
        },

        get countLabel() {
            // "Showing", not "Matching": the filtered count includes the
            // ancestors kept to give a match its place in the hierarchy, and
            // those did not match anything.
            return this.normQuery ? "Showing" : "Labels";
        },

        get countText() {
            var total = this.totalCount;
            if (!this.normQuery) {
                return String(total);
            }
            return crbLabelTreeCount(this.shownNodes) + " / " + total;
        },

        get emptyTitle() {
            if (this.normQuery) {
                return "No label matches “" + this.query + "”";
            }
            return "No " + this.cfg.typeLabel + " label yet";
        },

        get emptyHint() {
            if (this.normQuery) {
                return "Clear the search to see the whole hierarchy.";
            }
            if (this.isSelect) {
                return "Labels of this type are created from Maintain › Label & Tag.";
            }
            return this.canWrite
                ? "Create one to start organising your test cases."
                : "You do not have the permission to create one.";
        },

        /** May edit and delete. Read straight from the response's hasPermissions. */
        get canWrite() {
            return !this.isSelect && this.hasPermissions;
        },

        /** May create - the same permission, plus a host that offers the hook. */
        get canCreate() {
            return this.canWrite && typeof this.cfg.onCreate === "function";
        },

        get hasRefreshHook() {
            return typeof this.cfg.onRefresh === "function";
        },

        get selectedCount() {
            return this.selectedIds().length;
        },

        /* ---------------------------------------------------------------
         * Per-row accessors. Every one takes the node, never the row index:
         * the flattened list is rebuilt on each render and an index would go
         * stale between the click and the handler.
         * ------------------------------------------------------------ */
        labelName: function (node) {
            return (node.label && node.label.label) || "";
        },

        description: function (node) {
            return (node.label && node.label.description) || "";
        },

        hasDescription: function (node) {
            return this.description(node) !== "";
        },

        labelHtml: function (node) {
            return this.highlight(this.labelName(node));
        },

        descriptionHtml: function (node) {
            return this.highlight(this.description(node));
        },

        /**
         * Marks the matched run. Delegates to crbTable.js's shared highlighter
         * (this file is loaded after it), so the trees and the tables mark text
         * the same way and there is one implementation to keep correct.
         *
         * That one escapes each fragment separately rather than escaping first
         * and indexing into the escaped string - which is what this used to do,
         * and which meant a search for "amp" could land inside an &amp; it had
         * just produced. It also marks EVERY occurrence, not just the first.
         */
        highlight: function (text) {
            // Same floor as the tables (CRB_TABLE_HIGHLIGHT_MIN_LENGTH in
            // crbTable.js): one letter matches nearly every label and paints the
            // whole tree. The tree still FILTERS from the first character - only
            // the marking waits.
            var q = this.normQuery;
            if (q.length < CRB_TABLE_HIGHLIGHT_MIN_LENGTH) {
                return crbLabelTreeEscape(text);
            }
            return crbTableHighlight(text, q);
        },

        /**
         * The label's own colour with readable ink, from the shared WCAG helper
         * in global.js - the same one Label.java#chipStyle() mirrors server-side,
         * so a chip looks identical wherever it is built.
         */
        chipStyle: function (node) {
            return crbChipStyle((node.label && node.label.color) || "");
        },

        /**
         * Requirement attributes and the GLOBAL marker, as data.
         * ReadLabel.java builds the same list as HTML strings in the node's
         * `tags`; those are ignored here - "unknown" and empty are filtered the
         * same way, but the values stay values.
         */
        pills: function (node) {
            var label = node.label || {};
            var out = [];

            function push(value, title) {
                if (value === undefined || value === null) {
                    return;
                }
                var text = String(value).trim();
                if (text === "" || text.toLowerCase() === "unknown") {
                    return;
                }
                out.push({text: text, title: title, cls: ""});
            }

            if (label.type === "REQUIREMENT") {
                push(label.requirementType, "Requirement type");
                push(label.requirementStatus, "Requirement status");
                push(label.requirementCriticity, "Requirement criticity");
            }
            if (label.system === "") {
                out.push({
                    text: "GLOBAL",
                    title: "Shared by every system",
                    cls: "crb_ltree_pill--global"
                });
            }
            return out;
        },

        testCaseCount: function (node) {
            return Number(node.counter1) || 0;
        },

        /**
         * Test cases contributed by the sub-labels, i.e. what the aggregate adds
         * on top of this label's own count. Rendered as "+n" so the two numbers
         * read as one sum instead of two unrelated badges.
         */
        extraCount: function (node) {
            return Math.max(0, (Number(node.counter1WithChild) || 0) - this.testCaseCount(node));
        },

        testCaseTitle: function (node) {
            var own = this.testCaseCount(node);
            var extra = this.extraCount(node);
            var text = own + (own === 1 ? " test case carries" : " test cases carry") + " this label";
            if (extra > 0) {
                text += ", " + extra + " more through its sub-labels (" +
                    (Number(node.counter1WithChild) || 0) + " in total)";
            }
            return text;
        },

        subLabelTitle: function (node) {
            var n = Number(node.nbNodesWithChild) || 0;
            return n + (n === 1 ? " sub-label" : " sub-labels");
        },

        listHref: function (node) {
            if (typeof this.cfg.listHref === "function") {
                return this.cfg.listHref(node.label || {});
            }
            return "./TestCaseList.jsp?label=" + encodeURIComponent(this.labelName(node));
        },

        rowClass: function (row) {
            var cls = [];
            if (this.isSelect) {
                cls.push("crb_ltree_row--pick");
                if (this.isSelected(row.node)) {
                    cls.push("crb_ltree_row--on");
                }
            }
            return cls.join(" ");
        },

        /* ---------------------------------------------------------------
         * Expand / collapse
         * ------------------------------------------------------------ */
        isOpen: function (node) {
            return this.collapsedMap[node.id] !== true;
        },

        toggleOpen: function (node) {
            this.collapsedMap[node.id] = this.isOpen(node);
            this.refreshIcons();
        },

        expandAll: function () {
            this.collapsedMap = {};
            this.refreshIcons();
        },

        collapseAll: function () {
            var map = {};
            (function walk(list) {
                (list || []).forEach(function (n) {
                    if ((n.nodes || []).length) {
                        map[n.id] = true;
                        walk(n.nodes);
                    }
                });
            })(this.nodes);
            this.collapsedMap = map;
            this.refreshIcons();
        },

        /* ---------------------------------------------------------------
         * Selection (select mode)
         * ------------------------------------------------------------ */
        isSelected: function (node) {
            return this.selectedMap[node.id] === true;
        },

        /**
         * No cascade to children, deliberately: V1's multiSelect had none, and
         * ticking a parent silently attaching every descendant to a test case
         * would be a behaviour change hidden inside a redesign.
         */
        toggleSelect: function (node) {
            this.selectedMap[node.id] = !this.isSelected(node);
            this.refreshIcons();
        },

        clearSelection: function () {
            this.selectedMap = {};
            this.refreshIcons();
        },

        selectedIds: function () {
            var self = this;
            return Object.keys(this.selectedMap)
                .filter(function (id) { return self.selectedMap[id] === true; })
                .map(Number);
        },

        /* ---------------------------------------------------------------
         * Host callbacks
         * ------------------------------------------------------------ */
        create: function () {
            if (typeof this.cfg.onCreate === "function") {
                this.cfg.onCreate(this.cfg.type);
            }
        },

        refresh: function () {
            if (typeof this.cfg.onRefresh !== "function") {
                return;
            }
            var self = this;
            this.loading = true;
            // The hook may be synchronous or return a promise; either way the
            // spinner must stop, so both paths are handled rather than assuming.
            var result = this.cfg.onRefresh();
            if (result && typeof result.always === "function") {
                result.always(function () { self.loading = false; });
            } else if (result && typeof result.finally === "function") {
                result.finally(function () { self.loading = false; });
            } else {
                this.loading = false;
            }
        },

        edit: function (node) {
            if (typeof this.cfg.onEdit === "function") {
                this.cfg.onEdit(node.label || {}, node);
            }
        },

        remove: function (node) {
            if (typeof this.cfg.onDelete === "function") {
                this.cfg.onDelete(node.label || {}, node);
            }
        },

        /**
         * Only the static toolbar icons are lucide <i> elements; row icons are
         * inline SVG and need no pass. Called after the toolbar first renders
         * and after anything that can re-show a hidden toolbar button.
         */
        refreshIcons: function () {
            this.$nextTick(function () {
                if (window.lucide) {
                    lucide.createIcons();
                }
            });
        }
    };
}
