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
 * User management - V2, on js/global/crbTable.js.
 *
 * Endpoint: ReadUser?systems=true&roles=true. The response carries the usual
 * table-level `hasPermissions`, plus `isKeycloakManaged` / `keycloakRealm` /
 * `keycloakUrl`, which the toolbar reads to decide whether to offer the
 * "manage users in Keycloak" shortcut - hence toolbar(ctx) reading ctx.response.
 *
 * Action gating:
 *   - Edit     : permission
 *   - Password : permission AND the user is NOT Keycloak-managed (row-level flag;
 *                a Keycloak account's password does not live here)
 *   - Delete   : permission
 *
 * ONE DELIBERATE DEVIATION FROM V1, flagged rather than silently kept:
 * V1 rendered all three buttons unconditionally - it never read the
 * `hasPermissions` flag its own endpoint sends. A read-only user therefore saw
 * Edit and Delete on every account on the instance. (The servlets do check, so
 * pressing them produced an error rather than a change; the buttons were still
 * wrong to offer.) V2 gates them. Tell me if some role is expected to manage
 * users without that permission and I will put it back.
 *
 * Fixed on the way over:
 *   - every handler was built as onclick="editEntryClick('${escapeHtml(login)}')".
 *     HTML-escaping does not protect a JS string inside an attribute - the entity
 *     is decoded and THEN evaluated - so a login containing a quote broke the row.
 *     V2 passes the row to onClick.
 *   - the Date Modif column declared `data: "uateModif"` (typo), so its value came
 *     from nowhere and only its renderer saved it.
 *   - the Request column declared `sName: "reqest"` (typo), a column name the
 *     server does not know: sorting and filtering on it did nothing, silently.
 *     This is the legacy dead-sort defect the component now refuses to reproduce.
 * ========================================================================== */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();
    });
});

var CRB_USER_TABLE_ID = "userTableV2";

