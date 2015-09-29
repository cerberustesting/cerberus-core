/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

/*
 * Retrieves the plugin script that allows the generation of a loader.
 */
//$.getScript("js/jquery.blockUI.js");

function handleErrorAjaxAfterTimeout(result) {
    var doc = new Doc();

    if (result.readyState === 4 && result.status === 200) {
        $(location).prop("pathname", $(location).prop("pathname"));
        $(location).prop("search", $(location).prop("search"));
    } else {
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "unexpected_error_message"));
        showMessageMainPage(localMessage);
    }

}
/***
 * Returns a label depending on the type of entry
 * @param {type} type - type selected
 * @returns {String} - label associated with the type
 */
function getSubDataLabel(type) {
    var doc = getDoc();
    var docTestdatalibdata = doc.testdatalibdata;
    var labelEntry = "Entry";
    if (type === "STATIC") {
        labelEntry = displayDocLink(docTestdatalibdata.value);
    } else if (type === "SQL") {
        labelEntry = displayDocLink(docTestdatalibdata.column);
    } else if (type === "SOAP") {
        labelEntry = displayDocLink(docTestdatalibdata.parsingAnswer);
    }
    return labelEntry;
}

/*****INVARIANT LIST **********************************/
/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the invariant list
 * @param {String} idName value that filters the invariants that will be retrieved
 * @param {String} selectName value name of the select tag in the html
 * @returns {void}
 */
function displayInvariantList(idName, selectName) {
    $.when($.getJSON("FindInvariantByID", "idName=" + idName)).then(function (data) {
        for (var option in data) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value).val(data[option].value));
        }
    });
}

function displayInvariantListWithDesc(idName, selectName) {
    $.when($.getJSON("FindInvariantByID", "idName=" + idName)).then(function (data) {
        for (var option in data) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data[option].value + " - " + data[option].description).val(data[option].value));
        }
    });
}

/*****DEPLOYTYPE LIST **********************************/
/**
 * Method that display a combo box in all the selectName tags with the value retrieved from the DeployType list
 * @param {String} selectName value name of the select tag in the html
 * @returns {void}
 */
function displayDeployTypeList(selectName) {
    $.when($.getJSON("ReadDeployType", "")).then(function (data) {
        console.log(data);
        for (var option in data.contentTable) {
            $("[name='" + selectName + "']").append($('<option></option>').text(data.contentTable[option].deploytype + " - " + data.contentTable[option].description).val(data.contentTable[option].deploytype));
        }
    });
}

/**
 * Auxiliary method that retrieves a list containing the values that belong to the invariant that matches the provided idname.
 * @param {idName} idName value that filters the invariants that will be retrieved
 * @param {handleData} handleData method that handles the data retrieved
 */
function getInvariantList(idName, handleData) {
    $.when($.getJSON("GetInvariantList", "idName=" + idName)).then(function (data) {
        handleData(data);
    });
}
/**
 * Auxiliary method that retrieves set of invariants (based on a list of idnames), for each idname the method retrieves a list containing the associated values.
 * @param {list} list containing several idName values that filter the invariants that will be retrieved
 * @param {handleData} handleData method that handles the data retrieved 
 */
function getInvariantListN(list, handleData) {
    $.when($.post("GetInvariantList", {action: "getNInvariant", idName: JSON.stringify(list)}, "json")).then(function (data) {
        handleData(data);
    });
}
/***********************************************Messages/ALERT***************************************/
/**
 * Auxiliary method that gets the code returned by the service and translates it into the corresponding type of message
 * @param {type} code  string to be translated
 * @returns {String} the string used by the message to determine the type of message
 */
function getAlertType(code) {
    if (code === "OK") {
        return "success";
    } else if (code === "KO") {
        return "danger";
    } else if (code === "WARNING") {
        return "warning";
    }

    return code;
}

/**
 * Creates a message that should be presented to the user after the execution of an operation-
 * @param {type} messageType type of message (success, danger); it influes the color of the alert message
 * @param {type} message description of the message
 * @returns {Message} creates a object of a message
 */
