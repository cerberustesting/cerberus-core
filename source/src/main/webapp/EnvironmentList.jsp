<%-- 
    Document   : environment
    Created on : Nov 1, 2011, 11:19:18 AM
    Author     : vertigo
--%>

<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Environment Management</title>
        <link rel="stylesheet" 
              type="text/css" href="css/crb_style.css"
              />
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
    </head>

    <body>

        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>

        <%

           Date DatePageStart = new Date() ;

           Connection conn = db.connect();

            try {

                /* Parameter Setup */

                String country;
                Boolean country_def;
                if (request.getParameter("country") != null && request.getParameter("country").compareTo("") != 0) {
                    country = request.getParameter("country");;
                    country_def = false;
                } else {
                    country = new String("ALL");
                    country_def = true;
                }


                String env;
                Boolean env_def;
                if (request.getParameter("env") != null && request.getParameter("env").compareTo("") != 0) {
                    env = request.getParameter("env");;
                    env_def = false;
                } else {
                    env = new String("ALL");
                    env_def = true;
                }

                String envgp;
                Boolean envgp_def;
                if (request.getParameter("envgp") != null && request.getParameter("envgp").compareTo("") != 0) {
                    envgp = request.getParameter("envgp");;
                    envgp_def = false;
                } else {
                    envgp = new String("ALL");
                    envgp_def = true;
                }

                String build;
                if (request.getParameter("build") != null && request.getParameter("build").compareTo("") != 0) {
                    build = request.getParameter("build");;
                } else {
                    build = new String("ALL");
                }

                String revision;
                if (request.getParameter("revision") != null && request.getParameter("revision").compareTo("") != 0) {
                    revision = request.getParameter("revision");;
                } else {
                    revision = new String("ALL");
                }

                String chain;
                if (request.getParameter("chain") != null && request.getParameter("chain").compareTo("") != 0) {
                    chain = request.getParameter("chain");;
                } else {
                    chain = new String("ALL");
                }

                String active;
                if (request.getParameter("active") != null && request.getParameter("active").compareTo("") != 0) {
                    active = request.getParameter("active");;
                } else {
                    active = new String("ALL");
                }
                                              
                String type;
                if (request.getParameter("type") != null && request.getParameter("type").compareTo("") != 0) {
                    type = request.getParameter("type");;
                } else {
                    type = new String("ALL");
                }
                                              
                /* Filter part */

                Statement stmtCountry = conn.createStatement();
                Statement stmtEnv = conn.createStatement();
                Statement stmtEnvgp = conn.createStatement();
                Statement stmtBuild = conn.createStatement();
                Statement stmtRev = conn.createStatement();
                Statement stmtNextRev = conn.createStatement();
                Statement stmtChain = conn.createStatement();
                Statement stmtActive = conn.createStatement();
                Statement stmtType = conn.createStatement();


        %><table class="tablef"> <tr> <td> 
                    <form method="GET" name="environment" id="environment">
                        <ftxt><%=dbDocS(conn,"testcase","country","")%></ftxt> <select id="country" name="country" style="width: 100px" OnChange ="document.environment.submit()">
                            <option style="width: 400px" value="ALL">-- ALL --</option>
                            <%ResultSet rsCountry = stmtCountry.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 4 "
                                        + "ORDER BY sort ASC");
                                while (rsCountry.next()) {
                            %><option style="width: 400px" value="<%= rsCountry.getString(1)%>" <%=country.compareTo(rsCountry.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsCountry.getString(1)%> - <%= rsCountry.getString(2)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"invariant","environment","")%></ftxt> <select id="env" name="env" style="width: 100px" OnChange ="document.environment.submit()">
                            <option style="width: 500px" value="ALL">-- ALL --</option>
                            <%ResultSet rsEnv = stmtEnv.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 5 "
                                        + "ORDER BY sort ASC");
                                while (rsEnv.next()) {
                            %><option style="width: 5   00px" value="<%= rsEnv.getString(1)%>" <%=env.compareTo(rsEnv.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnv.getString(1)%> - <%= rsEnv.getString(2)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"invariant","environmentgp","")%></ftxt> <select id="envgp" name="envgp" style="width: 80px" OnChange ="document.environment.submit()">
                            <option style="width: 200px" value="ALL">-- ALL --</option>
                            <%ResultSet rsEnvgp = stmtEnvgp.executeQuery("SELECT distinct gp1 "
                                        + "FROM invariant "
                                        + "WHERE id = 5 "
                                        + "ORDER BY sort ASC");
                                while (rsEnvgp.next()) {
                            %><option style="width: 200px" value="<%= rsEnvgp.getString(1)%>" <%=envgp.compareTo(rsEnvgp.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsEnvgp.getString(1)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"invariant","build","")%></ftxt> <select id="build" name="build" style="width: 80px" OnChange ="document.environment.submit()">
                            <option style="width: 200px" value="ALL">-- ALL --</option>
                            <%ResultSet rsBuild = stmtBuild.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 8 "
                                        + "ORDER BY sort ASC");
                                while (rsBuild.next()) {
                            %><option style="width: 200px" value="<%= rsBuild.getString(1)%>" <%=build.compareTo(rsBuild.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsBuild.getString(1)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"invariant","revision","")%></ftxt> <select id="revision" name="revision" style="width: 80px" OnChange ="document.environment.submit()">
                            <option style="width: 200px" value="ALL">-- ALL --</option>
                            <%ResultSet rsRev = stmtRev.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 9 "
                                        + "ORDER BY sort ASC");
                                while (rsRev.next()) {
                            %><option style="width: 200px" value="<%= rsRev.getString(1)%>" <%=revision.compareTo(rsRev.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsRev.getString(1)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"countryenvparam","chain","")%></ftxt> <input id="chain" name="chain" style="width: 50px" value="<%=chain%>"/>
                        <ftxt><%=dbDocS(conn,"countryenvparam","active","")%></ftxt> <select id="active" name="active" style="width: 80px" OnChange ="document.environment.submit()">
                            <option style="width: 200px" value="ALL">-- ALL --</option>
                            <%ResultSet rsActive = stmtActive.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 11 "
                                        + "ORDER BY sort ASC");
                                while (rsActive.next()) {
                            %><option style="width: 200px" value="<%= rsActive.getString(1)%>" <%=active.compareTo(rsActive.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsActive.getString(1)%></option>
                            <% }
                            %></select>
                        <ftxt><%=dbDocS(conn,"countryenvparam","type","")%></ftxt> <select id="type" name="type" style="width: 100px" OnChange ="document.environment.submit()">
                            <option style="width: 200px" value="ALL">-- ALL --</option>
                            <%ResultSet rsType = stmtType.executeQuery("SELECT value, description "
                                        + "FROM invariant "
                                        + "WHERE id = 10 "
                                        + "ORDER BY sort ASC");
                                while (rsType.next()) {
                            %><option style="width: 200px" value="<%= rsType.getString(1)%>" <%=type.compareTo(rsType.getString(1)) == 0 ? " SELECTED " : ""%>><%= rsType.getString(1)%></option>
                            <% }
                            %></select>
                        <input type="submit" name="FilterApply" value="Apply">
                    </form>
                </td></tr></table>
                        <br>

        <%
            stmtCountry.close();
            stmtEnv.close();


            /* Page Display - START */

            Statement stmtCE = conn.createStatement();
            Statement stmtCEcnt = conn.createStatement();


            /* Country loop */
            String PCE;
            String PCE_cnt;
            String Build;
            String Revision;
            String Type;
            int i,j;
            
              
                // Country - Environment Page List.
                
                PCE = "SELECT DISTINCT c.Country, c.Environment, c.Build, c.Revision, c.Chain, c.Active, c.Type, "
                        + "c.DistribList, c.EMailBodyRevision, c.EmailBodyChain, i.gp1 "
                        + "FROM `countryenvparam` c "
                        + "left outer join invariant i  on Environment=i.value and i.id=5 "
                        + "left outer join invariant i1 on Country=i1.value    and i1.id=4 "
                        + "WHERE 1=1 ";
                PCE_cnt = "SELECT count(*) cnt "
                        + "FROM `countryenvparam` c "
                        + "left outer join invariant i on Environment=value and id=5 "
                        + "WHERE 1=1 ";
                if (!country.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Country='" + country + "' ";
                    PCE_cnt += " and Country='" + country + "' ";
                }
                if (!env.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Environment='" + env + "' ";
                    PCE_cnt += " and Environment='" + env + "' ";
                }
                if (!envgp.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and i.gp1='" + envgp + "' ";
                    PCE_cnt += " and i.gp1='" + envgp + "' ";
                }
                if (!build.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Build='" + build + "' ";
                    PCE_cnt += " and Build='" + build + "' ";
                }
                if (!revision.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Revision='" + revision + "' ";
                    PCE_cnt += " and Revision='" + revision + "' ";
                }
                if (!chain.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Chain='" + chain + "' ";
                    PCE_cnt += " and Chain='" + chain + "' ";
                }
                if (!active.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Active='" + active + "' ";
                    PCE_cnt += " and Active='" + active + "' ";
                }
                if (!type.trim().equalsIgnoreCase("ALL")) {
                    PCE += " and Type='" + type + "' ";
                    PCE_cnt += " and Type='" + type + "' ";
                }
                PCE += " ORDER BY i1.sort, i.sort ";
              
                ResultSet rsPCE = stmtCE.executeQuery(PCE);
                ResultSet rsPCE_cnt = stmtCEcnt.executeQuery(PCE_cnt);
                if (rsPCE_cnt.first()) {
                    i = Integer.valueOf(rsPCE_cnt.getString("cnt"));
                    i = i/2 + 1;
                                       }else{
                                         i=10;  
                                       }
                       %>
        
                       <table style="width: 100%">
            <tr>
                <td valign="top" >
                    
 <%
                j=0;
                String color = "white";
                String cty = "";
                int a = 0;
                
                while (rsPCE.next()) {
                    Build = rsPCE.getString("c.Build");
                    Revision = rsPCE.getString("c.Revision");
                
                    //Background color 
                    if (rsPCE.getString("c.Active").equals("Y"))
                   {color = "#f3f6fa";} else 
                   {color = "White";}   
                    // End of background color 
                   j++;
                   if ((j==1) || (j==i)) {
                   %>
                    
                    <table style="text-align: left; border-collapse: collapse">
                        <tr id="header">
                <td><%=dbDocS(conn,"testcase","country","")%></td>
                <td><%=dbDocS(conn,"invariant","environment","")%></td>
                <td><%=dbDocS(conn,"invariant","build","")%></td>
                <td><%=dbDocS(conn,"invariant","revision","")%></td>
                <td><%=dbDocS(conn,"countryenvparam","chain","")%></td>
                <td><%=dbDocS(conn,"countryenvparam","active","")%></td>
                <td><%=dbDocS(conn,"countryenvparam","type","")%></td>
                <td> </td>
            </tr>
            <%}%>
            <tr>
                <td style="background-color:<%=color%>"><b><%=rsPCE.getString("c.Country")%></b></td>
                <td style="background-color:<%=color%>"><b><%=rsPCE.getString("c.Environment")%></b></td>
                <td style="background-color:<%=color%>"><%=Build != null ? Build : ""%></td>
                <td style="background-color:<%=color%>"><%=Revision != null ? Revision : ""%></td>
                <td style="background-color:<%=color%>"><%=rsPCE.getString("c.Chain") != null ? rsPCE.getString("c.Chain") : ""%></td>
                <td style="background-color:<%=color%>"><%=rsPCE.getString("c.Active") != "N" ? rsPCE.getString("c.Active") : ""%></td>
                <td style="background-color:<%=color%>"><%=rsPCE.getString("c.Type") != null ? rsPCE.getString("c.Type") : ""%></td>
                <td style="background-color:<%=color%>"><a href="Environment.jsp?country=<%=rsPCE.getString("c.Country")%>&env=<%=rsPCE.getString("c.Environment")%>">select</a></td>
            </tr>
<%                   if (j==i-1) {
                   %>
        </table>  
                </td>
                <td>
            <%
            }
            }%>
            
        </table>  
                </td>
            </tr>
        </table>

        <%
            /* Page Display - END */

            stmtActive.close();
            stmtBuild.close();
            stmtCE.close();
            stmtCEcnt.close();
            stmtChain.close();
            stmtCountry.close();
            stmtEnv.close();
            stmtEnvgp.close();
            stmtNextRev.close();
            stmtRev.close();
            stmtType.close();
            
            rsActive.close();
            rsBuild.close();
            rsCountry.close();
            rsEnv.close();
            rsEnvgp.close();
            rsRev.close();
            rsType.close();
            

        } catch (Exception e) {
            Logger.getLogger("EnvironmentList.jsp").log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched.", e);
            out.println("<br> error message : " + e.getMessage() + " " + e.toString() + "<br>");
        } finally {
                                    try {
                                        conn.close();
                                    } catch (Exception ex) {
            Logger.getLogger("EnvironmentList.jsp").log(Level.SEVERE, Version.PROJECT_NAME_VERSION + " - Exception catched on close.", ex);
                                    }
                                }


    %>
<br><% out.print(display_footer(DatePageStart)); %>
</body>
</html>
