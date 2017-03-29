<%--

    Cerberus Copyright (C) 2013 - 2017 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>TestCase Creation</title>
        <%@ include file="include/dependenciesInclusions_old.html" %>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <form action="ImportTestCaseFromJson" method="post" name="selectFile" enctype="multipart/form-data">    
                            <input type="submit" value="importTestCase">
                            <input id="test" name="test" style="width:500px">
                            <input id="testcase" name="testcase" style="width:500px">
                            <input type="file" id="Path" name="Path" style="width:200px">
                            <input id="Load" name="Load" style="display:none" value="Y">
                        </form>
<br><% out.print(display_footer(DatePageStart));%>
<script type="text/javascript">

            var sys = document.getElementById("MySystem");
            var systemSelected = sys.options[sys.selectedIndex].value;
            var testS = document.getElementById("defTest").value;
            var tcS = document.getElementById("defTestCase").value;


            $(document).ready(function() {
                $.getJSON('GetTestBySystem?system=' + systemSelected, function(data) {
                    var test = $("#test");
                    test.empty();

                    for (var i = 0; i < data.testsList.length; i++) {
                        test.append($("<option></option>")
                                .attr("value", data.testsList[i])
                                .text(data.testsList[i]));
                    }
                    test.find('option').each(function(i, opt) {
                        if (opt.value === testS)
                            $(opt).attr('selected', 'selected');
                    });
                    if (tcS !== "%%") {
                        getTestCaseList();
                    }

                })
            });



        </script>
        <script type="text/javascript">
            function getTestCaseList() {
                var sys = document.getElementById("MySystem");
                var systemSelected = sys.options[sys.selectedIndex].value;
                var b = document.getElementById("test");
                var testSelected = b.options[b.selectedIndex].value;
                var tcS = document.getElementById("defTestCase").value;
                var countryS = document.getElementById("defCountry").value;

                $.getJSON('GetTestCaseForTest?system=' + systemSelected + '&test=' + testSelected, function(data) {
                    var testcase = $("#testcase");
                    testcase.empty();
                    $("#country").empty();
                    $("#environment").empty();
                    $("#myenvdata").empty();

                    for (var i = 0; i < data.testCaseList.length; i++) {
                        testcase.append($("<option></option>")
                                .attr("value", data.testCaseList[i].testCase)
                                .attr("data-testcase", data.testCaseList[i].testCase)
                                .attr("data-application", data.testCaseList[i].application)
                                .text(data.testCaseList[i].description));
                    }
                    testcase.find('option').each(function(i, opt) {
                        if (opt.value === tcS)
                            $(opt).attr('selected', 'selected');
                    });

                    getCountryList();
                });
            }
        </script>
</body>
</html>