function Message(messageType, message) {
    this.messageType = messageType;
    this.message = message;
}

/**
 * Clears the messages added in a dialog.
 * @param {type} dialog dialog where the messages are displayed
 */
function clearResponseMessage(dialog) {
    var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
    if (Boolean(elementAlert)) {
        elementAlert.fadeOut();
    }
}
/**
 * Clears the messages added in the main page.
 */
function clearResponseMessageMainPage() {
    $("#mainAlert").removeClass("alert-success");
    $("#mainAlert").removeClass("alert-danger");
    $("#alertDescription").html("");
    $("#mainAlert").fadeOut();
}
/**
 * Method that shows a message 
 * @param {type} obj - object containing the message and the message type
 * @param {type} dialog - dialog where the message should be displayed; if null then the message
 * is displayed in the main page.
 */
function showMessage(obj, dialog) {
    var code = getAlertType(obj.messageType);

    if (code !== "success" && dialog !== null) {
        //shows the error message in the current dialog    
        var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
        var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");

        elementAlertDescription.html(obj.message);
        elementAlert.addClass("alert-" + code);
        elementAlert.fadeIn();
    } else {
        //shows the message in the main page
        showMessageMainPage(code, obj.message);
    }

    /*if(dialog !== null && obj.messageType==="success"){
     jQuery(dialog).dialog('close');
     }*/
}
/**
 * Method that allows us to append a message in an already existing alert.
 * @param {type} obj  - object containing the message and the message type
 * @param {type} dialog - dialog where the message should be displayed; if null then the message
 * is displayed in the main page.
 */
function appendMessage(obj, dialog) {
    if (dialog !== null) {
        var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");
        elementAlertDescription.append("<br/>" + obj.message);
    } else {
        $("#alertDescription").append(obj.message);
    }
}

/***
 * Shows a message in the main page. The area is defined in the header.jsp
 * @param {type} type - type of message: success, info, ...
 * @param {type} message - message to show
 */
function showMessageMainPage(type, message) {
    $("#mainAlert").addClass("alert-" + type);
    $("#alertDescription").html(message);
    $("#mainAlert").fadeIn();
}
/*****************************************************************************/
$(function () {


    /*****************************************************************************/
    /**
     /*Closes the alert message that is visible in the main page
     */
    /*****************************************************************************/

    $("#buttonMainAlert").click(function () {
        var elementToClose = $(this).closest("." + $(this).attr("data-hide"));
        $(elementToClose).siblings("strong span[class='alert-description']").text("");
        $("#mainAlert").removeClass("alert-success");
        $("#mainAlert").removeClass("alert-danger");
        $(elementToClose).fadeOut();
    });



    /*****************************************************************************/
    /**
     /*Closes the alert page that is visible in the dialogs
     */
    /*****************************************************************************/
    $("[data-hide]").on("click", function () {
        var elementToClose = $(this).closest("." + $(this).attr("data-hide"));
        $(elementToClose).siblings("strong span[class='alert-description']").text("");
        $(elementToClose).parents("#mainAlert").removeClass("alert-success");
        $(elementToClose).parents("#mainAlert").removeClass("alert-danger");
        //$(this).closest("." + $(this).attr("data-hide")).hide();
        $(elementToClose).hide();
    });

    /*****************************************************************************/
    /**
     * Clears all the information from the modal that allows the upload of files
     */
    /*****************************************************************************/
    //resets the modal that allows the upload of files
    $('#modalUpload').on('hidden.bs.modal', function () {
        resetModalUpload();
    });
    //resets the confirmation modal data
    $('#confirmationModal').on('hidden.bs.modal', function () {
        resetConfirmationModal();
    });
});


/********************************LOADER*******************************************/
/**
 * Method that shows a loader inside a html element
 * @param {type} element 
 */
function showLoader(element) {
    var doc = new Doc();
    var processing = doc.getDocLabel("page_global", "processing");
    $(element).block({message: processing});
}
/**
 * Method that hides a loader that was specified in a modal dialog
 * @param {type} element
 */
