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
/***************************SEARCH/FILTERS**************************************/

/**
 * Function that allow to reset the filter selected
 * @param {type} oTable datatable object
 * @param {int} columnNumber If empty, reset all the column's filter
 * @param {boolean} clearGlobalSearch true if global search should be cleared.
 * @param {boolean} doRedraw true if draw should be triggeres.
 * @returns {undefined}
 */
function resetFilters(oTable, columnNumber, clearGlobalSearch = false, doRedraw = true) {
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        /**
         * if columnNumber defined, clear that column search
         */
        if (columnNumber !== undefined) {
            if (parseInt(columnNumber) === iCol) {
                oSettings.aoPreSearchCols[iCol].sSearch = '';
            }
            /**
             * else clear all columns
             */
        } else {
            oSettings.aoPreSearchCols[iCol].sSearch = '';
        }
    }
    /**
     * if clearGlobalSearch, clear search
     */
    if (clearGlobalSearch) {
        oSettings.oPreviousSearch.sSearch = '';
    }
    if (doRedraw) {
        oTable.fnDraw();
}
}

function resetTooltip() {
    $(".tooltip.fade").remove();
}


/**
 * Function that allow to filter column on specific value
 * @param {type} tableId >Id of the datatable
 * @param {type} column > Name of the column
 * @param {type} value > Value to filter
 * @returns {undefined}
 */
function filterOnColumn(tableId, column, value) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, undefined, undefined, false);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        if (oSettings.aoColumns[iCol].data === column) {
            oTable.api().column(iCol).search(value);
        }
    }
    oTable.fnDraw();
}

/**
 * Function that generate Array that will be send do table to define filter
 * @param {type} tableId > Id of the datatable
 * @param {type} searchColums > Array of columns
 * @returns {undefined}
 */
//function generateFiltersOnMultipleColumns(tableId, searchColumns) {
//    var filterConfiguration = Array();
//    /**
//     * Loop on searchColumns and get Parameter values >> Build an array of object
//     */
//    var searchArray = new Array;
//    for (var searchColumn = 0; searchColumn < searchColumns.length; searchColumn++) {
//        var param = GetURLParameters(searchColumns[searchColumn]);
//        var searchObject = {
//            param: searchColumns[searchColumn],
//            values: param};
//        searchArray.push(searchObject);
//    }
//    /**
//     * Apply the filter to the table
//     */
//    var oTable = $('#' + tableId).dataTable();
//    //resetFilters(oTable);
//    var oSettings = oTable.fnSettings();
//    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
//        for (sCol = 0; sCol < searchArray.length; sCol++) {
//            if (oSettings.aoColumns[iCol].data === searchArray[sCol].param
//                    && searchArray[sCol].values.length !== 0) {
//                var filter = {
//                    name: "sSearch_" + iCol,
//                    value: searchArray[sCol].values.join(", ")};
//                filterConfiguration.push(filter);
//            }
//        }
//
//    }
//
//    return (filterConfiguration);
//}

/**
 * Function that apply filters on given datatable's columns
 *
 * Values can either be contained into the given columns, or retrieved from the current URL.
 *
 * @param tableId the datatable from which filter columns
 * @param searchColumns the array of columns to filter. Column can be either an object {param, values}, or simply the name of the column (param)
 * @param fromURL if values are to be retrived from the current URL
 */
function applyFiltersOnMultipleColumns(tableId, searchColumns, fromURL) {
    // Get or create the search array
    var searchArray = searchColumns;
    if (fromURL) {
        searchArray = [];
        for (var searchColumn = 0; searchColumn < searchColumns.length; searchColumn++) {
            var param = GetURLParameters(searchColumns[searchColumn]);
            var searchObject = {
                param: searchColumns[searchColumn],
                values: param};
            searchArray.push(searchObject);
        }
    }

    // Apply filter on table
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, undefined, undefined, false);
    var oSettings = oTable.fnSettings();
    for (iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
        for (sCol = 0; sCol < searchArray.length; sCol++) {
            if (oSettings.aoColumns[iCol].data === searchArray[sCol].param) {
                oTable.api().column(iCol).search(searchArray[sCol].values);
            }
        }
    }
    oTable.fnDraw();
}

