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
<% Date DatePageStart = new Date();%>

<!DOCTYPE html>
<html>
    <head>
        <title>SQL Library</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <script type="text/javascript">

function refreshTable() {
    $('#tableDiv').hide();
                var oTable = $('#sqlLibraryList').dataTable({
                    "aaSorting": [[1, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllSqlLibrary",
                    "bJQueryUI": true,
                    "bProcessing": false,
                    "bPaginate": true,
                    "bDestroy":true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "Name", "sWidth": "10%"},
                        {"sName": "Type", "sWidth": "10%"},
                        {"sName": "Database", "sWidth": "10%"},
                        {"sName": "Script", "sWidth": "35%"},
                        {"sName": "Description", "sWidth": "35%"}
                        
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateSqlLibrary",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create SQL Data...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteSqlLibrary",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateSqlLibrary",
                    fnOnEdited: function() {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add SQL Data',
                        show: "blind",
                        hide: "explode",
                        width: "900px",
                        close: function() {
                                refreshTable();
                            }
                    },
                    "aoColumns": [
                        null,
                        {onblur: 'submit',
                            placeholder: ''}, 
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=PROPERTYDATABASE',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''}

                    ]
                })
                $('#tableDiv').show();
            }
            
            $(document).ready(function(){
                refreshTable();

            });


        </script>

    </head>
    <body  id="wrapper">
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <p class="dttTitle">SQL Library</p>
        <div id="tableDiv" style="width: 100%; font: 90% sans-serif">
            <table id="sqlLibraryList" class="display">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Database</th>
                        <th>Script</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add SQL Data" style="width:350px" method="post">
                <label for="Name" style="font-weight:bold">Name</label>
                <input id="Name" name="Name" style="width:350px;" 
                       class="ncdetailstext" rel="0" >
                <label for="Type" style="font-weight:bold">Type</label>
                <input id="Type" name="Type" style="width:350px;" 
                       class="ncdetailstext" rel="1" >
                <br />
                <br />
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" style="width:750px;" 
                       class="ncdetailstext" rel="4" >
                <br />
                <br />
                <label for="Database" style="font-weight:bold">Database</label>
                <%=ComboInvariantAjax(appContext, "Database", "", "Database", "2", "PROPERTYDATABASE", "", "", false)%>
                <br />
                <label for="Script" style="font-weight:bold">Script</label>
                <textarea id="Script" name="Script" style="width:800px;" rows="5" 
                       class="ncdetailstext" rel="3" ></textarea>
                <br />
                <br />
                <div style="width: 250px; float:right">
                    <button id="btnAddNewRowOk">Add</button>
                    <button id="btnAddNewRowCancel">Cancel</button>
                </div>
            </form>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>