function hideLoader(element) {
    $(element).unblock();
}
/**
 * Method that shows a loader inside the content of a modal dialog
 * @param {type} element dialog
 */
function showLoaderInModal(element) {
    var doc = new Doc();
    var processing = doc.getDocLabel("page_global", "processing");
    $(element).find(".modal-content").block({message: processing});
}
/**
 * Method that hides a loader that was specified in a modal dialog
 * @param {type} element dialog
 */
function hideLoaderInModal(element) {
    $(element).find(".modal-content").unblock();
}

/**
 * Method that reset form values from a modal
 * @param {type} event
 * @returns {void}
 */
function modalFormCleaner(event) {
    var modalID = event.data.extra;
    // reset form values
    $(modalID + " " + modalID + "Form")[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($(modalID));
}

/***********************************MODAL CONFIRMATION*************************************************/
/**
 * 
 * @param {type} handlerClickOk - method triggered when the "ok" is clicked
 * @param {type} title - title to be displayed
 * @param {type} message -  message to be displayed
 * @param {type} hiddenField -hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 * @returns {undefined}
 */
function showModalConfirmation(handlerClickOk, title, message, hiddenField) {
    setDataConfirmationModal(title, message, hiddenField);
    $('#confirmationModal #confirmOk').click(handlerClickOk);
    clearResponseMessageMainPage();
    $('#confirmationModal').modal('show');
}
/**
 * Method that cleans the confirmation modal after being closed.
 */
function resetConfirmationModal() {
    setDataConfirmationModal("", "", "");
    $('#confirmationModal #confirmOk').unbind('click');
}
/**
 * Method that allows the specification of a confirmation modal.
 * @param {type} title -  title to be displayed
 * @param {type} message -  message to be displayed
 * @param {type} hiddenField - hidden value that can be added to the confirmation modal. Useful when we want to delete an item, and we can specify it in this fiedl.
 */
function setDataConfirmationModal(title, message, hiddenField) {
    $('#confirmationModalLabel').html(title);
    $('#modalBody').html(message);
    if (hiddenField !== null) {
        $('#confirmationModal #hiddenField').prop("value", hiddenField);
    }
}

/**************************MODAL UPLOAD *********************************************/
/**
 * Auxiliary function that shows a modal dialog that allows the upload of files
 * @param {type} handlerClickOk / function that will be executed when the user clicks in the upload button
 * @param {type} fileExtension / extension of files that are allowed
 * @param {type} translations - the user can specify a new translations for the upload dialog.
 */
function showModalUpload(handlerClickOk, fileExtension, translations) {
    clearResponseMessageMainPage();
    //if translations are defined, then the title and buttons will be modified
    if (Boolean(translations)) {
        //update translations if a specific page secifies it     
        $.each(translations, function (index) {
            $("#" + index).text(translations[index]);
        });
    } else {
        //use the default translations (for the specific language)
        var doc = new Doc();
        $("#modalUploadLabel").text(doc.getDocLabel("modal_upload", "title"));
        $("#choseFileLabel").text(doc.getDocLabel("modal_upload", "btn_choose"));
        $("#cancelButton").text(doc.getDocLabel("modal_upload", "btn_cancel"));
        $("#uploadOk").text(doc.getDocLabel("modal_upload", "btn_upload"));
    }

    $('#modalUpload').modal('show');
    $('#modalUpload').find('#uploadOk').click(handlerClickOk);
    $('#modalUpload').find("#fileInput").change(function () {
        validatesFileExtension(this.value, fileExtension);
    });
}

/**
 * Auxiliary function that validates if a fileName has a valid extension
 * @param {type} fileName name to be validated
 * @param {type} fileExtension extension against with the name is validated
 */
function validatesFileExtension(fileName, fileExtension) {
    var ext = fileName.match(/^([^\\]*)\.(\w+)$/);

    if (ext !== null && ext[ext.length - 1].toUpperCase() === fileExtension.toUpperCase()) {
        clearResponseMessage($('#modalUpload'));
        $("#upload-file-info").html(fileName);
        $('#uploadOk').removeProp("disabled");
    } else {
        resetModalUpload();
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_global", "invalid_extension_message") + fileExtension + "!");
        showMessage(localMessage, $('#modalUpload'));
    }
}
/**
 * Method that resets the data entered in the modal used for uploading files.
 */
function resetModalUpload() {
    $('#modalUpload #fileInput').prop("value", "");
    $('#modalUpload #uploadOk').prop("disabled", "disabled");
    $('#modalUpload #uploadOk').unbind('click');

    $('#modalUpload #upload-file-info').text("");
    //gets the form and translates it in order to be uploadedshowModalUpload 
    $('#modalUpload #formUpload')[0].reset();
    clearResponseMessage($('#modalUpload'));
}

/********************************TABLES*******************************************/
/**
 * Allows the definition of a new ajax source for datatables
 * @param {type} oSettings settings 
 * @param {type} sNewSource new source
 */
$.fn.dataTableExt.oApi.fnNewAjax = function (oSettings, sNewSource) {
    if (typeof sNewSource !== 'undefined' && sNewSource !== null) {
        oSettings.sAjaxSource = sNewSource;
    }
    this.fnDraw();
}
/**
 * Auxiliary object that stores configurations that should be applied in a table that is client-side
 * @param {type} divId - table unique identifier
 * @param {type} data - data that is presented in the table
 * @param {type} aoColumnsFunction - function to render the columns
 * @returns {TableConfigurationsClientSide}
 */
function TableConfigurationsClientSide(divId, data, aoColumnsFunction) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.aaData = data;
    this.processing = false;
    this.serverSide = false;
    //not mandatory properties, and default values
    this.searchText = "";
    this.searchMenu = "";
    this.tableWidth = "1500px";
    this.displayLength = 10;
    this.bJQueryUI = true; //Enable jQuery UI ThemeRoller support (required as ThemeRoller requires some slightly different and additional mark-up from what DataTables has traditionally used
    this.paginate = true;
    this.paginationType = "full_numbers";
    //Enable or disable automatic column width calculation. This can be disabled as an optimisation (it takes some time to calculate the widths) if the tables widths are passed in using aoColumns.
    this.autoWidth = false;
    //Enable or disable state saving. When enabled a cookie will be used to save table display information such as pagination information, display length, filtering and sorting. As such when the end user reloads the page the display will match what they had previously set up
    this.stateSave = true;
    this.showColvis = true;
    this.scrollY = false;
    this.scrollCollapse = false;
    this.lang = getDataTableLanguage();
}

