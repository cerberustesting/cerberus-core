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
        <%@ include file="include/dependenciesInclusions_old.html" %>
        <title>ExecutionThreadMonitoring</title>
        <script>
            $(document).ready(function() {
                $.get('ReadCerberusDetailInformation', function(data) {
                    $("#sizeOfQueue").html(data.size_queue);
                    $("#QueueInExecution").html(data.queue_in_execution);
                    $("#NumberOfThread").html(data.number_of_thread);
                    $("#SimultaneousExecution").html(data.simultaneous_execution);
                    $("#SimultaneousSession").html(data.simultaneous_session);
                    $.each(data.active_users, function (a, v){
                        $("#ActiveUsers").append("<li>"+ v + "</li>");
                    });
                    $.each(data.simultaneous_execution_list, function (a, v){
                        function getParameter(param,sys,forceReload){
                            var cacheEntryName = "PARAMETER_"+param;
                            if (forceReload) {
                                sessionStorage.removeItem(cacheEntryName);
                            }
                            var system = sys!=undefined?"&system="+sys:"";
                            return new Promise(function(resolve, reject){
                                var parameter = JSON.parse(sessionStorage.getItem(cacheEntryName));
                                if(parameter === null){
                                    $.get("ReadParameter?param="+param+system, function(data){
                                        sessionStorage.setItem(cacheEntryName,JSON.stringify(data.contentTable))
                                        resolve(data.contentTable);
                                    });
                                }else{
                                    resolve(parameter);
                                }
                            });
                        }

                        getParameter("cerberus_executiondetail_use").then(function(data){
                            if(data.value == "N"){
                                $("#ExecutionList").append("<li>[<a href='./ExecutionDetail.jsp?id_tc="+ v.id + "'>"+ v.id + "</a>] : " + v.test + " " +v.testcase + "</li>");
                            }else{
                                $("#ExecutionList").append("<li>[<a href='./ExecutionDetail2.jsp?executionId="+ v.id + "'>"+ v.id + "</a>] : " + v.test + " " +v.testcase + "</li>");
                            }
                        });
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
        <h4>Thread Execution</h4>
        <p>Size Of Pending Execution In Queue : </p><p id="sizeOfQueue"></p>
        <br>
        <p>Number of Workers In Execution : </p><div style="float:left" id="QueueInExecution"></div><div style="float:left">/</div><div style="float:left" id="NumberOfThread"></div>
        <br>
        <input type="button" value="Reset Queue" onclick="resetThreadPool()">
        <br>
        <br>

        <h4>Execution</h4>
        <p>Number of Actual Simultaneous Execution : </p><p id="SimultaneousExecution"></p>
        <br>
        <p>Execution List : </p>
        <br>
        <ul id="ExecutionList"></ul>
        <br><br>
        <h3>Session Monitoring</h3>
        <p>Number of HTTP Session opened : </p><p id="SimultaneousSession"></p>
        <br>
        <p>List of Active Users : </p>
        <br>
        <ul id="ActiveUsers"></ul>
        <br>
    </body>
</html>