function initPage() {
    var doc = new Doc();

    displayPageLabel();

    // Modal save buttons and the systems checkbox helpers. Same as V1.
    $("#editUserButton").click(editEntryModalSaveHandler);
    $("#editUserPasswordButton").click(editEntryPassModalSaveHandler);
    $("#addUserButton").click(addEntryModalSaveHandler);

    $('#addcheckall').click(function () {
        setAllCheckboxes('#systems', '#systemsCheckboxList', '#addUserModal', true);
    });
    $('#adduncheckall').click(function () {
        setAllCheckboxes('#systems', '#systemsCheckboxList', '#addUserModal', false);
    });
    $('#editcheckall').click(function () {
        setAllCheckboxes('#systems', '#systemsCheckboxList', '#editUserModal', true);
    });
    $('#edituncheckall').click(function () {
        setAllCheckboxes('#systems', '#systemsCheckboxList', '#editUserModal', false);
    });

    $('#setAPIKey').click(function () {
        $("#apiKey").val(generateUUID());
    });
    $('#copyAPIKey').click(function () {
        navigator.clipboard.writeText($("#apiKey").val());
        showMessage(new Message("OK", 'apikey copied !!!'), $('#editUserModal'), false, 1000);
    });

    // include/transversal/Application.html is pulled into EVERY page by
    // modalInclusions.jsp and declares its own editEntryModalCloseHandler. Being
    // parsed in the body, after the head scripts, its declaration wins the global
    // name - so closing the Edit User modal (editUser.html:155 watches `open` and
    // calls this by name) reset the *Application* form instead, and the user form
    // kept its error styling and previous values. Reclaiming it at document-ready
    // is after every include has been parsed. Safe here: the Application modal is
    // never opened from this page. Same collision as Label.jsp.
    //
    // It has to be a DIFFERENTLY NAMED function: both declarations are global
    // `function editEntryModalCloseHandler`, so by the time this line runs the bare
    // name already resolves to Application.html's version and
    // `window.editEntryModalCloseHandler = editEntryModalCloseHandler` would be a
    // silent self-assignment. Verified: it was, before this comment existed.
    window.editEntryModalCloseHandler = userV2EditModalCloseHandler;

    createCerberusTable({
        id: CRB_USER_TABLE_ID,
        mount: "#userList",
        endpoint: "ReadUser?systems=true&roles=true",
        distinctEndpoint: "ReadUser?systems=true&roles=true",
        rowKey: "login",
        defaultSort: {field: "login", dir: "asc"},
        pageLength: 10,
        searchPlaceholder: "Search users...",
        emptyMessage: "No user matches your search",

        toolbar: function (ctx) {
            if (!ctx.hasPermissions) {
                return "";
            }
            var html = "<button id='createUserButton' type='button' onclick='addEntryClick()' " +
                "class='bg-sky-400 hover:bg-sky-500 text-white flex items-center gap-1 px-3 py-1 rounded-lg h-10'>" +
                "<i data-lucide='plus' class='w-4 h-4'></i><span>" +
                doc.getDocLabel("page_user", "button_create") + "</span></button>";

            // Keycloak-managed instances get a shortcut to its admin console. The
            // three values come from the list response, not from a separate call.
            if (ctx.response && ctx.response.isKeycloakManaged) {
                kcRealm = ctx.response.keycloakRealm;
                kcUrl = ctx.response.keycloakUrl;
                html += "<button id='manageUserButton' type='button' onclick='manageUserClick()' " +
                    "class='flex items-center gap-1.5 px-3 py-1 rounded-md h-10 border border-gray-300 " +
                    "dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition'>" +
                    "<i data-lucide='external-link' class='w-4 h-4'></i><span>" +
                    doc.getDocLabel("page_user", "manage_user") + "</span></button>";
            }
            return html;
        },

        // `field` = server column name (sName in V1), `prop` = key in the row.
        columns: [
            {field: "login", prop: "login", title: doc.getDocLabel("page_user", "login_col"),
             width: "180px", className: "font-medium", filterable: true},
            {field: "name", prop: "name", title: doc.getDocLabel("page_user", "name_col"),
             width: "220px", like: true},
            {
                // Not sortable or filterable: roles are a joined collection, not a
                // column the server can order by. Declaring a `field` for it is what
                // produced the legacy silent-dead-sort.
                title: doc.getDocLabel("page_user", "groups_col"), sortable: false, width: "380px",
                render: function (row) { return userV2Chips(row.roles, "role"); }
            },
            {
                title: doc.getDocLabel("page_user", "systems_col"), sortable: false, width: "220px",
                render: function (row) { return userV2Chips(row.systems, "system"); }
            },
            {field: "email", prop: "email", title: doc.getDocLabel("page_user", "email_col"),
             width: "240px", like: true},
            {field: "team", prop: "team", title: doc.getDocLabel("page_user", "team_col"),
             width: "140px", filterable: true},

            // ---- available from Config, hidden by default ----
            {field: "request", prop: "request", title: doc.getDocLabel("page_user", "request_col"),
             width: "110px", visible: false},
            {field: "attribute01", prop: "attribute01", title: doc.getDocLabel("user", "attribute01"),
             width: "140px", visible: false},
            {field: "attribute02", prop: "attribute02", title: doc.getDocLabel("user", "attribute02"),
             width: "140px", visible: false},
            {field: "attribute03", prop: "attribute03", title: doc.getDocLabel("user", "attribute03"),
             width: "140px", visible: false},
            {field: "attribute04", prop: "attribute04", title: doc.getDocLabel("user", "attribute04"),
             width: "140px", visible: false},
            {field: "attribute05", prop: "attribute05", title: doc.getDocLabel("user", "attribute05"),
             width: "140px", visible: false},
            {field: "apiKey", prop: "apiKey", title: doc.getDocLabel("user", "apiKey"),
             width: "260px", visible: false, className: "font-mono"},
            {field: "UsrCreated", prop: "usrCreated", title: doc.getDocOnline("transversal", "UsrCreated"),
             width: "130px", visible: false},
            {
                field: "DateCreated", prop: "dateCreated", title: doc.getDocOnline("transversal", "DateCreated"),
                width: "170px", visible: false, like: true,
                render: function (row) { return crbTableEscape(userV2Date(row.dateCreated)); }
            },
            {field: "UsrModif", prop: "usrModif", title: doc.getDocOnline("transversal", "UsrModif"),
             width: "130px", visible: false},
            {
                field: "DateModif", prop: "dateModif", title: doc.getDocOnline("transversal", "DateModif"),
                width: "170px", visible: false,
                render: function (row) { return crbTableEscape(userV2Date(row.dateModif)); }
            }
        ],

        actions: [
            {
                key: "edit", icon: "pencil", gate: "permission",
                title: doc.getDocLabel("page_user", "button_edit"),
                onClick: function (row) { editEntryClick(row.login); }
            },
            {
                // A Keycloak-managed account keeps its password in Keycloak, so the
                // button would open a form that cannot do anything. Row-level flag.
                key: "password", icon: "key-round",
                gate: function (row, ctx) { return Boolean(ctx.hasPermissions) && !row.isKeycloakManaged; },
                title: doc.getDocLabel("page_user", "button_password_edit"),
                onClick: function (row) { editEntryPassClick(row.login); }
            },
            {
                key: "remove", icon: "trash-2", gate: "permission", danger: true,
                title: doc.getDocLabel("page_user", "button_remove"),
                onClick: function (row) { removeEntryClick(row.login); }
            }
        ]
    });
}