/**
 * Function that allow to clear filter individually
 * @param {type} tableId >> ID of the datatable
 * @param {type} columnNumber >> Number of the column
 * @param {type} clearGlobalSearch >> boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function clearIndividualFilter(tableId, columnNumber, clearGlobalSearch) {
    var oTable = $('#' + tableId).dataTable();
    resetFilters(oTable, columnNumber, clearGlobalSearch);
}

/**
 * Function that allow to clear filter individually
 * @param {type} tableId >> ID of the datatable
 * @param {type} columnNumber >> Number of the column
 * @param {type} clearGlobalSearch >> boolean. true if global search should be cleared.
 * @returns {undefined}
 */
function clearIndividualFilterForClientSide(tableId, columnNumber, clearGlobalSearch) {
    columnSearchValuesForClientSide[columnNumber] = [];//reset the search value for the column
    clearIndividualFilter(tableId, columnNumber, clearGlobalSearch);
}

/**
 * Function that allow display of individual column when the table is created client side
 * @param {type} tableId >> ID of the datatable
 * @param {type} oSettings >> datatable settings
 * @returns {undefined}
 */
var columnSearchValuesForClientSide = [];//global var that take the role of the ajax function
function displayColumnSearchForClientSideTable(tableData, contentUrl, oSettings) {
    privateDisplayColumnSearch(tableData, contentUrl, oSettings, true); // table data not use ?
}

/**
 * Function that allow display of individual column searching on datatable
 * @param {type} tableId >> ID of the datatable
 * @param {type} contentUrl >> URL of the service to get content of each columns
 * @param {type} oSettings >> datatable settings
 * @returns {undefined}
 */
function displayColumnSearch(tableId, contentUrl, oSettings) {
    privateDisplayColumnSearch(tableId, contentUrl, oSettings, false);
}


