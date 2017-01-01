/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Level;
import org.cerberus.crud.dao.ITestCaseExecutionDAO;
import org.cerberus.crud.dao.ITestCaseStepActionExecutionDAO;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
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

    @Override
    public void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {
        this.testCaseStepActionExecutionDao.insertTestCaseStepActionExecution(testCaseStepActionExecution);

    }

    @Override
    public void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {
        this.testCaseStepActionExecutionDao.updateTestCaseStepActionExecution(testCaseStepActionExecution);
    }

    @Override
    public List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step, int index) {
        return testCaseStepActionExecutionDao.findTestCaseStepActionExecutionByCriteria(id, test, testCase, step, index);
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
                    if (!serie.equals("")) {
                        result.put(line);
                    }
                    line = new JSONArray();
                    serie = newserie;
                }
                data = new JSONArray();
                DateFormat df2 = new SimpleDateFormat(org.cerberus.util.DateUtil.DATE_FORMAT_DISPLAY);
                Date dat = df2.parse(listInformation.get(4));

                String datea = df2.format(dat);
                data.put(datea);
                String date1 = listInformation.get(5);
                String date2 = listInformation.get(6);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date d1 = df.parse(date1);
                Date d2 = df.parse(date2);
                double diffInMilliseconds = (double) ((int) (((double) (d1.getTime() - d2.getTime()) / 1000) * 100)) / 100;
                data.put(diffInMilliseconds);

                line.put(data);
            }
            result.put(line);
        } catch (Exception ex) {
            MyLogger.log(TestCaseStepActionExecutionService.class.getName(), Level.FATAL, ex.toString());
        }
        return result;
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testcase, int step, int index) {
        return testCaseStepActionExecutionDao.readByVarious1(executionId, test, testcase, step, index);
    }

    @Override
    public AnswerItem readByKey(long executionId, String test, String testcase, int step, int index, int sequence) {
        return testCaseStepActionExecutionDao.readByKey(executionId, test, testcase, step, index, sequence);
    }

    @Override
    public AnswerList readByVarious1WithDependency(long executionId, String test, String testcase, int step, int index) {
        AnswerList actions = this.readByVarious1(executionId, test, testcase, step, index);
        AnswerList response = null;
        List<TestCaseStepActionExecution> tcsaeList = new ArrayList();
        for (Object action : actions.getDataList()) {
            TestCaseStepActionExecution tcsae = (TestCaseStepActionExecution) action;
            AnswerList controls = testCaseStepActionControlExecutionService.readByVarious1(executionId, test, testcase, step, index, tcsae.getSequence());
            tcsae.setTestCaseStepActionControlExecutionList(controls);
            tcsaeList.add(tcsae);
        }
        response = new AnswerList(tcsaeList, actions.getTotalRows());
        return response;
    }
}
