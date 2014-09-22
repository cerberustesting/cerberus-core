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
package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.ISqlLibraryDAO;
import org.cerberus.entity.SqlLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ISqlLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SqlLibraryService implements ISqlLibraryService {

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
    public Integer getNumberOfSqlLibraryPerCriteria(String searchTerm, String inds) {
        return sqlLibraryDao.getNumberOfSqlLibraryPerCriteria(searchTerm, inds);
    }

    @Override
    public List<String> findDistinctTypeOfSqlLibrary(){
        return this.sqlLibraryDao.findDistinctTypeOfSqlLibrary();
    }
}
