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
import org.cerberus.crud.dao.ICountryEnvironmentDatabaseDAO;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class CountryEnvironmentDatabaseService implements ICountryEnvironmentDatabaseService {

    @Autowired
    private ICountryEnvironmentDatabaseDAO countryEnvironmentDatabaseDao;

    private final String OBJECT_NAME = "CountryEnvironmentDatabase";

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CountryEnvironmentDatabaseService.class);

    @Override
    public AnswerItem readByKey(String system, String country, String environment, String database) {
        return countryEnvironmentDatabaseDao.readByKey(system, country, environment, database);
    }

    @Override
    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException {
        return countryEnvironmentDatabaseDao.findCountryEnvironmentDatabaseByKey(system, country, environment, database);
    }

    @Override
    public List<CountryEnvironmentDatabase> findAll(String system) throws CerberusException {
        return countryEnvironmentDatabaseDao.findAll(system);
    }

    @Override
    public List<CountryEnvironmentDatabase> findListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnswerList readByVarious(String system, String country, String environment) {
        return countryEnvironmentDatabaseDao.readByVariousByCriteria(system, country, environment, 0, 0, null, null, null, null);
    }

    @Override
    public AnswerList readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return countryEnvironmentDatabaseDao.readByVariousByCriteria(system, country, environment, start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Integer count(String searchTerm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CountryEnvironmentDatabase> findListByCriteria(String system, String country, String environment) throws CerberusException {
        return countryEnvironmentDatabaseDao.findListByCriteria(system, country, environment);
    }

    @Override
    public void update_deprecated(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.update_deprecated(ced);
    }

    @Override
    public void delete_deprecated(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.delete_deprecated(ced);
    }

    @Override
    public void create_deprecated(CountryEnvironmentDatabase ced) throws CerberusException {
        countryEnvironmentDatabaseDao.create_deprecated(ced);
    }

    @Override
    public Answer create(CountryEnvironmentDatabase object) {
        return countryEnvironmentDatabaseDao.create(object);
    }

    @Override
    public Answer delete(CountryEnvironmentDatabase object) {
        return countryEnvironmentDatabaseDao.delete(object);
    }

    @Override
    public Answer update(CountryEnvironmentDatabase object) {
        return countryEnvironmentDatabaseDao.update(object);
    }

    @Override
    public Answer createList(List<CountryEnvironmentDatabase> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvironmentDatabase objectToCreate : objectList) {
            ans = countryEnvironmentDatabaseDao.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer deleteList(List<CountryEnvironmentDatabase> objectList) {
        Answer ans = new Answer(null);
        for (CountryEnvironmentDatabase objectToCreate : objectList) {
            ans = countryEnvironmentDatabaseDao.delete(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvironmentDatabase> newList) {
        Answer ans = new Answer(null);

        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        List<CountryEnvironmentDatabase> oldList = new ArrayList();
        try {
            oldList = this.convert(this.readByVarious(system, country, environement));
        } catch (CerberusException ex) {
            LOG.error(ex);
        }

        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<CountryEnvironmentDatabase> listToUpdateOrInsert = new ArrayList(newList);
        listToUpdateOrInsert.removeAll(oldList);
        List<CountryEnvironmentDatabase> listToUpdateOrInsertToIterate = new ArrayList(listToUpdateOrInsert);

        for (CountryEnvironmentDatabase objectDifference : listToUpdateOrInsertToIterate) {
            for (CountryEnvironmentDatabase objectInDatabase : oldList) {
                if (objectDifference.hasSameKey(objectInDatabase)) {
                    ans = this.update(objectDifference);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                    listToUpdateOrInsert.remove(objectDifference);
                }
            }
        }
        if (!listToUpdateOrInsert.isEmpty()) {
            ans = this.createList(listToUpdateOrInsert);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        List<CountryEnvironmentDatabase> listToDelete = new ArrayList(oldList);
        listToDelete.removeAll(newList);
        List<CountryEnvironmentDatabase> listToDeleteToIterate = new ArrayList(listToDelete);

        for (CountryEnvironmentDatabase tcsDifference : listToDeleteToIterate) {
            for (CountryEnvironmentDatabase tcsInPage : newList) {
                if (tcsDifference.hasSameKey(tcsInPage)) {
                    listToDelete.remove(tcsDifference);
                }
            }
        }
        if (!listToDelete.isEmpty()) {
            ans = this.deleteList(listToDelete);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
        }
        return finalAnswer;
    }

    @Override
    public boolean exist(String system, String country, String environment, String database) {
        try {
            convert(readByKey(system, country, environment, database));
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public CountryEnvironmentDatabase convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (CountryEnvironmentDatabase) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<CountryEnvironmentDatabase> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<CountryEnvironmentDatabase>) answerList.getDataList();
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
