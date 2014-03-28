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
package org.cerberus.service;

import java.util.List;
import org.cerberus.entity.Test;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
public interface ITestService {


    /**
     * @return List of Tests name
     */
    List<String> getListOfTests();

    /**
     * @return List of Test
     */
    List<Test> getListOfTest();

    /**
     *
     * @param test need to be created in database
     * @return TRUE if test have been inserted successfully
     */
    boolean createTest(Test test);

    /**
     *
     * @param test need to be deleted in database
     * @return TRUE if test have been deleted successfully
     */
    boolean deleteTest(Test test);
    
    /**
     * 
     * @param test Key of the table Test
     * @return Test object
     */
    Test findTestByKey(String test);
}
