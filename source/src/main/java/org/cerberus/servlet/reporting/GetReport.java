package org.cerberus.servlet.reporting;

import org.cerberus.entity.Invariant;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTCase;
import org.cerberus.factory.impl.FactoryTCase;
import org.cerberus.service.IInvariantService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "GetReport", urlPatterns = {"/GetReport"})
@Component
public class GetReport extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = applicationContext.getBean(ITestCaseService.class);
        ITestCaseExecutionService testCaseExecutionService = applicationContext.getBean(ITestCaseExecutionService.class);
        IInvariantService invariantService = applicationContext.getBean(IInvariantService.class);

        TCase tCase = this.getTestCaseFromRequest(req);

        //TODO only keep the last parameter
        String environment = this.getValue(req, "Environment");
        //TODO only keep the last parameter
        String build = this.getValue(req, "Build");
        //TODO only keep the last parameter
        String revision = this.getValue(req, "Revision");
        String ip = this.getValue(req, "Ip");
        String port = this.getValue(req, "Port");
        String tag = this.getValue(req, "Tag");
        String browserVersion = this.getValue(req, "BrowserFullVersion");
        String system = this.getValues(req, "System");
        List<TCase> list = testCaseService.findTestCaseByAllCriteria(tCase, "", system);

        JSONArray data = new JSONArray();
        Map<String, Map<String, Map<String, Integer>>> mapTests = new LinkedHashMap<String, Map<String, Map<String, Integer>>>();
        try {
            List<Invariant> tcestatus = invariantService.findListOfInvariantById("TCESTATUS");
            String oldTest = "";
            Map<String, Map<String, Integer>> map = new LinkedHashMap<String, Map<String, Integer>>();
            for (TCase tc : list) {
                if (!tc.getTest().equals(oldTest)){
                    map = new LinkedHashMap<String, Map<String, Integer>>();
                }
                JSONArray array = new JSONArray();
                array.put(tc.getTest());
                array.put(tc.getTestCase());
                array.put(tc.getApplication());
                array.put(tc.getShortDescription());
                array.put(tc.getPriority());
                array.put(tc.getStatus());
                for (String country : req.getParameterValues("Country")) {
                    Map<String, Integer> status;
                    if (!tc.getTest().equals(oldTest)){
                        status = new HashMap<String, Integer>();
                        for (Invariant inv : tcestatus){
                            status.put(inv.getValue(), 0);
                        }
                    } else {
                        status = mapTests.get(tc.getTest()).get(country);
                    }
                    for (String browser : req.getParameterValues("Browser")) {
                        TestCaseExecution tce = testCaseExecutionService.findLastTCExecutionByCriteria(tc.getTest(), tc.getTestCase(),
                                environment, country, build, revision, browser, browserVersion, ip, port, tag);
                        if (tce != null) {
                            JSONObject obj = new JSONObject();
                            obj.put("result", tce.getControlStatus());
                            obj.put("execID", tce.getId());
                            array.put(obj);
                            Date date = new Date(tce.getStart());
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            array.put(formatter.format(date));
                            status.put(tce.getControlStatus(), status.get(tce.getControlStatus())+1);
                        } else {
                            array.put("");
                            array.put("");
                        }
                    }
                    map.put(country, status);
                }
                array.put(tc.getComment());
                array.put(tc.getBugID() + "for " + tc.getTargetSprint() + "/" + tc.getTargetRevision());
                array.put(tc.getGroup());

                data.put(array);
                mapTests.put(tc.getTest(), map);
                oldTest = tc.getTest();
            }

            JSONObject json = new JSONObject();

            JSONObject statistic = new JSONObject();

            JSONArray test = new JSONArray();
            for (Map.Entry<String, Map<String, Map<String, Integer>>> entryTest : mapTests.entrySet()){
                JSONArray status = new JSONArray();
                status.put(entryTest.getKey());
                for (Map.Entry<String, Map<String, Integer>> entryCountry : entryTest.getValue().entrySet()){
                    int total = 0;
                    for (Map.Entry<String, Integer> entryStatus : entryCountry.getValue().entrySet()){
                        status.put(entryStatus.getValue());
                        total += entryStatus.getValue();
                    }
                    status.put(total);
                }
                test.put(status);
            }
            statistic.put("aaData", test);

            json.put("aaData", data);
            json.put("statistic", statistic);
            json.put("iTotalRecords", data.length());
            json.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (CerberusException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private TCase getTestCaseFromRequest(HttpServletRequest req) {
        String test = this.getValues(req, "Test");
        String project = this.getValues(req, "Project");
        String application = this.getValues(req, "Application");
        String active = this.getValues(req, "TcActive");
        //TODO only keep the last parameter
        int priority = -1;
        String temp = req.getParameter("Priority");
        if (temp != null && !temp.equalsIgnoreCase("All") && StringUtil.isNumeric(temp)) {
            priority = Integer.parseInt(temp);
        }
        String status = this.getValues(req, "Status");
        String group = this.getValues(req, "Group");
        String targetBuild = this.getValues(req, "TargetBuild");
        String targetRev = this.getValues(req, "TargetRev");
        String creator = this.getValues(req, "Creator");
        String implementer = this.getValues(req, "Implementer");
        String comment = this.getValue(req, "Comment");

        IFactoryTCase factoryTCase = new FactoryTCase();
        return factoryTCase.create(test, null, null, null, creator, implementer, null, project, null, null, application, null, null, null, priority, group,
                status, null, null, null, active, null, null, null, null, null, null, targetBuild, targetRev, comment, null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }

    private String getValues(HttpServletRequest req, String valueName) {
        StringBuilder whereClause = new StringBuilder();
        String[] values = req.getParameterValues(valueName);

        if (values != null) {
            if (values.length == 1) {
                if (!"All".equalsIgnoreCase(values[0]) && !"".equalsIgnoreCase(values[0].trim())) {
                    whereClause.append(values[0]);
                }
            } else {
                whereClause.append(" ( '").append(values[0]);
                for (int i = 1; i < values.length; i++) {
                    whereClause.append("', '").append(values[i]);
                }
                whereClause.append("' ) ");
            }
            return whereClause.toString();
        }
        return null;
    }
}
