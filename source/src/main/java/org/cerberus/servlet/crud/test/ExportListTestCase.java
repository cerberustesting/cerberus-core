/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.factory.impl.FactoryTestCase;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Search for all test cases given by the filters and convert them to CSV file
 *
 * @author Tiago Bernardes
 * @version 1.0, 17/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "ExportListTestCase", urlPatterns = {"/ExportListTestCase"})
public class ExportListTestCase extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ExportListTestCase.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String text = "%" + this.getValue(req, "ScText") + "%";
        String system = this.getValue(req, "ScSystem");
        TestCase tCase = this.getTestCaseFromRequest(req);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);

        List<TestCase> list = testCaseService.findTestCaseByAllCriteria(tCase, text, system);

        StringBuilder sb = new StringBuilder();
        sb.append("Test,TestCase,Origin,RefOrigin,Creator,Implementer,LastModifier,Project,Ticket,Function,Application,RunQA,RunUAT,RunPROD,Priority,Group,Status,");
        sb.append("ShortDescription,Description,HowTo,Active,fromMajor,FromMinor,ToMajor,ToMinor,LastExecution,BugID,TargetMajor,TargetMinor,Comment\n");

        for (TestCase tc : list) {
            sb.append(this.convertTCasetoStringCSV(tc));
        }

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=List_Test_Cases.csv");
        resp.setContentLength(sb.length());
        resp.getOutputStream().print(sb.toString());
    }

    private TestCase getTestCaseFromRequest(HttpServletRequest req) {
        String test = this.getValue(req, "ScTest");
        String testCase = "%" + this.getValue(req, "ScTestCase") + "%";
        String project = this.getValue(req, "ScProject");
        String ticket = this.getValue(req, "ScTicket");
        String bug = this.getValue(req, "ScBugID");
        JSONArray bugJSON = new JSONArray();
        try {
            bugJSON = new JSONArray(bug);
        } catch (Exception e) {
            LOG.error("Could not convert '" + bug + "' to JSONArray.", e);
        }
        String origine = this.getValue(req, "ScOrigine");
        String creator = this.getValue(req, "ScCreator");
        String executor = this.getValue(req, "ScExecutor");
        String application = this.getValue(req, "ScApplication");
        int priority = -1;
        if (req.getParameter("ScPriority") != null && !req.getParameter("ScPriority").equalsIgnoreCase("All") && StringUtil.isInteger(req.getParameter("ScPriority"))) {
            priority = Integer.parseInt(req.getParameter("ScPriority"));
        }
        String status = this.getValue(req, "ScStatus");
        String group = this.getValue(req, "ScGroup");
        String prod = this.getValue(req, "ScPROD");
        String qa = this.getValue(req, "ScQA");
        String uat = this.getValue(req, "ScUAT");
        String active = this.getValue(req, "ScActive");
        String conditionOperator = this.getValue(req, "ScConditionOperator");
        String conditionVal1 = this.getValue(req, "ScConditionVal1");
        String conditionVal2 = this.getValue(req, "ScConditionVal2");
        String conditionVal3 = this.getValue(req, "ScConditionVal3");
        String fBuild = this.getValue(req, "ScFBuild");
        String fRev = this.getValue(req, "ScFRev");
        String tBuild = this.getValue(req, "ScTBuild");
        String tRev = this.getValue(req, "ScTRev");
        String targetMajor = this.getValue(req, "ScTargetMajor");
        String targetMinor = this.getValue(req, "ScTargetMinor");
        String function = this.getValue(req, "function");

        IFactoryTestCase factoryTCase = new FactoryTestCase();
        return factoryTCase.create(test, testCase, origine, null, creator, executor, null, null, function, application, qa, uat, prod, priority, group,
                status, null, null, null, active, conditionOperator, conditionVal1, conditionVal2, conditionVal3, fBuild, fRev, tBuild, tRev, null, bugJSON, targetMajor, targetMinor, null, "", "", null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }

    private String convertTCasetoStringCSV(TestCase tc) {
        StringBuilder sb = new StringBuilder();

        sb.append("\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTest()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTestCase()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getOrigine()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getRefOrigine()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getUsrCreated()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getImplementer()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getUsrModif()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getExecutor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getApplication()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getActiveQA()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getActiveUAT()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getActivePROD()));
        sb.append("\",\"");
        sb.append(tc.getPriority());
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getType()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getDescription()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getDetailedDescription()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getHowTo()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTcActive()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromMajor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromMinor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToMajor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToMinor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getLastExecutionStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getBugID().toString()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetMajor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetMinor()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getComment()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFunction()));
        sb.append("\"\n");

        return sb.toString();
    }
}
