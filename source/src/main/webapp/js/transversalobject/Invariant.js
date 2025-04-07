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

var modalInvariantAttributeNB = 1;

/***
 * Open the modal with testcase information.
 * @param {String} invariant - idname of the invariant (ex : "SYSTEM")
 * @param {String} value - value of invariant (ex : "DEVTOOLS")
 * @param {String} mode - mode to open the modal. Can take the values : ADD, DUPLICATE, EDIT
 * @param {String} tab - name of the tab to activate
 * @returns {null}
 */
function openModalInvariant(invariant, value, mode, tab) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editInvariantModal').data("initLabel") === undefined) {
        initModalInvariant();
        $('#editInvariantModal').data("initLabel", true);
    }

    if (!isEmpty(tab)) {
        $('.nav-tabs a[href="#' + tab + '"]').tab('show');
    }

    if (mode === "EDIT") {
        editInvariantClick(invariant, value);
    } else if (mode === "ADD") {
        addInvariantClick(invariant, value);
    } else {
        // DUPLICATE
        duplicateInvariantClick(invariant, value);
    }
}

function initModalInvariant() {

    var doc = new Doc();
    $("[name='buttonClose']").html(doc.getDocLabel("page_global", "buttonClose"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_global", "btn_add"));
    $("[name='buttonDuplicate']").html(doc.getDocLabel("page_global", "btn_duplicate"));
    $("[name='buttonEdit']").html(doc.getDocLabel("page_global", "buttonEdit"));

    $("[name='descriptionField']").html(doc.getDocLabel("page_invariant", "description"));
    $("[name='idnameField']").html(doc.getDocLabel("page_invariant", "idname"));
    $("[name='ValueField']").html(doc.getDocLabel("page_invariant", "value"));
    $("[name='sortField']").html(doc.getDocLabel("page_invariant", "sort"));
    $("[name='veryShortDescField']").html(doc.getDocLabel("page_invariant", "veryShortDesc"));
    $("[name='gp1Field']").html(doc.getDocLabel("page_invariant", "gp1"));
    $("[name='gp2Field']").html(doc.getDocLabel("page_invariant", "gp2"));
    $("[name='gp3Field']").html(doc.getDocLabel("page_invariant", "gp3"));
    $("[name='gp4Field']").html(doc.getDocLabel("page_invariant", "gp4"));
    $("[name='gp5Field']").html(doc.getDocLabel("page_invariant", "gp5"));
    $("[name='gp6Field']").html(doc.getDocLabel("page_invariant", "gp6"));
    $("[name='gp7Field']").html(doc.getDocLabel("page_invariant", "gp7"));
    $("[name='gp8Field']").html(doc.getDocLabel("page_invariant", "gp8"));
    $("[name='gp9Field']").html(doc.getDocLabel("page_invariant", "gp9"));

    $("#editInvariantButton").off("click");
    $("#editInvariantButton").click(function () {
        confirmInvariantModalHandler("EDIT");
    });
    $("#addInvariantButton").off("click");
    $("#addInvariantButton").click(function () {
        confirmInvariantModalHandler("ADD");
    });
    $("#duplicateInvariantButton").off("click");
    $("#duplicateInvariantButton").click(function () {
        confirmInvariantModalHandler("DUPLICATE");
    });
    // We add an attribute button by clicking on add button
    // Click on close modal will reinitialize i  
    $('[name="buttonClose"],[class="close"]').off("click").click(function () {
        // Clear last added attribute index back to 1 when modal is closed.
        modalInvariantAttributeNB = 1;
    });
    $('#AddInvButton').off("click").click(function () {
        modalInvariantAttributeNB++;
        if ($('#Grpgp' + modalInvariantAttributeNB).is(":visible")) {
            modalInvariantAttributeNB++;
        }
        if (modalInvariantAttributeNB < 10) {
            $('#Grpgp' + modalInvariantAttributeNB).show();
        } else {
            var localMessage = new Message("WARNING", "You cannot add more than 9 attributes");
            showMessage(localMessage, $('#editInvariantModal'));
        }
    });
}

/***
 * Open the modal with queue information.
 * @param {String} invariant - invariant selected
 * @param {String} value - value selected
 * @returns {null}
 */
function editInvariantClick(invariant, value) {

    clearResponseMessage($('#editInvariantModal'));

    // When editing the execution queue, we can modify, modify and run or cancel.
    $('#editInvariantButton').attr('class', 'btn btn-primary');
    $('#editInvariantButton').removeProp('hidden');

    // We cannot duplicate.
    $('#duplicateInvariantButton').attr('class', '');
    $('#duplicateInvariantButton').attr('hidden', 'hidden');
    $('#addInvariantButton').attr('class', '');
    $('#addInvariantButton').attr('hidden', 'hidden');

    $('#editInvariantModalForm select[name="idname"]').off("change");
    $('#editInvariantModalForm select[name="idname"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeInvariantKey();
    });
    $('#editInvariantModalForm input[name="value"]').off("change");
    $('#editInvariantModalForm input[name="value"]').change(function () {
        // Compare with original value in order to display the warning message.
        displayWarningOnChangeInvariantKey();
    });

    feedInvariantModal(invariant, value, "editInvariantModal", "EDIT");
}

function displayWarningOnChangeInvariantKey() {
    // Compare with original value in order to display the warning message.
    let old1 = $("#originalIdName").val();
    let old2 = $("#originalValue").val();
    let new1 = $('#editInvariantModalForm select[name="idname"]').val();
    let new2 = $('#editInvariantModalForm input[name="value"]').val();
    if ((old1 !== new1) || (old2 !== new2)) {
        var localMessage = new Message("WARNING", "You are about to rename that invariant. For some invariant, it may have unexpected result. Do it with care.");
        showMessage(localMessage, $('#editInvariantModal'));
    } else {
        clearResponseMessage($('#editInvariantModal'));
    }
}

/***
 * Open the modal with queue information.
 * @param {String} invariant - idname of the invariant to duplicate.
 * @param {String} value - value of the invariant to duplicate.
 * @returns {null}
 */
function duplicateInvariantClick(invariant, value) {

    clearResponseMessage($('#editExecutionQueueModal'));

    $('#editInvariantButton').attr('class', '');
    $('#editInvariantButton').attr('hidden', 'hidden');
    $('#duplicateInvariantButton').attr('class', 'btn btn-primary');
    $('#duplicateInvariantButton').removeProp('hidden');
    $('#addInvariantButton').attr('class', '');
    $('#addInvariantButton').attr('hidden', 'hidden');

    $('#editInvariantModalForm select[name="idname"]').off("change");
    $('#editInvariantModalForm input[name="value"]').off("change");

    feedInvariantModal(invariant, value, "editInvariantModal", "DUPLICATE");
}

/***
 * Open the modal with queue information.
 * @param {String} invariant - idname of the invariant to duplicate.
 * @param {String} value - value of the invariant to duplicate.
 * @returns {null}
 */
function addInvariantClick(invariant, value) {

    clearResponseMessage($('#editExecutionQueueModal'));

    $('#editInvariantButton').attr('class', '');
    $('#editInvariantButton').attr('hidden', 'hidden');
    $('#addInvariantButton').attr('class', 'btn btn-primary');
    $('#addInvariantButton').removeProp('hidden');
    $('#duplicateInvariantButton').attr('class', '');
    $('#duplicateInvariantButton').attr('hidden', 'hidden');

    $('#editInvariantModalForm select[name="idname"]').off("change");
    $('#editInvariantModalForm input[name="value"]').off("change");

    feedInvariantModal(invariant, value, "editInvariantModal", "ADD");
}


/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function confirmInvariantModalHandler(mode) {
    clearResponseMessage($('#editInvariantModal'));

    var formEdit = $('#editInvariantModal #editInvariantModalForm');

    // Enable the test combo before submit the form.
    if (mode === 'EDIT') {
        formEdit.find("#idname").removeAttr("disabled");
    }
    // Calculate servlet name to call.
    var myServlet = "UpdateInvariant";
    if ((mode === "ADD") || (mode === "DUPLICATE")) {
        myServlet = "CreateInvariant";
    }

    // Get the header data from the form.
    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }

    var tcElement = formEdit.find("#value");
    if (isEmpty(data.value)) {
        tcElement.parents("div.form-group").addClass("has-error");
        var localMessage = new Message("danger", "Please specify an invariant value !");
        showMessage(localMessage, $('#editInvariantModal'));
        return;
    } else {
        tcElement.parents("div.form-group").removeClass("has-error");
    }

    showLoaderInModal('#editInvariantModal');
    var inv1 = data.originalIdName;
    var inv2 = data.idname;

    $.ajax({
        url: myServlet,
        async: true,
        method: "POST",
        data: {
            originalIdName: data.originalIdName,
            originalValue: data.originalValue,
            idName: data.idname,
            value: data.value,
            sort: data.sort,
            description: data.description,
            veryShortDesc: data.veryShortDesc,
            gp1: data.gp1,
            gp2: data.gp2,
            gp3: data.gp3,
            gp4: data.gp4,
            gp5: data.gp5,
            gp6: data.gp6,
            gp7: data.gp7,
            gp8: data.gp8,
            gp9: data.gp9
        },
        success: function (data) {
            // Clear last added attribute index back to 1 when modal is closed.
            modalInvariantAttributeNB = 1;
            data = JSON.parse(data);
            hideLoaderInModal('#editInvariantModal');
            if (getAlertType(data.messageType) === "success") {
                var oTable = $("#invariantsTable").dataTable();
                oTable.fnDraw(false);
                $('#editInvariantModal').data("Saved", true);
                $('#editInvariantModal').modal('hide');
                showMessage(data);
                // Clean local Storage
                cleanCacheInvariant(inv1);
                if (inv1 !== inv2) {
                    cleanCacheInvariant(inv2);
                }
            } else {
                showMessage(data, $('#editInvariantModal'));
            }
        },
        error: showUnexpectedError
    });
    if (mode === 'EDIT') { // Disable back the test combo before submit the form.
        formEdit.find("#idname").prop("disabled", "disabled");
    }

}