/**
 * Small grey chips for the roles / systems collections.
 *
 * Capped at four with a "+N" badge: an administrator carries a dozen roles, and
 * V1 printed every one of them, which wrapped to six lines and made that single
 * row four times taller than the others - the whole table became unscannable
 * because of one account. The full list is in the badge's tooltip, and in the
 * user's own modal.
 */
function userV2Chips(list, key) {
    var items = (list || []).map(function (item) { return item[key]; }).filter(Boolean);
    if (!items.length) {
        return "";
    }
    var MAX = 4;
    var chip = function (text, extra) {
        return '<span class="inline-flex items-center rounded px-1.5 py-0.5 text-[11px] font-medium ' +
            'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300"' + (extra || "") + '>' +
            crbTableEscape(text) + '</span>';
    };
    var html = items.slice(0, MAX).map(function (t) { return chip(t); }).join("");
    if (items.length > MAX) {
        var rest = items.slice(MAX);
        html += chip("+" + rest.length, ' title="' + crbTableEscape(rest.join(", ")) + '"');
    }
    return '<div class="flex flex-wrap gap-1">' + html + '</div>';
}

function userV2Date(value) {
    if (value === undefined || value === null || value === "" || value === 0) {
        return "";
    }
    return getDate(value);
}

/** Reloads the user list. Called by every modal save path. */
function userV2Reload() {
    var table = crbTableInstance(CRB_USER_TABLE_ID);
    if (table) {
        table.reload();
    }
}

function displayPageLabel() {
    var doc = new Doc();

    $("#title").html(doc.getDocLabel("page_user", "allUsers"));
    $("#pageTitle").html(doc.getDocLabel("page_user", "allUsers"));
    $("[name='editUserField']").html(doc.getDocLabel("page_user", "edituser_field"));
    $("[name='addUserField']").html(doc.getDocLabel("page_user", "adduser_field"));
    $("[name='loginField']").html(doc.getDocLabel("page_user", "login_field"));
    $("[name='nameField']").html(doc.getDocLabel("page_user", "name_field"));
    $("[name='teamField']").html(doc.getDocLabel("page_user", "team_field"));
    $("[name='defaultSystemField']").html(doc.getDocLabel("page_user", "defaultsystem_field"));
    $("[name='requestField']").html(doc.getDocLabel("page_user", "request_field"));
    $("[name='emailField']").html(doc.getDocLabel("page_user", "email_field"));
    $("[name='systemsField']").html(doc.getDocLabel("page_user", "systems_field"));
    $("[name='groupsField']").html(doc.getDocLabel("page_user", "groups_field"));
    $("[name='tabInformation']").html(doc.getDocLabel("page_user", "information_tab"));
    $("[name='tabSystems']").html(doc.getDocLabel("page_user", "systems_tab"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_user", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_user", "save_btn"));

    displayFooter(doc);
    displayGlobalLabel(doc);
}

/* --- Carried over from UserManager.js (V1) ---------------------------------
 * The checkbox-list helpers and every modal handler, unchanged apart from their
 * refresh: the three fnDraw calls on #usersTable now go through userV2Reload().
 * Marked "V2:" where changed.
 * ------------------------------------------------------------------------- */
