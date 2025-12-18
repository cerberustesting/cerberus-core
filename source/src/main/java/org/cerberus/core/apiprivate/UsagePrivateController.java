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
import org.cerberus.core.crud.entity.UserPrompt;
import org.cerberus.core.crud.entity.stats.UserPromptStats;
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
@RequestMapping("/usage")
public class UsagePrivateController {

    private static final Logger LOG = LogManager.getLogger(UsagePrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @Autowired
    UserPromptService userPromptService;

    @Operation(hidden=true)
    @PostMapping("/aiCallList")
    public String getAllAIUsage(HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        boolean userHasPermissions = request.isUserInRole("Administrator");

        JSONObject object = new JSONObject();
        try {

            AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
            AnswerList<UserPrompt> userPromptList = new AnswerList<>();

            DataTableInformation dti = new DataTableInformation(request, "id,login,sessionId,role,message,tokens,cost,usrCreated,dateCreated");

            userPromptList = userPromptService.readByCriteria(dti.getStartPosition(), dti.getLength(), dti.getColumnName(), dti.getSort(), dti.getSearchParameter(), dti.getIndividualSearch());

            JSONArray jsonArray = new JSONArray();
            if (userPromptList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                for (UserPrompt userPrompt : userPromptList.getDataList()) {
                    Gson gson = new Gson();
                    jsonArray.put(new JSONObject(gson.toJson(userPrompt)).put("hasPermissions", userHasPermissions));
                }
            }

            object.put("contentTable", jsonArray);
            object.put("hasPermissions", userHasPermissions);
            object.put("iTotalRecords", userPromptList.getTotalRows());
            object.put("iTotalDisplayRecords", userPromptList.getTotalRows());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }


    /**
     * Endpoint pour récupérer les statistiques d'usage AI sur une période
     *@return DTO avec totalInputTokens, totalOutputTokens, totalCost
     */
    @Operation(hidden=true)
    @GetMapping("/monthlyStats")
    public LogAIUsageMonthlyStatsDTOV001 getMonthlyStats(@RequestParam("user") String user) {

        LocalDate today = LocalDate.now();

        // Last 30 days
        LocalDate thisStartDate = today.minusDays(30);
        LocalDate thisEndDate   = today;

        // Previous 30 days
        LocalDate prevStartDate = today.minusDays(60);
        LocalDate prevEndDate   = today.minusDays(30);

        AnswerItem<UserPromptStats> currentAi = userPromptService.getUserPromptStats(
                thisStartDate.toString(), thisEndDate.toString(), null);

        AnswerItem<UserPromptStats> previousAi = userPromptService.getUserPromptStats(
                prevStartDate.toString(), prevEndDate.toString(), null);

        AnswerItem<UserPromptStats> currentUserAi = userPromptService.getUserPromptStats(
                thisStartDate.toString(), thisEndDate.toString(), user);

        AnswerItem<UserPromptStats> previousUserAi = userPromptService.getUserPromptStats(
                prevStartDate.toString(), prevEndDate.toString(), user);

        UserPromptStats current = currentAi.getItem();
        UserPromptStats previous = previousAi.getItem();
        UserPromptStats currentUser = currentUserAi.getItem();
        UserPromptStats previousUser = previousUserAi.getItem();

        // Conversion UserPromptStats → DTO
        LogAIUsageStatsDTOV001 currentDto = LogAIUsageStatsDTOV001.builder()
                .totalInputTokens(current.getTotalInputTokens())
                .totalOutputTokens(current.getTotalOutputTokens())
                .totalCost(current.getTotalCost())
                .totalUsers(current.getTotalUsers())
                .totalSessions(current.getTotalSessions())
                .startDate(thisStartDate.toString())
                .endDate(thisEndDate.toString())
                .build();

        LogAIUsageStatsDTOV001 previousDto = LogAIUsageStatsDTOV001.builder()
                .totalInputTokens(previous.getTotalInputTokens())
                .totalOutputTokens(previous.getTotalOutputTokens())
                .totalCost(previous.getTotalCost())
                .totalUsers(previous.getTotalUsers())
                .totalSessions(previous.getTotalSessions())
                .startDate(prevStartDate.toString())
                .endDate(prevEndDate.toString())
                .build();

        LogAIUsageStatsDTOV001 currentUserDto = LogAIUsageStatsDTOV001.builder()
                .totalInputTokens(currentUser.getTotalInputTokens())
                .totalOutputTokens(currentUser.getTotalOutputTokens())
                .totalCost(currentUser.getTotalCost())
                .totalUsers(currentUser.getTotalUsers())
                .totalSessions(currentUser.getTotalSessions())
                .startDate(thisStartDate.toString())
                .endDate(thisEndDate.toString())
                .build();

        LogAIUsageStatsDTOV001 previousUserDto = LogAIUsageStatsDTOV001.builder()
                .totalInputTokens(previousUser.getTotalInputTokens())
                .totalOutputTokens(previousUser.getTotalOutputTokens())
                .totalCost(previousUser.getTotalCost())
                .totalUsers(previousUser.getTotalUsers())
                .totalSessions(previousUser.getTotalSessions())
                .startDate(prevStartDate.toString())
                .endDate(prevEndDate.toString())
                .build();

        // Wrapper final
        return LogAIUsageMonthlyStatsDTOV001.builder()
                .currentPeriod(currentDto)
                .previousPeriod(previousDto)
                .userCurrentPeriod(currentUserDto)
                .userPreviousPeriod(previousUserDto)
                .build();
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

        JSONObject object = new JSONObject();
        try {
            AnswerList testCaseList = userPromptService.readDistinctValuesByCriteria(null, null, columnName);
            object.put("distinctValues", testCaseList.getDataList());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();
    }

    @Operation(hidden=true)
    @GetMapping("/usageByDay")
    public ResponseEntity<List<UserPromptStats>> getUsageByDay(
            @RequestParam(required = false, defaultValue = "ALL") String user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = endDate != null ? endDate : LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            Timestamp startTs = Timestamp.valueOf(start);
            Timestamp endTs = Timestamp.valueOf(end);

            List<UserPromptStats> statsList = userPromptService.getUsageByDay(startTs, endTs, user).getDataList();
            return ResponseEntity.ok(statsList);
        } catch (Exception e) {
            LOG.error("Error fetching usage summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint pour récupérer les statistiques d'usage AI sur une période
     *
     * @param startDate début de la période (format ISO 8601)
     * @param endDate   fin de la période (format ISO 8601)
     * @return DTO avec totalInputTokens, totalOutputTokens, totalCost
     */
    @Operation(hidden=true)
    @GetMapping("/stats")
    public LogAIUsageStatsDTOV001 getStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam("user") String user) {

        Timestamp startTs = Timestamp.valueOf(startDate);
        Timestamp endTs = Timestamp.valueOf(endDate);

        AnswerItem<UserPromptStats> statsItem = userPromptService.readSumByPeriod(startTs, endTs, user);
        UserPromptStats stats = statsItem.getItem();

        return LogAIUsageStatsDTOV001.builder()
                .totalInputTokens(stats.getTotalInputTokens())
                .totalOutputTokens(stats.getTotalOutputTokens())
                .totalCost(stats.getTotalCost())
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .build();
    }

}
