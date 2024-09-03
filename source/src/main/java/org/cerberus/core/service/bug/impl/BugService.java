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
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseService;
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
            return;
        }

        // Testcase should have a priority defined and in WORKING status
        if ((execution.getTestCasePriority() >= 1) && ("WORKING".equals(execution.getStatus()))) {
            // There should not be any already existing bug.
            if (!testCaseService.isBugAlreadyOpen(execution.getTestCaseObj())) {

                // All is fine to open a new bug
                Application currentAppli = new Application();
                try {
                    currentAppli = applicationService.convert(applicationService.readByKey(execution.getApplication()));
                } catch (CerberusException ex) {
                    LOG.warn(ex, ex);
                }

                if (currentAppli != null && Application.BUGTRACKER_JIRA.equalsIgnoreCase(currentAppli.getBugTrackerConnector())) {
                    jiraService.createJiraIssue(execution, currentAppli.getBugTrackerParam1(), currentAppli.getBugTrackerParam2());
                }
                if (currentAppli != null && Application.BUGTRACKER_GITHUB.equalsIgnoreCase(currentAppli.getBugTrackerConnector())) {
                    githubService.createGithubIssue(execution, currentAppli.getBugTrackerParam1(), currentAppli.getBugTrackerParam2());
                }
            }
        }
    }

}
