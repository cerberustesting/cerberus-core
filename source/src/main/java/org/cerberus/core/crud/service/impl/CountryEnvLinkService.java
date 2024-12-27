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

import java.util.ArrayList;
import java.util.List;

import org.cerberus.core.crud.dao.ICountryEnvLinkDAO;
import org.cerberus.core.crud.entity.CountryEnvLink;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ICountryEnvLinkService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvLinkService implements ICountryEnvLinkService {

    @Autowired
    ICountryEnvLinkDAO countryEnvLinkDao;

    private final String OBJECT_NAME = "CountryEnvLink";

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CountryEnvLinkService.class);

    @Override
    public AnswerList<CountryEnvLink> readByVarious(String system, String country, String environment) {
        return countryEnvLinkDao.readByVariousByCriteria(system, country, environment, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList<CountryEnvLink> readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvLinkDao.readByVariousByCriteria(system, country, environment, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer create(CountryEnvLink object) {
        return countryEnvLinkDao.create(object);
    }

    @Override
    public Answer delete(CountryEnvLink object) {
        return countryEnvLinkDao.delete(object);
    }

    @Override
    public Answer update(CountryEnvLink object) {
        return countryEnvLinkDao.update(object);
    }

    @Override
    public Answer createList(List<CountryEnvLink> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvLink objectToCreate : objectList) {
            ans = countryEnvLinkDao.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<CountryEnvLink> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvLink objectToCreate : objectList) {
            ans = countryEnvLinkDao.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvLink> newList) {
        Answer ans = new Answer();

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CountryEnvLink> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(system, country, environement));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<CountryEnvLink> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvLink> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (CountryEnvLink objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvLink objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        List<CountryEnvLink> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvLink> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (CountryEnvLink tcsDifference : listToDeleteToIterate) {
            for (CountryEnvLink tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
        }
        return finalAnswer;
    }

    @Override
    public CountryEnvLink convert(AnswerItem<CountryEnvLink> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvLink> convert(AnswerList<CountryEnvLink> answerList) throws CerberusException {
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

}
