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

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.crud.dao.ISqlLibraryDAO;
import org.cerberus.core.crud.entity.SqlLibrary;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ISqlLibraryService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SqlLibraryService implements ISqlLibraryService {

    private static final Logger LOG = LogManager.getLogger(SqlLibraryService.class);
    
    @Autowired
    ISqlLibraryDAO sqlLibraryDao;

    @Override
    public SqlLibrary findSqlLibraryByKey(String name) throws CerberusException {
        return sqlLibraryDao.findSqlLibraryByKey(name);
    }

    @Override
    public void createSqlLibrary(SqlLibrary sqlLibrary) throws CerberusException {
        sqlLibraryDao.createSqlLibrary(sqlLibrary);
    }

    @Override
    public void updateSqlLibrary(SqlLibrary sqlLibrary) throws CerberusException {
        sqlLibraryDao.updateSqlLibrary(sqlLibrary);
    }

    @Override
    public void deleteSqlLibrary(SqlLibrary sqlLibrary) throws CerberusException {
        sqlLibraryDao.deleteSqlLibrary(sqlLibrary);
    }

    @Override
    public List<SqlLibrary> findAllSqlLibrary() {
        return sqlLibraryDao.findAllSqlLibrary();
    }

    @Override
    public List<SqlLibrary> findSqlLibraryListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return sqlLibraryDao.findSqlLibraryListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public void updateSqlLibrary(String name, String columnName, String value) throws CerberusException {
        sqlLibraryDao.updateSqlLibrary(name, columnName, value);
    }

    @Override
    public void updateSqlLibrary(String name, String type, String database, String description, String script) {

        try {
            SqlLibrary s = sqlLibraryDao.findSqlLibraryByKey(name);
            s.setType(type);
            s.setDatabase(database);
            s.setDescription(description);
            s.setScript(script);
            sqlLibraryDao.updateSqlLibrary(s);
        }catch(CerberusException e){
            LOG.warn("Unable to execute query : " + e.toString());
        }
    }

    @Override
    public Integer getNumberOfSqlLibraryPerCriteria(String searchTerm, String inds) {
        return sqlLibraryDao.getNumberOfSqlLibraryPerCriteria(searchTerm, inds);
    }

    @Override
    public List<String> findDistinctTypeOfSqlLibrary(){
        return this.sqlLibraryDao.findDistinctTypeOfSqlLibrary();
    }

    @Override
    public AnswerList<SqlLibrary> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return sqlLibraryDao.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem<SqlLibrary> readByKey(String key) {
        return sqlLibraryDao.readByKey(key);
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return sqlLibraryDao.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(SqlLibrary object) {
        return sqlLibraryDao.create(object);
    }

    @Override
    public Answer update(SqlLibrary object) {
        return sqlLibraryDao.update(object);
    }

    @Override
    public Answer delete(SqlLibrary object) {
        return sqlLibraryDao.delete(object);
    }
}
