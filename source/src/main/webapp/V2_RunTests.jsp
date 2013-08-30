<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Run Tests</title>
    <script src="js/jquery-1.9.1.min.js" type="text/javascript"></script>
    <script src="js/ajax-loader.js" type="text/javascript"></script>
    <style type="text/css">
        body {
            font-family: helvetica;
            font-size: 70%;
        }

        input {
            font-family: helvetica;
            font-size: 85%;
        }

        select {
            font-size: 85%;
        }

        #divTest {
            position: relative;
            float: left;
        }

        #divTestCase {
            position: relative;
            float: left;
        }

        #divCountry {
            position: relative;
            float: left;
        }

        #divEnvironment {
            position: relative;
            clear: left;
        }

        #divDev {
            display: none;
        }

        #divExecutionParameters {
            position: relative;
            float: left;
        }

        #divRun {
            position: relative;
            float: right;
            padding-left: 15px;
            padding-top: 85px
        }

        .fields {
            background-color: #CAD3F1;
            border: 2px solid #8999C4;
            display: inline-block;
            border-radius: 15px;
            padding: 5px;
            margin-bottom: 3px;
            margin-top: 3px;
        }

        .field {
            padding-bottom: 5px;
            padding-left: 5px;
        }

        .field label {
            font-weight: bold;
            display: inline-block;
        }

        #toolParameters label {
            width: 120px;
        }

        #executionParameters label {
            width: 90px;
        }

        h3 {
            color: blue;
            margin-top: 5px;
            margin-bottom: 10px;
        }

            /* Ajax Loader styles */
        .ajax_loader {
            background: url("images/spinner_squares_circle.gif") no-repeat center center transparent;
            width: 100%;
            height: 100%;
        }
    </style>
</head>
<body>
<%@ include file="V2_header.jsp" %>
<form action="RunTestCase" method="GET" id="runTestCase">
    <div id="toolParameters" class="fields">
        <h3>Tool Parameters</h3>

        <div class="field">
            <label for="ss_ip">Selenium Server IP</label>
            <input id="ss_ip" name="ss_ip" type="text" value="localhost"/>
        </div>
        <div class="field">
            <label for="ss_port">Selenium Server Port</label>
            <input id="ss_port" name="ss_port" type="text" value="5555"/>
        </div>
        <div class="field">
            <label for="path">Selenium Path</label>
            <input id="path" name="path" type="text" value="/home/ip100003/Desenvolvimento/Selenium/"
                   style="width: 450px"/>
        </div>
        <div class="field">
            <label for="browser">Browser</label>
            <input id="browser" name="browser" type="text" value="*firefox" style="width: 450px"/>
        </div>
    </div>
    <br/>

    <div id="testParameters" class="fields">
        <h3>Test Parameters</h3>

        <div id="divTest" class="field">
            <label for="test">Test</label><br/>
            <select id="test" name="test" onchange="getTestCaseList();" style="width: 200px" size="16">
            </select>
        </div>
        <div id="divTestCase" class="field">
            <label for="testCase">Test Case</label><br/>
            <select id="testCase" name="testCase" onchange="getCountryList();" style="width: 600px" size="16">
            </select>
        </div>
        <div id="divCountry" class="field">
            <label for="country">Country</label><br/>
            <select id="country" name="country" onselect="" onchange="getEnvironmentList();" style="width: 50px"
                    size="16">
            </select>
        </div>
        <div id="divEnvironment" class="field">
            <label for="environment" style="width: 200px">Environment</label>
            <select id="environment" name="environment" onchange="changeDisplay()" style="width: 400px">
            </select>
        </div>
        <div id="divDev">
            <div id="divDevURL" class="field">
                <label for="devURL" style="width: 200px">URL</label>
                <input id="devURL" name="devURL" type="text" style="width: 575px"/>
            </div>
            <div id="divDevLogin" class="field">
                <label for="devLogin" style="width: 200px">Login Page</label>
                <input id="devLogin" name="devLogin" type="text" style="width: 575px"/>
            </div>
            <div id="divDevEnvironment" class="field">
            </div>
        </div>
    </div>
    <br/>

    <div id="executionParameters" class="fields">
        <div id="divExecutionParameters">
            <h3>Execution Parameters</h3>

            <div class="field">
                <label for="tag">Tag</label>
                <input id="tag" name="tag" type="text" value="TB New Cerberus" style="width: 200px"/>
            </div>
            <div class="field">
                <label for="outputFormat">Output Format</label>
                <select id="outputFormat" name="outputFormat" style="width: 90px">
                    <option value="gui" selected="selected">gui</option>
                    <option value="compact">compact</option>
                    <option value="verbose-txt">verbose-txt</option>
                </select>
            </div>
            <div class="field">
                <label for="verbose">Verbose Level</label>
                <select id="verbose" name="verbose" style="width: 90px">
                    <option value="0" selected="selected">0</option>
                    <option value="1">1</option>
                    <option value="2">2</option>
                </select>
            </div>
        </div>
        <div id="divRun">
            <input id="buttonRun" type="submit" value="Run">
        </div>
    </div>
    <br/>
