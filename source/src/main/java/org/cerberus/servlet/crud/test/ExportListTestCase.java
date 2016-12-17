/*
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

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.factory.impl.FactoryTestCase;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.TestCaseService;
import org.cerberus.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.cerberus.crud.factory.IFactoryTestCase;

/**
 * Search for all test cases given by the filters and convert them to CSV file
 *
 * @author Tiago Bernardes
 * @version 1.0, 17/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "ExportListTestCase", urlPatterns = {"/ExportListTestCase"})
public class ExportListTestCase extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String text = "%" + this.getValue(req, "ScText") + "%";
        String system = this.getValue(req, "ScSystem");
        TestCase tCase = this.getTestCaseFromRequest(req);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = appContext.getBean(TestCaseService.class);

        List<TestCase> list = testCaseService.findTestCaseByAllCriteria(tCase, text, system);

        StringBuilder sb = new StringBuilder();
        sb.append("Test,TestCase,Origin,RefOrigin,Creator,Implementer,LastModifier,Project,Ticket,Function,Application,RunQA,RunUAT,RunPROD,Priority,Group,Status,");
        sb.append("ShortDescription,Description,HowTo,Active,FromSprint,FromRevision,ToSprint,ToRevision,LastExecution,BugID,TargetSprint,TargetRevision,Comment\n");

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
        String origine = this.getValue(req, "ScOrigine");
        String creator = this.getValue(req, "ScCreator");
        String application = this.getValue(req, "ScApplication");
        int priority = -1;
        if (req.getParameter("ScPriority") != null && !req.getParameter("ScPriority").equalsIgnoreCase("All") && StringUtil.isNumeric(req.getParameter("ScPriority"))) {
            priority = Integer.parseInt(req.getParameter("ScPriority"));
        }
        String status = this.getValue(req, "ScStatus");
        String group = this.getValue(req, "ScGroup");
        String prod = this.getValue(req, "ScPROD");
        String qa = this.getValue(req, "ScQA");
        String uat = this.getValue(req, "ScUAT");
        String active = this.getValue(req, "ScActive");
        String conditionOper = this.getValue(req, "ScConditionOper");
        String conditionVal1 = this.getValue(req, "ScConditionVal1");
        String conditionVal2 = this.getValue(req, "ScConditionVal2");
        String fBuild = this.getValue(req, "ScFBuild");
        String fRev = this.getValue(req, "ScFRev");
        String tBuild = this.getValue(req, "ScTBuild");
        String tRev = this.getValue(req, "ScTRev");
        String targetBuild = this.getValue(req, "ScTargetBuild");
        String targetRev = this.getValue(req, "ScTargetRev");
        String function = this.getValue(req, "function");

        IFactoryTestCase factoryTCase = new FactoryTestCase();
        return factoryTCase.create(test, testCase, origine, null, creator, null, null, project, ticket,function, application, qa, uat, prod, priority, group,
                status, null, null, null, active, conditionOper, conditionVal1, conditionVal2, fBuild, fRev, tBuild, tRev, null, bug, targetBuild, targetRev, null, "", null, null, null, null);
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
        sb.append(StringUtil.getCleanCSVTextField(tc.getProject()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTicket()));
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
        sb.append(StringUtil.getCleanCSVTextField(tc.getGroup()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getDescription()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getBehaviorOrValueExpected()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getHowTo()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTcActive()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromBuild()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromRev()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToBuild()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToRev()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getLastExecutionStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getBugID()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetBuild()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetRev()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getComment()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFunction()));
        sb.append("\"\n");

        return sb.toString();
    }
}
