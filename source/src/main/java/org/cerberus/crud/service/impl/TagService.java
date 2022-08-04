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
package org.cerberus.crud.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITagDAO;
import org.cerberus.crud.entity.EventHook;
import org.cerberus.crud.entity.Tag;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryTag;
import org.cerberus.crud.service.ITagService;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.event.IEventService;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ciresult.ICIService;
import org.cerberus.service.notification.INotificationService;
import org.cerberus.service.robotproviders.IBrowserstackService;
import org.cerberus.service.robotproviders.ILambdaTestService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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
public class TagService implements ITagService {

    @Autowired
    private ITagDAO tagDAO;
    @Autowired
    private IFactoryTag factoryTag;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;
    @Autowired
    private ICIService ciService;
    @Autowired
    private IBrowserstackService browserstackService;
    @Autowired
    private ILambdaTestService lambdatestService;
    @Autowired
    private ITestCaseExecutionQueueService executionQueueService;
    @Autowired
    private IEventService eventService;

    private static final Logger LOG = LogManager.getLogger("TagService");

    private final String OBJECT_NAME = "Tag";

    @Override
    public AnswerItem<Tag> readByKey(String tag) {
        return tagDAO.readByKey(tag);
    }

    @Override
    public AnswerItem<Tag> readByKeyTech(long tag) {
        return tagDAO.readByKeyTech(tag);
    }

    @Override
    public AnswerList<Tag> readAll() {
        return tagDAO.readByVariousByCriteria(null, 0, 0, "id", "desc", null, null, null);
    }

    @Override
    public AnswerList<Tag> readByCampaign(String campaign) {
        return tagDAO.readByVariousByCriteria(campaign, 0, 0, "id", "desc", null, null, null);
    }

    @Override
    public AnswerList<Tag> readByVarious(List<String> systems, Date from, Date to) {
        return tagDAO.readByVarious(systems, from, to);
    }

    @Override
    public AnswerList<Tag> readByVarious(List<String> campaigns, List<String> group1s, List<String> group2s, List<String> group3s, List<String> environments, List<String> countries, List<String> robotDeclis, Date from, Date to) {
        return tagDAO.readByVarious(campaigns, group1s, group2s, group3s, environments, countries, robotDeclis, from, to);
    }