/**
 * Auxiliary object that stores configurations that should be applied in a table that is server-side
 * @param {type} divId - table unique identifier
 * @param {type} ajaxSource - ajax url
 * @param {type} ajaxProp -  json property 
 * @param {type} aoColumnsFunction - function to render the columns
 * @returns {TableConfigurationsServerSide}
 */
function TableConfigurationsServerSide(divId, ajaxSource, ajaxProp, aoColumnsFunction) {
    this.divId = divId;
    this.aoColumnsFunction = aoColumnsFunction;
    this.ajaxSource = ajaxSource;
    this.ajaxProp = ajaxProp;

    this.processing = true;
    this.serverSide = true;
    this.lengthMenu = [10, 25, 50, 100];
    //not mandatory properties, and default values
    this.searchText = "";
    this.searchMenu = "";
    this.tableWidth = "1500px";
    this.displayLength = 10;
    this.bJQueryUI = true; //Enable jQuery UI ThemeRoller support (required as ThemeRoller requires some slightly different and additional mark-up from what DataTables has traditionally used
    this.paginate = true;
    this.paginationType = "full_numbers";
    //Enable or disable automatic column width calculation. This can be disabled as an optimisation (it takes some time to calculate the widths) if the tables widths are passed in using aoColumns.
    this.autoWidth = false;
    //Enable or disable state saving. When enabled a cookie will be used to save table display information such as pagination information, display length, filtering and sorting. As such when the end user reloads the page the display will match what they had previously set up
    this.stateSave = true;
    this.showColvis = true;
    this.scrollY = false;
    this.scrollX = true;
    this.scrollCollapse = false;
    this.lang = getDataTableLanguage();
    this.orderClasses = true;
    this.bDeferRender = false;
}

