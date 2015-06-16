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

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>Test Data Library</title>
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">

        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link type="text/css" rel="stylesheet" href="css/dataTables.colVis.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        
        
        <link type="text/css" rel="stylesheet" href="css/jquery.multiselect.css">
        <link type="text/css" rel="stylesheet" href="css/bootstrap.css">
        <link type="text/css" rel="stylesheet" href="css/default.css"> 

        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/bootstrap.min.js"></script>
        
        
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script> 
        <script type="text/javascript" src="js/jquery.multiselect.js" charset="utf-8"></script>
        <script type="text/javascript" src="js/jquery.form.js"></script>
        <script type="text/javascript" src="js/dataTables.colVis.js"></script>
        <script type="text/javascript" src="js/pages/TestDataLibViewModel.js"></script>


    </head>
    <body>
        <%@ include file="include/function.jsp"%>
        <%@ include file="include/header.jsp"%>
 
        <div id="page-layout" class="container center">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/testdatalib/testdatalib.html"%>
                    <!-- form that adds the new library -->
            <%@ include file="include/testdatalib/addTestDataLib.jsp"%>       
            <%@ include file="include/testdatalib/editTestDataLib.html"%>       
            
            
            <!--<div class="well col-xs-7">
                <form>
                    <div class="row">
                        <div class="form-group col-xs-3">
                            <label>Name</label>
                            <input class="form-control"/>
                        </div>    
                        <div class="form-group col-xs-3">
                            <label>Type</label>
                            <select class="form-control"></select>
                        </div>    
                        <div class="form-group col-xs-3">
                            <label>Group</label>
                            <input class="form-control"/>
                        </div>    
                    </div>    
                    <div class="row">
                        <div class="form-group col-xs-3">
                            <label>System</label>
                            <select class="form-control"></select>
                        </div>    
                        <div class="form-group col-xs-3">
                            <label>Environment</label>
                            <select class="form-control"></select>
                        </div>    
                        <div class="form-group col-xs-3">
                            <label>Country</label>
                            <select class="form-control"></select>
                        </div>    
                    </div>    
                    <div class="row">
                        <div class="form-group">
                            <button class="btn btn-primary">Pesquisar</button>
                            <button class="btn btn-default">Limpar</button>
                        </div>    
                    </div>    
                </form>
            </div>
            -->

            <div id="testdatalib" >               
                <table  class="display" id="listOfTestDataLib" name="listOfTestDataLib">
                    <thead>
                        <tr>
                            <th>Subdata | Edit | Delete</th> 
                            <th>Name</th>
                            <th>System</th>
                            <th>Environment</th>
                            <th>Country</th>
                            <th>Group</th>
                            <th>Type</th>
                            <th>Database</th>
                            <th>Script</th>
                            <th>Service Path</th>
                            <th>Method</th>
                            <th>Envelope</th>
                            <th>Description</th>               
                        </tr>                        
                    </thead>
                </table>
            </div>
        </div>     

       



    </body> 

</html>
