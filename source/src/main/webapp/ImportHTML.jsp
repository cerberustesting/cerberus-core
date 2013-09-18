<%-- 
    Document   : ImportHTML
    Created on : 11 avr. 2012, 21:31:50
    Author     : bcivel
--%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.w3c.dom.Element"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="org.xml.sax.EntityResolver"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="org.xml.sax.SAXException"%>
<%@page import="javax.xml.parsers.DocumentBuilder"%>
<%@page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@page import="java.io.File"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringReader"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<!DOCTYPE html>
<% Date DatePageStart = new Date() ; %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <link rel="stylesheet" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <title>Import HTML</title>
        <script>
            function changeColor(field) {
                document.getElementById(field).style.background = "white";
            }

            var vars = {};
            var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
                vars[key] = value;
            });

            if(vars["submited"]){
                window.opener.location.href = window.opener.location.href;

                if (window.opener.progressWindow){
                    window.opener.progressWindow.close()
                }
                window.close();
            }
        </script>
    </head>
    <body>
        <%
                String test;
                if (request.getParameter("Test") != null) {
                    test = request.getParameter("Test");
                } else {
                    test = "";
                }

                String testcase;
                if (request.getParameter("Testcase") != null) {
                    testcase = request.getParameter("Testcase");
                } else {
                    testcase = "";
                }

                String step;
                if (request.getParameter("Step") != null) {
                    step = request.getParameter("Step");
                } else {
                    step = "";
                }

                Boolean load;
                if (request.getParameter("Load") != null
                        && request.getParameter("Load").compareTo("Y") == 0) {
                    load = true;
                } else {
                    load = false;
                }
                if (!load) {
        %>
        <form action="importFile" method="post" name="selectFile" enctype="multipart/form-data">    
            <table class="contour"><tr>
                    <td class="wob" style="display:none">
                        <input id="Test" name="Test" style="width:100px" value="<%=test%>">
                        <input id="Testcase" name="Testcase" style="width:100px" value="<%=testcase%>">
                        <input id="Step" name="Step" style="width:100px" value="<%=step%>"></td>
                </tr><tr id="header">
                    <td class="wob" colspan="2" style="width:600px">Feed this field with the Path to an HTML Selenium Scenario</td>
                </tr><tr>
                    <td class="wob">Path : </td>
                    <td class="wob"><input type="file" id="Path" name="Path" style="width:500px"></td>
                    <td class="wob"><input id="Load" name="Load" style="display:none" value="Y"></td>
                </tr>
                <tr>
                    <td><input type="submit" value="Import Selenium IDE"></td>
                </tr></table>
        </form>
        <%} else {



            //PARSE THE HTML FILE 
            String pathfile = request.getParameter("FilePath");
            File fXmlFile = new File(pathfile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    if (systemId.contains(".dtd")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });

            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            //get all <tr> 
            NodeList list = doc.getElementsByTagName("tr");
            String[] actionList = new String[list.getLength() - 1];
            String[] objectList = new String[list.getLength() - 1];
            String[] propertyList = new String[list.getLength() - 1];
            for (int temp = 1; temp < list.getLength(); temp++) {
                Node nNode = list.item(temp);
                Element eElement = (Element) nNode;
                actionList[temp - 1] = eElement.getElementsByTagName("td").item(0).getTextContent();
                objectList[temp - 1] = eElement.getElementsByTagName("td").item(1).getTextContent();
                propertyList[temp - 1] = eElement.getElementsByTagName("td").item(2).getTextContent();
            }


            /*
             * List data for testing String[] actionList = {"open", "type",
             * "type", "clickAndWait", "verifyElementPresent"}; String[]
             * objectList = {"/faces/DisponibilidadeArtigos.jsp",
             * "Disponibilidade:infoSubview:tableArtigos:0:referencia",
             * "Disponibilidade:infoSubview:tableArtigos:0:tamanho",
             * "Disponibilidade:buttonUpdate",
             * "Disponibilidade:infoSubview:tableArtigos:0:mensagemErro"};
             * String[] propertyList = {"", "123456789", "00032", "", ""};
             */
            List<String[]> actObjProp = new ArrayList();
            actObjProp.add(actionList);
            actObjProp.add(objectList);
            actObjProp.add(propertyList);
            //end of data for testing  

        %>
        <form action="ImportSeleniumIDE" method="post" name="selectTestCase">
            <table class="contour"><tr>
                    <td class="wob" style="display:none">
                        <input id="importTest" name="importTest" style="width:100px" value="<%=test%>">
                        <input id="importTestcase" name="importTestcase" style="width:100px" value="<%=testcase%>">
                        <input id="importStep" name="importStep" style="width:100px" value="<%=step%>"></td>
                </tr><tr>
                    <td class="wob" colspan="4">Define the name of the properties</td>    
                </tr><tr id="header">
                    <td class="wob" style="width:150px">Action</td>
                    <td class="wob" style="width:300px">Object</td>
                    <td class="wob" style="width:150px">PropertyValue</td>
                    <td class="wob" style="width:150px">PropertyName</td>
                </tr>
                <%
                    for (int i = 0; i < actionList.length; i++) {
                %>

                <tr>
                    <td class="wob"><input style="width:150px" id="importAction" name="importAction" value="<%=actionList[i]%>"></td>
                    <td class="wob"><input style="width:300px" id="importObject" name="importObject" value="<%=objectList[i]%>"></td>
                        <% if (!propertyList[i].equals("")) {%>
                    <td class="wob"><input style="width:150px" id="importProperty" name="importProperty" value="<%=propertyList[i]%>"></td>
                    <td class="wob"><input style="width:150px ; background: red" id="importPropertyName<%=i%>" name="importPropertyName" value=""
                                           onchange="changeColor('importPropertyName<%=i%>')"></td>
                        <%} else {%>
                    <td class="wob"><input style="display:none" id="importProperty" name="importProperty" value=""></td>
                    <td class="wob" style="display:none"><input id="importPropertyName" name="importPropertyName" value="null"></td>
                        <%}%>
                </tr>

                <%}%>
                <tr>
                    <td id="wob"><input type="submit" value="Import Selenium IDE"></td>       
                </tr></table>    
        </form>
        <%}%>
<br><%// out.print(display_footer(DatePageStart)); %>
    </body>
</html>
