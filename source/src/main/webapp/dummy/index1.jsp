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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="../dependencies/jQuery-2.2.3/jquery-2.2.3.min.js"></script>
        <%
            String delay = request.getParameter("asyncdelay");
            if (delay != null) {
                out.print("<script type=\"text/javascript\" src=\"index3.jsp?delay=" + delay + "\" async>");
            }
        %>
    </head>
    <body>
        <h1>Hello World!</h1>
        This is a fake page required to test Cerberus Engine by Cerberus itself....
        <!-- for testing HTML property -->
        <div class="theseusTag-divMain">
            <span id="crb-serverName">172.25.71.202</span>
            <span id="crb-TheseusVersion">41473.4102923727</span>
            <span id="crb-elaspedTime">328</span>
            <span id="crb-env">PRODUCTION</span>
            <span id="crb-dollar">$ 210</span>
            <span id="crb-string1">String with * star</span>
            <span id="crb-env-hidden" hidden>PRODUCTION</span>
            <input name="crb-name" type="hidden" value="CERBERUS"/>
            <span id="crb-empty"></span>
        </div>
        <form>
            <input type="password" name="input1" id="input1">
            <input name="input2" id="input2">
            <select id="combo1">
                <option value="1">1</option>
                <option value="2">2</option>
            </select>
        </form>
        <br>
        <script>
            function printValue(value) {
                document.selectFormulary.selectedValue.value = value.value;
            }
        </script>
        <script>
            function waitXSecondAndPrintValue(value) {
                window.setTimeout(function() {
                    var i = document.createElement('input');
                    i.setAttribute('id', 'selectedTimeout');
                    i.setAttribute('name', 'selectedTimeout');
                    i.setAttribute('value', 'Hello World!');
                    document.selectTimeout.appendChild(i);
                    document.getElementById('selectTimeoutHide').setAttribute('value', 'Hello World!');
                    document.getElementById('selectTimeoutHide').style.display = 'inline';
                }, value.value);
            }
        </script>
        <script language="text/javascript" type="text/javascript">
            var product = "12345";
        </script>
        <form name="selectFormulary">
            <p>Below is part to test select Regex</p>
            <input name="selectedValue" id="selectedValue">
            <select id="comboSelectRegex" onChange="printValue(this)">
                <option value="val1">TestRegex1</option>
                <option value="val2">TestRegex2</option>
                <option value="val3">Cerberus</option>
                <option value="val4">TestRegex3</option>
            </select>
        </form>
        </br>
        <form name="selectTimeout">
            <p>Below is part to test Cerberus timeout</p>
            <input id="selectTimeoutValue" onChange="waitXSecondAndPrintValue(this)">
            <input id="selectTimeoutHide" name="selectTimeoutHide" style="display: none"/>
        </form>
        <p>Below is part to test Attribute data-cerberus</p>
        <input data-cerberus="index1_input" value="Test Value">

        <p>Below is part to test PropertyType getAttributeFromHtml</p>
        <input data-cerberus="index1_input2" data-attribute="att1" value="Test Value">
        <br>
        <br>
        <a href="#" id="openPopup" data-cerberus="openPopup" onclick="javascript:window.open('./index2.jsp', 'popup',
                        'width=500,height=400,scrollbars=yes,menubar=false,location=false');
                return false;">Open Popup</a>
        <br><br>
        <a href="#" id="openPopupWithoutTitle" data-cerberus="openPopupWithoutTitle" onclick="javascript:window.open('./index4.jsp', 'popup',
                        'width=500,height=400,scrollbars=yes,menubar=false,location=false');
                return false;">Open Popup Without Title</a>
        <br>
        <br>
        <a href="#" id="alertPopup" data-cerberus="alertPopup" onclick="javascript:alert('ceci est une popup d\'alert')">Open Alert</a>
        <br>
        <br>
        <a href="#" id="confirmPopup" data-cerberus="confirmPopup" onclick="javascript:confirm('ceci est une popup de confirmation')">Open Confirm</a>
        <br>
        <br>
        <form name="selectFormulary2" action="index2.jsp">
            <p>Below is part to test Input and validate form</p>
            <input type="text" name="selectedValue2" id="selectedValue2">
            <input id="selectedValue2Submit" type="submit" value="Submit">
        </form>
        <br><br>
        <form method="POST" enctype="multipart/form-data">
            <p>Below is part to test action keypress on non-browser windows</p>
            File to upload: <input type="file" name="template"><br/>
            <br/>
        </form>

        <p> Parameters that can be used on that page :</p>
        'asyncdelay' : delay in ms can be used to call async ressource.
        <br>
        <br>
        <button ondblclick="myFunction()">Double-click</button>

        <p id="demo"></p>

        <p>A function is triggered when the button is double-clicked. The function outputs some text in a p element with id="demo".</p>

        <script>
            function myFunction() {
                document.getElementById("demo").innerHTML = "DoubleClick works!";
            }
        </script>
        <br>
        <br>
        <form>
            <label for="textField">Type anything</label>
            <br>
            <input type="text" id="textField" name="textField"></input>
            <br>
            <input type="button" value="submit" id="textFieldSubmit" onclick="showTextFieldContent()" >
            <br>
            <span id="textFieldContent"></span>
        </form>
        <script language="text/javascript" type="text/javascript">
            function showTextFieldContent() {
                var textContent = document.getElementById("textField").value;
                document.getElementById("textFieldContent").innerHTML = textContent;
            }
        </script>
        <br>
        <br>
        <form id="emailInputForm">
            <input id="emailInput" type="email">
            <span id="emailInputResult"></span>
        </form>
        <script>
            $("#emailInput").on("click", function() {
                $("#emailInputForm span").text("OK");
            });
        </script>
        <br>
        <br>
        <p>Below is part to test iframe switching</p>
        <iframe id="iframeCerberusWebsite" src="https://qa.cerberus-testing.org/dummy/index2.jsp" height="500" width="500">

        </iframe>
    </body>
</html>