var kcUrl;
var kcRealm;

/**
 * Build a toggle-row list from a hidden <select multiple>.
 * Uses styled rows with toggle switches for clean selection UX.
 */
function buildCheckboxList(selectSelector, containerSelector, modalSelector, onChangeCallback) {
    var $select = $(modalSelector + ' ' + selectSelector);
    var $container = $(modalSelector + ' ' + containerSelector);
    $container.empty();
    $select.find('option').each(function (idx) {
        var val = $(this).val();
        var text = $(this).text();
        var checked = $(this).prop('selected');
        var uid = 'cb_' + selectSelector.replace('#', '') + '_' + val.replace(/[^a-zA-Z0-9]/g, '_');

        var row = document.createElement('div');
        row.className = 'crb-toggle-row flex items-center justify-between px-4 py-2 cursor-pointer select-none transition-colors duration-150 ' +
            (checked ? 'bg-sky-50 dark:bg-sky-900/20' : 'hover:bg-slate-50 dark:hover:bg-slate-800/50');
        row.setAttribute('data-uid', uid);

        // Left: label text
        var labelSpan = document.createElement('span');
        labelSpan.className = 'text-sm font-medium ' + (checked ? 'text-sky-700 dark:text-sky-300' : 'text-slate-600 dark:text-slate-400');
        labelSpan.textContent = text;

        // Right: toggle switch
        var toggle = document.createElement('div');
        toggle.className = 'relative inline-flex h-5 w-9 shrink-0 rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out ' +
            (checked ? 'bg-sky-500' : 'bg-slate-300 dark:bg-slate-600');
        var knob = document.createElement('span');
        knob.className = 'pointer-events-none inline-block h-4 w-4 rounded-full bg-white shadow-sm ring-0 transition-transform duration-200 ease-in-out ' +
            (checked ? 'translate-x-4' : 'translate-x-0');
        toggle.appendChild(knob);

        row.appendChild(labelSpan);
        row.appendChild(toggle);

        // Hidden checkbox for form compat
        var cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.id = uid;
        cb.className = 'sr-only';
        cb.checked = checked;
        row.appendChild(cb);

        // Click handler
        $(row).on('click', function (e) {
            e.preventDefault();
            var $cb = $(this).find('input[type=checkbox]');
            var nowChecked = !$cb.prop('checked');
            $cb.prop('checked', nowChecked);
            $select.find('option[value="' + val + '"]').prop('selected', nowChecked);

            // Update visual state
            var $toggle = $(this).find('div');
            var $knob = $toggle.find('span');
            var $label = $(this).find('span').first();
            if (nowChecked) {
                $(this).removeClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').addClass('bg-sky-50 dark:bg-sky-900/20');
                $label.removeClass('text-slate-600 dark:text-slate-400').addClass('text-sky-700 dark:text-sky-300');
                $toggle.removeClass('bg-slate-300 dark:bg-slate-600').addClass('bg-sky-500');
                $knob.removeClass('translate-x-0').addClass('translate-x-4');
            } else {
                $(this).addClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').removeClass('bg-sky-50 dark:bg-sky-900/20');
                $label.addClass('text-slate-600 dark:text-slate-400').removeClass('text-sky-700 dark:text-sky-300');
                $toggle.addClass('bg-slate-300 dark:bg-slate-600').removeClass('bg-sky-500');
                $knob.addClass('translate-x-0').removeClass('translate-x-4');
            }

            if (typeof onChangeCallback === 'function') onChangeCallback(val, nowChecked);
        });

        $container.append(row);
    });
}

