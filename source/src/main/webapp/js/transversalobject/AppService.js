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

var imagePasteFromClipboard = undefined;//stock the picture if the user chose to upload it from his clipboard

function openModalAppService(service, mode, page = undefined) {
    if ($('#editSoapLibraryModal').data("initLabel") === undefined) {
        initModalAppService();
        $('#editSoapLibraryModal').data("initLabel", true);
    }

    $("#callSoapLibraryButton").off("click");
    $("#callSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler(mode, page, true);
    });

    reloadCollections();

    initBeforePerformCall();

    if (mode === "EDIT") {
        $('#callSoapLibraryButton').show();
        editAppServiceClick(service, page);

    } else if (mode === "ADD") {
        $('#callSoapLibraryButton').show();
        addAppServiceClick(service, page);

    } else {
        $('#callSoapLibraryButton').show();
        duplicateAppServiceClick(service);
    }

    bindToggleFullscreen();

}

function initModalAppService() {
    var doc = new Doc();

    displayInvariantList("type", "SRVTYPE", false, "REST", undefined, undefined, undefined, "editServiceModal");
    displayInvariantList("method", "SRVMETHOD", false, "GET", undefined, undefined, undefined, "editServiceModal");
    displayInvariantList("bodyType", "SRVBODYTYPE", false, "raw", undefined, undefined, undefined, "editServiceModal");
    displayAppServiceList("parentContentService", "", "");
    displayInvariantList("callSystem", "SYSTEM", false, undefined, undefined, undefined, undefined, "editServiceModal");
    displayInvariantList("authType", "AUTHTYPE", false, undefined, undefined, undefined, undefined, "editServiceModal");
    displayInvariantList("authAddTo", "AUTHADDTO", false, undefined, undefined, undefined, undefined, "editServiceModal");

    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));
    $("[name='buttonCall']").html(doc.getDocLabel("page_global", "buttonCall"));
    $("[name='addEntryField']").html(doc.getDocLabel("page_global", "btn_add"));
    $("[name='confirmationField']").html(doc.getDocLabel("page_global", "btn_delete"));
    $("[name='editEntryField']").html(doc.getDocLabel("page_global", "btn_edit"));
    $("[name='applicationField']").html(doc.getDocOnline("page_applicationObject", "Application"));
    $("[name='soapLibraryField']").html(doc.getDocLabel("appservice", "service"));
    $("[name='collectionField']").html(doc.getDocLabel("appservice", "collection"));
    $("[name='typeField']").html(doc.getDocLabel("appservice", "type"));
    $("[name='descriptionField']").html(doc.getDocLabel("appservice", "description"));
    $("[name='bodyTypeField']").html(doc.getDocOnline("appservice", "bodyType"));
    $("[name='servicePathField']").html(doc.getDocOnline("appservice", "servicePath"));
    $("[name='methodField']").html(doc.getDocLabel("appservice", "method"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_appservice", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
    $("#soapLibraryListLabel").html("<span class='glyphicon glyphicon-list'></span> " + doc.getDocLabel("appservice", "service"));
    $("[name='lbl_created']").html(doc.getDocOnline("transversal", "DateCreated"));
    $("[name='lbl_creator']").html(doc.getDocOnline("transversal", "UsrCreated"));
    $("[name='lbl_lastModified']").html(doc.getDocOnline("transversal", "DateModif"));
    $("[name='lbl_lastModifier']").html(doc.getDocOnline("transversal", "UsrModif"));


    /**
     * 
     * Trigger all autocomple events.
     * 
     */

    var configs = {
        'system': true,
        'object': false,
        'property': false,
        'identifier': "none"
    };

    initAutocompleteWithTags([$("[name='servicePath']")], configs, null);


    function modifyAutocomplete(e) {
//            console.log("modify feed autocomplete on input (generic).")
//            console.log("trigger settingsButton.");
        $(this).trigger("settingsButton");
    }


    function initAutocompleteElementHeader(e) {
//            console.log("start feed autocomplete on focus (element).");
        configs.identifier = "header";
        initAutocompleteWithTags($(this), configs, null);
    }

    function initAutocompleteElementHeaderValue(e) {
//            console.log("start feed autocomplete on focus (element).");
        configs.identifier = "headervalue";
        initAutocompleteWithTags($(this), configs, null);
    }

    function initAutocompleteElementVariable(e) {
//            console.log("start feed autocomplete on focus (element).");
        configs.identifier = "none";
        initAutocompleteWithTags($(this), configs, null);
    }

    // Adding Autocomplete on all fields. ##### crb-autocomplete-XXXX  #####
    $(document).on('focus', "input.crb-autocomplete-header:not([readonly])", initAutocompleteElementHeader);
    $(document).on('input', "input.crb-autocomplete-header:not([readonly])", modifyAutocomplete);

    $(document).on('focus', "textarea.crb-autocomplete-variable:not([readonly])", initAutocompleteElementVariable);
    $(document).on('input', "textarea.crb-autocomplete-variable:not([readonly])", modifyAutocomplete);
    $(document).on('focus', "input.crb-autocomplete-variable:not([readonly])", initAutocompleteElementVariable);
    $(document).on('input', "input.crb-autocomplete-variable:not([readonly])", modifyAutocomplete);

    $(document).on('focus', "textarea.crb-autocomplete-headervalue:not([readonly])", initAutocompleteElementHeaderValue);
    $(document).on('input', "textarea.crb-autocomplete-headervalue:not([readonly])", modifyAutocomplete);

    /**
     * 
     * End of autocomple configuration.
     * 
     */


    $('[data-toggle="popover"]').popover({
        'placement': 'auto',
        'container': 'body'
    }
    );

    srv_setUpDragAndDrop('#editSoapLibraryModal');

    var availablesearch = ["$.messageField1"];
    $('#editSoapLibraryModal').find("#kafkaFilterPath").autocomplete({
        source: availablesearch,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

    availablesearch = ["$.header.headerKey"];
    $('#editSoapLibraryModal').find("#kafkaFilterHeaderPath").autocomplete({
        source: availablesearch,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

}

/***
 * Open the modal with testcase information.
 * @param {String} service - type selected
 * @returns {null}
 */
function editAppServiceClick(service, page) {
    var doc = new Doc();
    $("[name='editSoapLibraryField']").html(doc.getDocLabel("page_appservice", "editSoapLibrary_field"));

    $("#editSoapLibraryButton").off("click");
    $("#editSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("EDIT", page);
    });

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#editSoapLibraryButton').removeProp('hidden');
    $('#duplicateSoapLibraryButton').attr('class', '');
    $('#duplicateSoapLibraryButton').attr('hidden', 'hidden');
    $('#addSoapLibraryButton').attr('class', '');
    $('#addSoapLibraryButton').attr('hidden', 'hidden');

    feedAppServiceModal(service, "editSoapLibraryModal", "EDIT");
    srv_listennerForInputTypeFile('#editSoapLibraryModal')
    srv_pasteListennerForClipboardPicture('#editSoapLibraryModal');
}

/***
 * Open the modal with testcase information.
 * @param {String} service - type selected
 * @returns {null}
 */
function duplicateAppServiceClick(service) {
    $("#duplicateSoapLibraryButton").off("click");
    $("#duplicateSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("DUPLICATE", undefined);
    });

    $('.nav-tabs a[href="#tabsSimul-4"]').tab('show');
    $('#resmessage').html("");
    $('#resmessage').removeClass("alert-danger");
    $('#resmessage').hide();

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#duplicateSoapLibraryButton').removeProp('hidden');
    $('#addSoapLibraryButton').attr('class', '');
    $('#addSoapLibraryButton').attr('hidden', 'hidden');

    feedAppServiceModal(service, "editSoapLibraryModal", "DUPLICATE");
    srv_listennerForInputTypeFile('#editSoapLibraryModal')
    srv_pasteListennerForClipboardPicture('#editSoapLibraryModal');
}

/***
 * Open the modal in order to create a new testcase.
 * @returns {null}
 */
function addAppServiceClick(service, page) {
    $("#addSoapLibraryButton").off("click");
    $("#addSoapLibraryButton").click(function () {
        confirmAppServiceModalHandler("ADD", page);
    });

    // Prepare all Events handler of the modal.
    prepareAppServiceModal();

    $('#editSoapLibraryButton').attr('class', '');
    $('#editSoapLibraryButton').attr('hidden', 'hidden');
    $('#duplicateSoapLibraryButton').attr('class', '');
    $('#duplicateSoapLibraryButton').attr('hidden', 'hidden');
    $('#addSoapLibraryButton').attr('class', 'btn btn-primary');
    $('#addSoapLibraryButton').removeProp('hidden');

    feedAppServiceModal(service, "editSoapLibraryModal", "ADD");
    srv_listennerForInputTypeFile('#editSoapLibraryModal')
    srv_pasteListennerForClipboardPicture('#editSoapLibraryModal');

}

/***
 * Function that initialise the modal with event handlers.
 * @returns {null}
 */
function prepareAppServiceModal() {

    // when type is changed we enable / disable type field.
    $("#editSoapLibraryModal #type").off("change");
    $("#editSoapLibraryModal #type").change(function () {
        updateMethod_and_refreshDisplayOnTypeChange();
//        refreshDisplayOnTypeChange();
    });

    $("#editSoapLibraryModal #isAvroEnable").off("change");
    $("#editSoapLibraryModal #isAvroEnable").change(function () {
        refreshDisplayOnTypeChange();
    });

    $("#editSoapLibraryModal #isAvroEnableKey").off("change");
    $("#editSoapLibraryModal #isAvroEnableKey").change(function () {
        refreshDisplayOnTypeChange();
    });

    $("#editSoapLibraryModal #isAvroEnableValue").off("change");
    $("#editSoapLibraryModal #isAvroEnableValue").change(function () {
        refreshDisplayOnTypeChange();
    });

    $("#editSoapLibraryModal #method").off("change");
    $("#editSoapLibraryModal #method").change(function () {
        refreshDisplayOnTypeChange();

    });

    $("#editSoapLibraryModal #bodyType").off("change");
    $("#editSoapLibraryModal #bodyType").change(function () {
        refreshDisplayOnTypeChange();

    });

    $("#editSoapLibraryModal #authType").off("change");
    $("#editSoapLibraryModal #authType").change(function () {
        refreshAuthorDisplayOnAnyChange();
    });

    // Adding rows in edit Modal.
    $('#addContent').off("click");
    $("#addContent").click(addNewContentRow);
    $('#addHeader').off("click");
    $("#addHeader").click(addNewHeaderRow);
    $('#addcallProp').off("click");
    $("#addcallProp").click(addNewCallPropRow);


}

function refreshAuthorDisplayOnAnyChange() {
    $('#editSoapLibraryModalForm #authUserSection').show();
    $('#editSoapLibraryModalForm #authKeyField').text("User");
    $('#editSoapLibraryModalForm #authValField').text("Password");
    switch ($('#editSoapLibraryModalForm #authType').val()) {
        case "none":
            $('#editSoapLibraryModalForm #authInfoSection').hide();
            $('#editSoapLibraryModalForm #authAddToSection').hide();
            break;
        case "API Key":
            $('#editSoapLibraryModalForm #authInfoSection').show();
            $('#editSoapLibraryModalForm #authKeyField').text("Key");
            $('#editSoapLibraryModalForm #authValField').text("Value");
            $('#editSoapLibraryModalForm #authAddToSection').show();
            break;
        case "Bearer Token":
            $('#editSoapLibraryModalForm #authInfoSection').show();
            $('#editSoapLibraryModalForm #authUserSection').hide();
            $('#editSoapLibraryModalForm #authValField').text("Token");
            $('#editSoapLibraryModalForm #authAddToSection').hide();
            break;
        case "Basic Auth":
            $('#editSoapLibraryModalForm #authInfoSection').show();
            $('#editSoapLibraryModalForm #authAddToSection').hide();
            break;
        default:
            break;
    }
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmAppServiceModalHandler(mode, page, doCall = false) {
    clearResponseMessage($('#editSoapLibraryModal'));
    var tempService = "";
    // If we make a call, we save the service to a temporary service name and clean it at the end.
    if (doCall) {
        mode = "ADD";
        tempService = "$TMP-" + generateUUID().substring(0, 18);
    }
    var serviceName = $('#editServiceModal #service').val();
    $('#editServiceModal #service').val($.trim(serviceName));
    serviceName = $('#editServiceModal #service').val();

    var formEdit = $('#editSoapLibraryModal #editSoapLibraryModalForm');

    initBeforePerformCall();

    showLoaderInModal('#editSoapLibraryModal');

    // Enable the test combo before submit the form.
//    if (mode === 'EDIT') {
//        formEdit.find("#service").removeAttr("disabled");
//    }

    // Calculate servlet name to call.
    var myServlet = "UpdateAppService";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateAppService";
    }

    // Get the header data from the form.
    var data = formEdit.serializeArray();
    data.servicePath = encodeURIComponent(data.servicePath);

    //Add envelope, not in the form
    var editorKey = ace.edit($("#editSoapLibraryModal #kfkKey")[0]);
    var editorRequest = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    var editorSchemaKey = ace.edit($("#editSoapLibraryModal #avrSchemaKey")[0]);
    var editorSchemaValue = ace.edit($("#editSoapLibraryModal #avrSchemaValue")[0]);

    // Getting Data from Content TAB
    var table1 = $("#contentTableBody tr");
    var table_content = [];
    for (var i = 0; i < table1.length; i++) {
        table_content.push($(table1[i]).data("content"));
    }
    // Getting Data from Header TAB
    var table2 = $("#headerTableBody tr");
    var table_header = [];
    for (var i = 0; i < table2.length; i++) {
        table_header.push($(table2[i]).data("header"));
    }

    var formData = new FormData();
    var file = $("#editSoapLibraryModal input[type=file]");

    for (var i in data) {
        if ((doCall) && (data[i].name === "service")) {
            formData.append(data[i].name, encodeURIComponent(tempService));
        } else {
            formData.append(data[i].name, encodeURIComponent(data[i].value));
        }
    }
    formData.append("contentList", JSON.stringify(table_content));
    formData.append("headerList", JSON.stringify(table_header));
    formData.append("kafkaKey", encodeURIComponent(editorKey.getSession().getDocument().getValue()));
    formData.append("srvRequest", encodeURIComponent(editorRequest.getSession().getDocument().getValue()));
    formData.append("avrSchemaKey", encodeURIComponent(editorSchemaKey.getSession().getDocument().getValue()));
    formData.append("avrSchemaValue", encodeURIComponent(editorSchemaValue.getSession().getDocument().getValue()));

    if (file.prop("files").length != 0) {
        formData.append("file", file.prop("files")[0]);
    }

    if (isEmpty(formData.get("isFollowRedir"))) {
        formData.append("isFollowRedir", 0);
    }
    if (isEmpty(formData.get("isAvroEnable"))) {
        formData.append("isAvroEnable", 0);
    }
    if (isEmpty(formData.get("isAvroEnableKey"))) {
        formData.append("isAvroEnableKey", 0);
    }
    if (isEmpty(formData.get("isAvroEnableValue"))) {
        formData.append("isAvroEnableValue", 0);
    }

    let callCountry = $('#callCountry').val();
    let callSystem = $('#callSystem').val();
    let callEnv = $('#callEnv').val();

    formData.append("callCountry", callCountry);
    formData.append("callEnvironment", callEnv);
    formData.append("callSystem", callSystem);

    let callData = getCallParam();
    formData.append("callInfo", JSON.stringify(callData));

    var temp = data.service;

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {

            hideLoaderInModal('#editSoapLibraryModal');
//            console.info("hide loader");

            // Update the original Service value.
//            console.info($('#editServiceModal #service').val());
            $('#editSoapLibraryModal #originalService').prop("value", $('#editServiceModal #service').val());

            data = JSON.parse(data);

            if (getAlertType(data.messageType) === "success") {
                if (doCall) {

                    performCall(tempService);

                } else {
                    if (page === "TestCase") {
                        var Tags = getTags();
                        for (var i = 0; i < Tags.length; i++) {
                            if (Tags[i].regex == null) {
                                Tags[i].array.push(temp);
                            }
                        }
                        $("." + temp).parent().find("input").trigger("input", ['first']);
                    } else {
                        var oTable = $("#soapLibrarysTable").dataTable();
                        oTable.fnDraw(false);
                    }
                    $('#editSoapLibraryModal').data("Saved", true);
                    $('#editSoapLibraryModal').modal('hide');
                    showMessage(data);
                }
            } else {
                showMessage(data, $('#editSoapLibraryModal'));
            }

        },
        error: showUnexpectedError
    });
//    if (mode === 'EDIT') { // Disable back the test combo before submit the form.
//        formEdit.find("#service").prop("disabled", "disabled");
//    }

}

function reloadCollections() {

    var availableCollection = getCollectionArray(true);
    $('#editServiceModal input#collection').autocomplete({
        source: availableCollection,
        minLength: 0,
        messages: {
            noResults: '',
            results: function (amount) {
                return '';
            }
        }
    }).on("focus", function () {
        $(this).autocomplete("search", "");
    });

}

function getCallParam() {
    let callCountry = $('#callCountry').val();
    let callSystem = $('#callSystem').val();
    let callEnv = $('#callEnv').val();
    let callApp = $('#editSoapLibraryModalForm #application').val();
    let callTimeout = $('#callTimeout').val();
    let callKafkaNb = $('#callKafkaNb').val();
    let callKafkaTime = $('#callKafkaTime').val();
    // Getting Data from Header TAB
    var table3 = $("#callPropTableBody tr");
    var table_callProp = [];
    for (var i = 0; i < table3.length; i++) {
        table_callProp.push($(table3[i]).data("callProp"));
    }
    let dataCall = {
        'country': callCountry,
        'system': callSystem,
        'environment': callEnv,
        'application': callApp,
        'timeout': callTimeout,
        'kafkanb': callKafkaNb,
        'kafkaTime': callKafkaTime,
        'props': table_callProp
    };

    return dataCall;
}

function performCall(service) {

//    console.info("now CALL " + service);

//    console.info(dataCall);

// TODO Show waithing icon.
//    console.info("show");

    $('#callStatus').show();
    $('#callStatus').removeClass("spin glyphicon-minus").removeClass("glyphicon-ok").removeClass("glyphicon-remove").addClass("spin glyphicon-refresh");
    $('#callStatusCode').html("");


    let callData = getCallParam();

    $.ajax({
        url: "api/public/services/call/" + encodeURIComponent(service),
        async: false,
        method: "POST",
        headers: {'X-API-VERSION': 1},
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(callData),
//        processData: false,
//        contentType: false,
        success: function (data) {

            let CallContent = JSON.parse(data.data);

//            console.info(CallContent);
            $('#resmessage').html(CallContent.message);
            if (CallContent.messageCode === "OK") {
                $('#resmessage').addClass("alert-info");
            } else {
                $('#resmessage').addClass("alert-danger");
            }
            $('#resmessage').show();



            if ((CallContent.messageCode !== "OK")) {

                // Show Parameters and Message Tab
                $('.nav-tabs a[href="#tabsSimul-4"]').tab('show');

                $('#callStatus').removeClass("spin glyphicon-refresh").addClass("glyphicon-remove");

            } else {

                $('#callStatus').removeClass("spin glyphicon-refresh").addClass("glyphicon-ok");

            }



            if (CallContent.hasOwnProperty('call')) {


                if (CallContent.call.hasOwnProperty('Response')) {

                    // Response code and duration
                    if ((CallContent.call.Response.hasOwnProperty('HTTP-ReturnCode')) && (CallContent.call.Response["HTTP-ReturnCode"] > 0)) {
                        if ((CallContent.call.Response["HTTP-ReturnCode"] < 300)) {
                            $('#callStatusCode').attr('style', 'font-size: small; text-align: center; background-color: rgb(0,210,122);');
                        } else if ((CallContent.call.Response["HTTP-ReturnCode"] < 500)) {
                            $('#callStatusCode').attr('style', 'font-size: small; text-align: center; background-color: rgb(245,128,62);');
                        } else {
                            $('#callStatusCode').attr('style', 'font-size: small; text-align: center; background-color: rgb(230,55,87);');
                        }
                        $('#callStatusCode').html(CallContent.call.Response["HTTP-ReturnCode"]);
                        $('#callStatusCode').show();
                        $('#callStatus').hide();
                    } else {
                        $('#callStatus').show();
                    }
                    if ((CallContent.call.Response.timings.hasOwnProperty('durationMs')) && (CallContent.call.Response.timings.durationMs > 0)) {
                        $('#callTiming').html(CallContent.call.Response.timings.durationMs + " ms");
                        $('#callTiming').show();
                    }


                    // Response detail
                    ace.edit($("#editSoapLibraryModal #srvResponseDet")[0]).destroy();
                    $('#editSoapLibraryModal  #srvResponseDet').text(JSON.stringify(CallContent.call.Response, null, '\t'));
                    //Highlight envelop on modal loading
                    var editorResponseDet = ace.edit($('#editSoapLibraryModal  #srvResponseDet')[0]);
                    editorResponseDet.setTheme("ace/theme/chrome");
                    editorResponseDet.getSession().setMode(defineAceMode(editorResponseDet.getSession().getDocument().getValue()));
                    editorResponseDet.setOptions({
                        maxLines: 50
                    });
                    document.getElementById('srvResponseDet').style.fontSize = '14px';
                    editorResponseDet.setReadOnly(true);
                    $($("#editSoapLibraryModal #srvResponseDet").get(0)).keyup(function () {
                        if (editorResponseDet.getSession().getMode().$id === "ace/mode/text") {
                            editorResponseDet.getSession().setMode(defineAceMode(editorResponseDet.getSession().getDocument().getValue()));
                        }
                    });

                    $('#htmlDisplay').hide();
                    document.getElementById("htmlDisplay").innerHTML = "";

                    if (CallContent.call.Response.hasOwnProperty('HTTP-ResponseBody')) {
                        // Response
                        ace.edit($("#editSoapLibraryModal #srvResponse")[0]).destroy();
                        if (CallContent.call.Response["HTTP-ResponseContentType"] === "JSON") {
                            $('#editSoapLibraryModal  #srvResponse').text(JSON.stringify(CallContent.call.Response["HTTP-ResponseBody"], null, '\t'));
                        } else if (CallContent.call.Response["HTTP-ResponseContentType"] === "XML") {
                            $('#editSoapLibraryModal  #srvResponse').text(vkbeautify.xml(CallContent.call.Response["HTTP-ResponseBody"]));
                        } else if (CallContent.call.Response["HTTP-ResponseContentType"] === "HTML") {
                            $('#editSoapLibraryModal  #srvResponse').text(CallContent.call.Response["HTTP-ResponseBody"].replace(/<[^>]*>?/gm, ''));
                            // TODO SECURE that HTML does not change the page layout
//                            document.getElementById("htmlDisplay").innerHTML = CallContent.call.Response["HTTP-ResponseBody"];
//                            $('#htmlDisplay').show();
                        } else {
                            $('#editSoapLibraryModal  #srvResponse').text(CallContent.call.Response["HTTP-ResponseBody"]);
                        }
                        //Highlight envelop on modal loading
                        var editorResponse = ace.edit($('#editSoapLibraryModal  #srvResponse')[0]);
                        editorResponse.setTheme("ace/theme/chrome");
                        editorResponse.getSession().setMode(defineAceMode(editorResponse.getSession().getDocument().getValue()));
                        editorResponse.setOptions({
                            maxLines: 50
                        });
                        document.getElementById('srvResponse').style.fontSize = '14px';
                        editorResponse.setReadOnly(true);
                        $($("#editSoapLibraryModal #srvResponse").get(0)).keyup(function () {
                            if (editorResponse.getSession().getMode().$id === "ace/mode/text") {
                                editorResponse.getSession().setMode(defineAceMode(editorResponse.getSession().getDocument().getValue()));
                            }
                        });

                        // Show Response Tab
                        $('.nav-tabs a[href="#tabsSimul-1"]').tab('show');

                    } else {

                        // Show Parameters and Message Tab
                        $('.nav-tabs a[href="#tabsSimul-4"]').tab('show');

                    }

                } else {

                    // Show Parameters and Message Tab
                    $('.nav-tabs a[href="#tabsSimul-4"]').tab('show');

                }

                if (CallContent.call.hasOwnProperty('Request')) {

                    // Request detail
                    ace.edit($("#editSoapLibraryModal #srvRequestDet")[0]).destroy();
                    $('#editSoapLibraryModal  #srvRequestDet').text(JSON.stringify(CallContent.call.Request, null, '\t'));
                    //Highlight envelop on modal loading
                    var editorRequestDet = ace.edit($('#editSoapLibraryModal  #srvRequestDet')[0]);
                    editorRequestDet.setTheme("ace/theme/chrome");
                    editorRequestDet.getSession().setMode(defineAceMode(editorRequestDet.getSession().getDocument().getValue()));
                    editorRequestDet.setOptions({
                        maxLines: 50
                    });
                    document.getElementById('srvRequestDet').style.fontSize = '14px';
                    editorRequestDet.setReadOnly(true);
                    $($("#editSoapLibraryModal #srvRequestDet").get(0)).keyup(function () {
                        if (editorRequestDet.getSession().getMode().$id === "ace/mode/text") {
                            editorRequestDet.getSession().setMode(defineAceMode(editorRequestDet.getSession().getDocument().getValue()));
                        }
                    });

                    // Request
                    ace.edit($("#editSoapLibraryModal #srvCallRequest")[0]).destroy();
                    if (CallContent.call.Request.ServiceType === "SOAP") {
                        $('#editSoapLibraryModal  #srvCallRequest').text(CallContent.call.Request["HTTP-Request"], null, '\t');
                    } else {
                        $('#editSoapLibraryModal  #srvCallRequest').text(CallContent.call.Request["HTTP-Request"], null, '\t');
                    }
                    //Highlight envelop on modal loading
                    var editorRequestSrvCall = ace.edit($('#editSoapLibraryModal  #srvCallRequest')[0]);
                    editorRequestSrvCall.setTheme("ace/theme/chrome");
                    editorRequestSrvCall.getSession().setMode(defineAceMode(editorRequestSrvCall.getSession().getDocument().getValue()));
                    editorRequestSrvCall.setOptions({
                        maxLines: 50
                    });
                    document.getElementById('srvCallRequest').style.fontSize = '14px';
                    editorRequestSrvCall.setReadOnly(true);
                    $($("#editSoapLibraryModal #srvCallRequest").get(0)).keyup(function () {
                        if (editorRequestSrvCall.getSession().getMode().$id === "ace/mode/text") {
                            editorRequestSrvCall.getSession().setMode(defineAceMode(editorRequestSrvCall.getSession().getDocument().getValue()));
                        }
                    });

                }
            } else {

                // Show Parameters and Message Tab
                $('.nav-tabs a[href="#tabsSimul-4"]').tab('show');


            }

//            console.info("hide");
//            $('#callStatus').removeClass("spin glyphicon-refresh").addClass("glyphicon-ok").addClass("glyphicon-remove");
            // TODO Hide  waiting icon.

        },
        error: showUnexpectedError
    });

}


function initBeforePerformCall() {

    $('#editSoapLibraryModal').find("#srvResponse").text("");

    $('#editSoapLibraryModal').find("#srvResponseDet").text("");

    $('#editSoapLibraryModal').find("#srvCallRequest").text("");

    $('#editSoapLibraryModal').find("#srvRequestDet").text("");

    $('#resmessage').html("");
    $('#resmessage').removeClass("alert-danger");
    $('#resmessage').hide();

    $('#callStatus').hide();

    $('#callStatusCode').html("");
    $('#callStatusCode').hide();

    $('#callTiming').hide();


    document.getElementById("htmlDisplay").innerHTML = " ";
    $('#htmlDisplay').hide();

}


function refreshDisplayOnTypeChange() {

    let newValueMethod = $("#editSoapLibraryModal #method").val();
    let newValueType = $("#editSoapLibraryModal #type").val();
    let newValueBodyType = $("#editSoapLibraryModal #bodyType").val();

    $('#editSoapLibraryModal #typeLogo').attr('src', './images/logo-' + newValueType + '.png').attr('alt', newValueType);

    $("#editSoapLibraryModal #tabRequest").show();
    $("#editSoapLibraryModal #tabRequestDetail").show();
    $("#editSoapLibraryModal #tabHeader").show();
    $("#editSoapLibraryModal #srvRequest").parent().parent().find("label").html("Service Request");
    $("label[name='operationField']").html("Operation");
    $("#editSoapLibraryModal #tabAuthor").hide();

    if (newValueType === "SOAP") {
        // If SOAP service, no need to feed the method.
        $('.upload-drop-zone').hide();
        $("label[name='screenshotfilenameField']").hide();
        $("label[name='operationField']").parent().show();
//        $("input[name='operation']").show();
        $("label[name='attachementurlField']").parent().show();
//        $("input[name='attachementurl']").show();
        $('#editSoapLibraryModal #method').prop("disabled", true);
        $('#editSoapLibraryModal #bodyType').parent().hide();
        $('#editSoapLibraryModal #addContent').prop("disabled", true);
        $('#editSoapLibraryModal #addHeader').prop("disabled", false);
        $("label[name='kafkaTopicField']").parent().hide();
        $("label[name='kafkaKeyField']").parent().hide();
        $("label[name='kafkaFilterField']").hide();
        $("#editSoapLibraryModal #kafkaFilter").hide();
        $("#editSoapLibraryModal #tabFilters").hide();
        $("label[name='avroField']").hide();
        $("#editSoapLibraryModal #avro").hide();
        $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
        $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
        $("label[name='isFollowRedirField']").parent().hide();
        $('#editSoapLibraryModal #tabRequestDetail').text("Request Detail");
        $("#editSoapLibraryModal #tabRequestDetail").hide();

    } else if (newValueType === "FTP") {
        $('#editSoapLibraryModal #method').prop("disabled", false);
        $('#editSoapLibraryModal #method option[value="DELETE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PUT"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PATCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="GET"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="POST"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="SEARCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PRODUCE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="FIND"]').css("display", "none");
        $('#editSoapLibraryModal #bodyType').parent().hide();
        $('#editSoapLibraryModal #addContent').prop("disabled", true);
        $('#editSoapLibraryModal #addHeader').prop("disabled", true);
        $('.upload-drop-zone').show();
        $("label[name='screenshotfilenameField']").show();
        $("label[name='operationField']").parent().hide();
//        $("input[name='operation']").hide();
        $("label[name='attachementurlField']").parent().hide();
//        $("input[name='attachementurl']").hide();
        $("label[name='kafkaTopicField']").parent().hide();
        $("label[name='kafkaKeyField']").parent().hide();
        $("label[name='kafkaFilterField']").hide();
        $("#editSoapLibraryModal #kafkaFilter").hide();
        $("#editSoapLibraryModal #tabFilters").hide();
        $("label[name='avroField']").hide();
        $("#editSoapLibraryModal #avro").hide();
        $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
        $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
        $("label[name='isFollowRedirField']").parent().hide();
        $('#editSoapLibraryModal #tabRequestDetail').text("Request Detail");
        if (newValueMethod == "GET") {
            $("#editSoapLibraryModal #srvRequestDiv").hide();
        } else {
            $("#editSoapLibraryModal #srvRequest").parent().parent().find("label").html("File Content");
            $("#editSoapLibraryModal #srvRequestDiv").show();
        }
        $("#editSoapLibraryModal #tabHeader").hide();
        $("#editSoapLibraryModal #tabRequestDetail").hide();

    } else if (newValueType === "KAFKA") {
        $("#editSoapLibraryModal #srvRequest").parent().parent().find("label").html("Kafka Value");

        $('#editSoapLibraryModal #method').prop("disabled", false);
        $('#editSoapLibraryModal #method option[value="DELETE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PUT"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PATCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="GET"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="POST"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="SEARCH"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="PRODUCE"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="FIND"]').css("display", "none");
        $('#editSoapLibraryModal #bodyType').parent().hide();
        $('#editSoapLibraryModal #addContent').prop("disabled", false);
        $('#editSoapLibraryModal #addHeader').prop("disabled", false);
        $('.upload-drop-zone').hide();
        $("label[name='screenshotfilenameField']").hide();
        $("label[name='operationField']").parent().hide();
//        $("input[name='operation']").hide();
        $("label[name='attachementurlField']").parent().hide();
//        $("input[name='attachementurl']").hide();
        $("label[name='kafkaTopicField']").parent().show();
        $("label[name='kafkaKeyField']").parent().show();
        $("#editSoapLibraryModal #kafkaFilter").show();
        $("label[name='kafkaFilterField']").show();
        $("#editSoapLibraryModal #tabFilters").show();
        $("label[name='avroField']").show();
        $("#editSoapLibraryModal #avro").show();
        if (newValueMethod === "SEARCH") {
            $("#editSoapLibraryModal #tabRequest").hide();
            $("#editSoapLibraryModal #kafkaFilter").show();
            $("label[name='kafkaFilterField']").show();
            $("#editSoapLibraryModal #tabFilters").show();
        } else {
            $("#editSoapLibraryModal #tabRequest").show();
            $("#editSoapLibraryModal #kafkaFilter").hide();
            $("label[name='kafkaFilterField']").hide();
            $("#editSoapLibraryModal #tabFilters").hide();
        }
        isAvro = $("#editSoapLibraryModal #isAvroEnable")[0].checked;
        if (isAvro) {
            $("#editSoapLibraryModal #schemaRegistryUrl").removeAttr("readonly");
            $("#editSoapLibraryModal #isAvroEnableValue").removeAttr("disabled");
            $("#editSoapLibraryModal #isAvroEnableKey").removeAttr("disabled");
            if ($("#editSoapLibraryModal #isAvroEnableKey")[0].checked) {
                $("#editSoapLibraryModal #avrSchemaKeyDiv").show();
            } else {
                $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
            }
            if ($("#editSoapLibraryModal #isAvroEnableValue")[0].checked) {
                $("#editSoapLibraryModal #avrSchemaValueDiv").show();
            } else {
                $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
            }
        } else {
            $("#editSoapLibraryModal #schemaRegistryUrl").prop("readonly", "true");
            $("#editSoapLibraryModal #isAvroEnableValue").prop("disabled", "true");
            $("#editSoapLibraryModal #isAvroEnableKey").prop("disabled", "true");
            $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
            $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
        }
        $("label[name='isFollowRedirField']").parent().hide();
        $('#editSoapLibraryModal #tabRequestDetail').text("KAFKA Props");

    } else if (newValueType === "MONGODB") {
        $("#editSoapLibraryModal #srvRequest").parent().parent().find("label").html("MongoDB Query");

        $('#editSoapLibraryModal #method').prop("disabled", false);
        $('#editSoapLibraryModal #method option[value="DELETE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PUT"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PATCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="GET"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="POST"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="SEARCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PRODUCE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="FIND"]').css("display", "block");
        $('#editSoapLibraryModal #bodyType').parent().hide();
        $('#editSoapLibraryModal #addContent').prop("disabled", true);
        $('#editSoapLibraryModal #addHeader').prop("disabled", true);
        $('.upload-drop-zone').hide();
        $("#editSoapLibraryModal #tabRequest").show();
        $("#editSoapLibraryModal #tabRequestDetail").hide();
        $("#editSoapLibraryModal #tabHeader").hide();
        $("label[name='screenshotfilenameField']").hide();
        $("label[name='operationField']").parent().show();
        $("label[name='operationField']").html("Database.Collection");
//        $("input[name='operation']").hide();
        $("label[name='attachementurlField']").parent().hide();
//        $("input[name='attachementurl']").hide();
        $("label[name='kafkaTopicField']").parent().hide();
        $("label[name='kafkaKeyField']").parent().hide();
        $("label[name='kafkaFilterField']").hide();
        $("#editSoapLibraryModal #kafkaFilter").hide();
        $("#editSoapLibraryModal #tabFilters").hide();
        $("label[name='avroField']").hide();
        $("#editSoapLibraryModal #avro").hide();
        $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
        $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
        $("label[name='isFollowRedirField']").parent().hide();
        $('#editSoapLibraryModal #tabRequestDetail').text("Request Detail");

    } else { // REST
        $("#editSoapLibraryModal #tabAuthor").show();
        $('#editSoapLibraryModal #method').prop("disabled", false);
        $('#editSoapLibraryModal #method option[value="DELETE"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="PUT"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="PATCH"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="GET"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="POST"]').css("display", "block");
        $('#editSoapLibraryModal #method option[value="SEARCH"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="PRODUCE"]').css("display", "none");
        $('#editSoapLibraryModal #method option[value="FIND"]').css("display", "none");
        $('#editSoapLibraryModal #bodyType').parent().show();
        $('#editSoapLibraryModal #addContent').prop("disabled", false);
        $('#editSoapLibraryModal #addHeader').prop("disabled", false);
        $('.upload-drop-zone').hide();
        $("label[name='screenshotfilenameField']").hide();
        $("label[name='operationField']").parent().hide();
//        $("input[name='operation']").hide();
        $("label[name='attachementurlField']").parent().hide();
//        $("input[name='attachementurl']").hide();
        $("label[name='kafkaTopicField']").parent().hide();
        $("label[name='kafkaKeyField']").parent().hide();
        $("label[name='kafkaFilterField']").hide();
        $("#editSoapLibraryModal #kafkaFilter").hide();
        $("#editSoapLibraryModal #tabFilters").hide();
        $("label[name='avroField']").hide();
        $("#editSoapLibraryModal #avro").hide();
        $("#editSoapLibraryModal #avrSchemaKeyDiv").hide();
        $("#editSoapLibraryModal #avrSchemaValueDiv").hide();
        $("label[name='isFollowRedirField']").parent().show();
        if ((newValueBodyType === "form-data") || (newValueBodyType === "form-urlencoded")) {
            //  form-data form-urlencoded
            $("#editSoapLibraryModal #tabRequest").hide();
            $("#editSoapLibraryModal #tabRequestDetail").show();
        } else if (newValueBodyType === "raw") {
            // raw
            $("#editSoapLibraryModal #tabRequest").show();
            $("#editSoapLibraryModal #tabRequestDetail").hide();
        } else {
            // none
            $("#editSoapLibraryModal #tabRequest").hide();
            $("#editSoapLibraryModal #tabRequestDetail").hide();
        }
        $('#editSoapLibraryModal #tabRequestDetail').text("Request Detail");
    }
}

// if couple Type / Method is not consistent, we force it to be.
function updateMethod_and_refreshDisplayOnTypeChange() {
    let lMethod = $('#editSoapLibraryModal #method').prop("value");
    let lType = $('#editSoapLibraryModal #type').prop("value");

    if (lType === "SOAP") {
        if (lMethod !== "") {
            $('#editSoapLibraryModal #method').prop("value", "");
        }
    } else if (lType === "FTP") {
        if ((lMethod !== "GET") && (lMethod !== "POST")) {
            $('#editSoapLibraryModal #method').prop("value", "GET");
        }

    } else if (lType === "KAFKA") {
        if ((lMethod !== "PRODUCE") && (lMethod !== "SEARCH")) {
            $('#editSoapLibraryModal #method').prop("value", "SEARCH");
        }

    } else if (lType === "MONGODB") {
        if (lMethod !== "FIND") {
            $('#editSoapLibraryModal #method').prop("value", "FIND");
        }

    } else {
        if ((lMethod !== "GET") && (lMethod !== "POST") && (lMethod !== "DELETE") && (lMethod !== "PUT") && (lMethod !== "PATCH")) {
            $('#editSoapLibraryModal #method').prop("value", "GET");
        }

    }
    refreshDisplayOnTypeChange();
}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} serviceName - type selected
 * @param {String} modalId - type selected
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedAppServiceModal(serviceName, modalId, mode) {
    clearResponseMessageMainPage();
    var formEdit = $('#' + modalId);

    if (mode === "DUPLICATE" || mode === "EDIT") {
        $.ajax({
            url: "ReadAppService",
            async: true,
            method: "POST",
            data: {
                service: serviceName
            },
            success: function (data) {
                if (data.messageType === "OK") {

                    // Feed the data to the screen and manage authorities.
                    var service = data.contentTable;
                    feedAppServiceModalData(service, modalId, mode, service.hasPermissions, data.extraInformation);

                    // Force a change event on method field.
                    refreshDisplayOnTypeChange();

                    // Force a change event on author field.
                    refreshAuthorDisplayOnAnyChange();

                    //initialize the select2
                    $('#editSoapLibraryModal #application').select2();
                    // set it with the service value
                    $("#editSoapLibraryModal #application").val(service.application).trigger('change');

                    //initialize the select2
                    $('#editSoapLibraryModal #parentContentService').select2(getComboConfigAppServiceList());
                    // set it with the service value
                    $("#editSoapLibraryModal #parentContentService").val(service.parentContentService).trigger('change');

                    formEdit.modal('show');
                } else {
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });

    } else {
        var serviceObj1 = {};
        var hasPermissions = true;
        serviceObj1.service = serviceName;
        serviceObj1.application = "";
        serviceObj1.type = "REST";
        serviceObj1.method = "GET";
        serviceObj1.bodyType = "raw";
        serviceObj1.servicePath = "";
        serviceObj1.kafkaTopic = "";
        serviceObj1.kafkaKey = "";
        serviceObj1.kafkaFilterPath = "";
        serviceObj1.kafkaFilterValue = "";
        serviceObj1.kafkaFilterHeaderPath = "";
        serviceObj1.kafkaFilterHeaderValue = "";
        serviceObj1.operation = "";
        serviceObj1.attachementurl = "";
        serviceObj1.description = "";
        serviceObj1.collection = "";
        serviceObj1.serviceRequest = "";
        serviceObj1.contentList = "";
        serviceObj1.headerList = "";
        serviceObj1.fileName = "Drag and drop Files";
        serviceObj1.isFollowRedir = true;
        serviceObj1.isAvroEnable = false;
        serviceObj1.schemaRegistryURL = "";
        serviceObj1.isAvroEnableKey = false;
        serviceObj1.avroSchemaKey = "";
        serviceObj1.isAvroEnableValue = true;
        serviceObj1.avroSchemaValue = "";
        serviceObj1.parentContentService = "";
        serviceObj1.authType = "none";
        serviceObj1.authKey = "";
        serviceObj1.authVal = "";
        serviceObj1.authAddTo = "Header";
        serviceObj1.simulationParameters = {
            "country": "",
            "environment": "",
            "system": "",
            "application": "",
            "kafkanb": "",
            "timeout": "",
            "kafkaTime": "",
            "props": []
        };
        feedAppServiceModalData(serviceObj1, modalId, mode, hasPermissions);

        // Force a change event on method field.
        refreshDisplayOnTypeChange();
        // Force a change event on author field.
        refreshAuthorDisplayOnAnyChange();

        formEdit.modal('show');

    }

}


/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} service - service object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedAppServiceModalData(service, modalId, mode, hasPermissionsUpdate, extraInfo) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();

    //Destroy the previous Ace object.
    ace.edit($("#editSoapLibraryModal #srvRequest")[0]).destroy();
    ace.edit($("#editSoapLibraryModal #kfkKey")[0]).destroy();
    ace.edit($("#editSoapLibraryModal #avrSchemaKey")[0]).destroy();
    ace.edit($("#editSoapLibraryModal #avrSchemaValue")[0]).destroy();

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_edit"));
        appendApplicationListServiceModal(service.application);
        appendAppServiceListServiceModal(service.parentContentService);
        formEdit.find("#service").prop("value", service.service);
        formEdit.find("#originalService").prop("value", service.service);
        formEdit.find("#usrcreated").prop("value", service.usrCreated);
        formEdit.find("#datecreated").prop("value", getDate(service.dateCreated));
        formEdit.find("#usrmodif").prop("value", service.usrModif);
        formEdit.find("#datemodif").prop("value", getDate(service.dateModif));
    } else { // DUPLICATE or ADD
        formEdit.find("#usrcreated").prop("value", "");
        formEdit.find("#datecreated").prop("value", "");
        formEdit.find("#usrmodif").prop("value", "");
        formEdit.find("#datemodif").prop("value", "");
        if (mode === "ADD") {
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_create"));
            formEdit.find("#service").prop("value", service.service);
            formEdit.find("#originalService").prop("value", service.service);
        } else { // DUPLICATE
            $("[name='editSoapLibraryField']").html(doc.getDocOnline("page_appservice", "button_duplicate"));
            formEdit.find("#service").prop("value", service.service);
            formEdit.find("#originalService").prop("value", service.service);
        }
    }
    if (isEmpty(service)) {
        formEdit.find("#originalService").prop("value", "");
        formEdit.find("#application").prop("value", "");
        formEdit.find("#type").prop("value", "REST");
        refreshDisplayOnTypeChange();
        formEdit.find("#method").prop("value", "GET");
        formEdit.find("#bodyType").prop("value", "raw");
        formEdit.find("#servicePath").prop("value", "");
        formEdit.find("#isFollowRedir").prop("checked", true);
        formEdit.find("#attachementurl").prop("value", "");
        formEdit.find("#srvRequest").text("");
        formEdit.find("#kfkKey").text("");
        formEdit.find("#avrSchemaKey").text("");
        formEdit.find("#avrSchemaValue").text("");
        formEdit.find("#isAvroEnable").prop("checked", false);
        formEdit.find("#isAvroEnableKey").prop("checked", false);
        formEdit.find("#isAvroEnableValue").prop("checked", false);
        formEdit.find("#schemaRegistryUrl").prop("value", "");
        formEdit.find("#parentContentService").prop("value", "");
        formEdit.find("#collection").prop("value", "");
        formEdit.find("#operation").prop("value", "");
        formEdit.find("#description").prop("value", "");
        formEdit.find("#Filename").val("Drag and drop Files");
        formEdit.find("#kafkaTopic").prop("value", "");
        formEdit.find("#kafkaKey").prop("value", "");
        formEdit.find("#kafkaFilterPath").prop("value", "");
        formEdit.find("#kafkaFilterValue").prop("value", "");
        formEdit.find("#kafkaFilterHeaderPath").prop("value", "");
        formEdit.find("#kafkaFilterHeaderValue").prop("value", "");
        formEdit.find("#callTimeout").prop("value", "");
        formEdit.find("#callKafkaNb").prop("value", "");
        formEdit.find("#callKafkaTime").prop("value", "");
        formEdit.find("#callCountry").prop("value", "");
        formEdit.find("#callEnv").prop("value", "");
        formEdit.find("#callSystem").prop("value", "");
        formEdit.find("#authType").prop("value", "none");
        formEdit.find("#authKey").prop("value", "");
        formEdit.find("#authVal").prop("value", "");
        formEdit.find("#authAddTo").prop("value", "Header");
    } else {
        formEdit.find("#application").val(service.application);
        formEdit.find("#type").val(service.type);
        formEdit.find("#method").val(service.method);
        formEdit.find("#bodyType").val(service.bodyType);
        formEdit.find("#servicePath").prop("value", service.servicePath);
        formEdit.find("#isFollowRedir").prop("checked", service.isFollowRedir);
        formEdit.find("#isAvroEnable").prop("checked", service.isAvroEnable);
        formEdit.find("#isAvroEnableKey").prop("checked", service.isAvroEnableKey);
        formEdit.find("#isAvroEnableValue").prop("checked", service.isAvroEnableValue);
        formEdit.find("#schemaRegistryUrl").prop("value", service.schemaRegistryURL);
        formEdit.find("#parentContentService").val(service.parentContentService);
        formEdit.find("#attachementurl").prop("value", service.attachementURL);
        formEdit.find("#srvRequest").text(service.serviceRequest);
        formEdit.find("#kfkKey").text(service.kafkaKey);
        formEdit.find("#avrSchemaKey").text(service.avroSchemaKey);
        formEdit.find("#avrSchemaValue").text(service.avroSchemaValue);
        formEdit.find("#collection").prop("value", service.collection);
        formEdit.find("#operation").prop("value", service.operation);
        formEdit.find("#description").prop("value", service.description);
        formEdit.find("#kafkaTopic").prop("value", service.kafkaTopic);
        formEdit.find("#kafkaFilterPath").prop("value", service.kafkaFilterPath);
        formEdit.find("#kafkaFilterValue").prop("value", service.kafkaFilterValue);
        formEdit.find("#kafkaFilterHeaderPath").prop("value", service.kafkaFilterHeaderPath);
        formEdit.find("#kafkaFilterHeaderValue").prop("value", service.kafkaFilterHeaderValue);
        formEdit.find("#authType").prop("value", service.authType);
        formEdit.find("#authKey").prop("value", service.authUser);
        formEdit.find("#authVal").prop("value", service.authPassword);
        formEdit.find("#authAddTo").prop("value", service.authAddTo);
        if (service.fileName === "") {
            srv_updateDropzone("Drag and drop Files", "#" + modalId);
        } else {
            srv_updateDropzone(service.fileName, "#" + modalId);
        }
        formEdit.find("#callTimeout").prop("value", service.simulationParameters.timeout);
        formEdit.find("#callKafkaNb").prop("value", service.simulationParameters.kafkanb);
        formEdit.find("#callKafkaTime").prop("value", service.simulationParameters.kafkaTime);
        if (extraInfo !== undefined) {
            formEdit.find("#callSystem").val(extraInfo.system);
            $("[name='callCountry']").empty();
            displayListFromData("callCountry", extraInfo.countries, service.simulationParameters.country);
            $("[name='callEnv']").empty();
            displayListFromData("callEnv", extraInfo.environments, service.simulationParameters.environment);
        } else {
            formEdit.find("#callSystem").val(service.simulationParameters.system);
            $("[name='callCountry']").empty();
            displayInvariantList("callCountry", "COUNTRY", false);
            $("[name='callEnv']").empty();
            displayInvariantList("callEnv", "ENVIRONMENT", false);
            formEdit.find("#callCountry").val(service.simulationParameters.country);
            formEdit.find("#callEnv").val(service.simulationParameters.environment);
        }

        // Feed the simulation parameters.
        feedAppServiceModalDataCallProp(service.simulationParameters.props);

        // Feed the content table.
        feedAppServiceModalDataContent(service.contentList);

        // Feed the header table.
        feedAppServiceModalDataHeader(service.headerList);
    }

    //Highlight envelop on modal loading
    var editor = ace.edit($("#editSoapLibraryModal #srvRequest")[0]);
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
    editor.setOptions({
        maxLines: 50
    });
    document.getElementById('srvRequest').style.fontSize = '14px';

    var editorKey = ace.edit($("#editSoapLibraryModal #kfkKey")[0]);
    editorKey.setTheme("ace/theme/chrome");
    editorKey.getSession().setMode(defineAceMode(editorKey.getSession().getDocument().getValue()));
    editorKey.setOptions({
        maxLines: 50
    });
    document.getElementById('kfkKey').style.fontSize = '14px';
    
    var editorSchemaKey = ace.edit($("#editSoapLibraryModal #avrSchemaKey")[0]);
    editorSchemaKey.setTheme("ace/theme/chrome");
    editorSchemaKey.getSession().setMode(defineAceMode(editorSchemaKey.getSession().getDocument().getValue()));
    editorSchemaKey.setOptions({
        maxLines: 50
    });
    document.getElementById('avrSchemaKey').style.fontSize = '14px';
    
    var editorSchemaValue = ace.edit($("#editSoapLibraryModal #avrSchemaValue")[0]);
    editorSchemaValue.setTheme("ace/theme/chrome");
    editorSchemaValue.getSession().setMode(defineAceMode(editorSchemaValue.getSession().getDocument().getValue()));
    editorSchemaValue.setOptions({
        maxLines: 50
    });
    document.getElementById('avrSchemaValue').style.fontSize = '14px';

    //On ADD, try to autodetect Ace mode until it is defined

    $($("#editSoapLibraryModal #srvRequest").get(0)).keyup(function () {
        if (editor.getSession().getMode().$id === "ace/mode/text") {
            editor.getSession().setMode(defineAceMode(editor.getSession().getDocument().getValue()));
        }
    });
    $($("#editSoapLibraryModal #kfkKey").get(0)).keyup(function () {
        if (editorKey.getSession().getMode().$id === "ace/mode/text") {
            editorKey.getSession().setMode(defineAceMode(editorKey.getSession().getDocument().getValue()));
        }
    });
    $($("#editSoapLibraryModal #avrSchemaKey").get(0)).keyup(function () {
        if (editorSchemaKey.getSession().getMode().$id === "ace/mode/text") {
            editorSchemaKey.getSession().setMode(defineAceMode(editorSchemaKey.getSession().getDocument().getValue()));
        }
    });
    $($("#editSoapLibraryModal #avrSchemaValue").get(0)).keyup(function () {
        if (editorSchemaValue.getSession().getMode().$id === "ace/mode/text") {
            editorSchemaValue.getSession().setMode(defineAceMode(editorSchemaValue.getSession().getDocument().getValue()));
        }
    });

    // Authorities
