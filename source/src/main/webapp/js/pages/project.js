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

$.when($.getScript("js/pages/global.js")).then(function () {
    $(document).ready(function () {

        // handle the click for specific action buttons
        $("#addProjectButton").click(saveNewProjectHandler);

        //clear the modals fields when closed
        $('#addProjectModal').on('hidden.bs.modal', addProjectModalCloseHandler);

        //configure and create the dataTable
        var configurations = new TableConfigurationsServerSide("projectsTable", "ReadProject", "contentTable", aoColumnsFunc());

        createDataTableWithPermissions(configurations, renderOptionsForProject);
    });
});

function saveNewProjectHandler() {
    clearResponseMessage($('#addProjectModal'));
    var formAdd = $("#addProjectModal #addProjectModalForm");

    var nameElement = formAdd.find("#idProject");
    var nameElementEmpty = nameElement.prop("value") === '';
    if (nameElementEmpty) {
        var localMessage = new Message("danger", "Please specify the name of the project!");
        nameElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addProjectModal'));
    } else {
        nameElement.parents("div.form-group").removeClass("has-error");
    }

    var codeElement = formAdd.find("#VCCode");
    var codeElementEmpty = codeElement.prop("value") === '';
    if (codeElementEmpty) {
        var localMessage = new Message("danger", "Please specify the code of the project!");
        codeElement.parents("div.form-group").addClass("has-error");
        showMessage(localMessage, $('#addProjectModal'));
    } else {
        codeElement.parents("div.form-group").removeClass("has-error");
    }

    // verif if all mendatory fields are not empty
    if (nameElementEmpty || codeElementEmpty)
        return;

    showLoaderInModal('#addProjectModal');
    var jqxhr = $.post("CreateProject", formAdd.serialize());
    $.when(jqxhr).then(function (data) {
        hideLoaderInModal('#addProjectModal');
        console.log(data.messageType);
        if (getAlertType(data.messageType) === 'success') {
            var oTable = $("#projectsTable").dataTable();
            //redraws table and goes to last page
            //It is possible to go directly to the last page because that is order by id
            //oTable.fnPageChange( 'last' );
            oTable.fnDraw(true);
            showMessage(data);
            $('#addProjectModal').modal('hide');
        } else {
            showMessage(data, $('#addProjectModal'));
        }
    }).fail(handleErrorAjaxAfterTimeout);
}

function addProjectModalCloseHandler() {
    // reset form values
    $('#addProjectModal #addProjectModalForm')[0].reset();
    // remove all errors on the form fields
    $(this).find('div.has-error').removeClass("has-error");
    // clear the response messages of the modal
    clearResponseMessage($('#addProjectModal'));
}

function CreateProjectClick() {
    clearResponseMessageMainPage();
    $('#addProjectModal').modal('show');
}

function editProject(object) {
    clearResponseMessageMainPage();
    console.log(object);
}

function renderOptionsForProject(data) {

    //check if user has permissions to perform the add and import operations
    if (data["hasPermissions"]) {
        if ($("#createProjectButton").length === 0) {
            var contentToAdd = "<div class='marginBottom10'><button id='createProjectButton' type='button' class='btn btn-default'>\n\
            Create new project</button></div>";

            $("#projectsTable_wrapper div.ColVis").before(contentToAdd);
            $('#project #createProjectButton').click(CreateProjectClick);
        }
    }
}

function aoColumnsFunc() {
    var aoColumns = [
        {"data": "button",
            className: "width150",
            "sName": "button",
            "bSortable": false,
            "bSearchable": false,
            "mRender": function (data, type, obj) {
                var editProject = '<button id="editProject" onclick="editProject(\'' + obj + '\');"\n\
                                class="editProject btn btn-default btn-xs margin-right5" \n\
                                name="editProject" title="Edit project" type="button">\n\
                                <span class="glyphicon glyphicon-pencil"></span></button>';
                var deleteProject = '<button id="deleteProject" \n\
                                class="deleteProject btn btn-default btn-xs margin-right5" \n\
                                name="deleteProject" title="Delete project" type="button">\n\
                                <span class="glyphicon glyphicon-trash"></span></button>';

                return '<div class="center btn-group width150">' + editProject + deleteProject + '</div>';
            }
        },
        {"data": "idProject",
            className: "width250",
            "sName": "idProject"},
        {"data": "code",
            className: "width80",
            "sName": "VCCode"},
        {"data": "description",
            className: "width350",
            "sName": "description"},
        {"data": "active",
            className: "width80",
            "sName": "active"},
        {"data": "dateCreation",
            className: "width250",
            "sName": "dateCre"}
    ];
    return aoColumns;
}