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
 * Open the modal with testcase information.
 * @param {String} parameter - parameter key to open the modal.
 * @param {String} system - system to open the parameter.
 * @returns {null}
 */
function openModalParameter(parameter, system) {

    // We only load the Labels and bind the events once for performance optimisations.
    if ($('#editParameterModal').data("initLabel") === undefined) {
        initModalParameter(system);
        $('#editParameterModal').data("initLabel", true);
    }

    editParameterClick(parameter, system);
}

/***
 * Initialise modal labels.
 * @param {String} system - system selected
 * @returns {null}
 */
function initModalParameter(system) {

    var doc = new Doc();
    $("#title").html(doc.getDocLabel("page_parameter", "allParameters"));
    $("[name='editParameterField']").html(doc.getDocLabel("page_parameter", "editparameter_field"));
    $("[name='parameterField']").html(doc.getDocLabel("page_parameter", "parameter_field"));
    $("[name='cerberusField']").html(doc.getDocLabel("page_parameter", "cerberus_field"));
    $("[name='descriptionField']").html(doc.getDocLabel("page_parameter", "description_field"));
    $("[name='buttonClose']").html(doc.getDocLabel("page_parameter", "close_btn"));
    $("[name='buttonAdd']").html(doc.getDocLabel("page_parameter", "save_btn"));

    $("#editParameterButton").off("click");
    $("#editParameterButton").click(function () {
        confirmParameterModalHandler(system);
    });
    //clear the modals fields when closed
    $('#editParameterModal').on('hidden.bs.modal', editParameterModalCloseHandler);
}

function editParameterModalCloseHandler() {
    // reset form values
    $('#editParameterModal #editParameterModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#editParameterModal'));
}

/***
 * Open the modal with testcase information.
 * @param {String} parameter - type selected
 * @param {String} system - system selected
 * @returns {null}
 */
function editParameterClick(parameter, system) {

    clearResponseMessage($('#editParameterModal'));

    // When editing the execution queue, we can modify, modify and run or cancel.
    $('#saveParameterButton').attr('class', 'btn btn-primary');
    $('#saveParameterButton').removeProp('hidden');
    $('#cancelParameterButton').attr('class', 'btn btn-primary');
    $('#cancelParameterButton').removeProp('hidden');

    feedParameterModal(parameter, system, "editParameterModal");
}

/***
 * Function that support the modal confirmation. Will call servlet to comit the transaction.
 * @param {String} system - system selected
 * @returns {null}
 */
function confirmParameterModalHandler(system) {
    clearResponseMessage($('#editParameterModal'));
    var formEdit = $('#editParameterModal #editParameterModalForm');

    var sa = formEdit.serializeArray();
    var data = {}
    for (var i in sa) {
        data[sa[i].name] = sa[i].value;
    }
    // Get the header data from the form.
    //var data = convertSerialToJSONObject(formEdit.serialize());
    showLoaderInModal('#editParameterModal');
    $.ajax({
        url: "UpdateParameter",
        async: true,
        method: "POST",
        data: {id: data.parameter,
            value: data.cerberusValue,
            system1Value: data.systemValue,
            system1: system},
        success: function (data) {
            data = JSON.parse(data);
            hideLoaderInModal('#editParameterModal');
            if (getAlertType(data.messageType) === 'success') {
                var oTable = $("#parametersTable").dataTable();
                oTable.fnDraw(false);
                $('#editParameterModal').modal('hide');
                showMessage(data);
            } else {
                showMessage(data, $('#editParameterModal'));
            }
        },
        error: showUnexpectedError
    });

}

/***
 * Feed the TestCase modal with all the data from the TestCase.
 * @param {String} param - id of the execution queue to load
 * @param {String} system - system to load
 * @param {String} modalId - modal id to feed.
 * @returns {null}
 */
function feedParameterModal(param, system, modalId) {
    clearResponseMessageMainPage();
    var doc = new Doc();

    var jqxhr = $.getJSON("ReadParameter", "system1=" + system + "&param=" + param);
    $.when(jqxhr).then(function (data) {
        var obj = data["contentTable"];

        var formEdit = $('#editParameterModal');

        formEdit.find("#parameter").prop("value", obj["param"]);
        formEdit.find("#cerberusValue").prop("value", obj["value"]);
        formEdit.find("#systemValue").prop("value", obj["system1value"]);
        formEdit.find("#description").html(obj["description"]);

        $("[name='systemField']").html(doc.getDocLabel("page_parameter", "system_field") + " (" + system + ")");

        if (data.isSecured) {
            var localMessage = new Message("WARNING", "This parameter contain secure data. Original data is hidden.");
            showMessage(localMessage, $('#editParameterModal'));
        }

        $("#cerberusValue").off("change");
        if (!(data["hasPermissions"] && obj["hasPermissionsUpdate"])) { // If readonly, we only readonly all fields
            formEdit.find("#cerberusValue").prop("readonly", "readonly");

            $('#editParameterButton').hide();
        } else {
            formEdit.find("#cerberusValue").removeAttr("readonly");
            formEdit.find("#editParameterButton").show();
//            if (data.isSecured) {
                $('#editParameterButton').attr('disabled', false);
//                if (data.isSystemManaged) {
//                    $("#cerberusValue").change(function () {
//                        if (($("#cerberusValue").val() !== "XXXXXXXXXX") && ($("#systemValue").val() !== "XXXXXXXXXX"))
//                        {
//                            $('#editParameterButton').attr('disabled', false);
//                        } else {
//                            $('#editParameterButton').attr('disabled', true);
//                        }
//                    });
//                    $("#systemValue").change(function () {
//                        if (($("#cerberusValue").val() !== "XXXXXXXXXX") && ($("#systemValue").val() !== "XXXXXXXXXX"))
//                        {
//                            $('#editParameterButton').attr('disabled', false);
//                        } else {
//                            $('#editParameterButton').attr('disabled', true);
//                        }
//                    });

//                } else {
//                    $("#cerberusValue").change(function () {
//                        $('#editParameterButton').attr('disabled', false);
//                    });
//
//                }
//            } else {
//                $('#editParameterButton').attr('disabled', false);
//            }
        }
        if (data.isSystemManaged) {
            $("#systemValuePanel").show();
        } else {
            $("#systemValuePanel").hide();
        }

        formEdit.modal('show');
    });

}