//    if (mode === "EDIT") {
//        formEdit.find("#service").prop("readonly", "readonly");
//    } else {
//        formEdit.find("#service").removeAttr("readonly");
//        formEdit.find("#service").removeProp("readonly");
//        formEdit.find("#service").removeAttr("disabled");
//    }
    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (!(hasPermissionsUpdate)) { // If readonly, we readonly all fields
        formEdit.find("#service").prop("readonly", "readonly");
        formEdit.find("#application").prop("readonly", "readonly");
        formEdit.find("#parentContentService").prop("readonly", "readonly");
        formEdit.find("#type").prop("disabled", "disabled");
        formEdit.find("#isFollowRedir").prop("disabled", "disabled");
        formEdit.find("#method").prop("disabled", "disabled");
        formEdit.find("#bodyType").prop("disabled", "disabled");
        formEdit.find("#addContent").prop("disabled", "disabled");
        formEdit.find("#addHeader").prop("disabled", "disabled");
        formEdit.find("#servicePath").prop("readonly", true);
        formEdit.find("#collection").prop("readonly", true);
        formEdit.find("#attachementurl").prop("readonly", true);
        formEdit.find("#srvRequest").prop("readonly", "readonly");
        formEdit.find("#kfkKey").prop("readonly", "readonly");
        formEdit.find("#avrSchemaKey").prop("readonly", "readonly");
        formEdit.find("#avrSchemaValue").prop("readonly", "readonly");
        formEdit.find("#description").prop("readonly", "readonly");
        formEdit.find("#isFollowRedir").prop("readonly", "readonly");
        formEdit.find("#kafkaTopic").prop("readonly", "readonly");
        formEdit.find("#kafkaFilterPath").prop("readonly", "readonly");
        formEdit.find("#kafkaFilterValue").prop("readonly", "readonly");
        // We hide Save button.
        $('#editSoapLibraryButton').attr('class', '');
        $('#editSoapLibraryButton').attr('hidden', 'hidden');
    } else {
        formEdit.find("#service").removeProp("readonly");
        formEdit.find("#application").removeProp("readonly");
        formEdit.find("#parentContentService").removeProp("readonly");
        formEdit.find("#type").removeProp("disabled");
        formEdit.find("#isFollowRedir").removeProp("disabled");
        formEdit.find("#method").removeProp("disabled");
        formEdit.find("#bodyType").removeProp("disabled");
        formEdit.find("#addContent").removeProp("disabled");
        formEdit.find("#addHeader").removeProp("disabled");
        formEdit.find("#servicePath").prop("readonly", false);
        formEdit.find("#collection").prop("readonly", false);
        formEdit.find("#attachementurl").prop("readonly", false);
        formEdit.find("#srvRequest").removeProp("readonly");
        formEdit.find("#kfkKey").removeProp("readonly");
        formEdit.find("#avrSchemaKey").removeProp("readonly");
        formEdit.find("#avrSchemaValue").removeProp("readonly");
        formEdit.find("#description").removeProp("disabled");
        formEdit.find("#isFollowRedir").prop("readonly", false);
        formEdit.find("#kafkaTopic").removeProp("disabled");
//        formEdit.find("#kafkaKey").removeProp("disabled");
        formEdit.find("#kafkaFilterPath").removeProp("disabled");
        formEdit.find("#kafkaFilterValue").removeProp("disabled");
    }

}