function returnMessageHandler(response) {
    if (response.hasOwnProperty("messageType") && response.hasOwnProperty("message")) {
        if (response.messageType !== "OK") {
            var type = getAlertType(response.messageType);

            showMessageMainPage(type, response.message);
        }
    } else {
        showUnexpectedError();
    }
}

function showUnexpectedError() {
    var type = getAlertType("KO");
    var message = "ERROR - An unexpected error occured, the servlet may not be available";

    showMessageMainPage(type, message);
}

function createDataTableWithPermissions(tableConfigurations, callbackfunction) {
    var domConf = 'Cl<"showInlineElement pull-left marginLeft5"f>rti<"marginTop5"p>';
    if (!tableConfigurations.showColvis) {
        domConf = 'l<"showInlineElement pull-left marginLeft5"f>rti<"marginTop5"p>';
    }

    var configs = {};
    configs["dom"] = domConf;
    configs["serverSide"] = tableConfigurations.serverSide;
    configs["processing"] = tableConfigurations.processig;
    configs["bJQueryUI"] = tableConfigurations.bJQueryUI;
    configs["paging"] = tableConfigurations.paginate;
    configs["autoWidth"] = tableConfigurations.autoWidth;
    configs["pagingType"] = tableConfigurations.paginationType;
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
    configs["lengthChange"] = true;
    configs["orderClasses"] = tableConfigurations.orderClasses;
    configs["bDeferRender"] = tableConfigurations.bDeferRender;


    if (tableConfigurations.serverSide) {
        configs["sAjaxSource"] = tableConfigurations.ajaxSource;
        configs["sAjaxDataProp"] = tableConfigurations.ajaxProp;
        configs["fnServerData"] = function (sSource, aoData, fnCallback, oSettings) {
            oSettings.jqXHR = $.ajax({
                "dataType": 'json',
                "type": "GET",
                "url": sSource,
                "data": aoData,
                "success": function (json) {
                    returnMessageHandler(json);
                    fnCallback(json);
                },
                "error": function (e) {
                    showUnexpectedError();
                }
            });
            $.when(oSettings.jqXHR).then(function (data) {
                //updates the table with basis on the permissions that the current user has
                callbackfunction(data);
            });
        };
    } else {
        configs["data"] = tableConfigurations.aaData;
    }
    var oTable = $("#" + tableConfigurations.divId).dataTable(configs);
    //if is a server side table then we use a delay to avoid too many calls to the server
    if (tableConfigurations.serverSide) {
        oTable.dataTable().fnSetFilteringDelay(500);
    }

    if (tableConfigurations.showColvis) {
        $("#" + tableConfigurations.divId + "_wrapper div.ColVis .ColVis_MasterButton").addClass("btn btn-default");
    }
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").addClass("form-control input-sm");
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").css("display", "inline");

    $("#" + tableConfigurations.divId + "_filter input[type='search']").addClass("form-control form-control input-sm");

    $("#" + tableConfigurations.divId + "_length").addClass("marginBottom10").addClass("width80");
    $("#" + tableConfigurations.divId + "_filter").addClass("marginBottom10").addClass("width150");

    return oTable;
}

/***
 * Creates a datatable that is server-side processed.
 * @param {type} tableConfigurations set of configurations that define how data is retrieved and presented
 */