/** Sync all toggle rows from the hidden select state */
function syncCheckboxesFromSelect(selectSelector, containerSelector, modalSelector) {
    var $select = $(modalSelector + ' ' + selectSelector);
    $(modalSelector + ' ' + containerSelector).find('.crb-toggle-row').each(function () {
        var $row = $(this);
        var uid = $row.attr('data-uid');
        var $cb = $row.find('input[type=checkbox]');
        $select.find('option').each(function () {
            var optUid = 'cb_' + selectSelector.replace('#', '') + '_' + $(this).val().replace(/[^a-zA-Z0-9]/g, '_');
            if (optUid === uid) {
                var isSelected = $(this).prop('selected');
                $cb.prop('checked', isSelected);
                var $toggle = $row.find('div');
                var $knob = $toggle.find('span');
                var $label = $row.find('span').first();
                if (isSelected) {
                    $row.removeClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').addClass('bg-sky-50 dark:bg-sky-900/20');
                    $label.removeClass('text-slate-600 dark:text-slate-400').addClass('text-sky-700 dark:text-sky-300');
                    $toggle.removeClass('bg-slate-300 dark:bg-slate-600').addClass('bg-sky-500');
                    $knob.removeClass('translate-x-0').addClass('translate-x-4');
                } else {
                    $row.addClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').removeClass('bg-sky-50 dark:bg-sky-900/20');
                    $label.addClass('text-slate-600 dark:text-slate-400').removeClass('text-sky-700 dark:text-sky-300');
                    $toggle.addClass('bg-slate-300 dark:bg-slate-600').removeClass('bg-sky-500');
                    $knob.addClass('translate-x-0').removeClass('translate-x-4');
                }
            }
        });
    });
}

/** Set all toggle rows + options */
function setAllCheckboxes(selectSelector, containerSelector, modalSelector, checked) {
    $(modalSelector + ' ' + selectSelector + ' option').prop('selected', checked);
    $(modalSelector + ' ' + containerSelector).find('.crb-toggle-row').each(function () {
        var $row = $(this);
        $row.find('input[type=checkbox]').prop('checked', checked);
        var $toggle = $row.find('div');
        var $knob = $toggle.find('span');
        var $label = $row.find('span').first();
        if (checked) {
            $row.removeClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').addClass('bg-sky-50 dark:bg-sky-900/20');
            $label.removeClass('text-slate-600 dark:text-slate-400').addClass('text-sky-700 dark:text-sky-300');
            $toggle.removeClass('bg-slate-300 dark:bg-slate-600').addClass('bg-sky-500');
            $knob.removeClass('translate-x-0').addClass('translate-x-4');
        } else {
            $row.addClass('hover:bg-slate-50 dark:hover:bg-slate-800/50').removeClass('bg-sky-50 dark:bg-sky-900/20');
            $label.addClass('text-slate-600 dark:text-slate-400').removeClass('text-sky-700 dark:text-sky-300');
            $toggle.addClass('bg-slate-300 dark:bg-slate-600').removeClass('bg-sky-500');
            $knob.addClass('translate-x-0').removeClass('translate-x-4');
        }
    });
}

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function manageUserClick(data) {
    window.open(kcUrl + 'admin/' + kcRealm + '/console/#/realms/' + kcRealm + '/users', '_blank');
}