function appendApplicationListServiceModal(defaultValue) {
    $('#editServiceModal [name="application"]').select2(getComboConfigApplicationList());
//    console.info($("#editServiceModal [name='application']")[0].options);
//    console.info(defaultValue);
    const select = $("#editServiceModal [name='application']")[0];
    const optionLabels = Array.from(select.options).map((opt) => opt.value);
    const hasOption = optionLabels.includes(defaultValue);

    if (!hasOption) {
        var myoption = $('<option></option>').text(defaultValue).val(defaultValue);
        $("#editServiceModal [name='application']").append(myoption).trigger('change'); // append the option and update Select2
    }

    $("#editSoapLibraryModal #editApplication").off("click");
    $("#editSoapLibraryModal #editApplication").click(function () {
        openModalApplication($("#editSoapLibraryModal #application").val(), "EDIT");
    });

}

function appendAppServiceListServiceModal(defaultValue) {
    $('#editServiceModal [name="parentContentService"]').select2(getComboConfigAppServiceList());
    var myoption = $('<option></option>').text(defaultValue).val(defaultValue);
    $("#editServiceModal [name='parentContentService']").append(myoption).trigger('change'); // append the option and update Select2
}

function feedAppServiceModalDataContent(contentList) {
    $('#contentTableBody tr').remove();
    if (!isEmpty(contentList)) {
        $.each(contentList, function (idx, obj) {
            obj.toDelete = false;
            appendContentRow(obj);
        });
    }
}