    @Override
    public AnswerList<Tag> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems) {
        return tagDAO.readByVariousByCriteria(null, startPosition, length, columnName, sort, searchParameter, individualSearch, systems);
    }

    @Override
    public AnswerList<Tag> readByVariousByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return tagDAO.readByVariousByCriteria(campaign, startPosition, length, columnName, sort, searchParameter, individualSearch, null);
    }

    @Override
    public boolean exist(String object) {
        AnswerItem objectAnswer = readByKey(object);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Tag object) {
        return tagDAO.create(object);
    }

    @Override
    public Answer delete(Tag object) {
        return tagDAO.delete(object);
    }

    @Override
    public Answer update(String tag, Tag object) {
        return tagDAO.update(tag, object);
    }

    private JSONArray sortJsonArray(JSONArray in) {
        JSONArray out = new JSONArray();
        try {

            List<String> list = new ArrayList<>();
            for (int i = 0; i < in.length(); i++) {
                list.add(in.getString(i));
            }

            Collections.sort(list, (String a, String b) -> a.compareTo(b));
            for (String string : list) {
                out.put(string);
            }

        } catch (JSONException ex) {
            LOG.error(ex, ex);
        }

        return out;
    }

    @Override
    public Answer updateEndOfQueueData(String tag) {

        try {
            Tag mytag = convert(readByKey(tag));

            // End of queue is now.
            mytag.setDateEndQueue(new Timestamp(new Date().getTime()));
            tagDAO.updateDateEndQueue(mytag);

            // Total execution.
            mytag.setNbExe(testCaseExecutionService.readNbByTag(tag));

            // All the rest of the data are coming from ResultCI Servlet.
            JSONObject jsonResponse = ciService.getCIResult(tag, mytag.getCampaign());
            mytag.setCiScore(jsonResponse.getInt("CI_finalResult"));
            mytag.setCiScoreThreshold(jsonResponse.getInt("CI_finalResultThreshold"));

            if (jsonResponse.getString("result").equalsIgnoreCase("PE")) {
                // If result is PE that probably means that another execution was manually inserted in the queue or started after the end of last execution. It should not be considered.
                mytag.setCiResult(ciService.getFinalResult(jsonResponse.getInt("CI_finalResult"), jsonResponse.getInt("CI_finalResultThreshold"), jsonResponse.getInt("TOTAL_nbOfExecution"), jsonResponse.getInt("status_OK_nbOfExecution")));
            } else {
                mytag.setCiResult(jsonResponse.getString("result"));
            }

            mytag.setEnvironmentList(sortJsonArray(jsonResponse.getJSONArray("environment_List")).toString());
            mytag.setCountryList(sortJsonArray(jsonResponse.getJSONArray("country_list")).toString());
            mytag.setRobotDecliList(sortJsonArray(jsonResponse.getJSONArray("robotdecli_list")).toString());
            mytag.setSystemList(sortJsonArray(jsonResponse.getJSONArray("system_list")).toString());
            mytag.setApplicationList(sortJsonArray(jsonResponse.getJSONArray("application_list")).toString());

            mytag.setNbOK(jsonResponse.getInt("status_OK_nbOfExecution"));
            mytag.setNbKO(jsonResponse.getInt("status_KO_nbOfExecution"));
            mytag.setNbFA(jsonResponse.getInt("status_FA_nbOfExecution"));
            mytag.setNbNA(jsonResponse.getInt("status_NA_nbOfExecution"));
            mytag.setNbNE(jsonResponse.getInt("status_NE_nbOfExecution"));
            mytag.setNbWE(jsonResponse.getInt("status_WE_nbOfExecution"));
            mytag.setNbPE(jsonResponse.getInt("status_PE_nbOfExecution"));
            mytag.setNbQU(jsonResponse.getInt("status_QU_nbOfExecution"));
            mytag.setNbQE(jsonResponse.getInt("status_QE_nbOfExecution"));
            mytag.setNbCA(jsonResponse.getInt("status_CA_nbOfExecution"));
            mytag.setNbExeUsefull(jsonResponse.getInt("TOTAL_nbOfExecution"));

            if (!StringUtil.isNullOrEmpty(mytag.getCampaign())) {
                // We get the campaig here and potencially trigger the event.
                eventService.triggerEvent(EventHook.EVENTREFERENCE_CAMPAIGN_END, mytag, null, null, null);
                if (mytag.getCiResult().equalsIgnoreCase("KO")) {
                    eventService.triggerEvent(EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO, mytag, null, null, null);
                }
            }

            return tagDAO.updateDateEndQueue(mytag);

        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(TagService.class.getName()).log(Level.SEVERE, null, ex);
            return null;

        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(TagService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public Answer updateDescription(String tag, Tag object) {
        return tagDAO.updateDescription(tag, object);
    }

    @Override
    public Answer updateComment(String tag, Tag object) {
        return tagDAO.updateComment(tag, object);
    }

    @Override
    public Answer updateXRayTestExecution(String tag, Tag object) {
        return tagDAO.updateXRayTestExecution(tag, object);
    }

    @Override
    public int lockXRayTestExecution(String tag, Tag object) {
        return tagDAO.lockXRayTestExecution(tag, object);
    }

    @Override
    public Answer createAuto(String tagS, String campaign, String user, JSONArray reqEnvironmentList, JSONArray reqCountryList) {
        AnswerItem answerTag;
        answerTag = readByKey(tagS);
        Tag tag = (Tag) answerTag.getItem();
        if (tag == null) {
            Tag newTag = factoryTag.create(0, tagS, "", "", campaign, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", "", "", "", "", "",
                    reqEnvironmentList.toString(), reqCountryList.toString(), "", "", "", "", user, null, user, null);
            Answer ans = tagDAO.create(newTag);
            // If campaign is not empty, we can notify the Start of campaign execution.
            if (!StringUtil.isNullOrEmpty(campaign)) {
                eventService.triggerEvent(EventHook.EVENTREFERENCE_CAMPAIGN_START, newTag, null, null, null);
            }
            return ans;
        } else {
            if ((StringUtil.isNullOrEmpty(tag.getCampaign())) && !StringUtil.isNullOrEmpty(campaign)) {
                tag.setCampaign(campaign);
                return tagDAO.update(tag.getTag(), tag);
            }
            return null;
        }
    }

    @Override
    public String enrichTagWithCloudProviderBuild(String provider, String system, String tagS, String user, String pass) {
        LOG.debug("Trying to enrish tag '" + tagS + "' with Cloud service provider Build (" + provider + ").");
        if (StringUtil.isNullOrEmpty(tagS)) {
            return null;
        }
        AnswerItem answerTag;
        answerTag = readByKey(tagS);
        Tag tag = (Tag) answerTag.getItem();
        switch (provider) {
            case TestCaseExecution.ROBOTPROVIDER_BROWSERSTACK:
                if ((tag != null) && (StringUtil.isNullOrEmpty(tag.getBrowserstackBuildHash()) || "BSHash".equalsIgnoreCase(tag.getBrowserstackBuildHash()))) {
                    String newBuildHash = browserstackService.getBrowserStackBuildHash(system, tagS, user, pass);
                    tag.setBrowserstackBuildHash(newBuildHash);
                    Answer ans = tagDAO.updateBrowserStackBuild(tagS, tag);
                    return newBuildHash;
                }
                break;
            case TestCaseExecution.ROBOTPROVIDER_LAMBDATEST:
                if ((tag != null) && (StringUtil.isNullOrEmpty(tag.getLambdaTestBuild()))) {
                    String newBuildHash = lambdatestService.getBuildValue(tagS, user, pass, system);
                    tag.setLambdaTestBuild(newBuildHash);
                    Answer ans = tagDAO.updateLambdatestBuild(tagS, tag);
                    return newBuildHash;
                }
                break;
        }

        return null;
    }

    @Override
    public Tag convert(AnswerItem<Tag> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Tag> convert(AnswerList<Tag> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return tagDAO.readDistinctValuesByCriteria(system, searchParameter, individualSearch, columnName);
    }

    @Override
    public void manageCampaignEndOfExecution(String tag) throws CerberusException {

        try {
            if (!StringUtil.isNullOrEmpty(tag)) {
                Tag currentTag = this.convert(this.readByKey(tag));
                if ((currentTag != null)) {
                    if (currentTag.getDateEndQueue().before(Timestamp.valueOf("1980-01-01 01:01:01.000000001"))) {
                        AnswerList answerListQueue = new AnswerList<>();
                        answerListQueue = executionQueueService.readQueueOpen(tag);
                        if (answerListQueue.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && (answerListQueue.getDataList().isEmpty())) {
                            LOG.debug("No More executions (in queue or running) on tag : " + tag + " - " + answerListQueue.getDataList().size() + " " + answerListQueue.getMessageCodeString() + " - ");
                            this.updateEndOfQueueData(tag);
                        } else {
                            LOG.debug("Still executions in queue on tag : " + tag + " - " + answerListQueue.getDataList().size() + " " + answerListQueue.getMessageCodeString());
                        }
                    } else {
                        LOG.debug("Tag is already flaged with recent timestamp. " + currentTag.getDateEndQueue());
                    }

                }
            }
        } catch (Exception e) {
            LOG.error(e, e);
        }

    }

    @Override
    public String formatResult(Tag tag) {
        StringBuilder result = new StringBuilder();
        result.append(tag.getNbExeUsefull());
        result.append(" Execution(s) - ");
        if (tag.getNbOK() > 0) {
            result.append(tag.getNbOK());
            result.append(" OK - ");
        }
        if (tag.getNbKO() > 0) {
            result.append(tag.getNbKO());
            result.append(" KO - ");
        }
        if (tag.getNbFA() > 0) {
            result.append(tag.getNbFA());
            result.append(" FA - ");
        }
        if (tag.getNbNA() > 0) {
            result.append(tag.getNbNA());
            result.append(" NA - ");
        }
        if (tag.getNbNE() > 0) {
            result.append(tag.getNbNE());
            result.append(" NE - ");
        }
        if (tag.getNbWE() > 0) {
            result.append(tag.getNbWE());
            result.append(" WE - ");
        }
        if (tag.getNbPE() > 0) {
            result.append(tag.getNbPE());
            result.append(" PE - ");
        }
        if (tag.getNbCA() > 0) {
            result.append(tag.getNbCA());
            result.append(" CA - ");
        }
        if (tag.getNbQU() > 0) {
            result.append(tag.getNbQU());
            result.append(" QU - ");
        }
        if (tag.getNbQE() > 0) {
            result.append(tag.getNbQE());
            result.append(" QE - ");
        }
        result.delete(result.length() - 3, result.length());
        return result.toString();
    }

    JSONObject getJSONEnriched() {
        JSONObject result = new JSONObject();
        return result;
    }

}
