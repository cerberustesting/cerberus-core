package com.redcats.tst.servlet.testCase;

import com.redcats.tst.entity.TestCase;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.refactor.ITestCaseBusiness;
import com.redcats.tst.refactor.StatusMessage;
import com.redcats.tst.refactor.TestCaseBusiness;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-17
 */
public class UpdateTestCase extends HttpServlet {

    /**
     * Process the post request from UpdateTestCase form in TestCase
     * page.
     * <p/>
     * Use {@link #updateTestCase(TestCase tc, int type)} to update the TestCase
     * information.
     *
     * @param request  information from the request page
     * @param response information from the response page
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TestCase tc = getInfo(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseBusiness testcaseBusiness = appContext.getBean(TestCaseBusiness.class);

        String str = testcaseBusiness.updateTestCase(tc, ITestCaseBusiness.UPDATE_INFORMATION);
        if (str.equals(StatusMessage.SUCCESS_TESTCASEUPDATE)) {

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateTestCase", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", "", ""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

            response.sendRedirect(response.encodeRedirectURL("TestCase.jsp?Tinf=Y&Load=Load&Test=" + tc.getTest() + "&TestCase=" + tc.getTestCase()));
        } else {
            response.getWriter().print(str);
        }
    }

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see com.redcats.tst.entity.TestCase
     */
    private TestCase getInfo(HttpServletRequest request) {
        TestCase tc = new TestCase();
        tc.setTest(request.getParameter("Test"));
        tc.setTestCase(request.getParameter("TestCase"));
        tc.setImplementer(request.getParameter("editImplementer"));
        tc.setLastModifier(request.getUserPrincipal().getName());
        tc.setProject(request.getParameter("editProject"));
        tc.setTicket(request.getParameter("editTicket"));
        tc.setApplication(request.getParameter("editApplication"));
        tc.setRunQA(request.getParameter("editRunQA"));
        tc.setRunUAT(request.getParameter("editRunUAT"));
        tc.setRunPROD(request.getParameter("editRunPROD"));
        tc.setPriority(Integer.parseInt(request.getParameter("editPriority")));
        tc.setGroup(request.getParameter("editGroup"));
        tc.setStatus(request.getParameter("editStatus"));
        List<String> countries = new ArrayList<String>();
        if (request.getParameterValues("testcase_country_general") != null) {
            Collections.addAll(countries, request.getParameterValues("testcase_country_general"));
        }
        tc.setCountryList(countries);
        tc.setShortDescription(request.getParameter("editDescription"));
        tc.setDescription(request.getParameter("BehaviorOrValueExpected"));
        tc.setHowTo(request.getParameter("HowTo"));
        tc.setActive(request.getParameter("editTcActive"));
        tc.setFromSprint(request.getParameter("editFromBuild"));
        tc.setFromRevision(request.getParameter("editFromRev"));
        tc.setToSprint(request.getParameter("editToBuild"));
        tc.setToRevision(request.getParameter("editToRev"));
        tc.setBugID(request.getParameter("editBugID"));
        tc.setTargetSprint(request.getParameter("editTargetBuild"));
        tc.setTargetRevision(request.getParameter("editTargetRev"));
        tc.setComment(request.getParameter("editComment"));
        return tc;
    }
}