/***
 * Feed the Invariant modal with all the data.
 * @param {String} invariant - idname of the invariant to load
 * @param {String} value - value of the invariant to load
 * @param {String} modalId - modal id to feed.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @returns {null}
 */
function feedInvariantModal(invariant, value, modalId, mode) {
    clearResponseMessageMainPage();

    var formEdit = $('#' + modalId);

    if (mode === "DUPLICATE" || mode === "EDIT") {
        $.ajax({
            url: "ReadInvariant",
            async: true,
            method: "POST",
            data: {
                idName: invariant,
                value: value
            },
            success: function (data) {
                if (data.messageType === "OK") {

                    // Feed the data to the screen and manage authorities.
                    var inv = data.contentTable;
                    var hasPermissions = data.contentTable.hasPermissionsUpdate;

                    feedInvariantModalData(inv, modalId, mode, hasPermissions);

                    formEdit.modal('show');
                } else {
                    showUnexpectedError();
                }
            },
            error: showUnexpectedError
        });

    } else {

        var inv = {};
        inv.idName = invariant;
        inv.value = "";
        inv.sort = "";
        inv.description = "";
        inv.veryShortDesc = "";
        if (invariant === "ROBOTHOST") {
            inv.gp1 = "10";
        } else {
            inv.gp1 = "";
        }
        inv.gp2 = "";
        inv.gp3 = "";
        inv.gp4 = "";
        inv.gp5 = "";
        inv.gp6 = "";
        inv.gp7 = "";
        inv.gp8 = "";
        inv.gp9 = "";
        var hasPermissions = true;
        feedInvariantModalData(inv, modalId, mode, hasPermissions);

        formEdit.modal('show');

    }

    $('#editInvariantModal #idname').off("change");
    $('#editInvariantModal #idname').change(function () {
        refresh_attributes();
    });


}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} inv - invariant object to be loaded.
 * @param {String} modalId - id of the modal form where to feed the data.
 * @param {String} mode - either ADD, EDIT or DUPLICATE in order to define the purpose of the modal.
 * @param {String} hasPermissionsUpdate - boolean if premition is granted.
 * @returns {null}
 */