function appendContentRow(content) {
    var doc = new Doc();
    var ro = 'readonly';
    var deleteBtn = $("<button disable type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));

    if (content.isInherited) {
        var ro = 'readonly ';
        var deleteBtn = "";
    }
    var activeSelect = $("<input type=\"checkbox\"></button>").addClass("form-control input-sm").prop("checked", content.isActive);
//    var activeSelect = getSelectInvariant("APPSERVICECONTENTACT", false, false);
    if (content.isInherited) {
        activeSelect.prop("disabled", "disabled");
    }

    var sortInput = $("<input " + ro + "maxlength=\"4\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Sort") + " --\">").addClass("form-control").val(content.sort);
    var keyInput = $("<input " + ro + "maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Key") + " --\">").addClass("form-control crb-autocomplete-variable").val(content.key);
    var valueInput = $("<textarea " + ro + "rows='1'  placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Value") + " --\"></textarea>").addClass("form-control crb-autocomplete-variable").val(content.value);
    var descriptionInput = $("<input  " + ro + "maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Description") + " --\">").addClass("form-control").val(content.description);
    var table = $("#contentTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var isActive = $("<td></td>").append(activeSelect);
    var sortName = $("<td></td>").append(sortInput);
    var keyName = $("<td></td>").append(keyInput);
    var valueName = $("<td></td>").append(valueInput);
    var descriptionName = $("<td></td>").append(descriptionInput);

    if (!content.isInherited) {
        deleteBtn.click(function () {
            content.toDelete = (content.toDelete) ? false : true;

            if (content.toDelete) {
                row.addClass("danger");
            } else {
                row.removeClass("danger");
            }
        });
    }
    activeSelect.change(function () {
        content.isActive = $(this)[0].checked;
//        console.info(content.isActive);
    });
    sortInput.change(function () {
        content.sort = $(this).val();
    });
    keyInput.change(function () {
        content.key = $(this).val();
    });
    valueInput.change(function () {
        content.value = $(this).val();
    });
    descriptionInput.change(function () {
        content.description = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(isActive);
    row.append(sortName);
    row.append(keyName);
    row.append(valueName);
    row.append(descriptionName);
    row.data("content", content);
    table.append(row);
}

function addNewContentRow() {
    var newContent = {
        isActive: true,
        isInherited: false,
        sort: 10,
        key: "",
        value: "",
        description: "",
        toDelete: false
    };
    appendContentRow(newContent);
}

function feedAppServiceModalDataHeader(headerList) {

    $('#headerTableBody tr').remove();
    if (!isEmpty(headerList)) {
        $.each(headerList, function (idx, obj) {
            obj.toDelete = false;
            appendHeaderRow(obj);
        });
    }
}

function appendHeaderRow(content) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var activeSelect = $("<input type=\"checkbox\"></button>").addClass("form-control input-sm").prop("checked", content.isActive);
    var sortInput = $("<input  maxlength=\"4\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Sort") + " --\">").addClass("form-control").val(content.sort);
    var keyInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Key") + " --\">").addClass("form-control crb-autocomplete-header").val(content.key);
    var valueInput = $("<textarea rows='1'  placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Value") + " --\"></textarea>").addClass("form-control crb-autocomplete-headervalue").val(content.value);
    var descriptionInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Description") + " --\">").addClass("form-control").val(content.description);
    var table = $("#headerTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var isActive = $("<td></td>").append(activeSelect);
    var sortName = $("<td></td>").append(sortInput);
    var keyName = $("<td></td>").append(keyInput);
    var valueName = $("<td></td>").append(valueInput);
    var descriptionName = $("<td></td>").append(descriptionInput);
    deleteBtn.click(function () {
        content.toDelete = (content.toDelete) ? false : true;
        if (content.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    activeSelect.change(function () {
        content.isActive = $(this)[0].checked;
//        console.info(content.isActive);
    });
    sortInput.change(function () {
        content.sort = $(this).val();
    });
    keyInput.change(function () {
        content.key = $(this).val();
//        console.info(content.key);
    });

    valueInput.change(function () {
        content.value = $(this).val();
    });
    descriptionInput.change(function () {
        content.description = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(isActive);
    row.append(sortName);
    row.append(keyName);
    row.append(valueName);
    row.append(descriptionName);
    row.data("header", content);
    table.append(row);


}

function addNewHeaderRow() {
    var newHeader = {
        isActive: true,
        sort: 10,
        key: "",
        value: "",
        description: "",
        toDelete: false
    };
    appendHeaderRow(newHeader);
}

function feedAppServiceModalDataCallProp(callPropList) {
//    console.info("feed")
    $('#callPropTableBody tr').remove();
    if (!isEmpty(callPropList)) {
        $.each(callPropList, function (idx, obj) {
            if (obj.toDelete === false) {
                appendCallPropRow(obj);
            }
        });
    }
}

function appendCallPropRow(content) {
    var doc = new Doc();
    var deleteBtn = $("<button type=\"button\"></button>").addClass("btn btn-default btn-xs").append($("<span></span>").addClass("glyphicon glyphicon-trash"));
    var activeSelect = $("<input type=\"checkbox\"></button>").addClass("form-control input-sm").prop("checked", content.isActive);
//    console.info(content.isActive);
    var keyInput = $("<input  maxlength=\"255\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Key") + " --\">").addClass("form-control").val(content.key);
    var valueInput = $("<textarea rows='1'  placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Value") + " --\"></textarea>").addClass("form-control").val(content.value);
    var descriptionInput = $("<input  maxlength=\"200\" placeholder=\"-- " + doc.getDocLabel("appservicecontent", "Description") + " --\">").addClass("form-control").val(content.description);
    var table = $("#callPropTableBody");

    var row = $("<tr></tr>");
    var deleteBtnRow = $("<td></td>").append(deleteBtn);
    var isActive = $("<td></td>").append(activeSelect.val(content.isActive.toString()));
    var keyName = $("<td></td>").append(keyInput);
    var valueName = $("<td></td>").append(valueInput);
    var descriptionName = $("<td></td>").append(descriptionInput);
    deleteBtn.click(function () {
        content.toDelete = (content.toDelete) ? false : true;
        if (content.toDelete) {
            row.addClass("danger");
        } else {
            row.removeClass("danger");
        }
    });
    activeSelect.change(function () {
        content.isActive = $(this)[0].checked;
//        console.info(content.isActive);
    });
    keyInput.change(function () {
        content.key = $(this).val();
    });
    valueInput.change(function () {
        content.value = $(this).val();
    });
    descriptionInput.change(function () {
        content.description = $(this).val();
    });
    row.append(deleteBtnRow);
    row.append(isActive);
    row.append(keyName);
    row.append(valueName);
    row.append(descriptionName);
    row.data("callProp", content);
    table.append(row);
}

function addNewCallPropRow() {
    var newCallProp = {
        isActive: true,
        key: "",
        value: "",
        description: "",
        toDelete: false
    };
    appendCallPropRow(newCallProp);
}

/**
 * get the picture from items and update the label with the name of the
 * return a boolean if whether or not it succeed to handle the file
 * @param {DataTransferItemList} items
 * @returns {boolean}
 */
function srv_handlePictureSend(items, idModal) {
    if (!items)
        return false;
    //access data directly
    for (var i = 0; i < items.length; i++) {
        var blob = items[i].getAsFile();
        imagePasteFromClipboard = blob;
        var URLObj = window.URL || window.webkitURL;
        var source = URLObj.createObjectURL(blob);
        var nameToDisplay = blob.name;
        srv_updateDropzone(nameToDisplay, idModal);
        return true
    }
}

/**
 * add a listenner for a paste event to catch clipboard if it's a picture
 * @returns {void}
 */
function srv_pasteListennerForClipboardPicture(idModal) {
    var _self = this;
    //handlers
    document.addEventListener('paste', function (e) {
        _self.paste_auto(e);
    }, false);
    //on paste
    this.paste_auto = function (e) {
        //handle paste event if the user do not select an input;
        if (e.clipboardData && !$(e.target).is("input")) {
            var items = e.clipboardData.items;
            srv_handlePictureSend(items, idModal);
            e.preventDefault();
        }
    };
}


/**
 * set up the event listenner to make a drag and drop dropzone
 * @returns {void}
 */
function srv_setUpDragAndDrop(idModal) {
    var dropzone = $(idModal).find("#dropzone")[0];
    dropzone.addEventListener("dragenter", srv_dragenter, false);
    dropzone.addEventListener("dragover", src_dragover, false);
    dropzone.addEventListener("drop", function (event) {
        srv_drop(event, idModal);
    });
}

/**
 * prevent the browser to open the file drag into an other tab
 * @returns {void}
 */
function srv_dragenter(e) {
    e.stopPropagation();
    e.preventDefault();
}

/**
 * prevent the browser to open the file drag into an other tab
 * @returns {void}
 */
function src_dragover(e) {
    e.stopPropagation();
    e.preventDefault();
}

/**
 * prevent the browser to open the file drag into an other tab and handle the file when the user put his file
 * @returns {void}
 */
function srv_drop(e, idModal) {
    e.stopPropagation();
    e.preventDefault();
    var dt = e.dataTransfer;
    var items = dt.items;
    srv_handlePictureSend(items, idModal);
}


/**
 * add a listenner for an input type file
 * @returns {void}
 */

function srv_listennerForInputTypeFile(idModal) {

    var inputs = $(idModal).find("#Filename");
    if (inputs[0] !== undefined) {
        inputs[0].addEventListener('change', function (e) {
            //check if the input is an image
            var fileName = '';
            if (this.files && this.files.length > 1)
                fileName = (this.getAttribute('data-multiple-caption') || '').replace('{count}', this.files.length);
            else
                fileName = e.target.value.split('\\').pop();
            if (fileName) {
                srv_updateDropzone(fileName, idModal);
            }
        });
    }
}

/**
 * change the text inside the label specified and add the attribute uploadSources
 * @param {string} id of the input the label link to
 * @param {string} message that will put inside the label
 * @param {boolean} is the picture upload should be taken from the clipboard
 * @returns {void}
 */
function srv_updateDropzone(messageToDisplay, idModal) {

    var dropzoneText = $(idModal).find("#dropzoneText");
    var glyphIconUpload = "<span class='glyphicon glyphicon-download-alt'></span>";
    dropzoneText.html(messageToDisplay + " " + glyphIconUpload);
    if (imagePasteFromClipboard !== undefined) {
        //reset value inside the input
        var inputs = $(idModal).find("#Filename")[0];
        if (inputs !== undefined) {
            inputs.value = "";
        }
    } else {
        //reset value for the var that stock the picture inside the clipboard
        imagePasteFromClipboard = undefined;
    }
}

function getComboConfigApplicationList() {
}

function getComboConfigAppServiceList() {
}

