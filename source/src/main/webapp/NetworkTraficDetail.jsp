<%-- 
    Document   : NetworkTraficDetail
    Created on : 18 févr. 2013, 21:00:39
    Author     : bcivel
--%>

<%@page import="java.util.Date"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.Statement"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Network Traffic Detail</title>
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <style media="screen" type="text/css">
            @import "css/demo_page.css";
            @import "css/demo_table.css";
            @import "css/demo_table_jui.css";
            @import "css/themes/base/jquery-ui.css";
            @import "css/themes/smoothness/jquery-ui-1.7.2.custom.css";
        </style>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" >
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
       
    </head>
    <body>
        <%
        String id = request.getParameter("id");
%>
<input style="display:none" id="idvalue" name="idvalue" value="<%=id%>">
<script type="text/javascript">      
       var id = document.getElementById("idvalue").value;
          
           $(document).ready(function(){
                $('#nonConformityList').dataTable({
                    "bServerSide": true,
                    "sAjaxSource": "TCEwwwDetail?id="+id,
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "bPaginate": true,
                    "bAutoWidth": false,
                    "sPaginationType": "full_numbers",
                    "bSearchable": true, 
                    "aoColumns": [
                        {"bVisible": false, "sName": "ID", "sWidth": "20px"},
                        {"bVisible": false, "sName": "EXECID", "sWidth": "50px"},
                        {"sName": "START", "sWidth": "100px"},
                        {"sName": "URL", "sWidth": "400px"},
                        {"sName": "END", "sWidth": "100px"},
                        {"sName": "EXT", "sWidth": "20px"},
                        {"sName": "StatusCode", "sWidth": "50px"},
                        {"sName": "Method", "sWidth": "30px"},
                        {"sName": "Bytes", "sWidth": "50px"},
                        {"sName": "TimeInMillis", "sWidth": "50px"},
                        {"sName": "ReqHeader_Host", "sWidth": "50px"},
                        {"sName": "ResHeader_ContentType", "sWidth": "50px"}
                    ]
                }
            )
            });
  
        </script>
          
         <% 
         Date DatePageStart = new Date() ;
                  %>
        <div style="width: 80%; padding: 25px; font: 70% sans-serif">
            <table id="nonConformityList" class="display">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>EXECID</th>
                        <th>START</th>
                        <th>URL</th>
                        <th>END</th>
                        <th>EXT</th>
                        <th>StatusCode</th>
                        <th>Method</th>
                        <th>Bytes</th>
                        <th>TimeInMillis</th>
                        <th>ReqHeader_Host</th>
                        <th>ResHeader_ContentType</th>
                        </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        
                     <%
                  
%>
    <br>          
    </body>
</html>