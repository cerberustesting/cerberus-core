<%-- 
    Document   : ExecutionThreadMonitoring
    Created on : 3 mars 2015, 12:42:00
    Author     : bcivel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <title>ExecutionThreadMonitoring</title>
        <script>
            $(document).ready(function() {
                $.get('ExecutionThreadMonitoring', function(data) {
                    $("#sizeOfQueue").html(data.size_queue);
                    $("#QueueInExecution").html(data.queue_in_execution);
                    $("#SimultaneousExecution").html(data.simultaneous_execution);
                    $("#SimultaneousSession").html(data.simultaneous_session);
                    $.each(data.active_users, function (a, v){
                        $("#ActiveUsers").append(v + "<br>");
                    });
                    
                });

            });
        </script>
        <script>
            function resetThreadPool(){
                $.get('ExecutionThreadReset', function(data) {
                    alert('Thread Pool Cleaned');
                });
            }
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <h3>Execution Monitoring</h3>
        <p>Total Size Of Queue In Memory: </p><p id="sizeOfQueue"></p>
        <br>
        <p>Number of Queue In Execution : </p><p id="QueueInExecution"></p>
        <br>
        <p>Number of Actual Simultaneous Execution : </p><p id="SimultaneousExecution"></p>
        <br>
        <input type="button" value="Reset Queue" onclick="resetThreadPool()">
        <br><br>
        <h3>Session Monitoring</h3>
        <p>Number of HTTP Session opened : </p><p id="SimultaneousSession"></p>
        <br>
        <p>List of Active Users : </p><p id="ActiveUsers"></p>
        <br>
    </body>
</html>