function editEntryClick(param) {
    clearResponseMessageMainPage();

    $("#editUserModal #id").val(param);

    var formEdit = $('#editUserModal');

    var jqxhr = $.getJSON("ReadUser?systems=true&roles=true", "login=" + param);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];


        formEdit.find("#login").prop("value", obj["login"]);
        formEdit.find("#name").prop("value", obj["name"]);
        formEdit.find("#email").prop("value", obj["email"]);

        formEdit.find("#attribute01").prop("value", obj["attribute01"]);
        formEdit.find("#attribute02").prop("value", obj["attribute02"]);
        formEdit.find("#attribute03").prop("value", obj["attribute03"]);
        formEdit.find("#attribute04").prop("value", obj["attribute04"]);
        formEdit.find("#attribute05").prop("value", obj["attribute05"]);
        formEdit.find("#apiKey").prop("value", obj["apiKey"]);

        formEdit.find("#usrcreated").prop("value", obj.usrCreated);
        formEdit.find("#datecreated").prop("value", getDate(obj.dateCreated));
        formEdit.find("#usrmodif").prop("value", obj.usrModif);
        formEdit.find("#datemodif").prop("value", getDate(obj.dateModif));

        formEdit.find("#defaultSystem").prop("value", obj["defaultSystem"]);
        formEdit.find("#defaultSystem").prop("readonly", "readonly");

        formEdit.find("#systems").empty();
        formEdit.find("#groups").empty();
        formEdit.find("#team").empty();

        displayInvariantList("systems", "SYSTEM", false, undefined, undefined, false);
        displayInvariantList("team", "TEAM", false, "", "", false);
        displayInvariantList("groups", "USERGROUP", false, undefined, undefined, false);

        formEdit.find("#team option[value='" + obj["team"] + "']").attr('selected', true);
        formEdit.find("#request").val(obj["request"]);

        if (!(data["hasPermissions"])) { // If readonly, we only readonly all fields
            formEdit.find("#login").prop("readonly", "readonly");
            formEdit.find("#name").prop("readonly", "readonly");
            formEdit.find("#team").prop("readonly", "readonly");
            formEdit.find("#defaultSystem").prop("readonly", "readonly");
            formEdit.find("#request").prop("readonly", "readonly");
            formEdit.find("#email").prop("readonly", "readonly");
            formEdit.find("#systems").prop("readonly", "readonly");
            formEdit.find("#groups").prop("readonly", "readonly");

            formEdit.find("#attribute01").prop("readonly", "readonly");
            formEdit.find("#attribute02").prop("readonly", "readonly");
            formEdit.find("#attribute03").prop("readonly", "readonly");
            formEdit.find("#attribute04").prop("readonly", "readonly");
            formEdit.find("#attribute05").prop("readonly", "readonly");

            formEdit.find("#apiKey").prop("readonly", "readonly");

            $('#editUserButton').attr('class', '');
            $('#editUserButton').attr('hidden', 'hidden');
        }

        // SYSTEMS
        formEdit.find("#systems option").each(function (i, e) {
            for (var i = 0; i < obj.systems.length; i++) {
                if (obj.systems[i].system == $(e).val()) {
                    $(e).attr('selected', 'selected');
                }
            }
        });
        // Build checkbox list for systems
        buildCheckboxList('#systems', '#systemsCheckboxList', '#editUserModal');

        // GROUPS
        formEdit.find("#groups option").each(function (i, e) {
            for (var i = 0; i < obj.roles.length; i++) {
                if (obj.roles[i].role == $(e).val()) {
                    $(e).attr('selected', 'selected');
                }
            }
        });
        // Build checkbox list for roles with cascading group logic
        buildCheckboxList('#groups', '#groupsCheckboxList', '#editUserModal', function(val, checked) {
            clickGroup(val, checked, $('#editUserModal'));
            // After clickGroup modifies other options, sync checkboxes
            syncCheckboxesFromSelect('#groups', '#groupsCheckboxList', '#editUserModal');
        });

        if (obj["isKeycloakManaged"]) {
            formEdit.find("#login").prop("readonly", "readonly");
            formEdit.find("#request").hide();
            $("[name='requestField']").hide();
//            formEdit.find("#email").hide();
//            $("[name='emailField']").hide();
//            $("#createTab3Text").hide();
        }

    });

    window.dispatchEvent(new CustomEvent('edituser-modal-open'));
}

function clickGroup(groupClicked, selected, formEdit) {
    console.debug("clickGroup : " + selected);
    if (selected) {
        switch (groupClicked) {
            case "TestRO":
                break;
            case "Test":
                formEdit.find("#groups option").each(function (i, e) {
                    if ("TestRO" === $(e).val()) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
            case "TestStepLibrary":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("TestRO" === $(e).val()) || ("Test" === $(e).val())) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
            case "TestAdmin":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("TestRO" === $(e).val()) || ("Test" === $(e).val()) || ("TestStepLibrary" === $(e).val())) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
            case "IntegratorRO":
                break;
            case "Integrator":
                formEdit.find("#groups option").each(function (i, e) {
                    if ("IntegratorRO" === $(e).val()) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
            case "IntegratorNewChain":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("IntegratorRO" === $(e).val())) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
            case "IntegratorDeploy":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("IntegratorRO" === $(e).val())) {
                        $(e).prop('selected', 'selected');
                    }
                });
                break;
        }
    } else {
        switch (groupClicked) {
            case "TestRO":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("Test" === $(e).val()) || ("TestAdmin" === $(e).val()) || ("TestStepLibrary" === $(e).val())) {
                        $(e).prop('selected', '');
                    }
                });
                break;
            case "Test":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("TestAdmin" === $(e).val()) || ("TestStepLibrary" === $(e).val())) {
                        $(e).prop('selected', '');
                    }
                });
                break;
            case "TestStepLibrary":
                formEdit.find("#groups option").each(function (i, e) {
                    if ("TestAdmin" === $(e).val()) {
                        $(e).prop('selected', '');
                    }
                });
                break;
            case "TestAdmin":
                break;
            case "IntegratorRO":
                formEdit.find("#groups option").each(function (i, e) {
                    if (("Integrator" === $(e).val()) || ("IntegratorNewChain" === $(e).val()) || ("IntegratorDeploy" === $(e).val())) {
                        $(e).prop('selected', '');
                    }
                });
                break;
            case "Integrator":
                break;
            case "IntegratorNewChain":
                break;
            case "IntegratorDeploy":
                break;
        }
    }
}

