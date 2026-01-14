/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.ai.LogAIUsageMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.ai.LogAIUsageStatsDTOV001;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.stats.UserPromptStats;
import org.cerberus.core.crud.service.impl.RobotService;
import org.cerberus.core.crud.service.impl.UserPromptService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.datatable.DataTableInformation;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author bcivel
 */
@RestController
@RequestMapping("/robots")
public class RobotPrivateController {

    private static final Logger LOG = LogManager.getLogger(RobotPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    RobotService robotService;

    @Operation(hidden=true)
    @PostMapping("/read")
    public String read(HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        boolean userHasPermissions = request.isUserInRole("Integrator");

        JSONObject object = new JSONObject();
        try {

            AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
            AnswerList<Robot> robotList = new AnswerList<>();

            DataTableInformation dti = new DataTableInformation(request, "robot,type,platform,browser,version,isActive,userAgent,screenSize,robotDecli,lbexemethod,description");
            robotList = robotService.readByCriteria(true, true, dti.getStartPosition(), dti.getLength(), dti.getColumnName(), dti.getSort(), dti.getSearchParameter(), dti.getIndividualSearch());

            JSONArray jsonArray = new JSONArray();
            if (robotList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (Robot robot : robotList.getDataList()) {
                    Gson gson = new Gson();
                    jsonArray.put(new JSONObject(gson.toJson(robot)).put("hasPermissions", userHasPermissions));
                }
            }

            object.put("contentTable", jsonArray);
            object.put("hasPermissions", userHasPermissions);
            object.put("iTotalRecords", robotList.getTotalRows());
            object.put("iTotalDisplayRecords", robotList.getTotalRows());
            object.put("messageType", "OK");

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }


    /**
     * Read Distinct Value Of Column
     *
     * @param request
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("readDistinctValueOfColumn")
    public String readDistinctValueOfColumn(HttpServletRequest request,
            @RequestParam("columnName") String columnName) {

        boolean userHasPermissions = request.isUserInRole("Integrator");

        JSONObject object = new JSONObject();
        try {
            DataTableInformation dti = new DataTableInformation(request, "robot,type,platform,browser,version,isActive,userAgent,screenSize,robotDecli,lbexemethod,description");
            AnswerList robotList = robotService.readDistinctValuesByCriteria(dti.getSearchParameter(), dti.getIndividualSearch(), columnName);
            object.put("distinctValues", robotList.getDataList());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }


}