function feedInvariantModalData(inv, modalId, mode, hasPermissionsUpdate) {
    var formEdit = $('#' + modalId);
    var doc = new Doc();
    var isEditable = (((hasPermissionsUpdate) && (mode === "EDIT"))
            || (mode === "DUPLICATE") || (mode === "ADD"));

    // Data Feed.
    if (mode === "EDIT") {
        $("[name='editInvariantField']").html(doc.getDocOnline("page_global", "btn_edit"));
    } else if (mode === "ADD") { // DUPLICATE or ADD
        $("[name='editInvariantField']").html(doc.getDocOnline("page_global", "btn_add"));
    } else if (mode === "DUPLICATE") { // DUPLICATE or ADD
        $("[name='editInvariantField']").html(doc.getDocOnline("page_global", "btn_duplicate"));
    }

    if (isEditable) {
        var idNameList = $("#idname");
        idNameList.empty();
        displayInvariantList("idname", "INVARIANTPUBLIC", false, inv.idName, undefined, true);
    } else {
        var idNameList = $("#idname");
        idNameList.empty();
        idNameList.append($('<option></option>').text(inv.idName).val(inv.idName));
    }

    if (isEmpty(inv)) {
        formEdit.find("#originalIdName").prop("value", "");
        formEdit.find("#originalValue").prop("value", "");
        formEdit.find("#idname").prop("value", "");
        formEdit.find("#value").prop("value", "");
        formEdit.find("#sort").prop("value", "");
        formEdit.find("#description").prop("value", "");
        formEdit.find("#veryShortDesc").prop("value", "");
        formEdit.find("#gp1").prop("value", "");
        formEdit.find("#gp2").prop("value", "");
        formEdit.find("#gp3").prop("value", "");
        formEdit.find("#gp4").prop("value", "");
        formEdit.find("#gp5").prop("value", "");
        formEdit.find("#gp6").prop("value", "");
        formEdit.find("#gp7").prop("value", "");
        formEdit.find("#gp8").prop("value", "");
        formEdit.find("#gp9").prop("value", "");
    } else {
        formEdit.find("#originalIdName").prop("value", inv.idName);
        formEdit.find("#originalValue").prop("value", inv.value);
        formEdit.find("#idname").val(inv.idName);
        formEdit.find("#value").prop("value", inv.value);
        formEdit.find("#sort").prop("value", inv.sort);
        formEdit.find("#description").prop("value", inv.description);
        formEdit.find("#veryShortDesc").prop("value", inv.veryShortDesc);
        formEdit.find("#gp1").prop("value", inv.gp1);
        formEdit.find("#gp2").prop("value", inv.gp2);
        formEdit.find("#gp3").prop("value", inv.gp3);
        formEdit.find("#gp4").prop("value", inv.gp4);
        formEdit.find("#gp5").prop("value", inv.gp5);
        formEdit.find("#gp6").prop("value", inv.gp6);
        formEdit.find("#gp7").prop("value", inv.gp7);
        formEdit.find("#gp8").prop("value", inv.gp8);
        formEdit.find("#gp9").prop("value", inv.gp9);
    }

    refresh_attributes();
    // Authorities
//    if (mode === "EDIT") {
//    } else {
//    }

    //We desactivate or activate the access to the fields depending on if user has the credentials to edit.
    if (isEditable) { // If readonly, we readonly all fields
        formEdit.find("#idname").removeAttr("disabled");
        formEdit.find("#value").prop("readonly", false);
        formEdit.find("#sort").prop("readonly", false);
        formEdit.find("#description").prop("readonly", false);
        formEdit.find("#veryShortDesc").prop("readonly", false);
        formEdit.find("#gp1").prop("readonly", false);
        formEdit.find("#gp2").prop("readonly", false);
        formEdit.find("#gp3").prop("readonly", false);
        formEdit.find("#gp4").prop("readonly", false);
        formEdit.find("#gp5").prop("readonly", false);
        formEdit.find("#gp6").prop("readonly", false);
        formEdit.find("#gp7").prop("readonly", false);
        formEdit.find("#gp8").prop("readonly", false);
        formEdit.find("#gp9").prop("readonly", false);
    } else {
        formEdit.find("#idname").prop("disabled", "disabled");
        formEdit.find("#value").prop("readonly", "readonly");
        formEdit.find("#sort").prop("readonly", "readonly");
        formEdit.find("#description").prop("readonly", "readonly");
        formEdit.find("#veryShortDesc").prop("readonly", "readonly");
        formEdit.find("#gp1").prop("readonly", "readonly");
        formEdit.find("#gp2").prop("readonly", "readonly");
        formEdit.find("#gp3").prop("readonly", "readonly");
        formEdit.find("#gp4").prop("readonly", "readonly");
        formEdit.find("#gp5").prop("readonly", "readonly");
        formEdit.find("#gp6").prop("readonly", "readonly");
        formEdit.find("#gp7").prop("readonly", "readonly");
        formEdit.find("#gp8").prop("readonly", "readonly");
        formEdit.find("#gp9").prop("readonly", "readonly");
    }
}

