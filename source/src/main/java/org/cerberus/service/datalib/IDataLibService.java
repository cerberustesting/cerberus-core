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
package org.cerberus.service.datalib;

import java.util.HashMap;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IDataLibService {

    /**
     *
     * @param lib
     * @param testCaseCountryProperty
     * @param tCExecution
     * @return
     */
    AnswerItem<HashMap<String, String>> getFromDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution tCExecution);

    /**
     * This method return the first ObjectData from DataSet
     *
     * @param dataSet
     * @return The first item from dataObjectList
     */
    AnswerItem<HashMap<String, String>> filterWithNatureSTATIC(AnswerList<HashMap<String, String>> dataSet);

    /**
     * This method return an ObjectData from DataSet picked randomly
     *
     * @param dataSet
     * @return An item from dataObjectList choosen randomly
     */
    AnswerItem<HashMap<String, String>> filterWithNatureRANDOM(AnswerList<HashMap<String, String>> dataSet);

    /**
     * This method return an ObjectData from DataSet picked randomly after
     * excluding ObjectData already used in previous execution
     *
     * @param dataSet
     * @param tCExecution : TestCaseExecution
     * @param testCaseProperties : TestCaseCountryProperties
     * @return An item from dataObjectList choosen randomly
     */
    AnswerItem<HashMap<String, String>> filterWithNatureRANDOMNEW(AnswerList<HashMap<String, String>> dataSet, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties);

    /**
     * This method return an ObjectData from dataObjectList that is not used in
     * another execution
     *
     * @param dataSet
     * @param tCExecution : TestCaseExecution
     * @param testCaseProperties
     * @return An item from dataObjectList excluding the one used in other
     * execution choosen randomly
     */
    AnswerItem<HashMap<String, String>> filterWithNatureNOTINUSE(AnswerList<HashMap<String, String>> dataSet, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties);

}
