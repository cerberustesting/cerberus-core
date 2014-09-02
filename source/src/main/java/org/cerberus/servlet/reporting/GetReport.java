package org.cerberus.servlet.reporting;

import org.apache.log4j.Logger;
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

/**
 * Build data for detail table and calculate data dor statistics tables
 *
 * @version 1.0
 * @since 2014-05-27
 */
@WebServlet(name = "GetReport", urlPatterns = {"/GetReport"})
@Component
public class GetReport extends HttpServlet {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(GetReport.class);

    private ITestCaseExecutionService testCaseExecutionService;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = applicationContext.getBean(ITestCaseService.class);
        this.testCaseExecutionService = applicationContext.getBean(ITestCaseExecutionService.class);
        IInvariantService invariantService = applicationContext.getBean(IInvariantService.class);

        //Get all input parameters from user form
        TCase tCase = this.getTestCaseFromRequest(req);
        String environment = this.getValues(req, "Environment");
        String build = this.getValues(req, "Build");
        String revision = this.getValues(req, "Revision");
        String ip = this.getValue(req, "Ip");
        String port = this.getValue(req, "Port");
        String tag = this.getValue(req, "Tag");
        String browserVersion = this.getValue(req, "BrowserFullVersion");
        String system = this.getValues(req, "System");
        String[] countries = req.getParameterValues("Country");
        String[] browsers = req.getParameterValues("Browser");

        //Get all test cases that match the user input
        List<TCase> list = testCaseService.findTestCaseByGroupInCriteria(tCase, system);