</form>
</body>
<script type="text/javascript">
    var test = new ajaxLoader("#divTest");
    $.get('GetShortTests', function (data) {
        for (var i = 0; i < data.testsList.length; i++) {
            $("#test").append($("<option></option>")
                    .attr("value", data.testsList[i])
                    .text(data.testsList[i]));
        }
        test.remove();
    });

    function changeDisplay() {
        if ($("#environment").val() == "DEV") {
            $("#divDev").show();
            getDevEnvironment();
        } else {
            $("#divDev").hide();
        }
    }

    function getTestCaseList() {
        var testCase = new ajaxLoader("#divTestCase");
        $("#testCase").empty();
        $("#country").empty();
        $("#environment").empty();
        $.get('GetTestCaseForTest', {test: $("#test").val()}, function (data) {
            for (var i = 0; i < data.testCaseList.length; i++) {
                $("#testCase").append($("<option></option>")
                        .attr("value", data.testCaseList[i].testCase)
                        .text(data.testCaseList[i].description));
            }
            testCase.remove();
        });
    }

    function getCountryList() {
        if ($("#testCase").val() != null) {
            var country = new ajaxLoader("#divCountry");
            $("#country").empty();
            $("#environment").empty();
            $.get('GetCountryForTestCase', {test: $("#test").val(), testCase: $("#testCase").val()}, function (data) {
                for (var i = 0; i < data.countriesList.length; i++) {
                    $("#country").append($("<option></option>")
                            .attr("value", data.countriesList[i])
                            .text(data.countriesList[i]));
                }
                country.remove();
            });
        }
    }

    function getEnvironmentList() {
        if ($("#country").val() != null) {
            var env = new ajaxLoader("#divEnvironment");
            $("#environment").empty();
            $.get('GetEnvironmentAvailable', {test: $("#test").val(), testCase: $("#testCase").val(), country: $("#country").val()}, function (data) {
                for (var i = 0; i < data.envList.length; i++) {
                    if (data.envList[i].environment == "UAT") {
                        $("#environment").append($("<option></option>")
                                .attr("value", data.envList[i].environment)
                                .text(data.envList[i].description)
                                .attr("selected", "selected"));
                    } else {
                        $("#environment").append($("<option></option>")
                                .attr("value", data.envList[i].environment)
                                .text(data.envList[i].description));
                    }
                }
                $('#environment').prepend($("<option value='DEV'>DEV You choose Environment</option>"));
                env.remove();
            });
        }
    }

    function getDevEnvironment() {
        $("#divDevEnvironment").empty();
        var dev = new ajaxLoader("#divDev");
        $.get('GetDevEnvironment', {}, function (data) {
            for (var i = 0; i < data.devEnvironment.length; i++) {
                $("#divDevEnvironment").append($("<label></label>").text(data.devEnvironment[i].database));
                $("#divDevEnvironment").append($("<select></select>").attr("name", "devEnvironment").attr("id", "devEnvironment" + i));
                for (var j = 0; j < data.devEnvironment[i].environment.length; j++) {
                    $("#devEnvironment" + i).append($("<option></option>")
                            .attr("value", data.devEnvironment[i].database + "_" + data.devEnvironment[i].environment[j])
                            .text(data.devEnvironment[i].environment[j]));
                }
            }
            dev.remove();
        });
    }
</script>
</html>