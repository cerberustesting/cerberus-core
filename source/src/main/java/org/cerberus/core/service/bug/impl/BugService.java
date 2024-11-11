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
package org.cerberus.core.service.bug.impl;

import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.ExecutionLog;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.bug.IBugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.cerberus.core.service.bug.github.IGithubService;
import org.cerberus.core.service.bug.jira.IJiraService;

/**
 *
 * @author vertigo17
 */
@Service
public class BugService implements IBugService {

    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private IGithubService githubService;
    @Autowired
    private IJiraService jiraService;

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(BugService.class);

    @Override
    @Async
    public void createIssue(TestCaseExecution execution) {

        if (!parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_autobugcreation_enable, execution.getSystem(), false)) {
            LOG.debug("Not creating issue due to parameter.");
            return;
        }
        LOG.debug("Trying to create issue.");
        execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Trying To create the issue.");
        // Testcase should have a priority defined and in WORKING status
        if ((execution.getTestCasePriority() >= 1) && !"OK".equalsIgnoreCase(execution.getControlStatus())) {
            LOG.debug("Execution is not OK, with prio > 0.");
            execution.addExecutionLog(ExecutionLog.STATUS_INFO, "Execution is not OK, with prio > 0 ");
            TestCase tc = null;
            try {
                tc = testCaseService.findTestCaseByKey(execution.getTest(), execution.getTestCase());

                // There should not be any already existing bug.
                if (!testCaseService.isBugAlreadyOpen(tc)) {

                    // All is fine to open a new bug
                    Application currentAppli = new Application();
                    try {
                        currentAppli = applicationService.convert(applicationService.readByKey(execution.getApplication()));
                    } catch (CerberusException ex) {
                        LOG.warn(ex, ex);
                    }

                    if (currentAppli != null) {
                        switch (currentAppli.getBugTrackerConnector()) {
                            case Application.BUGTRACKER_JIRA:
                                jiraService.createJiraIssue(tc, execution, currentAppli.getBugTrackerParam1(), currentAppli.getBugTrackerParam2());

                                break;
                            case Application.BUGTRACKER_GITHUB:
                                githubService.createGithubIssue(tc, execution, currentAppli.getBugTrackerParam1(), currentAppli.getBugTrackerParam2());

                                break;
                            default:
                                throw new AssertionError();
                        }
                    }
                } else {
                    LOG.debug("Not opening Issue because issue is already open");
                }
            } catch (CerberusException ex) {
                LOG.warn(ex, ex);
            }
        }
    }

}
