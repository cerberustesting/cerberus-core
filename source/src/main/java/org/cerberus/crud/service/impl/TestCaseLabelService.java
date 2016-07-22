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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseLabelDAO;

import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseLabelService implements ITestCaseLabelService {

    @Autowired
    private ITestCaseLabelDAO testCaseLabelDAO;

    private static final Logger LOG = Logger.getLogger("TestCaseLabelService");

    private final String OBJECT_NAME = "TestCaseLabel";

    @Override
    public AnswerItem readByKey(Integer id) {
        return testCaseLabelDAO.readByKey(id);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return testCaseLabelDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }
    
    @Override
    public AnswerList readByTestTestCase(String test, String testCase) {
        return testCaseLabelDAO.readByTestTestCase(test, testCase);
    }
    
    @Override
    public AnswerList readAll() {
        return readByCriteria( 0, 0, "sort", "asc", null, null);
    }


    @Override
    public boolean exist(Integer id) {
        AnswerItem objectAnswer = readByKey(id);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(TestCaseLabel object) {
        return testCaseLabelDAO.create(object);
    }
    
    @Override
    public Answer createList(List<TestCaseLabel> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseLabel objectToCreate : objectList) {
            ans = this.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer delete(TestCaseLabel object) {
        return testCaseLabelDAO.delete(object);
    }
    
    @Override
    public Answer deleteList(List<TestCaseLabel> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseLabel objectToDelete : objectList) {
            ans = this.delete(objectToDelete);
        }
        return ans;
    }

    @Override
    public Answer update(TestCaseLabel object) {
        return testCaseLabelDAO.update(object);
    }
    
    @Override
    public TestCaseLabel convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseLabel) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseLabel> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseLabel>) answerList.getDataList();
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
    public void compareListAndInsertDeleteElements(List<TestCaseLabel> newList, List<TestCaseLabel> oldList, boolean duplicate) throws CerberusException {
        /**
         * Iterate on (TestCaseLabel From Page - TestCaseLabel From
         * Database) If TestCaseLabel in Database has same key : 
         * remove from the list. If TestCaseLabel in database does ot exist
         * : Insert it.
         */
        List<TestCaseLabel> listToInsert = new ArrayList(newList);
        listToInsert.removeAll(oldList);
        for (TestCaseLabel newListElement : newList) {
            for (TestCaseLabel oldListElement : oldList) {
                if (newListElement.hasSameKey(oldListElement)) {
                    listToInsert.remove(newListElement);
                }
            }
        }
        this.createList(listToInsert);

        /**
         * Iterate on (TestCaseLabel From Database - TestCaseLabel
         * From Page). If TestCaseLabel in Page has same key : remove from
         * the list. Then delete the list of TestCaseLabel
         */
        if (!duplicate) {
            List<TestCaseLabel> listToDelete = new ArrayList(oldList);
            listToDelete.removeAll(newList);
            List<TestCaseLabel> listToDeleteToIterate = new ArrayList(listToDelete);

            for (TestCaseLabel objDifference : listToDeleteToIterate) {
                for (TestCaseLabel objInPage : newList) {
                    if (objDifference.hasSameKey(objInPage)) {
                        listToDelete.remove(objDifference);
                    }
                }
            }
            this.deleteList(listToDelete);
        }
    }

    
}
