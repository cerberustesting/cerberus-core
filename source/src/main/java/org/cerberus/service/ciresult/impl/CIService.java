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
package org.cerberus.service.ciresult.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;
import java.util.Map;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.dto.SummaryStatisticsDTO;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ciresult.ICIService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
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
    public JSONObject getCIResult(String tag, String campaign) {
        try {

            // If campaign is not defined here, we try to get it from tag. At the same time, we check that tag exist.
            if (StringUtil.isNullOrEmpty(campaign)) {
                Tag myTag = tagService.convert(tagService.readByKey(tag));
                campaign = myTag.getCampaign();
            }

            List<TestCaseExecution> myList = testExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
            JSONObject jsonResponse = CIService.this.getCIResult(tag, campaign, myList);

            jsonResponse.put("detail_by_declinaison", generateStats(myList));

            jsonResponse.put("environment_List", generateEnvList(myList));
            jsonResponse.put("country_list", generateCountryList(myList));
            jsonResponse.put("robotdecli_list", generateRobotDecliList(myList));
            jsonResponse.put("system_list", generateSystemList(myList));
            jsonResponse.put("application_list", generateApplicationList(myList));

            jsonResponse.put("nb_of_retry", myList.stream().mapToInt(it -> it.getNbExecutions() - 1).sum());

            return jsonResponse;
        } catch (CerberusException | ParseException | JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    private JSONObject getCIResult(String tag, String campaign, List<TestCaseExecution> myList) {
        try {
            JSONObject jsonResponse = new JSONObject();

            int nbok = 0;
            int nbko = 0;
            int nbfa = 0;
            int nbpe = 0;
            int nbne = 0;
            int nbwe = 0;
            int nbna = 0;
            int nbca = 0;
            int nbqu = 0;
            int nbqe = 0;
            int nbtotal = 0;

            int nbkop1 = 0;
            int nbkop2 = 0;
            int nbkop3 = 0;
            int nbkop4 = 0;
            int nbkop5 = 0;

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
                        && !curExe.getControlStatus().equals("PE") && !curExe.getControlStatus().equals("QU")) {
                    switch (curExe.getTestCaseObj().getPriority()) {
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
            if (!StringUtil.isNullOrEmpty(campaign)) {
                try {
                    LOG.debug("Trying to get CIScoreThreshold from campaign : '" + campaign + "'");
                    // Check campaign score here.
                    Campaign mycampaign = campaignService.convert(campaignService.readByKey(campaign));
                    if (!StringUtil.isNullOrEmpty(mycampaign.getCIScoreThreshold())) {
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

            jsonResponse.put("messageType", "OK");
            jsonResponse.put("message", "CI result calculated with success.");
            jsonResponse.put("tag", tag);
            jsonResponse.put("CI_OK_prio1", pond1);
            jsonResponse.put("CI_OK_prio2", pond2);
            jsonResponse.put("CI_OK_prio3", pond3);
            jsonResponse.put("CI_OK_prio4", pond4);
            jsonResponse.put("CI_OK_prio5", pond5);
            jsonResponse.put("CI_finalResult", resultCal);
            jsonResponse.put("CI_finalResultThreshold", resultCalThreshold);
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
            jsonResponse.put("result", result);
            jsonResponse.put("ExecutionStart", String.valueOf(new Timestamp(longStart)));
            jsonResponse.put("ExecutionEnd", String.valueOf(new Timestamp(longEnd)));

            return jsonResponse;

        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }
        return null;
    }

    @Override
    public String getFinalResult(int resultCal, int resultCalThreshold, int nbtotal, int nbok) {
        if ((resultCal < resultCalThreshold) && (nbtotal > 0) && nbok > 0) {
            return "OK";
        } else {
            return "KO";
        }
    }

    private JSONArray generateEnvList(List<TestCaseExecution> testCaseExecutions) throws JSONException {

        JSONArray jsonResult = new JSONArray();

        HashMap<String, String> statMap = new HashMap<String, String>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isNullOrEmpty(testCaseExecution.getEnvironment())) {
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

        HashMap<String, String> statMap = new HashMap<String, String>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isNullOrEmpty(testCaseExecution.getCountry())) {
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

        HashMap<String, String> statMap = new HashMap<String, String>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isNullOrEmpty(testCaseExecution.getRobotDecli())) {
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

        HashMap<String, String> statMap = new HashMap<String, String>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isNullOrEmpty(testCaseExecution.getSystem())) {
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

        HashMap<String, String> statMap = new HashMap<String, String>();
        for (TestCaseExecution testCaseExecution : testCaseExecutions) {
            if (!StringUtil.isNullOrEmpty(testCaseExecution.getApplication())) {
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

        HashMap<String, SummaryStatisticsDTO> statMap = new HashMap<String, SummaryStatisticsDTO>();
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
        TreeMap<String, SummaryStatisticsDTO> sortedKeys = new TreeMap<String, SummaryStatisticsDTO>(summaryMap);
        for (String key : sortedKeys.keySet()) {
            SummaryStatisticsDTO sumStats = summaryMap.get(key);
            //percentage values
            sumStats.updatePercentageStatistics();
            dataArray.put(new JSONObject(gson.toJson(sumStats)));
        }

        return dataArray;
    }
}
