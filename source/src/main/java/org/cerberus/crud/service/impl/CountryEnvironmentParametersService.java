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

import java.util.ArrayList;
import java.util.List;

import org.cerberus.crud.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.exception.CerberusException;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.observe.ObservableEngine;
import org.cerberus.util.observe.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.util.answer.AnswerUtil;

/**
 * @author bcivel
 */
@Service
public class CountryEnvironmentParametersService implements ICountryEnvironmentParametersService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CountryEnvironmentParametersService.class);

    @Autowired
    private ICountryEnvironmentParametersDAO countryEnvironmentParametersDao;

    @Autowired
    private ObservableEngine<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> observableEngine;

    @Override
    public AnswerItem<CountryEnvironmentParameters> readByKey(String system, String country, String environment, String application) {
        return countryEnvironmentParametersDao.readByKey(system, country, environment, application);
    }

    @Override
    public AnswerItem<Integer> readPoolSizeByKey(String system, String country, String environment, String application) {
        AnswerItem<Integer> poolSize = new AnswerItem<>();

        AnswerItem<CountryEnvironmentParameters> countryEnvironmentParameters = readByKey(system, country, environment, application);
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(countryEnvironmentParameters.getResultMessage().getSource())) {
            poolSize.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
            return poolSize;
        }
        poolSize.setItem(countryEnvironmentParameters.getItem().getPoolSize());
        poolSize.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return poolSize;
    }

    public AnswerItem readByKey(CountryEnvironmentParameters.Key key) {
        return readByKey(key.getSystem(), key.getCountry(), key.getEnvironment(), key.getApplication());
    }

    @Override
    public List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException {
        return countryEnvironmentParametersDao.findCountryEnvironmentParametersByCriteria(countryEnvironmentParameter);
    }

    @Override
    public AnswerList readByVarious(String system, String country, String environment, String application) {
        return countryEnvironmentParametersDao.readByVariousByCriteria(system, country, environment, application, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvironmentParametersDao.readByVariousByCriteria(system, country, environment, application, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer update(CountryEnvironmentParameters object) {
        Answer answer = countryEnvironmentParametersDao.update(object);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {
            fireUpdate(CountryEnvironmentParameters.Key.fromCountryEnvironmentParameters(object), object);
        }
        return answer;
    }

    @Override
    public Answer delete(CountryEnvironmentParameters object) {
        Answer answer = countryEnvironmentParametersDao.delete(object);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {
            fireDelete(CountryEnvironmentParameters.Key.fromCountryEnvironmentParameters(object), object);
        }
        return answer;
    }

    @Override
    public Answer create(CountryEnvironmentParameters object) {
        Answer answer = countryEnvironmentParametersDao.create(object);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {
            fireCreate(CountryEnvironmentParameters.Key.fromCountryEnvironmentParameters(object), object);
        }
        return answer;
    }

    @Override
    public Answer createList(List<CountryEnvironmentParameters> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvironmentParameters objectToCreate : objectList) {
            ans = countryEnvironmentParametersDao.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<CountryEnvironmentParameters> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvironmentParameters objectToCreate : objectList) {
            ans = countryEnvironmentParametersDao.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvironmentParameters> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CountryEnvironmentParameters> oldList = new ArrayList();
        try {
            oldList = this.convert(this.readByVarious(system, country, environement, null));
        } catch (CerberusException ex) {
            LOG.error(ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<CountryEnvironmentParameters> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvironmentParameters> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (CountryEnvironmentParameters objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvironmentParameters objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<CountryEnvironmentParameters> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvironmentParameters> listToDeleteToIterate = new ArrayList(listToDelete);

        for (CountryEnvironmentParameters tcsDifference : listToDeleteToIterate) {
            for (CountryEnvironmentParameters tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }
        return finalAnswer;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String system, String application, List<CountryEnvironmentParameters> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CountryEnvironmentParameters> oldList = new ArrayList();
        try {
            oldList = this.convert(this.readByVarious(system, null, null, application));
        } catch (CerberusException ex) {
            LOG.error(ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<CountryEnvironmentParameters> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvironmentParameters> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (CountryEnvironmentParameters objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvironmentParameters objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<CountryEnvironmentParameters> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvironmentParameters> listToDeleteToIterate = new ArrayList(listToDelete);

        for (CountryEnvironmentParameters tcsDifference : listToDeleteToIterate) {
            for (CountryEnvironmentParameters tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }
        return finalAnswer;
    }

    @Override
    public CountryEnvironmentParameters convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (CountryEnvironmentParameters) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvironmentParameters> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<CountryEnvironmentParameters>) answerList.getDataList();
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
    public int defaultPoolSize() {
        return countryEnvironmentParametersDao.getDefaultPoolSize();
    }

    @Override
    public boolean register(Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> observer) {
        return observableEngine.register(observer);
    }

    @Override
    public boolean register(CountryEnvironmentParameters.Key key, Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> observer) {
        return observableEngine.register(key, observer);
    }

    @Override
    public boolean unregister(CountryEnvironmentParameters.Key key, Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> observer) {
        return observableEngine.unregister(key, observer);
    }

    @Override
    public boolean unregister(Observer<CountryEnvironmentParameters.Key, CountryEnvironmentParameters> observer) {
        return observableEngine.unregister(observer);
    }

    @Override
    public void fireCreate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        observableEngine.fireCreate(key, countryEnvironmentParameters);
    }

    @Override
    public void fireUpdate(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        observableEngine.fireUpdate(key, countryEnvironmentParameters);
    }

    @Override
    public void fireDelete(CountryEnvironmentParameters.Key key, CountryEnvironmentParameters countryEnvironmentParameters) {
        observableEngine.fireDelete(key, countryEnvironmentParameters);
    }
}