        //Create object to be filled and then added to JSON response
        JSONArray data = new JSONArray();
        Map<String, Map<String, Map<String, Integer>>> mapTests = new LinkedHashMap<String, Map<String, Map<String, Integer>>>();
        Map<String, Map<String, Integer>> mapGroups = new LinkedHashMap<String, Map<String, Integer>>();
        Map<String, Map<String, Integer>> mapStatus = new LinkedHashMap<String, Map<String, Integer>>();
        try {
            //Get invariant for know the columns
            List<Invariant> tceStatus = invariantService.findListOfInvariantById("TCESTATUS");
            List<Invariant> tcGroups = invariantService.findListOfInvariantById("GROUP");
            //removed first item because is empty
            tcGroups.remove(0);
            List<Invariant> tcStatus = invariantService.findListOfInvariantById("TCSTATUS");

            //Build data for detail table and calculate data dor statistics tables
            this.calculateStatistics(list, countries, browsers, tceStatus, mapTests, mapGroups, mapStatus, data, environment,
                    build, revision, browserVersion, ip, port, tag);

            //Build JSON response
            JSONObject json = new JSONObject();
            json.put("statistic", this.getJSONObjectFromMap(mapTests));
            json.put("groups", this.getJSONObjectFromMap(mapGroups, tcGroups));
            json.put("status", this.getJSONObjectFromMap(mapStatus, tcStatus));
            json.put("aaData", data);
            json.put("iTotalRecords", data.length());
            json.put("iTotalDisplayRecords", data.length());

            //Return JSON response
            resp.setContentType("application/json");
            resp.getWriter().print(json.toString());

        } catch (JSONException e) {
            LOG.error("Unable to build JSON response due to exception", e);
        } catch (CerberusException e) {
            LOG.error("Unable to find Invariant", e);
        }
    }

    /**
     * Map all input from form page to a TCase object
     *
     * @param req http request with all form input
     * @return TCase object with information of form
     */
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

    /**
     * Get value from request parameter.
     *
     * @param req       http request with all form input
     * @param valueName name of the parameter to return
     * @return value of parameter or null if parameter null or 'All'
     */
    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }

    /**
     * Convert list of values from request parameter to string.
     *
     * @param req       http request with all form input
     * @param valueName name of the parameter to return
     * @return string containing all values of parameter or null if parameter null
     */
    private String getValues(HttpServletRequest req, String valueName) {
        StringBuilder whereClause = new StringBuilder();
        String[] values = req.getParameterValues(valueName);

        if (values != null) {
            whereClause.append(" '").append(values[0]);
            for (int i = 1; i < values.length; i++) {
                if (!"All".equalsIgnoreCase(values[i]) && !"".equalsIgnoreCase(values[i].trim())) {
                    whereClause.append("', '").append(values[i]);
                }
            }
            whereClause.append("' ");
            return whereClause.toString();
        }
        return null;
    }

    /**
     * Convert map object to JSONObject, with all columns of list
     * <p/>
     * Convert Map object Map<Test, Map<Column, Value>>} to JSONObject
     * {'aaData', [[Test 1, Value of Column 1, ..., Value of Column N, Total 1], ..., [Test N, Value of Column 1, ..., Value of Column N, Total N]]}
     * This JSONObject will be decoded by Datatables plugin and converted to dynamic table.
     *
     * @param map     map object Map<Test, Map<Column, Value>>
     * @param invList list of Invariant corresponding to the columns of the table
     * @return JSONObject with the array to populate the table
     * @throws JSONException
     */
    private JSONObject getJSONObjectFromMap(Map<String, Map<String, Integer>> map, List<Invariant> invList) throws JSONException {
        JSONObject statistic = new JSONObject();
        JSONArray array = new JSONArray();
        for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
            JSONArray arr = new JSONArray();
            arr.put(entry.getKey());
            int total = 0;
            for (Invariant inv : invList) {
                if (entry.getValue().containsKey(inv.getValue())) {
                    arr.put(entry.getValue().get(inv.getValue()));
                    total += entry.getValue().get(inv.getValue());
                } else {
                    arr.put("");
                }
            }
            arr.put(total);
            array.put(arr);
        }
        statistic.put("aaData", array);
        return statistic;
    }

    /**
     * Convert map object to JSONObject
     *
     * @param map object Map<Test, Map<Country, Map<Column, Value>>>
     * @return JSONObject with the array to populate the table
     * @throws JSONException
     */
    private JSONObject getJSONObjectFromMap(Map<String, Map<String, Map<String, Integer>>> map) throws JSONException {
        JSONObject statistic = new JSONObject();

        JSONArray test = new JSONArray();
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entryTest : map.entrySet()) {
            JSONArray status = new JSONArray();
            status.put(entryTest.getKey());
            for (Map.Entry<String, Map<String, Integer>> entryCountry : entryTest.getValue().entrySet()) {
                int total = 0;
                for (Map.Entry<String, Integer> entryStatus : entryCountry.getValue().entrySet()) {
                    status.put(entryStatus.getValue());
                    total += entryStatus.getValue();
                }
                status.put(total);
            }
            test.put(status);
        }
        statistic.put("aaData", test);

        return statistic;
    }

    /**
     * @param map   map that keep the values to count
     * @param total map that keep the total to count
     * @param value string to increment
     */
    private void incrementMapValue(Map<String, Integer> map, Map<String, Integer> total, String value) {
        if (map.containsKey(value)) {
            map.put(value, map.get(value) + 1);
            total.put(value, total.get(value) + 1);
        } else {
            map.put(value, 1);
            if (total.containsKey(value)) {
                total.put(value, total.get(value) + 1);
            } else {
                total.put(value, 1);
            }
        }
    }

    /**
     * @param tCaseList      list of test cases to calculate statistics
     * @param countries      array of countries to search testcaseexecution
     * @param browsers       array of browsers to search testcaseexecution
     * @param tceStatus      list of control status
     * @param mapTests       map for countries and control status
     * @param mapGroups      map for test cases groups
     * @param mapStatus      map for test cases status
     * @param data           JSONObject for details table
     * @param environment    environment for test case execution filter
     * @param build          build for test case execution filter
     * @param revision       revision for test case execution filter
     * @param browserVersion browser version for test case execution filter
     * @param ip             ip of execution for test case execution filter
     * @param port           port of execution for test case execution filter
     * @param tag            tag for test case execution filter
     * @throws JSONException
     */
    private void calculateStatistics(List<TCase> tCaseList, String[] countries, String[] browsers, List<Invariant> tceStatus,
                                     Map<String, Map<String, Map<String, Integer>>> mapTests, Map<String, Map<String, Integer>> mapGroups,
                                     Map<String, Map<String, Integer>> mapStatus, JSONArray data, String environment, String build,
                                     String revision, String browserVersion, String ip, String port, String tag) throws JSONException {

        String oldTest = "";
        Map<String, Map<String, Integer>> map = new LinkedHashMap<String, Map<String, Integer>>();
        Map<String, Integer> group = new LinkedHashMap<String, Integer>();
        Map<String, Integer> stat = new LinkedHashMap<String, Integer>();
        Map<String, Map<String, Integer>> mapTotal = new LinkedHashMap<String, Map<String, Integer>>();
        Map<String, Integer> statusTotal;
        Map<String, Integer> groupTotal = new LinkedHashMap<String, Integer>();
        Map<String, Integer> statTotal = new LinkedHashMap<String, Integer>();

        //loop for all test cases
        for (TCase tc : tCaseList) {
            if (!tc.getTest().equals(oldTest)) {
                map = new LinkedHashMap<String, Map<String, Integer>>();
                group = new LinkedHashMap<String, Integer>();
                stat = new LinkedHashMap<String, Integer>();
            }
            //Create new line on table
            JSONArray array = new JSONArray();
            array.put(tc.getTest());
            array.put(tc.getTestCase());
            array.put(tc.getApplication());
            array.put(tc.getShortDescription());
            array.put(tc.getPriority());
            array.put(tc.getStatus());

            //loop for all countries to create country columns
            for (String country : countries) {
                Map<String, Integer> status;

                //get or initialize line of control status statistic table
                if (!tc.getTest().equals(oldTest)) {
                    status = new LinkedHashMap<String, Integer>();
                    for (Invariant inv : tceStatus) {
                        status.put(inv.getValue(), 0);
                    }
                } else {
                    status = mapTests.get(tc.getTest()).get(country);
                }

                //get or initialize Total line of control status statistic table
                if (mapTotal.containsKey(country)) {
                    statusTotal = mapTotal.get(country);
                } else {
                    statusTotal = new LinkedHashMap<String, Integer>();
                    for (Invariant inv : tceStatus) {
                        statusTotal.put(inv.getValue(), 0);
                    }
                }

                //loop for all browsers to create browser columns
                for (String browser : browsers) {
                    //get information of execution
                    TestCaseExecution tce = this.testCaseExecutionService.findLastTCExecutionInGroup(tc.getTest(),
                            tc.getTestCase(), environment, country, build, revision, browser, browserVersion, ip, port, tag);

                    if (tce != null) {
                        //send JSONObject to create link with jquery
                        JSONObject obj = new JSONObject();
                        obj.put("result", tce.getControlStatus());
                        obj.put("execID", tce.getId());
                        array.put(obj);

                        //add date of execution
                        Date date = new Date(tce.getStart());
                        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        array.put(formatter.format(date));

                        //add 1 to Test/Country/Browser/ControlStatus counter
                        status.put(tce.getControlStatus(), status.get(tce.getControlStatus()) + 1);
                        //add 1 to Test/Country/Browser/Total counter
                        statusTotal.put(tce.getControlStatus(), statusTotal.get(tce.getControlStatus()) + 1);
                    } else {
                        array.put("");
                        array.put("");
                    }
                }
                //add line to statistic table
                map.put(country, status);
                //store for TOTAL line
                mapTotal.put(country, statusTotal);
            }

            array.put(tc.getComment());
            array.put(tc.getBugID() + "for " + tc.getTargetSprint() + "/" + tc.getTargetRevision());
            array.put(tc.getGroup());
            //add line to detail table
            data.put(array);

            //add 1 to Total counter (groups and status)
            this.incrementMapValue(group, groupTotal, tc.getGroup());
            this.incrementMapValue(stat, statTotal, tc.getStatus());

            //store statistics lines on table
            mapTests.put(tc.getTest(), map);
            mapGroups.put(tc.getTest(), group);
            mapStatus.put(tc.getTest(), stat);

            oldTest = tc.getTest();
        }
        //add TOTAL lines to tables
        mapTests.put("TOTAL", mapTotal);
        mapGroups.put("TOTAL", groupTotal);
        mapStatus.put("TOTAL", statTotal);
    }
}