function createDataTable(tableConfigurations, callback) {
    var domConf = 'Cl<"showInlineElement pull-left marginLeft5"f>rti<"marginTop5"p>';
    if (!tableConfigurations.showColvis) {
        domConf = 'l<"showInlineElement pull-left marginLeft5"f>rti<"marginTop5"p>';
    }


    var configs = {};
    configs["dom"] = domConf;
    configs["serverSide"] = tableConfigurations.serverSide;
    configs["processing"] = tableConfigurations.processig;
    configs["bJQueryUI"] = tableConfigurations.bJQueryUI;
    configs["paging"] = tableConfigurations.paginate;
    configs["autoWidth"] = tableConfigurations.autoWidth;
    configs["pagingType"] = tableConfigurations.paginationType;
    configs["columnDefs.targets"] = [0];
    configs["pageLength"] = tableConfigurations.displayLength;
    configs["scrollX"] = tableConfigurations.scrollX;
    configs["scrollY"] = tableConfigurations.scrollY;
    configs["scrollCollapse"] = tableConfigurations.scrollCollapse;
    configs["stateSave"] = tableConfigurations.stateSave;
    configs["language"] = tableConfigurations.lang.table;
    configs["columns"] = tableConfigurations.aoColumnsFunction;
    configs["colVis"] = tableConfigurations.lang.colVis;
    configs["lengthChange"] = true;
    configs["lengthMenu"] = tableConfigurations.lengthMenu;
    configs["createdRow"] = callback;
    configs["orderClasses"] = tableConfigurations.orderClasses;
    configs["bDeferRender"] = tableConfigurations.bDeferRender;


    if (tableConfigurations.serverSide) {
        configs["sAjaxSource"] = tableConfigurations.ajaxSource;
        configs["sAjaxDataProp"] = tableConfigurations.ajaxProp;
        configs["fnServerData"] = function (sSource, aoData, fnCallback, oSettings) {
            oSettings.jqXHR = $.ajax({
                "dataType": 'json',
                "type": "GET",
                "url": sSource,
                "data": aoData,
                "success": function (json) {
                    returnMessageHandler(json);
                    fnCallback(json);
                },
                "error": function (e) {
                    showUnexpectedError();
                }
            });
        };
    } else {
        configs["aaData"] = tableConfigurations.aaData;
    }
    var oTable = $("#" + tableConfigurations.divId).dataTable(configs);
    //if is a server side table then we use a delay to avoid too many calls to the server
    if (tableConfigurations.serverSide) {
        oTable.dataTable().fnSetFilteringDelay(500);
    }
    if (tableConfigurations.showColvis) {
        $("#" + tableConfigurations.divId + "_wrapper .ColVis_MasterButton").addClass("btn btn-default");
    }
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").addClass("form-control input-sm");
    $("#" + tableConfigurations.divId + "_length select[name='" + tableConfigurations.divId + "_length']").css("display", "inline");

    $("#" + tableConfigurations.divId + "_filter input[type='search']").addClass("form-control form-control input-sm");

    $("#" + tableConfigurations.divId + "_length").addClass("marginBottom10").addClass("width80");
    $("#" + tableConfigurations.divId + "_filter").addClass("marginBottom10").addClass("width150");

    return oTable;
}

/**
 * Create the entry and display the message retrieved by the ajax call
 * @param {type} servletName
 * @param {type} form
 * @param {type} tableID
 * @returns {void}
 */
