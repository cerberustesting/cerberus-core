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

import java.util.ArrayList;
import java.util.List;

import org.cerberus.dao.ITestDAO;
import org.cerberus.entity.Test;
import org.cerberus.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 0.9.0
 */
@Service
public class TestService implements ITestService {

    @Autowired
    private ITestDAO testDAO;

    @Override
    public List<String> getListOfTests() {
        List<String> result = new ArrayList<String>();
        List<Test> listOfTests = this.testDAO.findAllTest();

        for (Test lot : listOfTests) {
            result.add(lot.getTest());
        }

        return result;
    }

    @Override
    public List<Test> getListOfTest() {
        return testDAO.findAllTest();
    }


}
