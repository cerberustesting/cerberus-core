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



/***
 * Creates a datatable that is server-side processed.
 * @param {type} tableConfigurations set of configurations that define how data is retrieved and presented
 * @param {Function} callbackFunction callback function to be called after table creation (only on server side)
 * @param {String} objectWaitingLayer object that will report the waiting layer when external calls. Ex : #logViewer
 * @param {Array} filtrableColumns array of parameter name that can trigger filter on columns
 * @param {Boolean} checkPermissions boolean that define if user permission need to be checked
 * @param {type} userCallbackFunction
 * @param {Function} createdRowCallback callback function to be called after each row
 * @return {Object} Return the dataTable object to use the api
 */
function createDataTableWithPermissionsNew(tableConfigurations, callbackFunction, objectWaitingLayer, filtrableColumns, checkPermissions, userCallbackFunction, createdRowCallback, async = true) {
    /**
     * Define datatable config with tableConfiguration object received
     */
    var configs = {};
    var domConf = 'ZRCB<"clear">lf<"pull-right"p>rit<"marginTop5">'; // Z allow to activate table resize
    if (!tableConfigurations.showColvis) {
        domConf = 'Zlf<"pull-right"p>rit<"marginTop5">';
    }
    configs["dom"] = domConf;
    configs["stateDuration"] = tableConfigurations.stateDuration;
    configs["serverSide"] = tableConfigurations.serverSide;
    configs["processing"] = tableConfigurations.processing;
    configs["bJQueryUI"] = tableConfigurations.bJQueryUI;
    configs["bPaginate"] = tableConfigurations.bPaginate;
    configs["autoWidth"] = tableConfigurations.autoWidth;
    configs["sPaginationType"] = tableConfigurations.sPaginationType;
    configs["columns.searchable"] = false;
    configs["columnDefs.targets"] = [0];
    configs["pageLength"] = tableConfigurations.displayLength;
    configs["scrollX"] = tableConfigurations.tableWidth;
    configs["scrollY"] = tableConfigurations.scrollY;
    configs["scrollCollapse"] = tableConfigurations.scrollCollapse;
    configs["stateSave"] = tableConfigurations.stateSave;
    configs["language"] = tableConfigurations.lang.table;
    configs["columns"] = tableConfigurations.aoColumnsFunction;
    configs["colVis"] = tableConfigurations.lang.colVis;
    configs["scrollX"] = tableConfigurations.scrollX;
    configs["lengthChange"] = tableConfigurations.lengthChange;
    configs["lengthMenu"] = tableConfigurations.lengthMenu;
    configs["orderClasses"] = tableConfigurations.orderClasses;
    configs["bDeferRender"] = tableConfigurations.bDeferRender;
    configs["columnReorder"] = tableConfigurations.colreorder;
    configs["searchDelay"] = tableConfigurations.searchDelay;
    configs["buttons"] = ['colvis'];
    if (tableConfigurations.aaSorting !== undefined) {
        configs["aaSorting"] = [tableConfigurations.aaSorting];
    }
    if (createdRowCallback !== undefined) {
        configs["createdRow"] = createdRowCallback;
    }
    if (tableConfigurations.serverSide) {

        configs["sAjaxSource"] = tableConfigurations.ajaxSource;
        configs["sAjaxDataProp"] = tableConfigurations.ajaxProp;

        configs["fnStateSaveCallback"] = function (settings, data) {
            try {
                localStorage.setItem(
                    'DataTables_' + settings.sInstance + '_' + location.pathname,
                    JSON.stringify(data)
                );
            } catch (e) {
                console.error("access denied, " + e)
            }
        };
        configs["fnStateLoadCallback"] = function (settings) {
            //Get UserPreferences from user object
            var user = null;
            $.when(getUser()).then(function (data) {
                user = data;
            });
            while (user === null) {
                //Wait for user information make sure to don't loose it
            }

            if ("" !== user.userPreferences && undefined !== user.userPreferences && null !== user.userPreferences) {
                var userPref = JSON.parse(user.userPreferences);
                if (undefined !== userPref['DataTables_' + settings.sInstance + '_' + location.pathname]) {
                    return JSON.parse(userPref['DataTables_' + settings.sInstance + '_' + location.pathname]);
                }
            }
        };
        configs["colReorder"] = tableConfigurations.colreorder ? {
            fnReorderCallback: function () {
                $("#" + tableConfigurations.divId).DataTable().ajax.reload();
            }
        } : false;

        configs["fnServerData"] = function (sSource, aoData, fnCallback, oSettings) {

            var like = "";

            $.each(oSettings.aoColumns, function (index, value) {
                if (oSettings.aoColumns[index].like) {
                    like += oSettings.aoColumns[index].sName + ",";
                }
            });

            like = like.substring(0, like.length - 1);

            aoData.push({name: "sLike", value: like});
            if (sSource !== "ReadTest") { // RG, don't filter on system if it is a Test Folder
                for (var s in getUser().defaultSystems) {
                    aoData.push({name: "system", value: getUser().defaultSystems[s]});
                }
            }

            var objectWL = $(objectWaitingLayer);
            if (objectWaitingLayer !== undefined) {
                showLoader(objectWL);
            }

            oSettings.jqXHR = $.ajax({
                "dataType": 'json',
                "type": "POST",
                "async": async === undefined ? true : async,
                "url": sSource,
                "data": aoData,
                "success": function (json) {
                    if (objectWaitingLayer !== undefined) {
                        hideLoader(objectWL);
                    }
                    if (checkPermissions !== undefined && Boolean(checkPermissions)) {
                        var tabCheckPermissions = $("#" + tableConfigurations.divId);
                        var hasPermissions = false; //by default does not have permissions
                        if (Boolean(json["hasPermissions"])) { //if the response information about permissions then we will update it
                            hasPermissions = json["hasPermissions"];
                        }
                        //sets the permissions in the table
                        tabCheckPermissions.attr("hasPermissions", hasPermissions);
                    }
                    returnMessageHandler(json);
                    fnCallback(json);
                    if (Boolean(userCallbackFunction)) {
                        userCallbackFunction(json);
                    }
                },
                "error": showUnexpectedError
            });
            $.when(oSettings.jqXHR).then(function (data) {
                //updates the table with basis on the permissions that the current user has
                afterDatatableFeeds(tableConfigurations.divId, tableConfigurations.ajaxSource, oSettings);

                if (callbackFunction !== undefined)
                    callbackFunction(data);
            });
        };

    } else {

        configs["fnStateSaveCallback"] = function (oSettings, data) {
            try {
                localStorage.setItem(
                    'DataTables_' + oSettings.sInstance + '_' + location.pathname,
                    JSON.stringify(data)
                );
            } catch (e) {
                console.error("access denied, " + e);
            }
            afterDatatableFeedsForServerSide(tableConfigurations.aaData, tableConfigurations.divId, oSettings);
        };

        configs["data"] = tableConfigurations.aaData;

        configs["fnStateLoadCallback"] = function (settings) {
            //Get UserPreferences from user object

            var user = null;
            $.when(getUser()).then(function (data) {
                user = data;
            });
            while (user === null) {
                //Wait for user information make sure to don't loose it
            }

            if ("" !== user.userPreferences && undefined !== user.userPreferences && null !== user.userPreferences) {
                var userPref = JSON.parse(user.userPreferences);
                var currentTable = userPref['DataTables_' + settings.sInstance + '_' + location.pathname];
                if (undefined !== currentTable) {
                    for (var i = 0; i < JSON.parse(currentTable)["columns"].length; i++) {
                        var currentSearch = JSON.parse(currentTable)["columns"][i]["search"]["search"];
                        var search = currentSearch.substr(1, currentSearch.length - 2);
                        search = search.split("|");
                        columnSearchValuesForClientSide.push(search);
                    }
                    return JSON.parse(currentTable);
                }
            }
        };
    }

    var oTable = $("#" + tableConfigurations.divId).DataTable(configs);
    if (window.lucide) lucide.createIcons();

    var $wrapper = $("#" + tableConfigurations.divId + "_wrapper");

    // Sélectionne les éléments du "top" : length, filter, buttons
    var $topElements = $wrapper.find(".dt-buttons, .dataTables_length, .dataTables_filter, .dataTables_paginate, .dataTables_info");
    // Crée un nouveau div pour wrapper
    $topElements.wrapAll(`<div class="crb_card clearfix" id="${tableConfigurations.divId}_headerwrapper"></div>`);


    var doc = new Doc();
    var showHideButtonLabel = doc.getDocLabel("page_global", "btn_showHideColumns");
    var showHideButtonTooltip = doc.getDocLabel("page_global", "tooltip_showHideColumns");
    var saveTableConfigurationButtonLabel = doc.getDocLabel("page_global", "btn_savetableconfig");
    var saveTableConfigurationButtonTooltip = doc.getDocDescription("page_global", "tooltip_savetableconfig");
    var restoreFilterButtonLabel = doc.getDocLabel("page_global", "btn_restoreuserpreferences");
    var restoreFilterButtonTooltip = doc.getDocDescription("page_global", "tooltip_restoreuserpreferences");
    var resetTableConfigurationButtonLabel = doc.getDocLabel("page_global", "btn_resettableconfig");
    var resetTableConfigurationButtonTooltip = doc.getDocDescription("page_global", "tooltip_resettableconfig");
    var searchPlaceholder = tableConfigurations.searchPlaceholder || "Search...";
    if (tableConfigurations.showColvis) {

        var $headerwrapper = $("#" + tableConfigurations.divId + "_headerwrapper");
        var searchPlaceholder = tableConfigurations.searchPlaceholder || "Search...";

        // Nettoyage
        $headerwrapper.find("#saveTableConfigurationButton, #restoreFilterButton, #resetFilterButton").remove();

        // Masquer les éléments natifs (on les déplacera)
        $("#" + tableConfigurations.divId + "_length").hide();
        $("#" + tableConfigurations.divId + "_filter").hide();
        $("#" + tableConfigurations.divId + "_info").hide();

        // Bouton ColVis
        var $colvisButton = $headerwrapper.find(".dt-buttons.btn-group a");
        $colvisButton
            .addClass("flex items-center gap-2 px-3 py-2 w-full text-left hover:bg-slate-100 hover:dark:bg-slate-800")
            .attr("id", "showHideColumnsButton")
            .css("display", "none")
            .html(`<i data-lucide="columns" class="w-4 h-4 align-middle"></i><span>${showHideButtonLabel}</span>`);

        // Control Panel
        var $controlPanel = $(`
        <div id="${tableConfigurations.divId}_controlPanel" class="flex flex-col gap-2 mb-4">
            
            <!-- Ligne 1 : bouton wrapper externe -->
            <div id="${tableConfigurations.divId}_buttonWrapper" class="flex w-full gap-2"></div>
            
            <!-- Ligne 2 : search + refresh + config -->
            <div class="flex items-center justify-between gap-2">
                <div class="flex items-center gap-2 flex-grow">
                    <input type="search" id="${tableConfigurations.divId}_globalSearch"
                           class="flex-grow border rounded px-3 py-2 h-10 border-gray-300 dark:border-gray-600"
                           placeholder="🔍 ${searchPlaceholder}"
                           aria-label="Search DataTable">
                    <button id="${tableConfigurations.divId}_refresh"
                            type="button"
                            class="px-3 py-2 rounded border border-gray-300 dark:border-gray-600 h-10"
                            title="Refresh">
                        <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                    </button>
                </div>
                <div class="flex items-center gap-2">
                    <div id="${tableConfigurations.divId}_customInfo"
                         class="text-sm text-slate-600 dark:text-slate-300 whitespace-nowrap"></div>
                    <button id="${tableConfigurations.divId}_toggleConfig"
                            type="button"
                            class="px-3 py-2 rounded border border-gray-300 dark:border-gray-600 h-10 flex items-center gap-2">
                        <i data-lucide="sliders" class="w-4 h-4"></i>
                        <span>Config</span>
                    </button>
                </div>
            </div>
            
            <!-- Ligne 3 : filtre avancé -->
            <div id="${tableConfigurations.divId}_filterresult" class="p-2 flex w-full">
                <!-- Contenu filtre plus tard -->
            </div>
            
            <!-- Ligne 4 : boutons config + colonnes + pagination -->
            <div id="${tableConfigurations.divId}_configPanel" class="hidden flex flex-wrap gap-2 items-center p-2">
                <div id="${tableConfigurations.divId}_actionButtons" class="flex gap-2"></div>
                <div id="${tableConfigurations.divId}_colvisContainer"></div>
                <div id="${tableConfigurations.divId}_extraControls" class="ml-auto flex gap-4 items-center"></div>
            </div>
        </div>
    `);

        // Injecter le panneau
        $headerwrapper.append($controlPanel);

        // Déplacer le bouton ColVis
        $colvisButton.appendTo($controlPanel.find(`#${tableConfigurations.divId}_colvisContainer`));


        // Après init ou draw, déplacer length & pagination dans la 4ème ligne
        $("#" + tableConfigurations.divId).on('init.dt draw.dt', function () {
            $("#" + tableConfigurations.divId + "_length").appendTo($controlPanel.find(`#${tableConfigurations.divId}_extraControls`)).show();
            $("#" + tableConfigurations.divId + "_paginate").appendTo($controlPanel.find(`#${tableConfigurations.divId}_extraControls`)).show();
        });

        // Branche la recherche globale
        $("#" + tableConfigurations.divId + "_globalSearch").on("keyup", function () {
            $("#" + tableConfigurations.divId).DataTable().search(this.value).draw();
        });

        // Refresh
        $("#" + tableConfigurations.divId + "_refresh").click(function () {
            $("#" + tableConfigurations.divId).dataTable().fnDraw(false);
        });

        // Toggle panneau config
        $("#" + tableConfigurations.divId + "_toggleConfig").click(function () {
            $("#" + tableConfigurations.divId + "_configPanel").toggleClass("hidden");
        });

        // Mise à jour info
        $("#" + tableConfigurations.divId).on('draw.dt', function () {
            $("#" + tableConfigurations.divId + "_customInfo").html($("#" + tableConfigurations.divId + "_info").text());
            if (window.lucide) lucide.createIcons();
        }).trigger("draw.dt");
    }


// ==========================
// Adaptations DataTables UI
// ==========================

// Length & Filter → masqués car déplacés
    $("#" + tableConfigurations.divId + "_length").hide();
    $("#" + tableConfigurations.divId + "_filter").hide();

// Recherche globale reliée à DataTables
    $("#" + tableConfigurations.divId + "_globalSearch").on("keyup", function () {
        $("#" + tableConfigurations.divId).DataTable().search(this.value).draw();
    });


// Lucide après redraw
    $("#" + tableConfigurations.divId).on('draw.dt', function () {
        if (window.lucide) lucide.createIcons();
    });


    return oTable;
}