function editEntryModalSaveHandler() {
    clearResponseMessage($('#editUserModal'));
    var formEdit = $('#editUserModal #editUserModalForm');

    var sa = formEdit.serializeArray();
    var data = {};
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var systems = [];
    $('#editUserModal #systems :selected').each(function (i, selected) {
        systems[i] = $(selected).val();
    });

    data["systems"] = JSON.stringify(systems);

    var groups = [];
    $('#editUserModal #groups :selected').each(function (i, selected) {
        groups[i] = $(selected).val();
    });

    data["roles"] = JSON.stringify(groups);

    data["request"] = $('#editUserModal #request :selected').val();
    data["team"] = $('#editUserModal #team :selected').val();
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editUserModal');
    $.ajax({
        url: "UpdateUser",
        async: true,
        method: "POST",
        data: data,
        success: function (data) {

            data = JSON.parse(data);
            console.log(data.messageType);
            if (getAlertType(data.messageType) === 'success') {
                window.dispatchEvent(new CustomEvent('edituser-modal-close'));
                userV2Reload(); // V2: was $("#usersTable").dataTable().fnDraw(false)
                showMessage(data);
            } else {
                showMessage(data, $('#editUserModal'));
            }

            hideLoaderInModal('#editUserModal');
        },
        error: showUnexpectedError
    });

}

/** Renamed from editEntryModalCloseHandler - see the note in initPage(). */
function userV2EditModalCloseHandler() {
    var form = $('#editUserModal #editUserModalForm')[0];
    if (form) {
        form.reset();
    }
    $('#editUserModal').find('div.has-error').removeClass("has-error");
    clearResponseMessage($('#editUserModal'));
}

function editEntryPassModalSaveHandler() {
    clearResponseMessage($('#editUserPasswordModal'));
    var formEdit = $('#editUserPasswordModal #editUserPasswordModalForm');

    var sa = formEdit.serializeArray();
    var data = {};
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());

    showLoaderInModal('#editUserPasswordModal');
    $.ajax({
        url: "ChangeUserPasswordAdmin",
        async: true,
        method: "POST",
        data: data,
        success: function (data) {

//            data = JSON.parse(data);
            console.log(data.messageType);
            if (getAlertType(data.messageType) === 'success') {
                window.dispatchEvent(new CustomEvent('editpassword-modal-close'));
                showMessage(data);
            } else {
                showMessage(data, $('#editUserPasswordModal'));
            }

            hideLoaderInModal('#editUserPasswordModal');
        },
        error: showUnexpectedError
    });

}


function editEntryPassClick(param) {
    clearResponseMessageMainPage();

    $("#editUserPasswordModal #id").val(param);

    var formEdit = $('#editUserPasswordModal');
    formEdit.find("#login").prop("value", param);

    window.dispatchEvent(new CustomEvent('editpassword-modal-open'));
}

function editEntryPassModalCloseHandler() {
    var form = $('#editUserPasswordModal #editUserPasswordModalForm')[0];
    if (form) form.reset();
    $('#editUserPasswordModal').find('div.has-error').removeClass("has-error");
    clearResponseMessage($('#editUserPasswordModal'));
}

