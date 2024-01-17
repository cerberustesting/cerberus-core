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

import org.cerberus.core.util.datatable.DataTableInformation;
import com.google.gson.Gson;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.factory.IFactoryTest;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/testdatalib")
public class TestDataLibController {

    private static final Logger LOG = LogManager.getLogger(TestDataLibController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    TestCaseExecutionService testCaseExecutionService;
    @Autowired
    ITestService testService;
    @Autowired
    IFactoryTest factoryTest;
    @Autowired
    ILogEventService logEventService;
    @Autowired
    IFactoryLogEvent factoryLogEvent;
    @Autowired
    IParameterService parameterService;

    /**
     * Read By Key
     *
     * @param testdatalibid
     * @param request
     * @return
     */
    @GetMapping(path = "/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public String readCsv(
            //            @PathVariable("testdatalibid") String testdatalibid,
            HttpServletRequest request) {

        JSONObject object = new JSONObject();
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        try {
            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

//            testdatalibid = policy.sanitize(testdatalibid);
//            AnswerItem<Test> answerTest = testService.readByKey(test);
            object.put("hasPermissions", userHasPermissions);

        } catch (JSONException ex) {
            LOG.warn(ex, ex);
        }
        return "toto";

    }

}
