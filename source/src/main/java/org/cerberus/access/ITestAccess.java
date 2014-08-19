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
package org.cerberus.access;

import java.util.List;
import org.cerberus.entity.Test;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ITestAccess {

    /**
     *
     * @return All tests found in database
     */
    List<Test> findAllTest();

    /**
     *
     * @param test use to filter search in database
     * @return List of Tests found in database
     */
    List<Test> findTestByCriteria(Test test);

    /**
     *
     * @param test need to be created in database
     * @return TRUE if test is inserted in database
     * @throws org.cerberus.exception.CerberusException
     */
    boolean createTest(Test test) throws CerberusException;

    /**
     *
     * @param test need to be deleted in database
     * @return TRUE if test is deleted in database
     */
    boolean deleteTest(Test test);
    
    /**
     * 
     * @param Test Key of the test table
     * @return Test object
     */
    Test findTestByKey(String Test);
}
