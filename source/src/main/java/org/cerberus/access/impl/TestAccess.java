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
package org.cerberus.access.impl;

import java.util.List;
import org.cerberus.access.ITestAccess;
import org.cerberus.dao.ITestDAO;
import org.cerberus.entity.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author memiks
 */
@Service
public class TestAccess implements ITestAccess {

    @Autowired
    private ITestDAO testDAO;

    @Override
    public List<Test> findAllTest() {
        return this.testDAO.findAllTest();
    }

    @Override
    public List<Test> findTestByCriteria(Test test) {
        return this.testDAO.findTestByCriteria(test);
    }

}
