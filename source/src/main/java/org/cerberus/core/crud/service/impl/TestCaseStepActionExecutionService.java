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
package org.cerberus.core.crud.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.core.crud.dao.ITestCaseStepActionExecutionDAO;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.core.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionExecutionService implements ITestCaseStepActionExecutionService {

    @Autowired
    ITestCaseStepActionExecutionDAO testCaseStepActionExecutionDao;
    @Autowired
    ITestCaseExecutionDAO testCaseExecutionDao;
    @Autowired
    ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionExecutionService.class);

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets) {
        this.testCaseStepActionExecutionDao.insertTestCaseStepActionExecution(testCaseStepActionExecution, secrets);

    }

    @Override
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets) {
        this.testCaseStepActionExecutionDao.updateTestCaseStepActionExecution(testCaseStepActionExecution, secrets);
    }

    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int stepId, int index) {
        return testCaseStepActionExecutionDao.findTestCaseStepActionExecutionByCriteria(id, test, testCase, stepId, index);
    }

    @Override
    public JSONArray lastActionExecutionDuration(String test, String testcase, String country) {
        JSONArray result = new JSONArray();
        try {
            StringBuilder idList = new StringBuilder();
            List<String> listOfID = testCaseExecutionDao.getIDListOfLastExecutions(test, testcase, country);
            for (int a = 0; a < listOfID.size(); a++) {
                if (a != 0) {
                    idList.append(",");
                }
                idList.append("'");
                idList.append(listOfID.get(a));
                idList.append("'");
            }
            List<List<String>> listOfDuration = testCaseStepActionExecutionDao.getListOfSequenceDuration(idList.toString());
            String serie = "";
            JSONArray data;
            JSONArray line = new JSONArray();
            for (List<String> listInformation : listOfDuration) {
                String newserie = listInformation.get(1).concat("-").concat(listInformation.get(2)).concat("-").concat(listInformation.get(3)).concat("-").concat(listInformation.get(7));
                if (!serie.equals(newserie)) {
                    if (!serie.isEmpty()) {
                        result.put(line);
                    }
                    line = new JSONArray();
                    serie = newserie;
                }
                data = new JSONArray();
                DateFormat df2 = new SimpleDateFormat(org.cerberus.core.util.DateUtil.DATE_FORMAT_DISPLAY);
                Date dat = df2.parse(listInformation.get(4));

                String datea = df2.format(dat);
                data.put(datea);
                String date1 = listInformation.get(5);
                String date2 = listInformation.get(6);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date d1 = df.parse(date1);
                Date d2 = df.parse(date2);
                double diffInMilliseconds = (double) ((double) (((double) (d1.getTime() - d2.getTime()) / 1000) * 100)) / 100;
                data.put(diffInMilliseconds);

                line.put(data);
            }
            result.put(line);
        } catch (Exception ex) {
            LOG.warn(ex.toString());
        }
        return result;
    }

    @Override
    public AnswerList<TestCaseStepActionExecution> readByVarious1(long executionId, String test, String testcase, int stepId, int index) {
        return testCaseStepActionExecutionDao.readByVarious1(executionId, test, testcase, stepId, index);
    }

    @Override
    public AnswerItem<TestCaseStepActionExecution> readByKey(long executionId, String test, String testcase, int stepId, int index, int sequence) {
        return testCaseStepActionExecutionDao.readByKey(executionId, test, testcase, stepId, index, sequence);
    }

    @Override
    public AnswerList<TestCaseStepActionExecution> readByVarious1WithDependency(long executionId, String test, String testcase, int stepId, int index) {
        AnswerList<TestCaseStepActionExecution> actions = this.readByVarious1(executionId, test, testcase, stepId, index);
        AnswerList<TestCaseStepActionExecution> response = null;
        List<TestCaseStepActionExecution> tcsaeList = new ArrayList<>();
        for (Object action : actions.getDataList()) {

            TestCaseStepActionExecution tcsae = (TestCaseStepActionExecution) action;

            AnswerList<TestCaseStepActionControlExecution> controls = testCaseStepActionControlExecutionService.readByVarious1WithDependency(executionId, test, testcase, stepId, index, tcsae.getSequence());
            tcsae.setTestCaseStepActionControlExecutionList(controls.getDataList());

            AnswerList<TestCaseExecutionFile> files = testCaseExecutionFileService.readByVarious(executionId, tcsae.getTest() + "-" + tcsae.getTestCase() + "-" + tcsae.getStepId() + "-" + tcsae.getIndex() + "-" + tcsae.getSequence());
            tcsae.setFileList(files.getDataList());

            tcsaeList.add(tcsae);
        }
        response = new AnswerList<>(tcsaeList, actions.getTotalRows());
        return response;
    }
}
