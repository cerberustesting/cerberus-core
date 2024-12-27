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
package org.cerberus.core.service.datalib;

import java.util.HashMap;
import java.util.List;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author bcivel
 */
public interface IDataLibService {

    /**
     *
     * @param lib
     * @param testCaseCountryProperty
     * @param execution
     * @param testCaseExecutionData
     * @return
     */
    AnswerList<HashMap<String, String>> getFromDataLib(TestDataLib lib, TestCaseCountryProperties testCaseCountryProperty, TestCaseExecution execution, TestCaseExecutionData testCaseExecutionData);

    /**
     * This method return the first ObjectData from DataSet
     *
     * @param dataSet
     * @param outPutDimention
     * @return The first item from dataObjectList
     */
    AnswerList<HashMap<String, String>> filterWithNatureSTATIC(AnswerList<HashMap<String, String>> dataSet, int outPutDimention);

    /**
     * This method return an ObjectData from DataSet picked randomly
     *
     * @param dataSet
     * @param outPutDimention Define the nb of row that must be return in the
     * result.
     * @return A list of items from dataObjectList taken from the top.
     */
    AnswerList<HashMap<String, String>> filterWithNatureRANDOM(AnswerList<HashMap<String, String>> dataSet, int outPutDimention);

    /**
     * This method return an ObjectData from DataSet picked randomly after
     * excluding ObjectData already used in previous execution
     *
     * @param dataSet
     * @param tCExecution : TestCaseExecution
     * @param testCaseProperties : TestCaseCountryProperties
     * @param outPutDimention Define the nb of row that must be return in the
     * result.
     * @return A list of items from dataObjectList excluding the ones already
     * used in previous executions. (choosen randomly is more than enough)
     */
    AnswerList<HashMap<String, String>> filterWithNatureRANDOMNEW(AnswerList<HashMap<String, String>> dataSet, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties, int outPutDimention);

    /**
     * This method return an ObjectData from dataObjectList that is not used in
     * another execution
     *
     * @param dataSet
     * @param tCExecution : TestCaseExecution
     * @param testCaseProperties
     * @param outPutDimention Define the nb of row that must be return in the
     * result.
     * @return A list of item from dataObjectList excluding the one used in
     * other execution. (choosen randomly is more than enough)
     */
    AnswerList<HashMap<String, String>> filterWithNatureNOTINUSE(AnswerList<HashMap<String, String>> dataSet, TestCaseExecution tCExecution, TestCaseCountryProperties testCaseProperties, int outPutDimention);

    /**
     *
     * @param object
     * @return
     * @throws JSONException
     */
    public JSONArray convertToJSONObject(List<HashMap<String, String>> object) throws JSONException;    
}
