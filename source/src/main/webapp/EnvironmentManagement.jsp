<%--
  ~ Cerberus  Copyright (C) 2013  vertigo17
  ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  ~
  ~ This file is part of Cerberus.
  ~
  ~ Cerberus is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ Cerberus is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@page import="org.cerberus.service.IDocumentationService"%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Environment Management</title>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.filter.css">

        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/jquery.multiselect.filter.js"></script>
        <script type="text/javascript" src="js/jquery.form.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.columnFilter.js"></script>

        <style>
            #contents {
                margin: 3px;
                vertical-align: top;
                display: inline-block;
                *zoom: 1;
                *display: inline;
                width: 100%;
            }

            .formForDataTable {
                display: none;
            }

            #testbatteries {
                margin: 10px 0;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
        <%
            IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        %>
        <p class="dttTitle">Environments Per Country</p>
        <div style="width: 100%; font: 90% sans-serif">
            <table  class="display" id="countryEnvironmentTable" name="countryEnvironmentTable">
                <thead>
                    <tr>
                        <th>id</th>
                        <th><%=docService.findLabel("Invariant", "Country", "")%></th>
                        <th><%=docService.findLabel("Invariant", "Environment", "")%></th>
                        <th><%=docService.findLabel("buildrevisioninvariant", "versionname01", "")%></th>
                        <th><%=docService.findLabel("buildrevisioninvariant", "versionname02", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "Chain", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "DistribList", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "EmailBodyRevision", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "Type", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "EmailBodyChain", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "EmailBodyDisableEnvironment", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "Active", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "MaintenanceAct", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "MaintenanceStr", "")%></th>
                        <th><%=docService.findLabel("countryenvparam", "MaintenanceEnd", "")%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
                <tfoot>
                    <tr>
                        <th>System</th>
                        <th>Country</th>
                        <th>Env</th>
                        <th>Build</th>
                        <th>Revision</th>
                        <th>Chain</th>
                        <th>DistribList</th>
                        <th>EmailBodyRevision</th>
                        <th>Type</th>
                        <th>EmailBodyChain</th>
                        <th>EmailBodyDisableEnvironment</th>
                        <th>Active</th>
                        <th>MaintenanceAct</th>
                        <th>MaintenanceStr</th>
                        <th>MaintenanceEnd</th>
                    </tr>
                </tfoot>
            </table>
        </div>
        <br>
        <div id="childDiv" style="width: 49%;float:left; font: 90% sans-serif">
            <p class="dttTitle">Applications Per Environment</p>
            <table  class="display" id="countryEnvironmentApplicationTable" name="countryEnvironmentApplicationTable">
                <thead>
                    <tr>
                        <th><%=docService.findLabel("Application", "System", "")%></th>
                        <th><%=docService.findLabel("Application", "Country", "")%></th>
                        <th><%=docService.findLabel("Application", "Environment", "")%></th>
                        <th><%=docService.findLabel("Application", "Application", "")%></th>
                        <th><%=docService.findLabel("countryenvironmentparameters", "Ip", "")%></th>
                        <th><%=docService.findLabel("countryenvironmentparameters", "Domain", "")%></th>
                        <th><%=docService.findLabel("countryenvironmentparameters", "Url", "")%></th>
                        <th><%=docService.findLabel("countryenvironmentparameters", "UrlLogin", "")%></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
                <tfoot>
                    <tr>
                        <th>System</th>
                        <th>Country</th>
                        <th>Env</th>
                        <th>Application</th>
                        <th>Ip</th>
                        <th>Domain</th>
                        <th>Url</th>
                        <th>UrlLogin</th>
                    </tr>
                </tfoot>
            </table>
        </div>
        <div id="childDiv2"  style="width: 49%;float:right; font: 90% sans-serif">
            <p class="dttTitle">Connection Pool</p>
            <table  class="display" id="countryEnvironmentDatabaseTable" name="countryEnvironmentDatabaseTable">
                <thead>
                    <tr>
                        <th>id</th>
                        <th><%=docService.findLabel("Invariant", "Country", "")%></th>
                        <th><%=docService.findLabel("Invariant", "Environment", "")%></th>
                        <th>Database</th>
                        <th>ConnectionPoolName</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
                <tfoot>
                    <tr>
                        <th>System</th>
                        <th>Country</th>
                        <th>Env</th>
                        <th>Database</th>
                        <th>ConnectionPoolName</th>
                    </tr>
                </tfoot>
            </table>
        </div>
        <script>
            var oTable, oTableContent;
            function refreshTable() {
                $('#childDiv').hide();
                $('#childDiv2').hide();

                oTable = $('#countryEnvironmentTable').dataTable({
                    "bServerSide": false,
                    "sAjaxSource": "GetCountryEnvParamList?System=<%=request.getAttribute("MySystem")%>",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 10,
                    "bUseColVis":true,
                    "aoColumns": [
                        {"sName": "id", "sWidth": "5%", bVisible: false},
                        {"sName": "Country", "sWidth": "5%"},
                        {"sName": "Environment", "sWidth": "5%"},
                        {"sName": "Build", "sWidth": "5%"},
                        {"sName": "Revision", "sWidth": "5%"},
                        {"sName": "Chain", "sWidth": "5%"},
                        {"sName": "DistribList", "sWidth": "5%"},
                        {"sName": "EmailBodyRevision", "sWidth": "5%"},
                        {"sName": "Type", "sWidth": "5%"},
                        {"sName": "EmailBodyChain", "sWidth": "5%"},
                        {"sName": "EmailBodyDisableEnvironment", "sWidth": "5%"},
                        {"sName": "Active", "sWidth": "5%"},
                        {"sName": "MaintenanceActive", "sWidth": "5%"},
                        {"sName": "MaintenanceStart", "sWidth": "5%"},
                        {"sName": "MaintenanceEnd", "sWidth": "5%"}
                    ]
                }).makeEditable({
                    sAddURL: "CreateCountryEnvParam",
                    sAddNewRowFormId: "formAddNewCountryEnvParam",
                    sAddNewRowButtonId: "btnAddNewCountryEnvParam",
                    sAddNewRowOkButtonId: "btnAddNewCountryEnvParamOk",
                    sAddNewRowCancelButtonId: "btnAddNewCountryEnvParamCancel",
                    sAddDeleteToolbarSelector: "#countryEnvironmentTable_length",
                    sDeleteRowButtonId: "btnDeleteCountryEnvParam",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create CountryEnvParam</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add CountryEnvParam Entry',
                        show: "blind",
                        hide: "blind",
                        width: "700px"
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteCountryEnvParam",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateCountryEnvParam",
                    fnOnEdited: function() {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=COUNTRY',
                            loadtype: 'GET',
                            submit: 'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=ENVIRONMENT',
                            loadtype: 'GET',
                            submit: 'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            placeholder: '',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=APPLITYPE',
                            loadtype: 'GET',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            placeholder: '',
                            type: 'select',
                            loadurl: 'GetDeployTypeList',
                            loadtype: 'GET',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            placeholder: '',
                            type: 'select',
                            loadurl: 'GetDeployTypeList',
                            loadtype: 'GET',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        },
                        {
                            placeholder: '',
                            submit: 'Save changes'
                        }, {
                            placeholder: '',
                            submit: 'Save changes'
                        }
                    ]
                }).columnFilter();

                /* Add a click handler to the rows - this could be used as a callback */
                $('#countryEnvironmentTable').find('tbody').click(function(event) {
                    refreshContents($(event.target.parentNode).attr("id"));
                });
            }

            function refreshContents(id) {
                $('#childDiv').hide();
                $('#childDiv2').hide();

                var country = $('#countryEnvironmentTable').find("tr[id='" + id + "']").find('td:nth-child(1)').text();
                var env = $('#countryEnvironmentTable').find("tr[id='" + id + "']").find('td:nth-child(2)').text();
                $('#AddNewCountryEnvironmentParameterSystem').val($("#MySystem option:selected").val());
                $('#AddNewCountryEnvironmentParameterCountry').val(country);
                $('#AddNewCountryEnvironmentParameterEnvironment').val(env);

                // This example is fairly pointless in reality, but shows how fnDestroy can be used
                if (oTableContent) {
                    var list = $('#countryEnvironmentApplicationTable');
                    list.find('tbody').empty();
                    list.dataTable().fnDestroy();
                }

                $.getJSON("GetCountryEnvironmentParameterList", "System=<%=request.getAttribute("MySystem")%>&Country=" + country + "&Environment=" + env, function(data) {

                    oTableContent = $('#countryEnvironmentApplicationTable').dataTable({
                        "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bDestroy": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 10,
                        "bUseColVis":true,            
                        "aoColumns": [
                            {"sName": "System", "sWidth": "5%", bVisible: false},
                            {"sName": "Country", "sWidth": "5%", bVisible: false},
                            {"sName": "Environment", "sWidth": "10%", bVisible: false},
                            {"sName": "Application", "sWidth": "10%"},
                            {"sName": "Ip", "sWidth": "5%"},
                            {"sName": "Domain", "sWidth": "5%"},
                            {"sName": "Url", "sWidth": "5%"},
                            {"sName": "UrlLogin", "sWidth": "5%"},
                        ],
                        "aaData": data.aaData
                    }).makeEditable({
                        sAddURL: "CreateCountryEnvironmentParameter",
                        sAddNewRowFormId: "formAddNewCountryEnvironmentParameter",
                        sAddNewRowButtonId: "btnAddNewCountryEnvironmentParameter",
                        sAddNewRowOkButtonId: "btnAddNewCountryEnvironmentParameterOk",
                        sAddNewRowCancelButtonId: "btnAddNewCountryEnvironmentParameterCancel",
                        sAddDeleteToolbarSelector: "#countryEnvironmentApplicationTable_length",
                        sDeleteRowButtonId: "btnDeleteCountryEnvironmentParameter",
                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Configure Application for Environment</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Configure Application for Environment',
                            show: "blind",
                            hide: "blind",
                            width: "900px",
                            close: function() {
                                refreshContents(id);
                            }
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteCountryEnvironmentParameter",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary: 'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateCountryEnvironmentParameter",
                        fnOnEdited: function() {
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            },
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            },
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            },
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            },
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            }
                        ]
                    }).columnFilter();
                    $('#childDiv').show();
                });

                $.getJSON("FindCountryEnvironmentDatabase", "System=<%=request.getAttribute("MySystem")%>&Country=" + country + "&Environment=" + env, function(data) {

                    oTableContent = $('#countryEnvironmentDatabaseTable').dataTable({
                        "aaSorting": [[0, "asc"]],
                        "bServerSide": false,
                        "bJQueryUI": true,
                        "bProcessing": true,
                        "bDestroy": true,
                        "bPaginate": true,
                        "bAutoWidth": false,
                        "sPaginationType": "full_numbers",
                        "bSearchable": true,
                        "aTargets": [0],
                        "iDisplayLength": 10,
                        "bUseColVis":true,
                        "aoColumns": [
                            {"sName": "id", "sWidth": "5%", bVisible: false},
                            {"sName": "Country", "sWidth": "5%"},
                            {"sName": "Environment", "sWidth": "5%"},
                            {"sName": "Database", "sWidth": "5%"},
                            {"sName": "ConnectionPoolName", "sWidth": "5%"}
                        ],
                        "aaData": data.aaData
                    }).makeEditable({
                        sAddURL: "CreateCountryEnvironmentDatabase",
                        sAddNewRowFormId: "formAddNewCountryEnvironmentDatabase",
                        sAddNewRowButtonId: "btnAddNewCountryEnvironmentDatabase",
                        sAddNewRowOkButtonId: "btnAddNewCountryEnvironmentDatabaseOk",
                        sAddNewRowCancelButtonId: "btnAddNewCountryEnvironmentDatabaseCancel",
                        sAddDeleteToolbarSelector: "#countryEnvironmentDatabaseTable_length",
                        sDeleteRowButtonId: "btnDeleteCountryEnvironmentDatabase",
                        sAddHttpMethod: "POST",
                        oAddNewRowButtonOptions: {
                            label: "<b>Create Connection Pool</b>",
                            background: "#AAAAAA",
                            icons: {primary: 'ui-icon-plus'}
                        },
                        oAddNewRowFormOptions: {
                            title: 'Add Connection Pool',
                            show: "blind",
                            hide: "blind",
                            width: "700px"
                        },
                        sDeleteHttpMethod: "POST",
                        sDeleteURL: "DeleteCountryEnvironmentDatabase",
                        oDeleteRowButtonOptions: {
                            label: "Remove",
                            icons: {primary: 'ui-icon-trash'}
                        },
                        sUpdateURL: "UpdateCountryEnvironmentDatabase",
                        fnOnEdited: function() {
                            $(".dataTables_processing").css('visibility', 'hidden');
                        },
                        "aoColumns": [
                            {
                                loadtext: 'loading...',
                                type: 'select',
                                loadurl: 'GetInvariantList?idName=COUNTRY',
                                loadtype: 'GET',
                                submit: 'Save changes'
                            },
                            {
                                loadtext: 'loading...',
                                type: 'select',
                                loadurl: 'GetInvariantList?idName=ENVIRONMENT',
                                loadtype: 'GET',
                                submit: 'Save changes'
                            },
                            {
                                loadtext: 'loading...',
                                placeholder: '',
                                type: 'select',
                                loadurl: 'GetInvariantList?idName=PROPERTYDATABASE',
                                loadtype: 'GET',
                                submit: 'Save changes'
                            },
                            {
                                placeholder: '',
                                submit: 'Save changes'
                            }
                        ]
                    }).columnFilter();
                    $('#childDiv2').show();
                });


            }

            $(document).ready(function() {
                refreshTable();

            });
        </script>
        <form id="formAddNewCountryEnvParam" class="formForDataTable" action="#" title="Add Country Environment Entry" style="width:600px" method="post">
            <label for="System" rel="0" style="font-weight:bold"><%=docService.findLabelHTML("Application", "System", "")%></label>
            <%=ComboInvariantAjax(appContext, "System", "", "System", "0", "SYSTEM", "", "", false)%>
            <label for="Country" rel="1" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Country", "")%></label>
            <%=ComboInvariantAjax(appContext, "Country", "", "Country", "1", "COUNTRY", "", "", false)%>
            <label for="Environment" rel="2" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Environment", "")%></label>
            <%=ComboInvariantAjax(appContext, "Environment", "", "Environment", "2", "ENVIRONMENT", "", "", false)%>
            <br><br>
            <label for="Build" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "versionname01", "")%></label>
            <input id="Build" name="Build" style="width:200px;" 
                   class="ncdetailstext" rel="4" >
            <label for="Revision" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "versionname02", "")%></label>
            <input id="Revision" name="Revision" style="width:200px;" 
                   class="ncdetailstext" rel="3" >
            <br><br>
            <label for="Chain" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "Chain", "")%></label>
            <input id="Chain" name="Chain" style="width:100px;" 
                   class="ncdetailstext" rel="7" >
            <label for="Type" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "Type", "")%></label>
            <%=ComboDeployTypeAjax(appContext, "DeployType", "", "DeployType", "6", "", "")%>
            <br><br>
            <label for="DistribList" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "DistribList", "")%></label>
            <input id="DistribList" name="DistribList" style="width:500px;" 
                   class="ncdetailstext" rel="5" >
            <br><br>
            <label for="EmailBodyRevision" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "EmailBodyRevision", "")%></label>
            <input id="EmailBodyRevision" name="EmailBodyRevision" style="width:600px;" 
                   class="ncdetailstext" rel="14" >
            <br><br>
            <label for="EmailBodyChain" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "EmailBodyChain", "")%></label>
            <input id="EmailBodyChain" name="EmailBodyChain" style="width:500px;" 
                   class="ncdetailstext" rel="8" >
            <br><br>
            <label for="EmailBodyDisableEnvironment" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "EmailBodyDisableEnvironment", "")%></label>
            <input id="EmailBodyDisableEnvironment" name="EmailBodyDisableEnvironment" style="width:500px;" 
                   class="ncdetailstext" rel="9" >
            <br><br>
            <label for="Active" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "Active", "")%></label>
            <%=ComboInvariantAjax(appContext, "Active", "", "Active", "10", "TCACTIVE", "", "", false)%>
            <br><br> <label for="MaintenanceAct" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "MaintenanceAct", "")%></label>
            <%=ComboInvariantAjax(appContext, "MaintenanceAct", "", "MaintenanceAct", "11", "TCACTIVE", "", "", false)%>
            <label for="MaintenanceStr" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "MaintenanceStr", "")%></label>
            <input id="MaintenanceStr" name="MaintenanceStr" style="width:50px;" 
                   class="ncdetailstext" rel="12" >
            <label for="MaintenanceEnd" style="font-weight:bold"><%=docService.findLabelHTML("countryenvparam", "MaintenanceEnd", "")%></label>
            <input id="MaintenanceEnd" name="MaintenanceEnd" style="width:50px;" 
                   class="ncdetailstext" rel="13" >
            <br><br>
            <button id="btnAddNewCountryEnvParamOk">Add</button>
            <button id="btnAddNewCountryEnvParamCancel">Cancel</button>
        </form>
        <form id="formAddNewCountryEnvironmentParameter" class="formForDataTable" title="Add Content Entry" style="width:900px; height:600px" method="post">
            <label for="AddNewCountryEnvironmentParameterSystem" style="font-weight:bold"><%=docService.findLabelHTML("Application", "System", "")%></label>
            <input id="AddNewCountryEnvironmentParameterSystem" name="System" style="width:150px;" 
                   class="ncdetailstext" rel="0" readonly>
            <label for="AddNewCountryEnvironmentParameterCountry" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Country", "")%></label>
            <input id="AddNewCountryEnvironmentParameterCountry" name="Country" style="width:150px;" 
                   class="System" rel="1" value="<%=request.getAttribute("Country")%>" readonly>
            <label for="AddNewCountryEnvironmentParameterEnvironment" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Environment", "")%></label>
            <input id="AddNewCountryEnvironmentParameterEnvironment" name="Environment" style="width:100px;" 
                   class="ncdetailstext" rel="2" readonly>
            <br>
            <br>
            <br>
            <br>
            <label for="Application" style="font-weight:bold"><%=docService.findLabelHTML("Application", "Application", "")%></label>
            <input id="Application" name="Application" style="width:400px;" 
                   class="ncdetailstext" rel="3" >
            <br><br>
            <label for="IP" style="font-weight:bold"><%=docService.findLabelHTML("countryenvironmentparameters", "IP", "")%></label>
            <input id="IP" name="IP" style="width:100px;" 
                   class="ncdetailstext" rel="4" >
            <br><br>
            <label for="Domain" style="font-weight:bold"><%=docService.findLabelHTML("countryenvironmentparameters", "domain", "")%></label>
            <input id="domain" name="domain" style="width:100px;" 
                   class="ncdetailstext" rel="5" >
            <br><br>
            <label for="Url" style="font-weight:bold"><%=docService.findLabelHTML("countryenvironmentparameters", "url", "")%></label>
            <input id="Url" name="Url" style="width:400px;" 
                   class="ncdetailstext" rel="6" >
            <br><br>
            <label for="UrlLogin" style="font-weight:bold"><%=docService.findLabelHTML("countryenvironmentparameters", "urllogin", "")%></label>
            <input id="UrlLogin" name="UrlLogin" style="width:600px;" 
                   class="ncdetailstext" rel="7" >
            <br><br>
            <button id="btnAddNewCountryEnvironmentParameterOk">Add</button>
            <button id="btnAddNewCountryEnvironmentParameterCancel">Cancel</button>
        </form>
            <form id="formAddNewCountryEnvironmentDatabase" class="formForDataTable" action="#" title="Add Connection Pool" style="width:600px" method="post">
                <label for="System" rel="0" style="font-weight:bold"><%=docService.findLabelHTML("Application", "System", "")%></label>
                <%=ComboInvariantAjax(appContext, "System", "", "System", "0", "SYSTEM", "", "", false)%>
                <label for="Country" rel="1" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Country", "")%></label>
                <%=ComboInvariantAjax(appContext, "Country", "", "Country", "1", "COUNTRY", "", "", false)%>
                <label for="Environment" rel="2" style="font-weight:bold"><%=docService.findLabelHTML("Invariant", "Environment", "")%></label>
                <%=ComboInvariantAjax(appContext, "Environment", "", "Environment", "2", "ENVIRONMENT", "", "", false)%>
                <br><br>
                <label for="Database" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "Database", "")%></label>
                <input id="Database" name="Database" style="width:200px;" 
                       class="ncdetailstext" rel="3" >
                <label for="ConnectionPoolName" style="font-weight:bold"><%=docService.findLabelHTML("buildrevisioninvariant", "ConnectionPoolName", "")%></label>
                <input id="ConnectionPoolName" name="ConnectionPoolName" style="width:200px;" 
                       class="ncdetailstext" rel="4" >
                <br><br>
                <button id="btnAddNewCountryEnvironmentDatabaseOk">Add</button>
                <button id="btnAddNewCountryEnvironmentDatabaseCancel">Cancel</button>
            </form>
    </body>
</html>
l