function createEntry(servletName, form, tableID) {
    var jqxhr = $.post(servletName, form.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#addEntryModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $(tableID).dataTable();
            oTable.fnDraw(true);
            showMessage(data);
            $("#addEntryModal").modal('hide');
        } else {
            showMessage(data, $("#addEntryModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * Update the entry and display the message retrieved by the ajax call (does not change pagination)
 * @param {type} servletName
 * @param {type} form
 * @param {type} tableID
 * @returns {void}
 */
function updateEntry(servletName, form, tableID) {
    var jqxhr = $.post(servletName, form.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal("#editEntryModal");
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $(tableID).dataTable();
            oTable.fnDraw(false);
            showMessage(data);
            $("#editEntryModal").modal('hide');
        } else {
            showMessage(data, $("#editEntryModal"));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

/**
 * This function is used to stop the propagtion of the click on the "?" anchor present on dataTables header so when we click on it, the sorting of the column doesn't change.
 * It should be called directly in the <a> tag with the onclick attribute
 * @param {type} event
 * @returns {undefined}
 */

function stopPropagation(event) {
    if (event.stopPropagation !== undefined) {
        event.stopPropagation();
    } else {
        event.cancelBubble = true;
    }
}

/**
 * Plugin used to delay the search filter; the purpose is to minimise the number of requests made to the server. Source: //cdn.datatables.net/plug-ins/1.10.7/api/fnSetFilteringDelay.js
 * @param {type} oSettings table settings 
 * @param {type} iDelay time to delay
 * @returns {jQuery.fn.dataTableExt.oApi}
 */
jQuery.fn.dataTableExt.oApi.fnSetFilteringDelay = function (oSettings, iDelay) {
    var _that = this;

    if (iDelay === undefined) {
        iDelay = 250;
    }

    this.each(function (i) {
        $.fn.dataTableExt.iApiIndex = i;
        var
                $this = this,
                oTimerId = null,
                sPreviousSearch = null,
                anControl = $('input', _that.fnSettings().aanFeatures.f);

        anControl.unbind('keyup search input').bind('keyup search input', function () {
            var $$this = $this;

            if (sPreviousSearch === null || sPreviousSearch != anControl.val()) {
                window.clearTimeout(oTimerId);
                sPreviousSearch = anControl.val();
                oTimerId = window.setTimeout(function () {
                    $.fn.dataTableExt.iApiIndex = i;
                    _that.fnFilter(anControl.val());
                }, iDelay);
            }
        });

        return this;
    });
    return this;
};
/*AUTOCOMPLETE*/
/**
 * Auxiliary function that sets the autocomplete option in one html element.
 * @param {type} selector
 * @param {type} source
 * @returns {undefined}
 */
function setAutoCompleteServerSide(selector, source) {
    var configurations = {};
    //sets the source of data
    configurations["source"] = source;
    //does not display the summary text
    configurations["messages"] = {
        noResults: '',
        results: function () {
        }
    };
    //specifies a delay to avoid excessive requests to the server
    configurations["delay"] = 500;
    //sets the autocomplete in the element
    $(selector).autocomplete(configurations);

}

/**
 * display global label
 * @returns {void}
 */
function displayGlobalLabel(doc) {
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "buttonAdd"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonConfirm']").html(doc.getDocLabel("page_global", "buttonConfirm"));
    $("[name='buttonDismiss']").html(doc.getDocLabel("page_global", "buttonDismiss"));
}

/**
 * generate and display the footer
 * @param {JavaScript Object} doc
 * @returns {void}
 */
function displayFooter(doc) {
    var cerberusInformation = getCerberusInformation();

    var footerString = doc.getDocLabel("page_global", "footer_text");
    var footerBugString = doc.getDocLabel("page_global", "footer_bug");
    var date = new Date();
    var loadTime = window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart;

    footerString = footerString.replace("%VERSION%", cerberusInformation.projectName + cerberusInformation.projectVersion);
    footerString = footerString.replace("%ENV%", cerberusInformation.environment);
    footerString = footerString.replace("%DATE%", date.toDateString());
    footerString = footerString.replace("%TIMING%", loadTime);
    footerBugString = footerBugString.replace("%LINK%", "https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + cerberusInformation.projectVersion);
    $("#footer").html(footerString + " - " + footerBugString);
}

/**
 * Get the parameter passed in the url Example : url?param=value
 * @param {type} sParam parameter you want to get value from
 * @returns {GetURLParameter.sParameterName} the value or null if not found
 */
function GetURLParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');

    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] === sParam)
        {
            return decodeURIComponent(sParameterName[1]);
        }
    }
    return null;
}

/**
 * Bind the toggle action to the panel body
 * @param {type} id of the panel body to be collapsed
 * @returns {void}
 */
function bindToggleCollapse(id) {
    $(id).on('shown.bs.collapse', function () {
        $(this).prev().find(".toggle").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right");
    });

    $(id).on('hidden.bs.collapse', function () {
        $(this).prev().find(".toggle").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
    });
}