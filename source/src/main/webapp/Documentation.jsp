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
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.springframework.context.ApplicationContext" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="org.cerberus.crud.entity.Documentation"%>
<%@page import="org.cerberus.crud.service.IDocumentationService"%>
<%@page import="org.cerberus.util.StringUtil"%>
<%@page import="org.cerberus.util.ParameterParserUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <%
        String DocTable = ParameterParserUtil.parseStringParam(request.getParameter("DocTable"), "empty");
        String DocField = ParameterParserUtil.parseStringParam(request.getParameter("DocField"), "empty");
        String DocValue = ParameterParserUtil.parseStringParam(request.getParameter("DocValue"), "empty");
        String LangValue = ParameterParserUtil.parseStringParam(request.getParameter("Lang"), "en");
        boolean DocValue_isdefined = true;
        if (DocValue.equalsIgnoreCase("empty")) {
            DocValue_isdefined = false;
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IDocumentationService documentationService = appContext.getBean(IDocumentationService.class);

        String Title = "";
        List<String> TitleList;
        TitleList = new ArrayList<String>();
        String Doc = "";
        List<String> DocList;
        DocList = new ArrayList<String>();

        if (DocValue_isdefined == false) {

            if (DocTable.equalsIgnoreCase("all")) { // All the documentation displayed inside a single page.
                Documentation documentation;
                Iterator<Documentation> iterAllDoc = documentationService.findAll(LangValue).iterator();

                while (iterAllDoc.hasNext()) {
                    documentation = iterAllDoc.next();
                    Doc = documentation.getDocDesc();
                    if (StringUtil.isNullOrEmpty(documentation.getDocValue())) {
                        if (StringUtil.isNullOrEmpty(request.getParameter("HideKey"))) {
                            Title = "[" + documentation.getDocTable() + " - " + documentation.getDocField() + "] - " + documentation.getDocLabel();
                        } else {
                            Title = documentation.getDocLabel();
                        }

                        List<Documentation> documentations = documentationService.findDocumentationsWithNotEmptyValueAndDescription(documentation.getDocTable(), documentation.getDocField(), LangValue);
                        Doc = Doc + "<table>";
                        for(int i=0; i<documentations.size();i++) {
                                Doc = Doc + "<tr><td><a href=\"?DocTable=" + documentation.getDocTable();
                                Doc = Doc + "&amp;DocField=" + documentation.getDocField() + "&amp;DocValue=" + documentations.get(i).getDocValue() + "\">";
                                Doc = Doc + documentations.get(i).getDocValue() + "</a></td><td>" + documentations.get(i).getDocLabel() + "</td></tr>";
                        }
                        Doc = Doc + "</table>";

                    } else {
                        if (StringUtil.isNullOrEmpty(request.getParameter("HideKey"))) {
                            Title = "[" + documentation.getDocTable() + " - " + documentation.getDocField() + " - " + documentation.getDocValue() + "] - " + documentation.getDocLabel();
                        } else {
                            Title = documentation.getDocLabel();
                        }
                    }
                    TitleList.add(Title);
                    DocList.add(Doc);
                }
            } else { // Documentation of a normal field. The field could potencially have occurences at Value level that will be displayed.
                List<Documentation> documentations = documentationService.findDocumentationsWithEmptyValueAndNotEmptyDescription(DocTable, DocField, LangValue);
                if (documentations != null && documentations.size() > 0) {
                    Title = documentations.get(0).getDocLabel();
                    Doc = documentations.get(0).getDocDesc();

                    documentations = documentationService.findDocumentationsWithNotEmptyValueAndDescription(DocTable, DocField, LangValue);
                    Doc = Doc + "<table>";
                    for(int i=0; i<documentations.size();i++) {
                            Doc = Doc + "<tr><td><a href=\"?DocTable=" + DocTable;
                            Doc = Doc + "&amp;DocField=" + DocField + "&amp;DocValue=" + documentations.get(i).getDocValue() + "\">";
                            Doc = Doc + documentations.get(i).getDocValue() + "&amp;Lang=" + LangValue + "</a></td><td>" + documentations.get(i).getDocLabel() + "</td></tr>";
                    }
                    Doc = Doc + "</table>";

                    TitleList.add(Title);
                    DocList.add(Doc);
                } else {
                    Title = "No Documentation Found !";
                    Doc = "";
                    TitleList.add(Title);
                    DocList.add(Doc);
                }
            }

        } else { // Documentation of the detail of a field + value.


            String docLabel = documentationService.findLabelFromTableAndField(DocTable, DocField, LangValue);

            String nav = "";
            if (docLabel != null) {
                nav = "<a href=\"?DocTable=" + DocTable + "&amp;DocField=" + DocField + "\">" + docLabel + "</a>";
            }

            String docDesc = documentationService.findDescriptionFromTableFieldAndValue(DocTable, DocField, DocValue, LangValue);
            if (docDesc != null) {
                Title = nav + " >> " + DocValue;
                Doc = docDesc;
                Doc = Doc + "<br><br>Back to " + nav;
                TitleList.add(Title);
                DocList.add(Doc);
            } else {
                Title = "No Documentation Found !";
                Doc = "";
                TitleList.add(Title);
                DocList.add(Doc);
            }

        }

        if (TitleList.size() > 1) {
            Title = "Full Documentation";
        }

    %>


    <head>
        <title><%= Title%></title>
        <link rel="stylesheet" href="css/crb_style_doc.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />        
        <link type="text/css" rel="stylesheet" href="css/bootstrap.css">
    </head>

    <body>
        <%
            Integer i = 0;
            for (String tcs : TitleList) {
                if (i > 0) {
                    out.print("<br>");
                }
        %>      
        <table class="doctb">
            <tr id="header" class="doctl"><td><%= tcs%></td></tr>
            <tr>
                <td style="width: 100%;">
                    <%= DocList.get(i)%>
                </td>
            </tr>
        </table>
        <%
                    i++;
                }
        %>
        <br>
        <span class="close"><a href="javascript:self.close()">Close</a> the popup.</span>
        <br>
        <span class="footer">DocTable:<%= DocTable%> | DocField:<%= DocField%> | DocValue:<%= DocValue%>  | Lang:<%= LangValue%> </span>
    </body>
</html>