function refresh_attributes() {
//    console.info("refresh.")
    var idname = $('#editInvariantModal #idname').val();
    //we display an attribute field if a value already exists
    for (j = 2; j < 10; j++) {
        if ($('#gp' + j).val().trim() !== '') {
            $('#Grpgp' + j).show();
        } else {
            $('#Grpgp' + j).hide();
        }
    }
    ;

    var autocompleAttr1 = [];
    var autocompleAttr2 = [];

    if (idname === "COUNTRY") {
        autocompleAttr2 = getlocalelist();
    } else if (idname === "ENVIRONMENT") {
        autocompleAttr1 = ["PROD", "QA", "UAT"];
    }
    $('#editInvariantModal').find("#gp1").autocomplete({
        source: autocompleAttr1,
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

    $('#editInvariantModal').find("#gp2").autocomplete({
        source: autocompleAttr2,
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
;


function getlocalelist() {
    return [
        'aa',
        'ab',
        'ae',
        'af',
        'af-NA',
        'af-ZA',
        'agq-CM',
        'ak',
        'ak-GH',
        'am',
        'am-ET',
        'an',
        'ar',
        'ar-AE',
        'ar-BH',
        'ar-DJ',
        'ar-DZ',
        'ar-EG',
        'ar-EH',
        'ar-ER',
        'ar-IL',
        'ar-IQ',
        'ar-JO',
        'ar-KM',
        'ar-KW',
        'ar-LB',
        'ar-LY',
        'ar-MA',
        'ar-MR',
        'ar-OM',
        'ar-PS',
        'ar-QA',
        'ar-SA',
        'ar-SD',
        'ar-SO',
        'ar-SS',
        'ar-SY',
        'ar-TD',
        'ar-TN',
        'ar-YE',
        'ar-001',
        'arn-CL',
        'as',
        'as-IN',
        'asa-TZ',
        'ast-ES',
        'av',
        'ay',
        'az',
        'az-AZ',
        'az-AZ',
        'ba',
        'ba-RU',
        'bas-CM',
        'be',
        'be-BY',
        'bem-ZM',
        'bez-TZ',
        'bg',
        'bg-BG',
        'bi',
        'bm',
        'bm-ML',
        'bn',
        'bn-BD',
        'bn-IN',
        'bo',
        'bo-CN',
        'bo-IN',
        'br',
        'br-FR',
        'brx-IN',
        'bs',
        'bs-BA',
        'bs-BA',
        'byn-ER',
        'ca',
        'ca-AD',
        'ca-ES',
        'ca-FR',
        'ca-IT',
        'ccp-BD',
        'ccp-IN',
        'ce',
        'ce-RU',
        'ceb-PH',
        'cgg-UG',
        'ch',
        'chr-US',
        'ckb-IQ',
        'ckb-IR',
        'co',
        'co-FR',
        'cr',
        'cs',
        'cs-CZ',
        'cu',
        'cv',
        'cv-RU',
        'cy',
        'cy-GB',
        'da',
        'da-DK',
        'da-GL',
        'dav-KE',
        'de',
        'de-AT',
        'de-BE',
        'de-CH',
        'de-DE',
        'de-IT',
        'de-LI',
        'de-LU',
        'dje-NE',
        'dsb-DE',
        'dua-CM',
        'dv',
        'dv-MV',
        'dyo-SN',
        'dz',
        'dz-BT',
        'ebu-KE',
        'ee',
        'ee-GH',
        'ee-TG',
        'el',
        'el-CY',
        'el-GR',
        'en',
        'en-AD',
        'en-AE',
        'en-AG',
        'en-AI',
        'en-AL',
        'en-AR',
        'en-AS',
        'en-AT',
        'en-AU',
        'en-BA',
        'en-BB',
        'en-BD',
        'en-BE',
        'en-BG',
        'en-BI',
        'en-BM',
        'en-BR',
        'en-BS',
        'en-BW',
        'en-BZ',
        'en-CA',
        'en-CC',
        'en-CH',
        'en-CK',
        'en-CL',
        'en-CM',
        'en-CN',
        'en-CO',
        'en-CX',
        'en-CY',
        'en-CZ',
        'en-DE',
        'en-DG',
        'en-DK',
        'en-DM',
        'en-EE',
        'en-ER',
        'en-ES',
        'en-FI',
        'en-FJ',
        'en-FK',
        'en-FM',
        'en-FR',
        'en-GB',
        'en-GD',
        'en-GG',
        'en-GH',
        'en-GI',
        'en-GM',
        'en-GR',
        'en-GU',
        'en-GY',
        'en-HK',
        'en-HR',
        'en-HU',
        'en-ID',
        'en-IE',
        'en-IL',
        'en-IM',
        'en-IN',
        'en-IO',
        'en-IS',
        'en-IT',
        'en-JE',
        'en-JM',
        'en-JP',
        'en-KE',
        'en-KI',
        'en-KN',
        'en-KR',
        'en-KY',
        'en-LC',
        'en-LR',
        'en-LS',
        'en-LT',
        'en-LU',
        'en-LV',
        'en-ME',
        'en-MG',
        'en-MH',
        'en-MM',
        'en-MO',
        'en-MP',
        'en-MS',
        'en-MT',
        'en-MU',
        'en-MV',
        'en-MW',
        'en-MX',
        'en-MY',
        'en-NA',
        'en-NF',
        'en-NG',
        'en-NL',
        'en-NO',
        'en-NR',
        'en-NU',
        'en-NZ',
        'en-PG',
        'en-PH',
        'en-PK',
        'en-PL',
        'en-PN',
        'en-PR',
        'en-PT',
        'en-PW',
        'en-RO',
        'en-RS',
        'en-RU',
        'en-RW',
        'en-SA',
        'en-SB',
        'en-SC',
        'en-SD',
        'en-SE',
        'en-SG',
        'en-SH',
        'en-SI',
        'en-SK',
        'en-SL',
        'en-SS',
        'en-SX',
        'en-SZ',
        'en-TC',
        'en-TH',
        'en-TK',
        'en-TO',
        'en-TR',
        'en-TT',
        'en-TV',
        'en-TW',
        'en-TZ',
        'en-UA',
        'en-UG',
        'en-UM',
        'en-US',
        'en-US',
        'en-VC',
        'en-VG',
        'en-VI',
        'en-VU',
        'en-WS',
        'en-ZA',
        'en-ZM',
        'en-ZW',
        'en-001',
        'en-150',
        'eo',
        'eo-001',
        'es',
        'es-AG',
        'es-AI',
        'es-AR',
        'es-AW',
        'es-BB',
        'es-BL',
        'es-BM',
        'es-BO',
        'es-BQ',
        'es-BR',
        'es-BS',
        'es-BZ',
        'es-CA',
        'es-CL',
        'es-CO',
        'es-CR',
        'es-CU',
        'es-CW',
        'es-DM',
        'es-DO',
        'es-EA',
        'es-EC',
        'es-ES',
        'es-FK',
        'es-GD',
        'es-GF',
        'es-GL',
        'es-GP',
        'es-GQ',
        'es-GT',
        'es-GY',
        'es-HN',
        'es-HT',
        'es-IC',
        'es-KN',
        'es-KY',
        'es-LC',
        'es-MF',
        'es-MQ',
        'es-MS',
        'es-MX',
        'es-NI',
        'es-PA',
        'es-PE',
        'es-PH',
        'es-PM',
        'es-PR',
        'es-PY',
        'es-SR',
        'es-SV',
        'es-SX',
        'es-TC',
        'es-TT',
        'es-US',
        'es-UY',
        'es-VC',
        'es-VE',
        'es-VG',
        'es-VI',
        'es-419',
        'et',
        'et-EE',
        'eu',
        'eu-ES',
        'ewo-CM',
        'fa',
        'fa-AF',
        'fa-IR',
        'ff',
        'ff-BF',
        'ff-CM',
        'ff-GH',
        'ff-GM',
        'ff-GN',
        'ff-GW',
        'ff-LR',
        'ff-MR',
        'ff-NE',
        'ff-NG',
        'ff-SL',
        'ff-SN',
        'fi',
        'fi-FI',
        'fil-PH',
        'fj',
        'fo',
        'fo-DK',
        'fo-FO',
        'fr',
        'fr-BE',
        'fr-BF',
        'fr-BI',
        'fr-BJ',
        'fr-BL',
        'fr-CA',
        'fr-CD',
        'fr-CF',
        'fr-CG',
        'fr-CH',
        'fr-CI',
        'fr-CM',
        'fr-DJ',
        'fr-DZ',
        'fr-FR',
        'fr-GA',
        'fr-GF',
        'fr-GN',
        'fr-GP',
        'fr-GQ',
        'fr-HT',
        'fr-KM',
        'fr-LU',
        'fr-MA',
        'fr-MC',
        'fr-MF',
        'fr-MG',
        'fr-ML',
        'fr-MQ',
        'fr-MR',
        'fr-MU',
        'fr-NC',
        'fr-NE',
        'fr-PF',
        'fr-PM',
        'fr-RE',
        'fr-RW',
        'fr-SC',
        'fr-SN',
        'fr-SY',
        'fr-TD',
        'fr-TG',
        'fr-TN',
        'fr-VU',
        'fr-WF',
        'fr-YT',
        'fur-IT',
        'fy',
        'fy-NL',
        'ga',
        'ga-IE',
        'gaa-GH',
        'gd',
        'gd-GB',
        'gez-ER',
        'gez-ET',
        'gl',
        'gl-ES',
        'gn',
        'gn-PY',
        'gsw-CH',
        'gsw-FR',
        'gsw-LI',
        'gu',
        'gu-IN',
        'guz-KE',
        'gv',
        'gv-IM',
        'ha',
        'ha-GH',
        'ha-NE',
        'ha-NG',
        'haw-US',
        'he',
        'he-IL',
        'hi',
        'hi-IN',
        'ho',
        'hr',
        'hr-BA',
        'hr-HR',
        'hsb-DE',
        'ht',
        'hu',
        'hu-HU',
        'hy',
        'hy-AM',
        'hz',
        'ia',
        'ia-001',
        'id',
        'id-ID',
        'ie',
        'ig',
        'ig-NG',
        'ii',
        'ii-CN',
        'ik',
        'io',
        'io-001',
        'is',
        'is-IS',
        'it',
        'it-CH',
        'it-IT',
        'it-SM',
        'it-VA',
        'iu',
        'iu-CA',
        'ja',
        'ja-JP',
        'jbo-001',
        'jgo-CM',
        'jmc-TZ',
        'jv',
        'jv-ID',
        'ka',
        'ka-GE',
        'kab-DZ',
        'kaj-NG',
        'kam-KE',
        'kcg-NG',
        'kde-TZ',
        'kea-CV',
        'kg',
        'khq-ML',
        'ki',
        'ki-KE',
        'kj',
        'kk',
        'kk-KZ',
        'kkj-CM',
        'kl',
        'kl-GL',
        'kln-KE',
        'km',
        'km-KH',
        'kn',
        'kn-IN',
        'ko',
        'ko-KP',
        'ko-KR',
        'kok-IN',
        'kpe-GN',
        'kpe-LR',
        'kr',
        'ks',
        'ks-IN',
        'ks-IN',
        'ks-IN',
        'ksb-TZ',
        'ksf-CM',
        'ksh-DE',
        'ku',
        'ku-TR',
        'kv',
        'kw',
        'kw-GB',
        'ky',
        'ky-KG',
        'la',
        'lag-TZ',
        'lb',
        'lb-LU',
        'lg',
        'lg-UG',
        'li',
        'lkt-US',
        'ln',
        'ln-AO',
        'ln-CD',
        'ln-CF',
        'ln-CG',
        'lo',
        'lo-LA',
        'lrc-IQ',
        'lrc-IR',
        'lt',
        'lt-LT',
        'lu',
        'lu-CD',
        'luo-KE',
        'luy-KE',
        'lv',
        'lv-LV',
        'mas-KE',
        'mas-TZ',
        'mer-KE',
        'mfe-MU',
        'mg',
        'mg-MG',
        'mgh-MZ',
        'mgo-CM',
        'mh',
        'mi',
        'mi-NZ',
        'mk',
        'mk-MK',
        'ml',
        'ml-IN',
        'mn',
        'mn-MN',
        'mni-IN',
        'mni-IN',
        'moh-CA',
        'mr',
        'mr-IN',
        'ms',
        'ms-BN',
        'ms-BN',
        'ms-MY',
        'ms-MY',
        'ms-SG',
        'mt',
        'mt-MT',
        'mua-CM',
        'my',
        'my-MM',
        'myv-RU',
        'mzn-IR',
        'na',
        'naq-NA',
        'nb',
        'nb-NO',
        'nb-SJ',
        'nd',
        'nd-ZW',
        'nds-DE',
        'nds-NL',
        'ne',
        'ne-IN',
        'ne-NP',
        'ng',
        'nl',
        'nl-AW',
        'nl-BE',
        'nl-BQ',
        'nl-CW',
        'nl-NL',
        'nl-SR',
        'nl-SX',
        'nmg-CM',
        'nn',
        'nn-NO',
        'nnh-CM',
        'no',
        'nqo-GN',
        'nr',
        'nr-ZA',
        'nso-ZA',
        'nus-SS',
        'nv',
        'ny',
        'ny-MW',
        'nyn-UG',
        'oc',
        'oc-FR',
        'oj',
        'om',
        'om-ET',
        'om-KE',
        'or',
        'or-IN',
        'os',
        'os-GE',
        'os-RU',
        'pa',
        'pa-IN',
        'pa-PK',
        'pa-PK',
        'pi',
        'pl',
        'pl-PL',
        'ps',
        'ps-AF',
        'ps-PK',
        'pt',
        'pt-AO',
        'pt-BR',
        'pt-CH',
        'pt-CV',
        'pt-FR',
        'pt-GQ',
        'pt-GW',
        'pt-LU',
        'pt-MO',
        'pt-MZ',
        'pt-PT',
        'pt-ST',
        'pt-TL',
        'qu',
        'qu-BO',
        'qu-EC',
        'qu-PE',
        'rm',
        'rm-CH',
        'rn',
        'rn-BI',
        'ro',
        'ro-MD',
        'ro-RO',
        'rof-TZ',
        'ru',
        'ru-BY',
        'ru-KG',
        'ru-KZ',
        'ru-MD',
        'ru-RU',
        'ru-UA',
        'rw',
        'rw-RW',
        'rwk-TZ',
        'sa',
        'sa-IN',
        'sah-RU',
        'saq-KE',
        'sat-IN',
        'sat-IN',
        'sbp-TZ',
        'sc',
        'sc-IT',
        'scn-IT',
        'sd',
        'sd-PK',
        'se',
        'se-FI',
        'se-NO',
        'se-SE',
        'seh-MZ',
        'ses-ML',
        'sg',
        'sg-CF',
        'shi-MA',
        'shi-MA',
        'si',
        'si-LK',
        'sk',
        'sk-SK',
        'sl',
        'sl-SI',
        'sm',
        'smn-FI',
        'sn',
        'sn-ZW',
        'so',
        'so-DJ',
        'so-ET',
        'so-KE',
        'so-SO',
        'sq',
        'sq-AL',
        'sq-MK',
        'sq-XK',
        'sr',
        'sr-BA',
        'sr-BA',
        'sr-ME',
        'sr-ME',
        'sr-RS',
        'sr-RS',
        'sr-XK',
        'sr-XK',
        'ss',
        'ss-SZ',
        'ss-ZA',
        'st',
        'st-LS',
        'st-ZA',
        'su',
        'sv',
        'sv-AX',
        'sv-FI',
        'sv-SE',
        'sw',
        'sw-CD',
        'sw-KE',
        'sw-TZ',
        'sw-UG',
        'syr-IQ',
        'syr-SY',
        'ta',
        'ta-IN',
        'ta-LK',
        'ta-MY',
        'ta-SG',
        'te',
        'te-IN',
        'teo-KE',
        'teo-UG',
        'tg',
        'tg-TJ',
        'th',
        'th-TH',
        'ti',
        'ti-ER',
        'ti-ET',
        'tig-ER',
        'tk',
        'tk-TM',
        'tl',
        'tn',
        'tn-BW',
        'tn-ZA',
        'to',
        'to-TO',
        'tr',
        'tr-CY',
        'tr-TR',
        'trv-TW',
        'ts',
        'ts-ZA',
        'tt',
        'tt-RU',
        'tw',
        'twq-NE',
        'ty',
        'tzm-MA',
        'ug',
        'ug-CN',
        'uk',
        'uk-UA',
        'ur',
        'ur-IN',
        'ur-IN',
        'ur-IN',
        'ur-PK',
        'ur-PK',
        'ur-PK',
        'uz',
        'uz-AF',
        'uz-UZ',
        'uz-UZ',
        'vai-LR',
        'vai-LR',
        've',
        've-ZA',
        'vi',
        'vi-VN',
        'vo',
        'vun-TZ',
        'wa',
        'wa-BE',
        'wae-CH',
        'wal-ET',
        'wo',
        'wo-SN',
        'xh',
        'xh-ZA',
        'xog-UG',
        'yav-CM',
        'yi',
        'yi-001',
        'yo',
        'yo-BJ',
        'yo-NG',
        'yue-CN',
        'yue-HK',
        'za',
        'zgh-MA',
        'zh',
        'zh-CN',
        'zh-CN',
        'zh-HK',
        'zh-HK',
        'zh-MO',
        'zh-MO',
        'zh-SG',
        'zh-TW',
        'zu',
        'zu-ZA'];
}

function inv_keyispressed(e) {
    var idname = $('#editInvariantModal #idname').val();
    if (idname === "COUNTRY" || idname === "ENVIRONMENT" || idname === "SYSTEM") {
        var toto = "|:| |(|)|é|à|è|ê|\"|'|&|<|>|ù|&|#|{|[|`|\|ç|^|@|]|}|=|$|£|µ|*|!|.|;|,|?|§|/|%|°|+|";
        var charval = "|" + e.key + "|";
        if (toto.indexOf(charval) !== -1) {
            var localMessage = new Message("WARNING", "Character '" + e.key + "' is not allowed for idnames COUNTRY, ENVIRONMENT and SYSTEM. Please use numeric or letter.");
            showMessage(localMessage, $('#editInvariantModal'), false, 1000);
            return false;
        }
    }
    return true;
}

