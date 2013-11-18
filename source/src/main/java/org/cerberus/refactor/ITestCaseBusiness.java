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
package org.cerberus.refactor;

import org.cerberus.entity.TestCase;

/**
 * @author Tiago Bernardes <tbernardes@redoute.pt>
 * @version 1.0
 * @since 2012-08-20
 */
public interface ITestCaseBusiness {

    int UPDATE_INFORMATION = 0;
    int UPDATE_PROPERTIES = 1;
    int UPDATE_ACTIONS = 2;
    int UPDATE_CONTROLS = 3;
    int UPDATE_ALL = 4;

    String createTestCase(TestCase tc);

    TestCase getTestCase(String test, String testcase);

    String removeTestCase(String test, String testcase);

    String updateTestCase(TestCase tc, int type);
}
