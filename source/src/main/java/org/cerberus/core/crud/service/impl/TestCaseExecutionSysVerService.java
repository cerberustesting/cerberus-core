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
import org.cerberus.core.crud.dao.ITestCaseExecutionSysVerDAO;
import org.cerberus.core.crud.entity.TestCaseExecutionSysVer;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.service.ITestCaseExecutionSysVerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionSysVerService implements ITestCaseExecutionSysVerService {

    @Autowired
    ITestCaseExecutionSysVerDAO testCaseExecutionSysVerDao;

    @Override
    public void insertTestCaseExecutionSysVer(TestCaseExecutionSysVer testCaseExecutionSysVer) throws CerberusException{
        this.testCaseExecutionSysVerDao.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);
    }

    @Override
    public List<TestCaseExecutionSysVer> findTestCaseExecutionSysVerById(long id) {
        return testCaseExecutionSysVerDao.findTestCaseExecutionSysVerById(id);
    }
}
