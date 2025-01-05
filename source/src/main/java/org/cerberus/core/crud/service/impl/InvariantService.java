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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IInvariantDAO;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bcivel
 */
@Service
public class InvariantService implements IInvariantService {

    @Autowired
    IInvariantDAO invariantDao;
    @Autowired
    ITestCaseCountryService testCaseCountryService;
    @Autowired
    ITestCaseCountryPropertiesService testCaseCountryPropertiesService;

    private static final Logger LOG = LogManager.getLogger(InvariantService.class);

    @Override
    public AnswerItem<Invariant> readByKey(String id, String value) {
        return AnswerUtil.convertToAnswerItem(() -> invariantDao.readByKey(id, value));
    }

    @Override
    public AnswerItem<Invariant> readFirstByIdName(String id) {
        return AnswerUtil.convertToAnswerItem(() -> invariantDao.readFirstByIdName(id));
    }

    /**
     * Use readByIdName instead to avoid Answer
     *
     * @param idName
     * @return
     */
    @Override
    @Deprecated
    public AnswerList<Invariant> readByIdname(String idName) {
        return AnswerUtil.convertToAnswerList(() -> invariantDao.readByIdname(idName));
    }

    @Override
    public List<Invariant> readByIdName(String idName) throws CerberusException {
        return invariantDao.readByIdname(idName);
    }

    @Override
    public Map<String, Invariant> readByIdNameToHash(String idName) throws CerberusException {
        return this.readByIdName(idName)
                .stream()
                .collect(Collectors.toMap(Invariant::getValue, Function.identity()));
    }

    @Override
    public HashMap<String, Integer> readToHashMapGp1IntegerByIdname(String idName, Integer defaultValue) {
        HashMap<String, Integer> result = new HashMap<>();

        try {
            for (Invariant inv : readByIdName(idName)) {
                int gp1ToInt = ParameterParserUtil.parseIntegerParam(inv.getGp1(), defaultValue);
                result.put(inv.getValue(), gp1ToInt);
            }
        } catch (CerberusException ex) {
            LOG.error("Exception catched when getting invariant list.", ex);
        }
        return result;
    }

    @Override
    public HashMap<String, String> readToHashMapGp1StringByIdname(String idName, String defaultValue) {
        HashMap<String, String> result = new HashMap<>();

        try {
            for (Invariant inv : readByIdName(idName)) {
                String gp1 = ParameterParserUtil.parseStringParam(inv.getGp1(), defaultValue);
                result.put(inv.getValue(), gp1);
            }
        } catch (CerberusException ex) {
            LOG.error("Exception catched when getting invariant list.", ex);
        }
        return result;
    }

    @Override
    public List<Invariant> convertCountryPropertiesToCountryInvariants(HashMap<String, TestCaseCountry> testCaseCountries, HashMap<String, Invariant> countryInvariants) throws CerberusException {

        List<Invariant> countryInvariantsToReturn = new ArrayList<>();
        testCaseCountries.forEach((key, value) -> countryInvariantsToReturn.add(countryInvariants.get(key)));
        return countryInvariantsToReturn;
    }

    @Override
    public List<Invariant> convertCountryPropertiesToCountryInvariants(List<String> countries, HashMap<String, Invariant> countryInvariants) throws CerberusException {
        List<Invariant> countryInvariantsToReturn = new ArrayList<>();
        for (String country : countries) {
            countryInvariantsToReturn.add(countryInvariants.get(country));
        }
        return countryInvariantsToReturn;
    }

    @Override
    public AnswerList<Invariant> readByIdnameGp1(String idName, String gp) {
        return invariantDao.readByIdnameByGp1(idName, gp);
    }

    @Override
    public AnswerList<Invariant> readByIdnameNotGp1(String idName, String gp) {
        return invariantDao.readByIdnameByNotGp1(idName, gp);
    }

    @Override
    public AnswerList<Invariant> readCountryListEnvironmentLastChanges(String system, Integer nbDays) {
        return invariantDao.readCountryListEnvironmentLastChanges(system, nbDays);
    }

    @Override
    public AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<Invariant> answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<Invariant> answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByPublicByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<String> answer = invariantDao.readDistinctValuesByCriteria(column, dir, searchTerm, individualSearch, searchSQL, columnName);

        return answer;
    }

    @Override
    public AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        // We first get the list of all Private invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<Invariant> answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Private invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<Invariant> answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList<String> readDistinctValuesByPrivateByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList<String> answer = invariantDao.readDistinctValuesByCriteria(column, dir, searchTerm, individualSearch, searchSQL, columnName);

        return answer;
    }

    @Override
    public AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        //gets all invariants
        return invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, "");//no filter public or private is sent
    }

    @Override
    public boolean isInvariantExist(String idName, String value) {
        AnswerItem objectAnswer = readByKey(idName, value);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public boolean isInvariantPublic(Invariant object) {
        AnswerItem objectAnswer = readByKey("INVARIANTPUBLIC", object.getIdName());
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(Invariant invariant) {
        return invariantDao.create(invariant);
    }

    @Override
    public Answer delete(Invariant invariant) {
        return invariantDao.delete(invariant);
    }

    @Override
    public Answer update(String idname, String value, Invariant invariant) {
        return invariantDao.update(idname, value, invariant);
    }

    @Override
    public String getPublicPrivateFilter(String filter) {
        String searchSQL = " 1=0 ";

        List<Invariant> invPrivate;
        try {
            invPrivate = this.readByIdName(filter);

            List<String> idnameList = null;
            idnameList = new ArrayList<>();
            for (Invariant toto : invPrivate) {
                idnameList.add(toto.getValue());
            }
            searchSQL = SqlUtil.createWhereInClause("idname", idnameList, true);
        } catch (CerberusException ex) {
            LOG.warn("JSON exception when getting Country List.", ex);
        }

        return searchSQL;
    }

    @Override
    public boolean hasPermissionsRead(Invariant invariant, HttpServletRequest request) {
        // Access right calculation.
        return true;
    }

    @Override
    public boolean hasPermissionsUpdate(Invariant invariant, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("Administrator") && isInvariantPublic(invariant));
    }

    @Override
    public boolean hasPermissionsCreate(Invariant invariant, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("Administrator") && isInvariantPublic(invariant));
    }

    @Override
    public boolean hasPermissionsDelete(Invariant invariant, HttpServletRequest request) {
        // Access right calculation.
        return (request.isUserInRole("Administrator") && isInvariantPublic(invariant));
    }

    @Override
    public Invariant convert(AnswerItem<Invariant> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Invariant> convert(AnswerList<Invariant> answerList) throws CerberusException {
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
    public Invariant findCountryInvariantFromCountries(String country, List<Invariant> countries) {
        return countries
                .stream()
                .filter(invariant -> country.equals(invariant.getValue()))
                .findFirst().orElse(null);
    }

    @Override
    public Invariant findEnvironmentInvariantFromEnvironments(String environment, List<Invariant> environments) {
        return environments
                .stream()
                .filter(invariant -> environment.equals(invariant.getValue()))
                .findFirst().orElse(null);
    }

    @Override
    public Invariant findPriorityInvariantFromPriorities(int priority, List<Invariant> priorities) {
        return priorities
                .stream()
                .filter(invariant -> String.valueOf(priority).equals(invariant.getValue()))
                .findFirst().orElse(null);
    }
}
