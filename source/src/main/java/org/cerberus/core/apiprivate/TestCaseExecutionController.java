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
package org.cerberus.core.apiprivate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONArray;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/testcaseexecution")
public class TestCaseExecutionController {

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    TestCaseExecutionService testCaseExecutionService;

    @GetMapping("/getLastByCriteria")
    public String getLastByCriteria(
            @RequestParam(name = "test", value = "test") String test,
            @RequestParam(name = "testCase", value = "testCase") String testCase,
            @RequestParam(name = "numberOfExecution", value = "Number of execution expected. If empty, all execution matching the criteria will be returned", required = false) Integer numberOfExecution,
            @RequestParam(name = "tag", value = "Tag of the execution expected", required = false) String tag,
            @RequestParam(name = "campaign", value = "Campaign name of the execution expected", required = false) String campaign) {

        try {
            test = policy.sanitize(test);
            testCase = policy.sanitize(testCase);
            tag = policy.sanitize(tag);
            JSONArray ja = testCaseExecutionService.getLastByCriteria(test, testCase, tag, numberOfExecution);
            return ja.toString();
        } catch (CerberusException ex) {
            LOG.warn(ex);
            return "error";
        }
    }

}
