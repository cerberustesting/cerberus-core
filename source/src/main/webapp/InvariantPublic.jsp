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
        <title>Public Invariant</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <script type="text/javascript">

            $(document).ready(function() {
                var oTable = $('#invariantPublicList').dataTable({
                    "aaSorting": [[1, "asc"]],
                    "bServerSide": true,
                    "sAjaxSource": "FindAllInvariantPublic",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true,
                    "aTargets": [0],
                    "aLengthMenu": [
                        [10, 25, 50, 100, -1],
                        [10, 25, 50, 100, "All"]
                    ], 
                    "iDisplayLength": 25,
                    "aoColumns": [
                        {"sName": "key", "bVisible": false},
                        {"sName": "IdName", "sWidth": "10%"},
                        {"sName": "value", "sWidth": "10%"},
                        {"sName": "sort", "sWidth": "5%"},
                        {"sName": "description", "sWidth": "30%"},
                        {"sName": "veryShortdesc", "sWidth": "15%"},
                        {"sName": "gp1", "sWidth": "10%"},
                        {"sName": "gp2", "sWidth": "10%"},
                        {"sName": "gp3", "sWidth": "10%"}
                        
                    ]
                }
            ).makeEditable({
                    sAddURL: "CreateInvariant",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "<b>Create Invariant entry...</b>",
                        background: "#AAAAAA",
                        icons: {primary: 'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteInvariant",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary: 'ui-icon-trash'}
                    },
                    oAddNewRowFormOptions: {
                        title: 'Add Invariant Entry',
                        show: "blind",
                        hide: "explode",
                        width: "600px"
                    },
                    sUpdateURL: "UpdateInvariant",
                    fnOnEdited: function(status) {
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        null,
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''},
                        {onblur: 'submit',
                            placeholder: ''}

                    ]
                })
            });


        </script>

    </head>
    <body  id="wrapper">
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <p class="dttTitle">Public Invariants</p>
        <div style="width: 100%;  font: 90% sans-serif">
            <table id="invariantPublicList" class="display">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>ID Name</th>
                        <th>Value</th>
                        <th>Sort</th>
                        <th>Description</th>
                        <th>Very Short Desc</th>
                        <th>Group 1</th>
                        <th>Group 2</th>
                        <th>Group 3</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div>
            <form id="formAddNewRow" action="#" title="Add Public Invariant" style="width:600px" method="post">
                <div style="width: 200px; float:left; display: none">
                    <label for="InvKey" style="font-weight:bold">InvKey</label>
                    <input id="InvKey" name="InvKey" style="width:100px;" 
                           class="ncdetailstext" rel="0" >
                </div>
                <label for="IDName" style="font-weight:bold">ID Name</label>
                <%=ComboInvariantAjax(appContext, "IDName", "", "IDName", "1", "INVARIANTPUBLIC", "", "", false)%>

                <label for="Value" style="font-weight:bold">Value</label>
                <input id="Value" name="Value" style="width:150px;" 
                       class="ncdetailstext" rel="2" >
                <label for="Sort" style="font-weight:bold">Sort</label>
                <input id="Sort" name="Sort" style="width:50px;" 
                       class="ncdetailstext" rel="3" ><br>
                <br><br>
                <label for="Description" style="font-weight:bold">Description</label>
                <input id="Description" name="Description" style="width:300px;" 
                       class="ncdetailstext" rel="4" >
                <br><br>
                <label for="VeryShortDesc" style="font-weight:bold">Very Short Desc.</label>
                <input id="VeryShortDesc" name="VeryShortDesc" style="width:100px;" 
                       class="ncdetailstext" rel="5" >
                <br><br>
                <label for="gp1" style="font-weight:bold">Group 1</label>
                <input id="gp1" name="gp1" style="width:100px;" 
                       class="ncdetailstext" rel="6" >
                <label for="gp2" style="font-weight:bold">Group 2</label>
                <input id="gp2" name="gp2" style="width:100px;" 
                       class="ncdetailstext" rel="7" >
                <label for="gp3" style="font-weight:bold">Group 3</label>
                <input id="gp3" name="gp3" style="width:100px;" 
                       class="ncdetailstext" rel="8" >
                <br><br>
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>