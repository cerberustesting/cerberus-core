package com.redcats.tst.servlet.testCase;

import com.redcats.tst.dao.ITestCaseCountryDAO;
import com.redcats.tst.dao.ITestCaseCountryPropertiesDAO;
import com.redcats.tst.dao.impl.TestCaseCountryDAO;
import com.redcats.tst.dao.impl.TestCaseCountryPropertiesDAO;
import com.redcats.tst.entity.*;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ITestCaseCountryService;
import com.redcats.tst.service.ITestCaseService;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 * @version 1.0, 07/02/2013
 * @since 2.0.0
 */
@WebServlet(name = "GetTestCase", urlPatterns = {"/GetTestCase"})
public class GetTestCase extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testService = appContext.getBean(ITestCaseService.class);
            
            //TODO pass DAO to Service
            ITestCaseCountryPropertiesDAO testCaseService = appContext.getBean(TestCaseCountryPropertiesDAO.class);
            //TODO pass DAO to Service
            ITestCaseCountryDAO testCaseCountryDAO = appContext.getBean(TestCaseCountryDAO.class);
            ITestCaseCountryService tccService = appContext.getBean(ITestCaseCountryService.class);
            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            String test = policy.sanitize(httpServletRequest.getParameter("test"));
            String testcase = policy.sanitize(httpServletRequest.getParameter("testcase"));
            JSONObject jsonObject = new JSONObject();

            TCase tcInfo = testService.findTestCaseByKey(test, testcase);
            List<TestCaseCountryProperties> properties = testCaseService.findDistinctPropertiesOfTestCase(test, testcase);
    //        List<Step> steps = testService.loadAllStepSequences(test, testcase);
    //
    //        try {
    //
    //            jsonObject.put("origin", tcInfo.getOrigin());
    //            jsonObject.put("refOrigin", tcInfo.getRefOrigin());
    //            jsonObject.put("creator", tcInfo.getCreator());
    //            jsonObject.put("implementer", tcInfo.getImplementer());
    //            jsonObject.put("lastModifier", tcInfo.getLastModifier());
    //            jsonObject.put("project", tcInfo.getProject());
    //            jsonObject.put("ticket", tcInfo.getTicket());
    //            jsonObject.put("application", tcInfo.getApplication());
    //            jsonObject.put("runQA", tcInfo.isRunQA());
    //            jsonObject.put("runUAT", tcInfo.isRunUAT());
    //            jsonObject.put("runPROD", tcInfo.isRunPROD());
    //            jsonObject.put("priority", tcInfo.getPriority());
    //            jsonObject.put("group", tcInfo.getGroup());
    //            jsonObject.put("status", tcInfo.getStatus());
    //            jsonObject.put("countriesList", tcInfo.getCountryList());
    //            jsonObject.put("shortDescription", tcInfo.getShortDescription());
    //            jsonObject.put("description", tcInfo.getDescription());
    //            jsonObject.put("howTo", tcInfo.getHowTo());
    //            jsonObject.put("active", tcInfo.isActive());
    //            jsonObject.put("fromSprint", tcInfo.getFromSprint());
    //            jsonObject.put("fromRevision", tcInfo.getFromRevision());
    //            jsonObject.put("toSprint", tcInfo.getToSprint());
    //            jsonObject.put("toRevision", tcInfo.getToRevision());
    //            jsonObject.put("lastExecutionStatus", tcInfo.getLastExecutionStatus());
    //            jsonObject.put("bugID", tcInfo.getBugID());
    //            jsonObject.put("targetSprint", tcInfo.getTargetSprint());
    //            jsonObject.put("targetRevision", tcInfo.getTargetRevision());
    //            jsonObject.put("comment", tcInfo.getComment());
    //            jsonObject.put("test", tcInfo.getTest());
    //            jsonObject.put("testcase", tcInfo.getTestCase());
    //
    //            JSONArray propertyList = new JSONArray();
    //            List<String> countriesExisting = tccService.findListOfCountryByTestTestCase(test, testcase);
    //
    //            for (TestCaseCountryProperties prop : properties) {
    //                JSONObject property = new JSONObject();
    //
    //                property.put("property", prop.getProperty());
    //                property.put("type", prop.getType());
    //                property.put("database", prop.getDatabase());
    //                property.put("value", prop.getValue());
    //                property.put("length", prop.getLength());
    //                property.put("rowLimit", prop.getRowLimit());
    //                property.put("nature", prop.getNature());
    //                List<String> countriesSelected = testCaseService.findCountryByProperty(prop);
    //                for (String country : countriesExisting) {
    //                    if (countriesSelected.contains(country)) {
    //                        property.put(country, true);
    //                    } else {
    //                        property.put(country, false);
    //                    }
    //                }
    //                propertyList.put(property);
    //            }
    //            jsonObject.put("properties", propertyList);
    //
    //            JSONArray list = new JSONArray();
    //            for (Step step : steps) {
    //                JSONObject stepObject = new JSONObject();
    //                stepObject.put("number", step.getNumber());
    //                stepObject.put("name", step.getName());
    //                int i = 1;
    //                JSONArray actionList = new JSONArray();
    //                JSONArray controlList = new JSONArray();
    //                JSONArray sequenceList = new JSONArray();
    //
    //                for (Sequence seq : step.getSequences()) {
    //                    if (seq instanceof Action) {
    //                        Action action = (Action) seq;
    //                        JSONObject actionObject = new JSONObject();
    //                        actionObject.put("sequence", i);
    //                        actionObject.put("action", action.getAction());
    //                        actionObject.put("object", action.getObject());
    //                        actionObject.put("property", action.getProperty());
    //                        actionObject.put("fatal", "");
    //                        actionList.put(actionObject);
    //                        sequenceList.put(actionObject);
    //                    } else if (seq instanceof Control) {
    //                        Control control = (Control) seq;
    //                        JSONObject controlObject = new JSONObject();
    //                        controlObject.put("step", control.getStep());
    //                        controlObject.put("sequence", control.getSequence());
    //                        controlObject.put("order", control.getOrder());
    //                        controlObject.put("action", control.getAction());
    //                        controlObject.put("object", control.getObject());
    //                        controlObject.put("property", control.getProperty());
    //                        controlObject.put("fatal", control.isFatal());
    //                        controlList.put(controlObject);
    //                        //test
    //                        controlObject = new JSONObject();
    //                        controlObject.put("sequence", i);
    //                        controlObject.put("action", control.getAction());
    //                        controlObject.put("object", control.getObject());
    //                        controlObject.put("property", control.getProperty());
    //                        controlObject.put("fatal", control.isFatal());
    //                        sequenceList.put(controlObject);
    //                    }
    //                    i++;
    //                }
    //                stepObject.put("actions", actionList);
    //                stepObject.put("controls", controlList);
    //                stepObject.put("sequences", sequenceList);
    //                list.put(stepObject);
    //            }
    ////            jsonObject.put("actions", actionList);
    ////            jsonObject.put("controls", controlList);
    //            jsonObject.put("list", list);
    //
    //            httpServletResponse.setContentType("application/json");
    //            httpServletResponse.getWriter().print(jsonObject.toString());
    //        } catch (JSONException exception) {
    //            MyLogger.log(GetTestCase.class.getName(), Level.WARN, exception.toString());
    //        }
        } catch (CerberusException ex) {
            Logger.getLogger(GetTestCase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}