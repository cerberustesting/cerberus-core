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

import org.cerberus.core.crud.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerUtil;

/**
 * @author bcivel
 */
@Service
public class CountryEnvironmentParametersService implements ICountryEnvironmentParametersService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(CountryEnvironmentParametersService.class);

    @Autowired
    private ICountryEnvironmentParametersDAO countryEnvironmentParametersDao;
    @Autowired
    private ICountryEnvParamService countryEnvParamService;
    @Autowired
    private IFactoryCountryEnvParam factoryCountryEnvParam;
    @Autowired
    private ILogEventService logEventService;

    @Override
    public AnswerItem<CountryEnvironmentParameters> readByKey(String system, String country, String environment, String application) {
        return countryEnvironmentParametersDao.readByKey(system, country, environment, application);
    }

    @Override
    public List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException {
        return countryEnvironmentParametersDao.findCountryEnvironmentParametersByCriteria(countryEnvironmentParameter);
    }

    @Override
    public AnswerList<CountryEnvironmentParameters> readByVarious(String system, String country, String environment, String application) {
        return countryEnvironmentParametersDao.readByVariousByCriteria(system, country, environment, application, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList<CountryEnvironmentParameters> readDependenciesByVarious(String system, String country, String environment) {
        return countryEnvironmentParametersDao.readDependenciesByVarious(system, country, environment);
    }

    @Override
    public AnswerList<CountryEnvironmentParameters> readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvironmentParametersDao.readByVariousByCriteria(system, country, environment, application, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Answer update(CountryEnvironmentParameters object) {
        Answer answer = countryEnvironmentParametersDao.update(object);
        return answer;
    }

    @Override
    public Answer delete(CountryEnvironmentParameters object) {
        Answer answer = countryEnvironmentParametersDao.delete(object);
        return answer;
    }

    @Override
    public Answer create(CountryEnvironmentParameters object) {
        Answer answer;
        if (!countryEnvParamService.exist(object.getSystem(), object.getCountry(), object.getEnvironment())) {
            CountryEnvParam env = factoryCountryEnvParam.create(object.getSystem(),
                    object.getCountry(), object.getEnvironment(), "", "", "", "", "", "", "STD", "", "", true, false, "00:00:00", "00:00:00", "");
            answer = countryEnvParamService.create(env);
            if (!answer.isCodeStringEquals("OK")) {
                return answer;
            } else {
                logEventService.createForPrivateCalls("", "CREATE", LogEvent.STATUS_INFO , "Create CountryEnvParam : ['" + object.getSystem() + "'|'" + object.getCountry() + "'|'" + object.getEnvironment() + "']");
            }
        }
        answer = countryEnvironmentParametersDao.create(object);
        return answer;
    }

    @Override
    public Answer createList(List<CountryEnvironmentParameters> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvironmentParameters objectToCreate : objectList) {
            ans = create(objectToCreate);
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

        List<CountryEnvironmentParameters> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(system, country, environement, null));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<CountryEnvironmentParameters> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvironmentParameters> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (CountryEnvironmentParameters objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvironmentParameters objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<CountryEnvironmentParameters> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvironmentParameters> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (CountryEnvironmentParameters tcsDifference : listToDeleteToIterate) {
            for (CountryEnvironmentParameters tcsInPage : newList) {
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
    public Answer compareListAndUpdateInsertDeleteElements(String system, String application, List<CountryEnvironmentParameters> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CountryEnvironmentParameters> oldList = new ArrayList<>();
        try {
            oldList = this.convert(this.readByVarious(system, null, null, application));
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }

        /**
         * Update and Create all objects database Objects from newList
         */
        List<CountryEnvironmentParameters> listToUpdateOrInsert = new ArrayList<>(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvironmentParameters> listToUpdateOrInsertToIterate = new ArrayList<>(listToUpdateOrInsert);

        for (CountryEnvironmentParameters objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvironmentParameters objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    // If new value has a SECRET String, we replace it by the initial Password.
                    if (objectDifference.getIp().contains(StringUtil.SECRET_STRING)) {
                        objectDifference.setIp(objectDifference.getIp().replace(StringUtil.SECRET_STRING, StringUtil.getPasswordFromAnyUrl(objectInDatabase.getIp())));
                    }
                    if (StringUtil.SECRET_STRING.equals(objectDifference.getSecret1())) {
                        objectDifference.setSecret1(objectInDatabase.getSecret1());
                    }
                    if (StringUtil.SECRET_STRING.equals(objectDifference.getSecret2())) {
                        objectDifference.setSecret2(objectInDatabase.getSecret2());
                    }
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }

        /**
         * Delete all objects database Objects that do not exist from newList
         */
        List<CountryEnvironmentParameters> listToDelete = new ArrayList<>(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvironmentParameters> listToDeleteToIterate = new ArrayList<>(listToDelete);

        for (CountryEnvironmentParameters tcsDifference : listToDeleteToIterate) {
            for (CountryEnvironmentParameters tcsInPage : newList) {
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
    public CountryEnvironmentParameters convert(AnswerItem<CountryEnvironmentParameters> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvironmentParameters> convert(AnswerList<CountryEnvironmentParameters> answerList) throws CerberusException {
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
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR_WITH_DETAIL).resolveDescription("DETAIL", answer.getMessageDescription()));
    }
}
