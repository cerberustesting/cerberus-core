<%-- 
    Document   : index1
    Created on : Aug 9, 2012, 9:27:28 PM
    Author     : vertigo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
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
            function printValue(value){
            document.selectFormulary.selectedValue.value = value.value;    
            }    
        </script>
        <script language="text/javascript" type="text/javascript">
            var product = "12345";    
        </script>
        <form name="selectFormulary">
            <input name="selectedValue" id="selectedValue">
            <select id="comboSelectRegex" onChange="printValue(this)">
                <option value="val1">TestRegex1</option>
                <option value="val2">TestRegex2</option>
                <option value="val3">Cerberus</option>
                <option value="val4">TestRegex3</option>
            </select>
        </form>
    </body>
</html>
