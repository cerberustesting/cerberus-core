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

import org.cerberus.core.crud.dao.ITestCaseContentSearchDAO;
import org.cerberus.core.crud.entity.TestCaseContentMatch;
import org.cerberus.core.crud.service.ITestCaseContentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class TestCaseContentSearchService implements ITestCaseContentSearchService {

    @Autowired
    private ITestCaseContentSearchDAO testCaseContentSearchDAO;

    @Override
    public List<TestCaseContentMatch> search(String search, List<String> systems, String testFolder, String testcase,
                                             List<TestCaseContentMatch.Scope> scopes, int maxResults) {
        return testCaseContentSearchDAO.search(search, systems, testFolder, testcase, scopes, maxResults);
    }
}
