/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.servlet.manualTestCase;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.Application;
import org.cerberus.entity.CountryEnvParam;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.entity.TestCaseExecutionSysVer;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryTestCaseExecution;
import org.cerberus.factory.IFactoryTestCaseExecutionSysVer;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ICountryEnvParamService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseExecutionSysVerService;
import org.cerberus.service.ITestCaseService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.version.Infos;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "SaveManualExecution", urlPatterns = {"/SaveManualExecution"})
public class SaveManualExecution extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String test = req.getParameter("test");
        String testCase = req.getParameter("testCase");
        String env = req.getParameter("env");
        String country = req.getParameter("country");
        String controlStatus = req.getParameter("controlStatus");
        String controlMessage = req.getParameter("controlMessage");
        String tag = req.getParameter("tag");
        String browser = req.getParameter("browser");
        String browserVersion = req.getParameter("browserVersion");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);

        IFactoryTestCaseExecution factoryTCExecution = appContext.getBean(IFactoryTestCaseExecution.class);
        IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer = appContext.getBean(IFactoryTestCaseExecutionSysVer.class);

        try {
            Application application = null;
            TCase tCase = testService.findTestCaseByKey(test, testCase);
            if (tCase != null) {
                application = applicationService.findApplicationByKey(tCase.getApplication());
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
            }
            CountryEnvParam countryEnvParam;
            try {
                countryEnvParam = countryEnvParamService.findCountryEnvParamByKey(application.getSystem(), country, env);
            } catch (CerberusException e) {
                CerberusException ex = new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                ex.getMessageError().setDescription("Combination Environment: '" + env + "' and Country: '" + country
                        + "' not defined for System/Application: " + application.getSystem() + "/" + application.getApplication());
                throw ex;
            }
            String build = countryEnvParam.getBuild();
            String revision = countryEnvParam.getRevision();
            long now = new Date().getTime();
            String version = Infos.getInstance().getProjectNameAndVersion();

            String myUser = "";
            if (!(req.getUserPrincipal() == null)) {
                myUser = ParameterParserUtil.parseStringParam(req.getUserPrincipal().getName(), "");
            }

            if (myUser == null || myUser.length() <= 0) {
                myUser = "Manual";
            }

            TestCaseExecution execution = factoryTCExecution.create(0, test, testCase, build, revision, env, country, browser, "", "", browserVersion, now, now,
                    controlStatus, controlMessage, application, "", "", "", tag, "Y", 0, 0, 0, 0, true, "", "", tCase.getStatus(), version,
                    null, null, null, false, "", "", "", "", "", "", null, null, myUser);

            execution.setId(testCaseExecutionService.insertTCExecution(execution));

            TestCaseExecutionSysVer testCaseExecutionSysVer = factoryTestCaseExecutionSysVer.create(execution.getId(), application.getSystem(), build, revision);
            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);

            resp.getWriter().print(execution.getId());

        } catch (CerberusException e) {
            MyLogger.log(SaveManualExecution.class.getName(), Level.FATAL, "" + e.getMessageError().getDescription());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessageError().getDescription());
        }
    }
}