var firstclickOnShowHide = true;
function privateDisplayColumnSearch(tableId, contentUrl, oSettings, clientSide) {
    // init special var for client side
    var fctClearIndividualFilter = "clearIndividualFilter";

    if (clientSide) {
        fctClearIndividualFilter = "clearIndividualFilterForClientSide";
    }

    //Build the Message that appear when filter is fed
    var showFilteredColumnsAlertMessage = "<br><div id='filterAlertDiv' class='pull-right col-xs-12 filterTable marginBottom10'><div class='col-xs-11 row' id='activatedFilters'></div><div class='col-xs-1  filterMessageButtons'><span id='clearFilterButton' data-toggle='tooltip' title='Clear filters' class='pull-right glyphicon glyphicon-remove-sign'  style='cursor:pointer;padding:15px'></span></div>";
    $("#filterAlertDiv").remove();
    $("#" + tableId + "_filter").after($(showFilteredColumnsAlertMessage).hide());
    //if ($("#" + tableId + "_paginate").length !== 0) {
    //Hide filtered alert message displayed when filtered column
    //  $("#" + tableId + "_paginate").parent().after($(showFilteredColumnsAlertMessage).hide());
    //} else {
    //Hide filtered alert message displayed when filtered column
    //  $("#showHideColumnsButton").parent().after($(showFilteredColumnsAlertMessage).hide());
    //}

    //Load the table
    var table = $("#" + tableId).dataTable().api();
    var columnVisibleIndex = 0;//Used to Match visible column with columns available
    var doc = new Doc();

    //Start building the Alert Message for filtered column information
    //TODO : Replace with data from doc table
    var filteredInformation = new Array();
    filteredInformation.push("<div class=\"col-xs-2 \" style=\"margin-bottom:0px; padding:15px\">Filters : </div>");
    if (table.search() !== "") {
        filteredInformation.push("<div class=\"col-xs-2  label labelBlue marginTop5\" style=\"margin-left:10px;margin-bottom:0px;height:30px;border-radius:30px\">");
        filteredInformation.push("<span id='clearFilterButtonGlobal' onclick='" + fctClearIndividualFilter + "(\"" + tableId + "\", null, true)'  data-toggle='tooltip' title='Clear global filter' class='glyphicon glyphicon-remove-sign pull-right'  style='cursor:pointer;'></span>");
        filteredInformation.push("<div data-toggle='tooltip' data-html='true' title=" + table.search() + " style=\"margin-bottom:0px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;\">");
        filteredInformation.push("[" + table.search() + "]</div></div>");
        filteredInformation.push(" ");
    }
    //Get the column name in the right order TODO check if it's correct
    var orderedColumns = [];
    $.each(oSettings.aoColumns, function (i, columns) {
        if (clientSide) {
            if (columns.sName !== "") {
                if (columns.sName.split(".")[1] === undefined) {
                    orderedColumns.push(columns.sName);
                } else {
                    orderedColumns.push(columns.sName.split(".")[1]);
                }
            } else {
                orderedColumns.push("labels");
            }
        } else {
            orderedColumns.push(columns.sName);
        }
    });

    // Delete and Build a new tr in the header to display the editable mark
    //So first delete in case on page reload
    $("#" + tableId + "_wrapper #filterHeader").remove();
    $("#" + tableId + '_wrapper .dataTables_scrollBody').find("#filterHeader").remove();
    //Set the table with relative position position for the editable box.
    $("#" + tableId + "_wrapper").attr("style", "position: relative");
    //Remove the relative position of the header
    $("#" + tableId + '_wrapper [class="dataTables_scrollHead ui-state-default"]').attr("style", "overflow: hidden; border: 0px; width: 100%;");
    //Then create a th tag for each columns
    $("#" + tableId + '_wrapper .dataTables_scrollHeadInner table thead').append('<tr id="filterHeader"></tr>');
    $("#" + tableId + '_wrapper .dataTables_scrollHeadInner table thead tr th').each(function () {
        $("#" + tableId + "_wrapper #filterHeader").append("<th name='filterColumnHeader'></th>");
    });

    var allcolumnSearchValues = new Object();

    //Iterate on all columns
    $.each(orderedColumns, function (index, value) {
        var columnSearchValues;
        var json_obj = JSON.stringify(table.ajax.params());

        if (clientSide) { // TODO verify if it's normal it's different for clientSide
            columnSearchValues = columnSearchValuesForClientSide[index]; //Get the value from storage (To display specific string if already filtered)
        } else {
            columnSearchValues = JSON.parse(json_obj)["sSearch_" + index].split(',');
        }

        if (columnSearchValues != undefined) {
            if (columnSearchValues[0] == "" || columnSearchValues[0] === undefined) {
                allcolumnSearchValues[value] = undefined
            } else {
                allcolumnSearchValues[value] = columnSearchValues
            }
        } else {
            allcolumnSearchValues[value] = columnSearchValues
        }

        //Get the column names (for title display)
        var title = value;
        //Build the specific tooltip for filtered columns and the tooltip for not filtered columns
        var emptyFilter = doc.getDocLabel("page_global", "tooltip_column_filter_empty");
        var selectedFilter = doc.getDocLabel("page_global", "tooltip_column_filter_filtered");
        var display = '<input placeholder="Search..." autocomplete="off" id="inputsearch_' + index + '" class="form-control input-sm" name="searchField" data-toggle="tooltip" data-html="true" title="' + emptyFilter + '" />';
        var valueFiltered = [];

        if (columnSearchValues !== undefined && columnSearchValues.length > 0 && columnSearchValues[0] !== '') {
            //Build the Alert Message for filtered column information
            var filteredColumnInformation = new Array();
            var filteredTooltip = '<div>';

            $(columnSearchValues).each(function (i) {
                valueFiltered[i] = $('<p>' + columnSearchValues[i] + '</p>').text();
                filteredTooltip += "<br><span>" + $('<p>' + columnSearchValues[i] + '</p>').text() + "</span> ";
                filteredColumnInformation.push(columnSearchValues[i]);
                filteredColumnInformation.push(" | ");
            });
            filteredColumnInformation.pop();
            filteredTooltip += '</div>';
            filteredInformation.push("<div class=\"col-xs-2 label labelBlue marginTop5\" style=\"margin-left:10px;margin-bottom:0px;height:30px;border-radius:30px\">");
            filteredInformation.push("<span id='clearFilterButton" + index + "' onclick='" + fctClearIndividualFilter + "(\"" + tableId + "\", \"" + index + "\", false)' data-toggle='tooltip' title='Clear filter " + title + "' class='pull-right glyphicon glyphicon-remove-sign'  style='cursor:pointer;margin-top:8px'></span>");
            filteredInformation.push("<div style=\"margin-bottom:0px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;\">");
            if (oSettings.aoColumns[index].like) {
                filteredInformation.push("<strong>" + title + "</strong> LIKE <br>");
            } else {
                filteredInformation.push("<strong>" + title + "</strong> IN <br>");
            }
            filteredInformation.push("<div data-toggle=\"tooltip\" data-html=\"true\" title=\"" + filteredTooltip + "\" id=\"alertFilteredValues" + index + "\">[ ");
            filteredInformation.push(filteredColumnInformation);
            filteredInformation.push(" ]</div></div></div>");
            filteredInformation.push(" ");
            display = "<input placeholder='Search...' autocomplete='off' id='inputsearch_" + index + "' class='form-control input-sm' name='searchField' data-toggle='tooltip' data-html='true' title='" + valueFiltered.length + " " + selectedFilter + " : " + filteredTooltip + "' />";
        }

        //init column filter only if column visible
        if (oSettings.aoColumns[index].bVisible) {

            //This is the list of distinct value of the column
            //This will determine value proposed inside the select
            //TODO : to replace by server call to get distinct value in server mode
            var data = [];
            if (title !== "labels") {
                table.column(index).data().unique().sort().each(function (d, j) {
                    data.push(d);
                });
            } else {//For the navigation filter list
                oSettings.aoColumns[index].bSearchable = false;
            }

            //Get the header cell to display the filter
            var tableCell = $($("#" + tableId + '_wrapper [name="filterColumnHeader"]')[columnVisibleIndex])[0];

            $(tableCell).removeClass().addClass("filterHeader");
            if (clientSide && oSettings.aoColumns[index].bSearchable || !clientSide && table.ajax.params()["bSearchable_" + index]) { // TODO verify why it's different
                //Then init the editable object
                var select =
                        $('<span></span>')
                        .appendTo($(tableCell).attr('data-id', 'filter_' + columnVisibleIndex)
                                .attr('data-order', index))
                        .editable({
                            type: 'checklist',
                            title: title,
                            source: function () {
                                if (clientSide) {
                                    return data;
                                } else if (oSettings.aoColumns[index].like) {
                                    return [];
                                }

                                //Check if URL already contains parameters
                                var urlSeparator = contentUrl.indexOf("?") > -1 ? "&" : "?";
                                var url = './' + contentUrl + urlSeparator + 'columnName=' + title + getUser().defaultSystemsQuery;
                                var result;

                                var params = table.ajax.params()

                                var like = ""

                                $.each(oSettings.aoColumns, function (index, value) {
                                    if (oSettings.aoColumns[index].like) {
                                        like += oSettings.aoColumns[index].sName + ","
                                    }
                                })

                                like = like.substring(0, like.length - 1);
                                params["sLike"] = like

                                $.ajax({
                                    type: 'POST',
                                    async: false,
                                    url: url,
                                    data: params,
                                    success: function (responseObject) {
                                        if (responseObject.distinctValues !== undefined) {
                                            result = responseObject.distinctValues;
                                        } else {
                                            let newResult = JSON.parse(responseObject);
                                            if (newResult.distinctValues !== undefined) {
                                                result = newResult.distinctValues;
                                            } else {
                                                //TODO : To remove when all servlet have method to find distinct values
                                                //if undefined, display the distinct value displayed in the table
                                                result = data;
                                            }
                                        }
                                    },
                                    error: function () {
                                        //TODO : To remove when all servlet have method to find distinct values
                                        //if error, display the distinct value displayed in the table
                                        result = data;
                                    }
                                });
                                return result;
                            }
                            ,
                            onblur: 'cancel',
                            mode: 'popup',
                            placement: 'bottom',
                            emptytext: display,
                            send: 'always',
                            validate: function (value) {
                                if (value === null || value === '' || value.length === 0) {
                                    $("#" + tableId).dataTable().fnFilter('', Math.max($("#" + tableId + " [name='filterColumnHeader']").index($(this).parent()), index));
                                }
                            },
                            display: function (value, sourceData) {

                            },
                            success: function (response, newValue) {
                                if (clientSide) {
                                    columnSearchValuesForClientSide[index] = newValue;
                                    var filterForFnFilter = "";//create the filter list that will be used by fnFilter
                                    for (var i in newValue) {
                                        filterForFnFilter += newValue[i] + "|";
                                    }
                                    filterForFnFilter = filterForFnFilter.slice(0, -1);
                                    $("#" + tableId).dataTable().fnFilter("(" + filterForFnFilter + ")", index, true);
                                } else {
                                    $("#" + tableId).dataTable().fnFilter(newValue, Math.max($("#" + tableId + " [name='filterColumnHeader']").index($(this).parent()), index));
                                }

                            }
                        });

                if (!oSettings.aoColumns[index].like || isEmpty(oSettings.aoColumns[index])) {
                    $(select).click(function (e) {
                        $(this).editable("setValue", allcolumnSearchValues[value], false)
                    })
                }
            }
            columnVisibleIndex++;
        }
    }); // end of loop on columns


    //Display the filtered alert message only if search is activated in at least 1 column
    //filteredInformation.pop();
    var focusOnNextSearchInputBool = false;
    if (filteredInformation.length > 1) {

        var filteredStringToDisplay = "";
        for (var l = 0; l < filteredInformation.length; l++) {
            filteredStringToDisplay += filteredInformation[l];
        }

        $("#" + tableId + "_wrapper #activatedFilters").html(filteredStringToDisplay);
        $("#" + tableId + "_wrapper #clearFilterButton").off("click").click(function () {
            if (clientSide) {
                columnSearchValuesForClientSide = [];//reset the search value when the user click on the clear all buton
            }
            resetFilters($("#" + tableId).dataTable(), undefined, true);
        });
        $("#" + tableId + "_wrapper #restoreFilterButton").click(function () {
            location.reload();
        });
        $("#" + tableId + "_wrapper #filterAlertDiv").show();
        focusOnNextSearchInputBool = true;
    }

    //call the displayColumnSearch when table configuration is changed
    $("#" + tableId + "_wrapper #showHideColumnsButton").click(function () {
        // FIX #1508, auto datatable to show/hide column display documentation (<a href= ....)
        // To fix it, i replace correct name of column on first click on show/hide button
        if (firstclickOnShowHide) {

            $(".dt-button.buttons-columnVisibility").each(function (index, value) {
                $(value).find("a").text(oSettings.aoColumns[index].nTh.innerText);
            });

            firstclickOnShowHide = false;
            // Important! Recharge screen with a double click on button to recalculate position of the box
            $("#" + tableId + "_wrapper #showHideColumnsButton").click();

        }
        // end FIX #1508
        $('ul[class="dt-button-collection dropdown-menu"] li').click(function () {
            privateDisplayColumnSearch(tableId, contentUrl, oSettings, clientSide);
        });
    });

    //To put into a function
    //When clicking on the edit filter links
    $("#" + tableId + "_wrapper .editable").click(function () {

        var currentValue = $(this).next().find(".popover-title").text() != "" ? $(this).next().find(".popover-title").text() : "undefined"
        //Clear custom fields to avoid duplication
        $("#" + tableId + "_wrapper [data-type='custom']").remove();

        if (allcolumnSearchValues[currentValue] === undefined) {
            if ($(this).find("span").size() < 2) {
                $("#" + tableId + '_wrapper .editable-checklist').find("input").prop('checked', true);
            } else {
                $(this).find("span").each(function () {
                    $("#" + tableId + '_wrapper .editable-checklist').find("input[value='" + $(this).text() + "']").prop('checked', true);
                });
            }
        }


        //Add an input field to search specific checkbox (search ignore case)

        $.extend($.expr[":"], {
            "containsIN": function (elem, i, match, array) {
                return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
            }
        });

        searchInput = $(this.parentNode).find("input[name='searchField']");

        searchInput.on('keyup', function () {
            var currentColumn = oSettings.aoColumns[$(this).attr('id').split('_')[1]]
            if (currentColumn.like == null || !currentColumn.like) {
                var allElement = $('#' + tableId + '_wrapper .editable-checklist > div')
                var elementsTocheck = $('#' + tableId + '_wrapper .editable-checklist > div:containsIN(' + $(this).val() + ')')
                //uncheck and hive all element
                allElement.find("[type='checkbox']").prop('checked', false);
                allElement.hide();
                //check and show element that need to be check

                if (allcolumnSearchValues[currentValue] != undefined) {
                    $.each(allcolumnSearchValues[currentValue], function (index, value) {
                        elementsTocheck.find("input[value='" + value + "']").prop('checked', true);
                    })
                } else {
                    elementsTocheck.find("[type='checkbox']").prop('checked', true);
                }
                elementsTocheck.show();
            }

        });

        searchInput.off('keydown'); // desactive old keydown handler
        searchInput.off('click');

        searchInput.keydown(function (e) {
            var keyCode = e.keyCode || e.which;

            if (keyCode === 9 || keyCode === 13) { //  TAB CHARACTER or ENTER CHARACTER
                lastSearchInput = $(this)[0];
                if (!isEmpty($(this).val())) {// if field is empty, don't submit
                    $(this).parent().parent().find(".editable-submit").click();
                    // if field is not empty, focus on next searchinput is done when filter is done. Don't do it here.
                } else {
                    focusOnNextSearchInput(lastSearchInput);
                }
                return false;
            }
        });

        $(searchInput).parent().parent().find(".editable-submit").click(function (e) {
            var currentColumn = oSettings.aoColumns[$(searchInput).attr('id').split('_')[1]]
            if (currentColumn.like != null && currentColumn.like) {
                var value = [$(searchInput).val()]
                $(searchInput).parent().editable("submit", value)
            }
        });

        searchInput.click(function (e) {
            if ($(this).parent().parent().find(".popover.editable-popup").length > 0) {
                return false;
            }
        });

        searchInput.focus();

        if ($(searchInput).length) {
            var currentColumn = oSettings.aoColumns[$(searchInput).attr('id').split('_')[1]]
            if (currentColumn.like == null || !currentColumn.like) {
                //Add selectAll/unSelectAll button
                $("#" + tableId + "_wrapper .popover-title").after(
                        $('<button>').attr('class', 'glyphicon glyphicon-check')
                        .attr('type', 'button')
                        .attr('title', 'select all').attr('name', 'selectAll')
                        .attr('data-type', 'custom').on('click', function () {
                    $(this).parent().parent().find("[type='checkbox']:visible").prop('checked', true);
                }));
                $("#" + tableId + "_wrapper .popover-title").after(
                        $('<button>').attr('class', 'glyphicon glyphicon-unchecked')
                        .attr('type', 'button')
                        .attr('title', 'unselect all').attr('name', 'unSelectAll')
                        .attr('data-type', 'custom').on('click', function () {
                    $(this).parent().parent().find("[type='checkbox']:visible").prop('checked', false);
                }));
            }

        }


    });

    if (focusOnNextSearchInputBool) {
        focusOnNextSearchInput(lastSearchInput);
    }
    resetTooltip(); // after filter loading, we remove all tootip
}

var lastSearchInput = null;
var handlerSearchInputClick = [];

function  focusOnNextSearchInput(startElement) {
    lastSearchInput = null;
    if (startElement === null)
        return;

    startElement = $("#" + startElement.id);

    if (startElement[0].name !== "searchField")
        return; // if start element is not an input "searchField", do nothing

    //get all text inputs
    var inputs = $('input[name="searchField"]');

    //search inputs for one that comes after starting element
    for (var i = 0; i < inputs.length; i++) {
        if (isAfter(inputs[i], startElement)) {
            var nextInput = inputs[i];
            if ($(':focus') == $(nextInput)) { // prevent infinite loop
                break;
            }
            $(nextInput).click();
            $(nextInput).click(); // double click is important !!!
            break;
        }
    }
}

//is element before or after
function isAfter(elA, elB) {
    return ($('*').index($(elA).last()) > $('*').index($(elB).first()));
}