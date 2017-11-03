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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IInvariantDAO;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class InvariantService implements IInvariantService {

    @Autowired
    IInvariantDAO invariantDao;

    private static final Logger LOG = LogManager.getLogger(InvariantService.class);

    @Override
    public AnswerItem readByKey(String id, String value) {
        return invariantDao.readByKey(id, value);
    }

    @Override
    public AnswerList readByIdname(String idName) {
        return invariantDao.readByIdname(idName);
    }

    @Override
    public HashMap<String, Integer> readToHashMapGp1IntegerByIdname(String idName, Integer defaultValue) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();

        AnswerList answer = readByIdname(idName); //TODO: handle if the response does not turn ok
        for (Invariant inv : (List<Invariant>) answer.getDataList()) {
            int gp1ToInt = ParameterParserUtil.parseIntegerParam(inv.getGp1(), defaultValue);
            result.put(inv.getValue(), gp1ToInt);
        }
        return result;
    }

    @Override
    public HashMap<String, String> readToHashMapGp1StringByIdname(String idName, String defaultValue) {
        HashMap<String, String> result = new HashMap<String, String>();

        AnswerList answer = readByIdname(idName); //TODO: handle if the response does not turn ok
        for (Invariant inv : (List<Invariant>) answer.getDataList()) {
            String gp1 = ParameterParserUtil.parseStringParam(inv.getGp1(), defaultValue);
            result.put(inv.getValue(), gp1);
        }
        return result;
    }

    @Override
    public AnswerList readByIdnameGp1(String idName, String gp) {
        return invariantDao.readByIdnameByGp1(idName, gp);
    }

    @Override
    public AnswerList readCountryListEnvironmentLastChanges(String system, Integer nbDays) {
        return invariantDao.readCountryListEnvironmentLastChanges(system, nbDays);
    }

    @Override
    public AnswerList readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList readDistinctValuesByPublicByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readDistinctValuesByCriteria(column, dir, searchTerm, individualSearch, searchSQL, columnName);

        return answer;
    }

    @Override
    public AnswerList readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        // We first get the list of all Private invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Private invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);

        return answer;
    }

    @Override
    public AnswerList readDistinctValuesByPrivateByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.

        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readDistinctValuesByCriteria(column, dir, searchTerm, individualSearch, searchSQL, columnName);

        return answer;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
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

        AnswerList answer = this.readByIdname(filter);
        List<Invariant> invPrivate = answer.getDataList();

        List<String> idnameList = null;
        idnameList = new ArrayList<String>();
        for (Invariant toto : invPrivate) {
            idnameList.add(toto.getValue());
        }
        searchSQL = SqlUtil.createWhereInClause("idname", idnameList, true);

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
    public Invariant convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (Invariant) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Invariant> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<Invariant>) answerList.getDataList();
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
