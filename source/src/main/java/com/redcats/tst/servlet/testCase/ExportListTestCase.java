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

package com.redcats.tst.servlet.testCase;

import com.redcats.tst.entity.TCase;
import com.redcats.tst.factory.IFactoryTCase;
import com.redcats.tst.factory.impl.FactoryTCase;
import com.redcats.tst.service.ITestCaseService;
import com.redcats.tst.service.impl.TestCaseService;
import com.redcats.tst.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
        TCase tCase = this.getTestCaseFromRequest(req);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = appContext.getBean(TestCaseService.class);

        List<TCase> list = testCaseService.findTestCaseByAllCriteria(tCase, text, system);

        StringBuilder sb = new StringBuilder();
        sb.append("Test,TestCase,Origin,RefOrigin,Creator,Implementer,LastModifier,Project,Ticket,Application,RunQA,RunUAT,RunPROD,Priority,Group,Status,");
        sb.append("ShortDescription,Description,HowTo,Active,FromSprint,FromRevision,ToSprint,ToRevision,LastExecution,BugID,TargetSprint,TargetRevision,Comment\n");

        for (TCase tc : list) {
            sb.append(this.convertTCasetoStringCSV(tc));
        }

        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=List_Test_Cases.csv");
        resp.setContentLength(sb.length());
        resp.getOutputStream().print(sb.toString());
    }

    private TCase getTestCaseFromRequest(HttpServletRequest req) {
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
        String fBuild = this.getValue(req, "ScFBuild");
        String fRev = this.getValue(req, "ScFRev");
        String tBuild = this.getValue(req, "ScTBuild");
        String tRev = this.getValue(req, "ScTRev");
        String targetBuild = this.getValue(req, "ScTargetBuild");
        String targetRev = this.getValue(req, "ScTargetRev");

        IFactoryTCase factoryTCase = new FactoryTCase();
        return factoryTCase.create(test, testCase, origine, null, creator, null, null, project, ticket, application, qa, uat, prod, priority, group,
                status, null, null, null, active, fBuild, fRev, tBuild, tRev, null, bug, targetBuild, targetRev, null, null, null, null, null);
    }

    private String getValue(HttpServletRequest req, String valueName) {
        String value = null;
        if (req.getParameter(valueName) != null && !req.getParameter(valueName).equalsIgnoreCase("All")) {
            value = req.getParameter(valueName);
        }
        return value;
    }

    private String convertTCasetoStringCSV(TCase tc) {
        StringBuilder sb = new StringBuilder();

        sb.append("\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTest()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTestCase()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getOrigin()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getRefOrigin()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getCreator()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getImplementer()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getLastModifier()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getProject()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTicket()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getApplication()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getRunQA()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getRunUAT()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getRunPROD()));
        sb.append("\",\"");
        sb.append(tc.getPriority());
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getGroup()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getShortDescription()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getDescription()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getHowTo()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getActive()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromSprint()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getFromRevision()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToSprint()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getToRevision()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getLastExecutionStatus()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getBugID()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetSprint()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getTargetRevision()));
        sb.append("\",\"");
        sb.append(StringUtil.getCleanCSVTextField(tc.getComment()));
        sb.append("\"\n");

        return sb.toString();
    }
}
