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
package org.cerberus.core.service.ciresult.impl;

import com.google.gson.Gson;
import org.apache.commons.collections4.CollectionUtils;
import org.cerberus.core.api.entity.CICampaignResult;
import org.cerberus.core.api.entity.CampaignExecutionResult;
import org.cerberus.core.api.entity.CampaignExecutionResultPriority;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.api.exceptions.FailedReadOperationException;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.dto.SummaryStatisticsDTO;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.ciresult.ICIService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bcivel
 */
@Service
public class CIService implements ICIService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CIService.class);

    @Autowired
    private ITestCaseExecutionService testExecutionService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ICampaignService campaignService;
    @Autowired
    private ITagService tagService;

    @Override
    public JSONObject getCIResult(String tag, String campaign, List<TestCaseExecution> executions) {
        try {

            // If campaign is not defined here, we try to get it from tag. At the same time, we check that tag exist.
            if (StringUtil.isEmptyOrNull(campaign)) {
                Tag myTag = tagService.convert(tagService.readByKey(tag));
                campaign = myTag.getCampaign();
            }

            JSONObject jsonResponse = CIService.this.calculateCIResult(tag, campaign, executions);

            jsonResponse.put("detail_by_declinaison", generateStats(executions));

            jsonResponse.put("environment_List", generateEnvList(executions));
            jsonResponse.put("country_list", generateCountryList(executions));
            jsonResponse.put("robotdecli_list", generateRobotDecliList(executions));
            jsonResponse.put("system_list", generateSystemList(executions));
            jsonResponse.put("application_list", generateApplicationList(executions));

            // Semms not necessary as already in 'TOTAL_nbOfExecution'
//            jsonResponse.put("nb_of_retry", executions.stream().mapToInt(it -> it.getNbExecutions() - 1).sum());
            return jsonResponse;
        } catch (CerberusException | JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    private JSONObject calculateCIResult(String tag, String campaign, List<TestCaseExecution> myList) {
        try {
            JSONObject jsonResponse = new JSONObject();

            int nbok = 0, nbko = 0, nbfa = 0, nbpe = 0, nbne = 0, nbwe = 0, nbna = 0, nbca = 0, nbqu = 0, nbqe = 0;

            int nbtotal = 0;
            int nbflaky = 0;
            int nbismuted = 0;

            // Used for CICD Score calculation (Muted execution ignored)
            int nbkop0 = 0, nbkop1 = 0, nbkop2 = 0, nbkop3 = 0, nbkop4 = 0, nbkop5 = 0;
            // Used for CICD Max Score calculation (Muted execution ignored)
            int nbp0 = 0, nbp1 = 0, nbp2 = 0, nbp3 = 0, nbp4 = 0, nbp5 = 0;

            long longStart = 0;
            long longEnd = 0;

            for (TestCaseExecution curExe : myList) {

                if (longStart == 0) {
                    longStart = curExe.getStart();
                }
                if (curExe.getStart() < longStart) {
                    longStart = curExe.getStart();
                }

                if (longEnd == 0) {
                    longEnd = curExe.getEnd();
                }
                if (curExe.getEnd() > longEnd) {
                    longEnd = curExe.getEnd();
                }

                nbtotal++;
                if (curExe.isFlaky()) {
                    nbflaky++;
                }
                if (curExe.isTestCaseIsMuted()) {
                    nbismuted++;
                }

                switch (curExe.getControlStatus()) {
                    case TestCaseExecution.CONTROLSTATUS_KO:
                        nbko++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_OK:
                        nbok++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_FA:
                        nbfa++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_NA:
                        nbna++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_CA:
                        nbca++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_PE:
                        nbpe++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_NE:
                        nbne++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_WE:
                        nbwe++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_QU:
                        nbqu++;
                        break;
                    case TestCaseExecution.CONTROLSTATUS_QE:
                        nbqe++;
                        break;
                }

                if (!curExe.getControlStatus().equals("OK") && !curExe.getControlStatus().equals("NE")
                        && !curExe.getControlStatus().equals("PE") && !curExe.getControlStatus().equals("QU")
                        && !curExe.isTestCaseIsMuted()) {
                    switch (curExe.getTestCasePriority()) {
                        case 0:
                            nbkop0++;
                            break;
                        case 1:
                            nbkop1++;
                            break;
                        case 2:
                            nbkop2++;
                            break;
                        case 3:
                            nbkop3++;
                            break;
                        case 4:
                            nbkop4++;
                            break;
                        case 5:
                            nbkop5++;
                            break;
                    }
                }

                if (!curExe.isTestCaseIsMuted()) {
                    switch (curExe.getTestCasePriority()) {
                        case 0:
                            nbp0++;
                            break;
                        case 1:
                            nbp1++;
                            break;
                        case 2:
                            nbp2++;
                            break;
                        case 3:
                            nbp3++;
                            break;
                        case 4:
                            nbp4++;
                            break;
                        case 5:
                            nbp5++;
                            break;
                    }
                }
            }

            int pond1 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio1", "", 0);
            int pond2 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio2", "", 0);
            int pond3 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio3", "", 0);
            int pond4 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio4", "", 0);
            int pond5 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio5", "", 0);
            String result;

            // Getting threshold from parameter.
            int resultCalThreshold = parameterService.getParameterIntegerByKey("cerberus_ci_threshold", "", 100);

            // If tag is linked to campaign, we get the threshold from the campaign definition (if exist and can be converted to integer).
            if (!StringUtil.isEmptyOrNull(campaign)) {
                try {
                    LOG.debug("Trying to get CIScoreThreshold from campaign : '" + campaign + "'");
                    // Check campaign score here.
                    Campaign mycampaign = campaignService.convert(campaignService.readByKey(campaign));
                    if (!StringUtil.isEmptyOrNull(mycampaign.getCIScoreThreshold())) {
                        try {
                            resultCalThreshold = Integer.valueOf(mycampaign.getCIScoreThreshold());
                        } catch (NumberFormatException ex) {
                            LOG.error("Could not convert campaign CIScoreThreshold '" + mycampaign.getCIScoreThreshold() + "' to integer.", ex);
                        }
                    }
                } catch (CerberusException ex) {
                    LOG.error("Could not find campaign when calculating CIScore.", ex);
                }
            }

            int resultCal = (nbkop1 * pond1) + (nbkop2 * pond2) + (nbkop3 * pond3) + (nbkop4 * pond4) + (nbkop5 * pond5);
            if ((nbtotal > 0) && nbqu + nbpe > 0) {
                result = "PE";
            } else {
                result = getFinalResult(resultCal, resultCalThreshold, nbtotal, nbok);
            }

            int resultCalMax = (nbp1 * pond1) + (nbp2 * pond2) + (nbp3 * pond3) + (nbp4 * pond4) + (nbp5 * pond5);

            jsonResponse.put("messageType", "OK");
            jsonResponse.put("message", "CI result calculated with success.");
            jsonResponse.put("tag", tag);
            jsonResponse.put("CI_OK_prio1", pond1);
            jsonResponse.put("CI_OK_prio2", pond2);
            jsonResponse.put("CI_OK_prio3", pond3);
            jsonResponse.put("CI_OK_prio4", pond4);
            jsonResponse.put("CI_OK_prio5", pond5);
            jsonResponse.put("CI_finalResult", resultCal);
            jsonResponse.put("CI_finalResultMax", resultCalMax);
            jsonResponse.put("CI_finalResultThreshold", resultCalThreshold);
            jsonResponse.put("NonOK_prio0_nbOfExecution", nbkop0);
            jsonResponse.put("NonOK_prio1_nbOfExecution", nbkop1);
            jsonResponse.put("NonOK_prio2_nbOfExecution", nbkop2);
            jsonResponse.put("NonOK_prio3_nbOfExecution", nbkop3);
            jsonResponse.put("NonOK_prio4_nbOfExecution", nbkop4);
            jsonResponse.put("NonOK_prio5_nbOfExecution", nbkop5);
            jsonResponse.put("status_OK_nbOfExecution", nbok);
            jsonResponse.put("status_KO_nbOfExecution", nbko);
            jsonResponse.put("status_FA_nbOfExecution", nbfa);
            jsonResponse.put("status_PE_nbOfExecution", nbpe);
            jsonResponse.put("status_NA_nbOfExecution", nbna);
            jsonResponse.put("status_CA_nbOfExecution", nbca);
            jsonResponse.put("status_NE_nbOfExecution", nbne);
            jsonResponse.put("status_WE_nbOfExecution", nbwe);
            jsonResponse.put("status_QU_nbOfExecution", nbqu);
            jsonResponse.put("status_QE_nbOfExecution", nbqe);
            jsonResponse.put("TOTAL_nbOfExecution", nbtotal);
            jsonResponse.put("TOTAL_nbOfFlaky", nbflaky);
            jsonResponse.put("TOTAL_nbOfMuted", nbismuted);
            jsonResponse.put("result", result);
            jsonResponse.put("ExecutionStart", String.valueOf(new Timestamp(longStart)));
            jsonResponse.put("ExecutionEnd", String.valueOf(new Timestamp(longEnd)));

            return jsonResponse;

        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    public CICampaignResult getCIResultApi(String tag, String campaign) {
        Optional<Tag> campaignExecution;
        List<TestCaseExecution> executions;
        try {
            //Get the last campaign execution when campaign id is specified
            if (StringUtil.isNotEmptyOrNull(campaign)) {
                AnswerList<Tag> tags = tagService.readByVariousByCriteria(campaign, 0, 1, "id", "desc", null, null);
                if (CollectionUtils.isNotEmpty(tags.getDataList()) && tags.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
                    tag = tags.getDataList().get(0).getTag();
                } else {
                    throw new EntityNotFoundException(CICampaignResult.class, "campaignId", campaign);
                }
            }

            campaignExecution = Optional.ofNullable(tagService.convert(tagService.readByKey(tag)));
            if (campaignExecution.isPresent()) {
                campaign = campaignExecution.get().getCampaign();
            } else {
                throw new EntityNotFoundException(CICampaignResult.class, "campaignExecutionId", tag);

            }

            int coefficientLevel1 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio1", "", 0);
            int coefficientLevel2 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio2", "", 0);
            int coefficientLevel3 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio3", "", 0);
            int coefficientLevel4 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio4", "", 0);
            int coefficientLevel5 = parameterService.getParameterIntegerByKey("cerberus_ci_okcoefprio5", "", 0);

            executions = testExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
            Map<String, Long> executionStatusCount = executions.stream().collect(Collectors.groupingBy(TestCaseExecution::getControlStatus, Collectors.counting()));
            int nbOk = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_OK) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_OK).intValue();
            int nbKo = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_KO) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_KO).intValue();
            int nbFa = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_FA) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_FA).intValue();
            int nbNa = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_NA) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_NA).intValue();
            int nbCa = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_CA) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_CA).intValue();
            int nbPe = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_PE) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_PE).intValue();
            int nbNe = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_NE) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_NE).intValue();
            int nbWe = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_WE) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_WE).intValue();
            int nbQu = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_QU) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_QU).intValue();
            int nbQe = executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_QE) == null ? 0 : executionStatusCount.get(TestCaseExecution.CONTROLSTATUS_QE).intValue();
            int nbTotal = nbOk + nbKo + nbFa + nbNa + nbCa + nbPe + nbNe + nbWe + nbQu + nbQe;

            Map<Integer, Long> statusKoCountPriority = executions
                    .stream()
                    .filter(
                            testCaseExecution -> (!testCaseExecution.getControlStatus().equals(TestCaseExecution.CONTROLSTATUS_OK)
                            && !testCaseExecution.getControlStatus().equals(TestCaseExecution.CONTROLSTATUS_NE)
                            && !testCaseExecution.getControlStatus().equals(TestCaseExecution.CONTROLSTATUS_PE)
                            && !testCaseExecution.getControlStatus().equals(TestCaseExecution.CONTROLSTATUS_QU))
                    )
                    .collect(Collectors.groupingBy(testCaseExecution -> testCaseExecution.getTestCaseObj().getPriority(), Collectors.counting()));

            int nbKoPriority1 = statusKoCountPriority.get(1) == null ? 0 : statusKoCountPriority.get(1).intValue();
            int nbKoPriority2 = statusKoCountPriority.get(2) == null ? 0 : statusKoCountPriority.get(2).intValue();
            int nbKoPriority3 = statusKoCountPriority.get(3) == null ? 0 : statusKoCountPriority.get(3).intValue();
            int nbKoPriority4 = statusKoCountPriority.get(4) == null ? 0 : statusKoCountPriority.get(4).intValue();
            int nbKoPriority5 = statusKoCountPriority.get(5) == null ? 0 : statusKoCountPriority.get(5).intValue();

            int resultCalThreshold = convertCIScoreThreshold(campaign);
            int resultCal = (nbKoPriority1 * coefficientLevel1) + (nbKoPriority2 * coefficientLevel2) + (nbKoPriority3 * coefficientLevel3) + (nbKoPriority4 * coefficientLevel4) + (nbKoPriority5 * coefficientLevel5);
            String globalResult = ((nbTotal > 0) && nbQu + nbPe > 0) ? "PE" : this.getFinalResult(resultCal, resultCalThreshold, nbTotal, nbOk);

            CampaignExecutionResult campaignResult = CampaignExecutionResult.builder()
                    .ok(nbOk)
                    .ca(nbCa)
                    .pe(nbPe)
                    .qe(nbQe)
                    .qu(nbQu)
                    .ne(nbNe)
                    .we(nbWe)
                    .ko(nbKo)
                    .na(nbNa)
                    .fa(nbFa)
                    .total(nbTotal)
                    .totalWithRetries(nbTotal + executions.stream().mapToInt(it -> it.getNbExecutions() - 1).sum())
                    .build();

            CampaignExecutionResultPriority campaignResultPriority = CampaignExecutionResultPriority.builder()
                    .okCoefficientPriorityLevel1(coefficientLevel1)
                    .okCoefficientPriorityLevel2(coefficientLevel2)
                    .okCoefficientPriorityLevel3(coefficientLevel3)
                    .okCoefficientPriorityLevel4(coefficientLevel4)
                    .okCoefficientPriorityLevel5(coefficientLevel5)
                    .nonOkExecutionsPriorityLevel1(nbKoPriority1)
                    .nonOkExecutionsPriorityLevel2(nbKoPriority2)
                    .nonOkExecutionsPriorityLevel3(nbKoPriority3)
                    .nonOkExecutionsPriorityLevel4(nbKoPriority4)
                    .nonOkExecutionsPriorityLevel5(nbKoPriority5)
                    .build();

            return CICampaignResult.builder()
                    .globalResult(globalResult)
                    .campaignExecutionId(tag)
                    .detailByDeclinations(generateStats(executions))
                    .countries(generateCountryList(executions))
                    .environments(generateEnvList(executions))
                    .robotDeclinations(generateRobotDecliList(executions))
                    .systems(generateSystemList(executions))
                    .applications(generateApplicationList(executions))
                    .calculatedResult(resultCal)
                    .resultThreshold(resultCalThreshold)
                    .result(campaignResult)
                    .resultByPriority(campaignResultPriority)
                    .executionStart(String.valueOf(campaignExecution.get().getDateCreated()))
                    .executionEnd(String.valueOf(campaignExecution.get().getDateEndQueue()))
                    .build();
        } catch (JSONException | ParseException | CerberusException e) {
            throw new FailedReadOperationException("An error occurred when retrieving the campaign execution.");
        }
    }

    @Override
    public String getFinalResult(int resultCal, int resultCalThreshold, int nbtotal, int nbok) {
        if ((resultCal < resultCalThreshold) && (nbtotal > 0)) {
            return "OK";
        } else {
            return "KO";
        }
    }

    private JSONArray generateEnvList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isEmptyOrNull(testCaseExecution.getEnvironment())) {
                statMap.put(testCaseExecution.getEnvironment(), null);
            }
        }
        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonResult.put(key);
        }
        return jsonResult;
    }

    private JSONArray generateCountryList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isEmptyOrNull(testCaseExecution.getCountry())) {
                statMap.put(testCaseExecution.getCountry(), null);
            }
        }
        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonResult.put(key);
        }
        return jsonResult;
    }

    private JSONArray generateRobotDecliList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isEmptyOrNull(testCaseExecution.getRobotDecli())) {
                statMap.put(testCaseExecution.getRobotDecli(), null);
            }
        }
        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonResult.put(key);
        }
        return jsonResult;
    }

    private JSONArray generateSystemList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isEmptyOrNull(testCaseExecution.getSystem())) {
                statMap.put(testCaseExecution.getSystem(), null);
            }
        }
        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonResult.put(key);
        }
        return jsonResult;
    }

    private JSONArray generateApplicationList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isEmptyOrNull(testCaseExecution.getApplication())) {
                statMap.put(testCaseExecution.getApplication(), null);
            }
        }
        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonResult.put(key);
        }
        return jsonResult;
    }

    private JSONArray generateStats(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONObject jsonResult = new JSONObject();

        HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            StringBuilder key = new StringBuilder();
            key.append(testCaseExecution.getEnvironment());
            key.append("_");
            key.append(testCaseExecution.getCountry());
            key.append("_");
            key.append(testCaseExecution.getRobotDecli());
            key.append("_");
            key.append(testCaseExecution.getApplication());

            SummaryStatisticsDTO stat = new SummaryStatisticsDTO();
            stat.setEnvironment(testCaseExecution.getEnvironment());
            stat.setCountry(testCaseExecution.getCountry());
            stat.setRobotDecli(testCaseExecution.getRobotDecli());
            stat.setApplication(testCaseExecution.getApplication());
            statMap.put(key.toString(), stat);
        }

        return getStatByEnvCountryRobotDecli(testCaseExecutions, statMap);
    }

    private JSONArray getStatByEnvCountryRobotDecli(List<TestCaseExecution> testCaseExecutions, HashMap<String, SummaryStatisticsDTO> statMap) throws JSONException {
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {

            StringBuilder key = new StringBuilder();

            key.append(testCaseExecution.getEnvironment());
            key.append("_");
            key.append((testCaseExecution.getCountry()));
            key.append("_");
            key.append((testCaseExecution.getRobotDecli()));
            key.append("_");
            key.append(testCaseExecution.getApplication());

            if (statMap.containsKey(key.toString())) {
                statMap.get(key.toString()).updateStatisticByStatus(testCaseExecution.getControlStatus());
            }
        }
        return extractSummaryData(statMap);
    }

    private JSONArray extractSummaryData(HashMap<String, SummaryStatisticsDTO> summaryMap) throws JSONException {
        JSONObject extract = new JSONObject();
        Gson gson = new Gson();
        JSONArray dataArray = new JSONArray();
        //sort keys
        TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<>(summaryMap);
        for (String key : sortedKeys.keySet()) {
            SummaryStatisticsDTO sumStats = summaryMap.get(key);
            //percentage values
            sumStats.updatePercentageStatistics();
            dataArray.put(new JSONObject(gson.toJson(sumStats)));
        }

        return dataArray;
    }

    private int convertCIScoreThreshold(String campaign) throws CerberusException {
        int ciScoreThreshold = parameterService.getParameterIntegerByKey("cerberus_ci_threshold", "", 100);
        if (StringUtil.isNotEmptyOrNull(campaign)) {
            LOG.debug("Trying to get CIScoreThreshold from campaign : {}", campaign);
            // Check campaign score here.
            Campaign campaignRetrieved = campaignService.convert(campaignService.readByKey(campaign));
            if (StringUtil.isNotEmptyOrNull(campaignRetrieved.getCIScoreThreshold())) {
                try {
                    ciScoreThreshold = Integer.parseInt(campaignRetrieved.getCIScoreThreshold());
                } catch (NumberFormatException ex) {
                    LOG.error("Could not convert campaign CIScoreThreshold {} to integer", campaignRetrieved.getCIScoreThreshold(), ex);
                }
            }
        }
        return ciScoreThreshold;
    }

    public String generateSvg(String id, String globalResult) {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"350\" height=\"20\">"
                + "<linearGradient id=\"b\" x2=\"0\" y2=\"100%\">"
                + "<stop offset=\"0\" stop-color=\"#bbb\" stop-opacity=\".1\"></stop>"
                + "<stop offset=\"1\" stop-opacity=\".1\"></stop>"
                + "</linearGradient>"
                //RECTANGLE
                + "<rect rx=\"3\" fill=\"#555\" width=\"250\" height=\"20\"></rect>"
                + "<rect rx=\"3\" x=\"210\" fill=\"" + this.getColor(globalResult) + "\" width=\"40\" height=\"20\"></rect>"
                //TEXT
                + "<g fill=\"#fff\" text-anchor=\"start\" font-family=\"DejaVu Sans,Verdana,Geneva,sans-serif\" font-size=\"9\">"
                + "<text x=\"10\" y=\"14\">" + id + "</text>"
                + "<text x=\"225\" y=\"14\">" + globalResult + "</text>"
                + "</g>"
                + "</svg>";
    }

    private String getColor(String controlStatus) {
        String color;
        switch (controlStatus) {
            case "OK":
                color = "#5CB85C";
                break;
            case "KO":
                color = "#D9534F";
                break;
            default:
                color = "#3498DB";
                break;
        }
        return color;
    }
}