/**
 * @param objectWaitingLayer
 */
function updateUserPreferences(objectWaitingLayer) {
    var objectWL = $(objectWaitingLayer);
    if (objectWaitingLayer !== undefined) {
        showLoader(objectWL);
    }
    let tempLocalStorage = [];
    tempLocalStorage = localStorage;


    for (var i = 0; i < tempLocalStorage.length; i++) {
        if (tempLocalStorage.key(i).startsWith("DataTables_")) {
            let temp = JSON.parse(tempLocalStorage.getItem(tempLocalStorage.key(i)));
            temp.start = 0;
            tempLocalStorage.setItem(tempLocalStorage.key(i), JSON.stringify(temp));
        }
    }
//    console.info(tempLocalStorage);
    var uPref = "";
    uPref = JSON.stringify(localStorage);
//    console.info("upload user perf to Cerberus :");
//    console.info(uPref);

    $.ajax({url: "UpdateMyUser",
        type: "POST",
        data: {column: "userPreferences", value: uPref},
        async: false,
        success: function (data) {
            var messageType = getAlertType(data.messageType);
            if (messageType === "success") {
                readUserFromDatabase();
            }
            //show message in the main page
            showMessageMainPage(messageType, data.message, true);
        }
    });
    if (objectWaitingLayer !== undefined) {
        hideLoader(objectWL);
    }
}