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
<%@page import="org.cerberus.entity.Invariant"%>
<%@page import="org.cerberus.exception.CerberusException"%>
<%@page import="org.cerberus.service.IInvariantService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.TreeMap"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.entity.Country"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.mysql.jdbc.ResultSetImpl"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.cerberus.version.Version"%>
<%@page import="org.cerberus.database.DatabaseSpring" %>
<%!
    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

    String ComboInvariant(Connection conn, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLClass, String combonumber, String value, String HTMLOnChange, String firstOption) {
        try {
            Statement stmtQuery = conn.createStatement();
            try {
                String sq = "SELECT value from invariant where idname = '" + combonumber + "' order by sort";
                ResultSet q = stmtQuery.executeQuery(sq);
                try {
                    String ret = "<select id=\"" + HTMLId + "\" class=\"" + HTMLClass + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
                    if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                        ret = ret + " onchange=\"" + HTMLOnChange + "\"";
                    }
                    ret = ret + ">";
                    if (firstOption != null) {
                        ret = ret + "<option value=\"" + firstOption + "\">--" + firstOption + "--</option>";
                    }
                    while (q.next()) {
                        ret = ret + "<option value=\"" + q.getString("value") + "\"";
                        if ((value != null) && (value.compareTo(q.getString("value")) == 0)) {
                            ret = ret + " SELECTED ";
                        }
                        ret = ret + ">" + q.getString("value");
                        ret = ret + "</option>";
                    }
                    ret = ret + "</select>";

                    return ret;
                } finally {
                    q.close();
                }
            } finally {
                stmtQuery.close();
            }
        } catch (SQLException e) {
            return e.toString();
        }
    }

    String ComboInvariant(String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLClass, String combonumber, String value, String HTMLOnChange, String firstOption) {
        try {
            IInvariantService invFunctionService = appContext.getBean(IInvariantService.class);
            List<Invariant> invFunctionList = invFunctionService.findListOfInvariantById(combonumber);
            String ret = "<select id=\"" + HTMLId + "\" class=\"" + HTMLClass + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
            if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                ret = ret + " onchange=\"" + HTMLOnChange + "\"";
            }
            ret = ret + ">";
            if (firstOption != null) {
                ret = ret + "<option value=\"" + firstOption + "\">--" + firstOption + "--</option>";
            }
            for (Invariant invFunction : invFunctionList) {
                ret = ret + "<option value=\"" + invFunction.getValue() + "\"";
                if ((value != null) && (value.compareTo(invFunction.getValue()) == 0)) {
                    ret = ret + " SELECTED ";
                }
                ret = ret + ">" + invFunction.getValue();
                ret = ret + "</option>";
            }
            ret = ret + "</select>";

            return ret;

        } catch (CerberusException e) {
            return e.toString();
        } 
    }

    String ComboInvariantAjax(Connection conn, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String combonumber, String value, String HTMLOnChange, boolean emptyfirstoption) {
        try {
            Statement stmtQuery = conn.createStatement();
            try {
                String sq = "SELECT value from invariant where idname = '" + combonumber + "' order by sort";
                ResultSet q = stmtQuery.executeQuery(sq);
                try {
                    String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
                    if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                        ret = ret + " onchange=\"" + HTMLOnChange + "\"";
                    }
                    ret = ret + ">";
                    if (emptyfirstoption) {
                        ret = ret + "<option value=\"\"></option>";
                    }
                    while (q.next()) {
                        ret = ret + "<option value=\"" + q.getString("value") + "\"";
                        if ((value != null) && (value.compareTo(q.getString("value")) == 0)) {
                            ret = ret + " SELECTED ";
                        }
                        ret = ret + ">" + q.getString("value");
                        ret = ret + "</option>";
                    }
                    ret = ret + "</select>";
                    return ret;
                } finally {
                    q.close();
                }
            } finally {
                stmtQuery.close();
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    String ComboInvariantMultipleAjax(Connection conn, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String combonumber, String value, String HTMLOnChange, boolean emptyfirstoption) {
        try {
            Statement stmtQuery = conn.createStatement();
            try {
                String sq = "SELECT value from invariant where idname = '" + combonumber + "' order by sort";
                ResultSet q = stmtQuery.executeQuery(sq);
                try {
                    String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
                    if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                        ret = ret + " onchange=\"" + HTMLOnChange + "\"";
                    }
                    ret = ret + " multiple >";
                    if (emptyfirstoption) {
                        ret = ret + "<option value=\"\"></option>";
                    }
                    while (q.next()) {
                        ret = ret + "<option value=\"" + q.getString("value") + "\"";
                        if ((value != null) && (value.compareTo(q.getString("value")) == 0)) {
                            ret = ret + " SELECTED ";
                        }
                        ret = ret + ">" + q.getString("value");
                        ret = ret + "</option>";
                    }
                    ret = ret + "</select>";
                    return ret;
                } finally {
                    q.close();
                }
            } finally {
                stmtQuery.close();
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    String ComboProject(Connection conn, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLClass, String value, String HTMLOnChange, boolean emptyfirstoption, String FirstValue, String FirstDescription) {
        try {
            Statement stmtQuery = conn.createStatement();
            try {
                String sq = "SELECT idproject, VCCode, Description, active FROM project WHERE active='Y' or idproject='" + value + "' ORDER BY idproject";
                ResultSet q = stmtQuery.executeQuery(sq);
                try {
                    String ret = "<select id=\"" + HTMLId + "\" class=\"" + HTMLClass + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
                    if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                        ret = ret + " onchange=\"" + HTMLOnChange + "\"";
                    }
                    ret = ret + ">";
                    if (emptyfirstoption) {
                        ret = ret + " <option value=\"" + FirstValue + "\">" + FirstDescription + "</option>";
                    }
                    while (q.next()) {
                        ret = ret + " <option value=\"" + q.getString("idproject") + "\"";
                        ret = ret + " style=\"width: 200px;";
                        if (q.getString("active").equalsIgnoreCase("Y")) {
                            ret = ret + "font-weight:bold;";
                        }
                        ret = ret + "\"";
                        if ((value != null) && (value.compareTo(q.getString("idproject")) == 0)) {
                            ret = ret + " SELECTED ";
                        }
                        ret = ret + ">" + q.getString("idproject") + " " + q.getString("Description");
                        ret = ret + "</option>";
                    }
                    ret = ret + " </select>";
                    return ret;
                } finally {
                    q.close();
                }
            } finally {
                stmtQuery.close();
            }
        } catch (SQLException e) {
            return e.toString();
        }
    }

    String ComboDeployTypeAjax(Connection conn, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String value, String HTMLOnChange) {
        try {
            Statement stmtQuery = conn.createStatement();
            try {
                String sq = "SELECT deploytype from deploytype ";
                ResultSet q = stmtQuery.executeQuery(sq);
                try {
                    String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
                    if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                        ret = ret + " onchange=\"" + HTMLOnChange + "\"";
                    }
                    ret = ret + ">";
                    while (q.next()) {
                        ret = ret + "<option value=\"" + q.getString("deploytype") + "\"";
                        if ((value != null) && (value.compareTo(q.getString("deploytype")) == 0)) {
                            ret = ret + " SELECTED ";
                        }
                        ret = ret + ">" + q.getString("deploytype");
                        ret = ret + "</option>";
                    }
                    ret = ret + "</select>";
                    return ret;
                } finally {
                    q.close();
                }
            } finally {
                stmtQuery.close();
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    Boolean checkSelected(Collection<Country> col, String selection) {
        Iterator<Country> it = col.iterator();
        while (it.hasNext()) {
            if (it.next().getCountry().compareTo(selection) == 0) {
                return true;
            }
        }
        return false;
    }

    String replaceTextEnter(String desc) {
        String s = desc;
        s = s.replaceAll("\r", "__enter__");
        s = s.replaceAll("\n", "__enter__");
        if (s.contains("br")) {
            s = s.replaceAll("<br>", "__breakpoint__");
            s = s.replaceAll("</br>", "__breakpoint__");
            s = s.replaceAll("<br/>", "__breakpoint__");
            s = s.replaceAll("<br />", "__breakpoint__");
        }
        s = s.replaceAll("<", "__abrir__");
        s = s.replaceAll("/>", "__fechar__");
        return s;
    }

    String display_footer(Date DatePageStart) {
        Date mydate = new Date();
        long Duration = mydate.getTime() - DatePageStart.getTime();
        String footer = "Page started generating on <b><span id=\"foot-loaddatetime\">" + DatePageStart.toString() + "</span></b>"
                + " by <b><span id=\"foot-projectname\">" + Version.PROJECT_NAME + "</span></b>"
                + " <b><span id=\"foot-version\">" + Version.VERSION + "</span></b>"
                + " in <b><span id=\"foot-env\">" + System.getProperty("org.cerberus.environment") + "</span></b>"
                + " and took <b><span id=\"foot-duration\">" + Duration + "</span>ms</b>"
                + " - Open a bug or ask for any new feature <a target=\"_blank\"  href=\"https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + Version.VERSION + "\">here</a>.";
        return footer;
    }

    String generateMultiSelect(String parameterName, String[] parameters, TreeMap<String, String> options, String headerText, String noneSeletedText, String selectedText, int selectedList, boolean firstValueAll) {
        String parameter = "";
        if (parameters != null && parameters.length > 0 && (parameters[0]).compareTo("All") != 0) {
            parameter = StringUtils.join(parameters, ",");
        }
        parameter += ",";

        String select = "<select class=\"multiSelectOptions\" multiple  "
                + "data-header=\"" + headerText + "\" "
                + "data-none-selected-text=\"" + noneSeletedText + "\" "
                + "data-selected-text=\"" + selectedText + "\" "
                + "data-selected-list=\"" + selectedList + "\" "
                + "size=\"3\" id=\"" + parameterName + "\" name=\"" + parameterName + "\">\n";
        if (firstValueAll) {
            select += "<option value=\"All\">-- ALL --</option>\n";
        }
        for (String key : options.keySet()) {
            select += " <option value=\"" + key + "\"";

            if ((parameter != null) && (parameter.indexOf(key + ",") >= 0)) {
                select += " SELECTED ";
            }
            select += ">" + options.get(key) + "</option>\n";
        }
        select += "</select>\n";
        select += "<!-- " + parameter + " -->\n";
        return select;
    }

    String generateWhereClausesForParametersAndColumns(String[] parameters, String[] columns, HttpServletRequest request) {
        StringBuffer whereClause = new StringBuffer();
        for (int index = 0; index < parameters.length; index++) {
            String[] values = request.getParameterValues(parameters[index]);
            if (values != null) {
                if (values.length == 1) {
                    if (!"all".equalsIgnoreCase(values[0]) && !"".equalsIgnoreCase(values[0].trim())) {
                        whereClause.append(" AND ").append(columns[index]).append("='").append(values[0]).append("'");
                    }
                } else {
                    whereClause.append(" AND ( 1!=1 ");
                    for (String value : values) {
                        whereClause.append(" OR ").append(columns[index]).append("='").append(value).append("'");
                    }
                    whereClause.append(" ) ");
                }
            }
        }
        return whereClause.toString();
    }
%>
<% if (session.getAttribute("flashMessage") != null) {
        out.println("alert(" + session.getAttribute("flashMessage") + ")");
        session.removeAttribute("flashMessage");
    }

    DatabaseSpring db = appContext.getBean(DatabaseSpring.class);

%>