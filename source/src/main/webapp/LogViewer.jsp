<%-- 
    Document   : LogViewer
    Created on : 29/Fev/2012, 14:41:26
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
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <title>Log Viewer</title>

        <script type="text/javascript">      
            $(document).ready(function(){
                $('#logsTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200],
                        [20, 50, 100, 200]
                    ], 
                    "aaSorting": [[ 0, "desc" ]],
                    "iDisplayLength" : 50,
                    "bServerSide": true,
                    "sAjaxSource": "GetLogEvent",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "bSearchable": false, "aTargets": [ 0 ],
                    "aoColumns": [
                        {"mDataProp": "time", "sName": "Time"},
                        {"mDataProp": "login", "sName": "Login"},
                        {"mDataProp": "page", "sName": "Page"},
                        {"mDataProp": "action", "sName": "Action"},
                        {"mDataProp": "log", "sName": "Log"}
                    ]
                })
            });
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <div style="width: 80%; padding: 25px; font: 90% sans-serif">
            <table id="logsTable" class="display">
                <thead>
                    <tr>
                        <th>Time</th>
                        <th>Login</th>
                        <th>Page</th>
                        <th>Action</th>
                        <th>Log</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br><% out.print(display_footer(DatePageStart));%>
    </body>
</html>
