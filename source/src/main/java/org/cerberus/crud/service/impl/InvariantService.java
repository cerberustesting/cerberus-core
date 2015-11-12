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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.crud.dao.IInvariantDAO;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class InvariantService implements IInvariantService {

    @Autowired
    IInvariantDAO invariantDao;

    @Override
    public Invariant findInvariantByIdValue(String idName, String value) throws CerberusException {
        return invariantDao.readByKey(idName, value);
    }

    @Override
    public Invariant findInvariantByIdSort(String idName, Integer sort) throws CerberusException {
        return invariantDao.readByIdnameBySort(idName, sort);
    }

    @Override
    public AnswerList readByIdname(String idName) {
        return invariantDao.readByIdname(idName);
    }

    @Override
    public AnswerList findInvariantByIdGp1(String idName, String gp) {
        return invariantDao.readByIdnameByGp1(idName, gp);
    }

    @Override
    public List<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Public invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        // Then, we build the list of invariant entry based on the filter.
        
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);
        
        return answer.getDataList();
    }

    @Override
    public List<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        // We first get the list of all Private invariant from the invariant table.
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        // Then, we build the list of invariant entry based on the filter.
        //TODO this method should return a AnswerList, after complete refactoring this method should be changed
        AnswerList answer = invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, searchSQL);
        
        return answer.getDataList();                
    }
    
    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        //gets all invariants
        return invariantDao.readByCriteria(start, amount, column, dir, searchTerm, individualSearch, "");//no filter public or private is sent        
    }

    @Override
    public Integer getNumberOfPublicInvariant(String searchTerm) {
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPUBLIC");
        try {
            return invariantDao.getNumberOfInvariant(searchTerm, searchSQL);
        } catch (CerberusException ex) {
            Logger.getLogger(InvariantService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public Integer getNumberOfPrivateInvariant(String searchTerm) {
        String searchSQL = this.getPublicPrivateFilter("INVARIANTPRIVATE");
        try {
            return invariantDao.getNumberOfInvariant(searchTerm, searchSQL);
        } catch (CerberusException ex) {
            Logger.getLogger(InvariantService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public boolean isInvariantExist(String idName, String value) {
        try {
            findInvariantByIdValue(idName, value);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public void createInvariant(Invariant invariant) throws CerberusException {
        invariantDao.create(invariant);
    }

    @Override
    public void deleteInvariant(Invariant invariant) throws CerberusException {
        invariantDao.delete(invariant);
    }

    @Override
    public void updateInvariant(Invariant invariant) throws CerberusException {
        invariantDao.update(invariant);
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
}
