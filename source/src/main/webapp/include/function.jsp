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
<%@page import="org.cerberus.enums.MessageEventEnum"%>
<%@page import="org.cerberus.util.answer.AnswerList"%>
<%@page import="org.cerberus.crud.entity.SessionCounter"%>
<%@page import="org.cerberus.crud.entity.Project"%>
<%@page import="org.cerberus.crud.service.IProjectService"%>
<%@page import="org.cerberus.crud.entity.Invariant"%>
<%@page import="org.cerberus.exception.CerberusException"%>
<%@page import="org.cerberus.crud.service.IInvariantService"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.TreeMap"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.cerberus.crud.entity.Country"%>
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
<%@page import="org.cerberus.version.Infos"%>
<%@page import="org.cerberus.database.DatabaseSpring" %>
<%@page import="org.cerberus.crud.service.IDeployTypeService" %>
<%@page import="org.cerberus.crud.entity.DeployType" %>
<%!String ComboInvariant(ApplicationContext appContext, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLClass, String combonumber, String value, String HTMLOnChange, String firstOption) {

        IInvariantService invFunctionService = appContext.getBean(IInvariantService.class);
        AnswerList answer = invFunctionService.readByIdname(combonumber);
        List<Invariant> invFunctionList = (List<Invariant>)answer.getDataList();

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
    }

    String ComboInvariantAjax(ApplicationContext appContext, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String combonumber, String value, String HTMLOnChange, boolean emptyfirstoption) {
        try {
            IInvariantService invFunctionService = appContext.getBean(IInvariantService.class);
            AnswerList answer = invFunctionService.readByIdname(combonumber);
            List<Invariant> invFunctionList = (List<Invariant>)answer.getDataList();
            

            String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
            if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                ret += " onchange=\"" + HTMLOnChange + "\"";
            }
            ret += ">";
            if (emptyfirstoption) {
                ret += "<option value=\"\"></option>";
            }
            for (Invariant inv : invFunctionList) {
                ret += "<option value=\"" + inv.getValue() + "\"";
                if ((value != null) && (value.compareTo(inv.getValue()) == 0)) {
                    ret += " SELECTED ";
                }
                ret += ">" + inv.getValue() + "</option>";
            }
            ret += "</select>";
            return ret;

        } catch (Exception e) {
            return e.toString();
        }
    }

    String ComboInvariantMultipleAjax(ApplicationContext appContext, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String combonumber, String value, String HTMLOnChange, boolean emptyfirstoption) {
        try {
            IInvariantService invFunctionService = appContext.getBean(IInvariantService.class);
            AnswerList answer = invFunctionService.readByIdname(combonumber);
            List<Invariant> invFunctionList = (List<Invariant>)answer.getDataList(); 

            String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
            if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                ret += " onchange=\"" + HTMLOnChange + "\"";
            }
            ret += " multiple >";
            if (emptyfirstoption) {
                ret += "<option value=\"\"></option>";
            }
            for (Invariant inv : invFunctionList) {
                ret += "<option value=\"" + inv.getValue() + "\"";
                if ((value != null) && (value.compareTo(inv.getValue()) == 0)) {
                    ret += " SELECTED ";
                }
                ret += ">" + inv.getValue() + "</option>";
            }
            ret += "</select>";
            return ret;

        } catch (Exception e) {
            return e.toString();
        }
    }

    String ComboProject(ApplicationContext appContext, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLClass, String value, String HTMLOnChange, boolean emptyfirstoption, String FirstValue, String FirstDescription) {
        try {
            IProjectService invProjectService = appContext.getBean(IProjectService.class);
            List<Project> invProjectList = invProjectService.convert(invProjectService.readAll());
            String ret = "<select id=\"" + HTMLId + "\" class=\"" + HTMLClass + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
            if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                ret = ret + " onchange=\"" + HTMLOnChange + "\"";
            }
            ret = ret + ">";
            if (emptyfirstoption) {
                ret = ret + " <option value=\"" + FirstValue + "\">" + FirstDescription + "</option>";
            }
            for (Project p : invProjectList) {
                ret = ret + " <option value=\"" + p.getIdProject() + "\"";
                ret = ret + " style=\"width: 200px;";
                if (p.getActive().equalsIgnoreCase("Y")) {
                    ret = ret + "font-weight:bold;";
                }
                ret = ret + "\"";
                if ((value != null) && (value.compareTo(p.getIdProject()) == 0)) {
                    ret = ret + " SELECTED ";
                }
                ret = ret + ">" + p.getIdProject() + " " + p.getDescription();
                ret = ret + "</option>";
            }
            ret = ret + " </select>";
            return ret;

        } catch (Exception e) {
            return e.toString();
        }
    }

    String ComboDeployTypeAjax(ApplicationContext appContext, String HTMLComboName, String HTMLComboStyle, String HTMLId, String HTMLrel, String value, String HTMLOnChange) {
        try {
            IDeployTypeService deployTypeService = appContext.getBean(IDeployTypeService.class);

            String ret = "<select id=\"" + HTMLId + "\" rel=\"" + HTMLrel + "\" style=\"" + HTMLComboStyle + "\" name=\"" + HTMLComboName + "\"";
            if (HTMLOnChange.compareToIgnoreCase("") != 0) {
                ret += " onchange=\"" + HTMLOnChange + "\"";
            }
            ret += ">";
            
            
            AnswerList resp = deployTypeService.readAll();

            if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (DeployType deployType : (List<DeployType>) resp.getDataList()) {
                    ret += "<option value=\"" + deployType.getDeploytype() + "\"";
                    if ((value != null) && (value.compareTo(deployType.getDeploytype()) == 0)) {
                        ret += " SELECTED ";
                    }
                    ret += ">" + deployType.getDeploytype();
                    ret += "</option>";
                }
                
                
            } 
            
            ret += "</select>";
            return ret;

        } catch (Exception e) {
            return e.toString();
        }
    }

    Boolean checkSelected(Collection<Country> col, String selection) {
        for (Country aCol : col) {
            if (aCol.getCountry().compareTo(selection) == 0) {
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
                + " by <b><span id=\"foot-projectname\">" + Infos.getInstance().getProjectName() + "</span></b>"
                + " <b><span id=\"foot-version\">" + Infos.getInstance().getProjectVersion() + "</span></b>"
                + " in <b><span id=\"foot-env\">" + System.getProperty("org.cerberus.environment") + "</span></b>"
                + " and took <b><span id=\"foot-duration\">" + Duration + "</span>ms</b>"
                + " - Open a bug or ask for any new feature <a target=\"_blank\"  href=\"https://github.com/vertigo17/Cerberus/issues/new?body=Cerberus%20Version%20:%20" + Infos.getInstance().getProjectVersion() + "\">here</a>.";
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

            if (parameter.contains(key + ",")) {
                select += " SELECTED ";
            }
            select += ">" + options.get(key) + "</option>\n";
        }
        select += "</select>\n";
        select += "<!-- " + parameter + " -->\n";
        return select;
    }

    String generateWhereClausesForParametersAndColumns(String[] parameters, String[] columns, HttpServletRequest request) {
        StringBuilder whereClause = new StringBuilder();
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

    String getRequestParameterWildcardIfEmpty(HttpServletRequest request, String parameter) {
        String result;
        if (request.getParameter(parameter) != null
                && request.getParameter(parameter).compareTo("All") != 0) {
            result = request.getParameter(parameter);
        } else {
            result = "";
        }
        return result;
    }

    boolean getBooleanParameterFalseIfEmpty(HttpServletRequest request, String parameter) {
        return request.getParameter(parameter) != null
                && request.getParameter(parameter).compareTo("Y") == 0;
    }%>
<% if (session.getAttribute("flashMessage") != null) {
        out.println("alert(" + session.getAttribute("flashMessage") + ")");
        session.removeAttribute("flashMessage");
    }

    ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    DatabaseSpring db = appContext.getBean(DatabaseSpring.class);

%>
<%
    SessionCounter sc = appContext.getBean(SessionCounter.class);
    if (request.getUserPrincipal()!=null){
        sc.identifiateUser(request.getSession().getId(), request.getUserPrincipal().getName());
    }
    %>