function addEntryClick() {
    clearResponseMessageMainPage();
    $("#addUserModal #user").empty();

    $("#addUserModal").find("#systems").empty();
    $("#addUserModal").find("#groups").empty();
    $("#addUserModal").find("#defaultSystem").empty();
    $("#addUserModal").find("#team").empty();

    displayInvariantList("systems", "SYSTEM", false, undefined, undefined, false);
    displayInvariantList("defaultSystem", "SYSTEM", false, undefined, undefined, false);
    displayInvariantList("groups", "USERGROUP", false, undefined, undefined, false);
    displayInvariantList("team", "TEAM", false, "", "", false);

    // Build checkbox list for systems
    buildCheckboxList('#systems', '#systemsCheckboxList', '#addUserModal');

    // Build checkbox list for roles with cascading group logic
    buildCheckboxList('#groups', '#groupsCheckboxList', '#addUserModal', function(val, checked) {
        clickGroup(val, checked, $('#addUserModal'));
        syncCheckboxesFromSelect('#groups', '#groupsCheckboxList', '#addUserModal');
    });

    $("#addUserModal").find("#request").show();
    $("[name='requestField']").show();

    window.dispatchEvent(new CustomEvent('adduser-modal-open'));
}

function addEntryModalSaveHandler() {
    clearResponseMessage($('#addUserModal'));
    var formEdit = $('#addUserModal #addUserModalForm');

    var sa = formEdit.serializeArray();
    var data = {};
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var systems = [];
    $('#addUserModal #systems :selected').each(function (i, selected) {
        systems[i] = $(selected).val();
    });

    data["systems"] = JSON.stringify(systems);

    var groups = [];
    $('#addUserModal #groups :selected').each(function (i, selected) {
        groups[i] = $(selected).val();
    });

    data["roles"] = JSON.stringify(groups);

    data["defaultSystem"] = $('#addUserModal #defaultSystem :selected').val();
    data["request"] = $('#addUserModal #request :selected').val();
    data["team"] = $('#addUserModal #team :selected').val();

    showLoaderInModal('#addUserModal');
    $.ajax({
        url: "CreateUser",
        async: true,
        method: "POST",
        data: data,
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#addUserModal');
            if (getAlertType(data.messageType) === 'success') {
                window.dispatchEvent(new CustomEvent('adduser-modal-close'));
                userV2Reload(); // V2: was $("#usersTable").dataTable().fnDraw(false)
                showMessage(data);
            } else {
                showMessage(data, $('#addUserModal'));
            }
        },
        error: showUnexpectedError
    });

}

function addEntryModalCloseHandler() {
    var form = $('#addUserModal #addUserModalForm')[0];
    if (form) form.reset();
    $('#addUserModal').find('div.has-error').removeClass("has-error");
    clearResponseMessage($('#addUserModal'));
}

async function removeEntryClick(key) {
    clearResponseMessageMainPage();
    var doc = new Doc();
    var messageComplete = doc.getDocLabel("page_user", "message_remove") || "Are you sure you want to delete user '%USER%'?";
    messageComplete = messageComplete.replace('%USER%', key);

    const result = await crbConfirmDelete({
        title: doc.getDocLabel("page_user", "title_remove") || 'Delete User',
        html: messageComplete,
        confirmText: doc.getDocLabel("page_global", "btn_delete") || 'Delete',
        cancelText: doc.getDocLabel("page_global", "buttonClose") || 'Cancel',
        preConfirm: async () => {
            try {
                const resp = await $.ajax({
                    url: "DeleteUser?login=" + encodeURIComponent(key),
                    method: "GET",
                    dataType: "json"
                });
                var parsed = (typeof resp === 'string') ? JSON.parse(resp) : resp;
                if (getAlertType(parsed.messageType) !== "success") {
                    Swal.showValidationMessage(parsed.message || "Delete failed");
                    return null;
                }
                return parsed;
            } catch (e) {
                Swal.showValidationMessage("Unexpected error");
                return null;
            }
        }
    });

    if (result.isConfirmed && result.value) {
        userV2Reload(); // V2: was $("#usersTable").dataTable().fnDraw(false)
        showMessageMainPage(getAlertType(result.value.messageType), result.value.message, false);
    }
}

