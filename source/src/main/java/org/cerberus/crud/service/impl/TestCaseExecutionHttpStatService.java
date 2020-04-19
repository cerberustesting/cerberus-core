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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.ITestCaseExecutionHttpStatDAO;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;

import org.cerberus.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionHttpStat;
import org.cerberus.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionHttpStatService implements ITestCaseExecutionHttpStatService {

    @Autowired
    private ITestCaseExecutionHttpStatDAO testCaseExecutionHttpStatDAO;
    @Autowired
    private IFactoryTestCaseExecutionHttpStat factoryTestCaseExecutionHttpStat;

    private static final Logger LOG = LogManager.getLogger("TestCaseExecutionHttpStatService");

    private final String OBJECT_NAME = "TestCaseExecutionHttpStat";

    @Override
    public Answer create(TestCaseExecutionHttpStat object) {
        return testCaseExecutionHttpStatDAO.create(object);
    }

    @Override
    public AnswerList<TestCaseExecutionHttpStat> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to, List<String> system, List<String> countries, List<String> environments, List<String> robotDecli) {
        return testCaseExecutionHttpStatDAO.readByCriteria(controlStatus, testcases, from, to, system, countries, environments, robotDecli);
    }

    @Override
    public Answer saveStat(TestCaseExecution tce, JSONObject har) {
        Answer res = new Answer();
        try {
            JSONObject s = har.getJSONObject("stat");
            int t1 = s.getJSONObject("total").getJSONObject("requests").getInt("nb");
            int t2 = s.getJSONObject("total").getJSONObject("size").getInt("sum");
            int t3 = s.getJSONObject("total").getJSONObject("time").getInt("totalDuration");
            int i1 = s.getJSONObject("internal").getJSONObject("requests").getInt("nb");
            int i2 = s.getJSONObject("internal").getJSONObject("size").getInt("sum");
            int i3 = s.getJSONObject("internal").getJSONObject("time").getInt("totalDuration");
            int img1 = s.getJSONObject("total").getJSONObject("type").getJSONObject("img").getInt("sizeSum");
            int img2 = s.getJSONObject("total").getJSONObject("type").getJSONObject("img").getInt("sizeMax");
            int img3 = s.getJSONObject("total").getJSONObject("type").getJSONObject("img").getInt("requests");
            int js1 = s.getJSONObject("total").getJSONObject("type").getJSONObject("js").getInt("sizeSum");
            int js2 = s.getJSONObject("total").getJSONObject("type").getJSONObject("js").getInt("sizeMax");
            int js3 = s.getJSONObject("total").getJSONObject("type").getJSONObject("js").getInt("requests");
            int css1 = s.getJSONObject("total").getJSONObject("type").getJSONObject("css").getInt("sizeSum");
            int css2 = s.getJSONObject("total").getJSONObject("type").getJSONObject("css").getInt("sizeMax");
            int css3 = s.getJSONObject("total").getJSONObject("type").getJSONObject("css").getInt("requests");
            int html1 = s.getJSONObject("total").getJSONObject("type").getJSONObject("html").getInt("sizeSum");
            int html2 = s.getJSONObject("total").getJSONObject("type").getJSONObject("html").getInt("sizeMax");
            int html3 = s.getJSONObject("total").getJSONObject("type").getJSONObject("html").getInt("requests");
            int media1 = s.getJSONObject("total").getJSONObject("type").getJSONObject("media").getInt("sizeSum");
            int media2 = s.getJSONObject("total").getJSONObject("type").getJSONObject("media").getInt("sizeMax");
            int media3 = s.getJSONObject("total").getJSONObject("type").getJSONObject("media").getInt("requests");
            int nb3p = s.getInt("nbThirdParty");
            TestCaseExecutionHttpStat object = factoryTestCaseExecutionHttpStat.create(tce.getId(), new Timestamp(0), tce.getControlStatus(), tce.getSystem(), tce.getApplication(), tce.getTest(), tce.getTestCase(), tce.getCountry(), tce.getEnvironment(), tce.getRobotDecli(),
                    t1, t2, t3, i1, i2, i3, img1, img2, img3, js1, js2, js3, css1, css2, css3, html1, html2, html3, media1, media2, media3, nb3p, tce.getCrbVersion(), s, tce.getUsrCreated(), null, tce.getUsrModif(), null);
            return this.create(object);
        } catch (JSONException ex) {
            LOG.error(OBJECT_NAME);
        }
        return res;
    }

    @Override
    public TestCaseExecutionHttpStat convert(AnswerItem<TestCaseExecutionHttpStat> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseExecutionHttpStat) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionHttpStat> convert(AnswerList<TestCaseExecutionHttpStat> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseExecutionHttpStat>) answerList.getDataList();
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

}
