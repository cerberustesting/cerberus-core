<%-- 
    Document   : UserManager
    Created on : 22/Fev/2012, 11:24:25
    Author     : ip100003
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
        <title>Build Revision Definition</title>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <script type="text/javascript">
            $(document).ready(function(){
                $('#buildrevTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 50,
                    "bServerSide": false,
                    "sAjaxSource": "GetBuildRevisionInvariant?System=<%=request.getAttribute("MySystem")%>",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "aaSorting": [[ 1, "asc" ]],
                    "aoColumns": [
                        {"mDataProp": "system", "sName": "system"},
                        {"mDataProp": "level", "sName": "Level"},
                        {"mDataProp": "seq", "sName": "Sequence"},
                        {"mDataProp": "versionName", "sName": "Version Name"}
                    ]
                }
            ).makeEditable({
                    sAddURL: "AddBuildRevisionInvariant",
                    sAddHttpMethod: "POST",
                    oAddNewRowButtonOptions: {
                        label: "Add...",
                        icons: {primary:'ui-icon-plus'}
                    },
                    sUpdateURL: "UpdateBuildRevisionInvariant",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        null,
                        null,
                        {
                            submit:'Save changes'
                        }
                    ]
                });
            });
        </script>
        <div style="width: 80%; padding: 25px; font: 90% sans-serif">
            <table id="buildrevTable" class="display">
                <thead>
                    <tr>
                        <th>System</th>
                        <th>Level</th>
                        <th>Seq</th>
                        <th>Version Name</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
