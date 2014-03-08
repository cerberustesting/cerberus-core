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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% Date DatePageStart = new Date();%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <style media="screen" type="text/css">
            @import "css/demo_page.css";
            @import "css/demo_table.css";
            @import "css/demo_table_jui.css";
            @import "css/themes/base/jquery-ui.css";
            @import "css/themes/smoothness/jquery-ui-1.7.2.custom.css";
        </style>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <title>User Manager</title>

        <script type="text/javascript">
            var oTable;
            var groups;
            $(document).ready(function(){
                var anOpen = [];
                var sImageUrl = "images/";
                oTable = $('#usersTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 50,
                    "bServerSide": false,
                    "sAjaxSource": "GetUsers",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "aaSorting": [[ 1, "asc" ]],
                    "aoColumns": [
                        {
                            "mDataProp": null,
                            "sClass": "control center",
                            "sDefaultContent": '<img src="'+sImageUrl+'details_open.png'+'">',
                            "bSortable": false
                        },
                        {"mDataProp": "login", "sName": "Login"},
                        {"mDataProp": "name", "sName": "Name"},
                        {"mDataProp": "team", "sName": "Team"},
                        {"mDataProp": "system", "sName": "DefaultSystem"},
                        {"mDataProp": "request", "sName": "Request_Password"}
                    ],
                    "fnCreatedRow": function( nRow, aData, iDisplayIndex ) {
                        $(nRow).attr("id", aData.login);
                        return nRow;
                    }
                    //                });
                }
            ).makeEditable({
                    sAddURL: "AddUser",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "Add...",
                        icons: {primary:'ui-icon-plus'}
                    },
                    sDeleteHttpMethod: "POST",
                    sDeleteURL: "DeleteUser",
                    sAddDeleteToolbarSelector: ".dataTables_length",
                    oDeleteRowButtonOptions: {
                        label: "Remove",
                        icons: {primary:'ui-icon-trash'}
                    },
                    sUpdateURL: "UpdateUser",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {},
                        {},
                        {
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=TEAM',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {
                            type: 'select',
                            loadurl: 'GetInvariantList?idName=SYSTEM',
                            loadtype: 'GET',
                            submit:'Save changes'
                        },
                        {
                            loadtext: 'loading...',
                            type: 'select',
                            onblur: 'submit',
                            data: "{'Y':'Y','N':'N'}",
                            submit:'Save changes'

                        }
                    ]
                });

                $(document).on("click", "#usersTable td.control", function () {
                    var nTr = this.parentNode;
                    var i = $.inArray( nTr, anOpen );
                    if ( i === -1 ) {
                        $('img', this).attr( 'src', sImageUrl+"details_close.png" );
                        var nDetailsRow = oTable.fnOpen( nTr, fnFormatDetails(oTable, nTr), 'details' );
                        $('div.innerDetails', nDetailsRow).slideDown();
                        anOpen.push( nTr );
                    }
                    else {
                        $('img', this).attr( 'src', sImageUrl+"details_open.png" );
                        $('div.innerDetails', $(nTr).next()[0]).slideUp( function () {
                            oTable.fnClose( nTr );
                            anOpen.splice( i, 1 );
                        } );
                    }
                });

                $.post('GetInvariantList', {idName: "USERGROUP"}, function (data) {
                    groups = data;
                });
            });

            function fnFormatDetails( oTable, nTr ){
                var oData = oTable.fnGetData( nTr );
                var sOut = '<div class="innerDetails"><form id="formUserGroup" name="formUserGroup">' +
                    '<select name="'+oData.login+'_group" id="'+oData.login+'_group" multiple> ';

                for(var key in groups){
                    sOut += '<option value="'+key+'" ';
                    if(oData.group.indexOf(key) > -1){
                        sOut += 'selected';
                    }
                    sOut += '>'+key+'</option>';
                }
                sOut += '</select>' +
                    '<input type="button" value="Save changes" onclick="updateUserGroup(\''+oData.login+'\')"/>' +
                    '<input type="hidden" value="0" name="columnPosition"/><input type="hidden" value="'+oData.login+'" name="id"/>' +
                    '<input type="hidden" value="" name="value"/></form></div>';
                return sOut;
            }

            function updateUserGroup(oData){
                $.ajax({
                    type: 'POST',
                    url: 'UpdateUser',
                    data:$('#formUserGroup').serialize()
                }).done(function(){
                    var data = oTable.fnGetData( $('#'+oData)[0] );
                    data.group = [];
                    $('#'+oData+'_group option:selected').each(function(){
                        data.group.push($(this).val());
                    });
                    $("tr[id="+oData+"] td.control").trigger('click');
                });
            }

        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div style="width: 80%; padding: 25px; font: 90% sans-serif">
            <%
                /*
                 * Database connexion
                 */
                Connection conn = db.connect();
                try {

            %>
            Documentation on how to define groups can be found <a href="javascript:popup('Documentation.jsp?DocTable=usergroup&DocField=GroupName')">here</a>.
            <table id="usersTable" class="display">
                <thead>
                    <tr>
                        <th></th>
                        <th>Login</th>
                        <th>Name</th>
                        <th>Team</th>
                        <th>Default System</th>
                        <th>Request New Password</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>

        <div>
            <form id="formAddNewRow" action="#" title="Add new user" method="post">
                <label for="login">Login</label>
                <input type="text" name="login" id="login" class="required" maxlength="10" rel="1" />
                <br /><br />
                <label for="name">Name</label>
                <input type="text" name="name" id="name" maxlength="25" rel="2" />
                <br /><br />
                <label for="name">Email</label>
                <input type="text" name="email" id="email" maxlength="100" rel="5" />
                <br /><br />
                <label for="team">Team</label>
                <%=ComboInvariantAjax(conn, "team", "", "team", "3", "TEAM", "", "", false)%>
                <br /><br />
                <label for="defaultSystem">Default System</label>
                <%=ComboInvariantAjax(conn, "defaultSystem", "", "defaultSystem", "4", "SYSTEM", "", "", false)%>
                <br /><br />
                <label for="newPassword">Request New Password ?</label>
                <select name="newPassword" id="newPassword" rel="5">
                    <option value="Y">Y</option>
                    <option value="N">N</option>
                </select>
                <br /><br />
                <label for="groups">User Group</label>
                <%=ComboInvariantMultipleAjax(conn, "groups", "", "groups", "0", "USERGROUP", "", "", false)%>
                <br /><br />
                <button id="btnAddNewRowOk">Add</button>
                <button id="btnAddNewRowCancel">Cancel</button>
            </form>
        <%
            } catch (Exception e) {
                out.println(e);
            } finally {
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }
        %>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
