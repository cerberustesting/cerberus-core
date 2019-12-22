/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.dao;

import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo17
 * @author FNogueira
 */
public interface ITestDataLibDataDAO {

    /**
     *
     * @param testDataLibID
     * @param subData
     * @return
     */
    AnswerItem<TestDataLibData> readByKey(Integer testDataLibID, String subData);

    /**
     *
     * @param testDataLibDataID
     * @return
     */
    AnswerItem<TestDataLibData> readByKeyTech(Integer testDataLibDataID);

    /**
     *
     * @param testDataLibID resultSet
     * @param columnEmpty
     * @param parsingAnswerEmpty
     * @param columnPositionEmpty
     * @return
     */
    AnswerList<TestDataLibData> readByVarious(Integer testDataLibID, String columnEmpty, String parsingAnswerEmpty, String columnPositionEmpty);

    /**
     *
     * @return All TestData
     */
    AnswerList readAll();

    /**
     *
     * @param start
     * @param amount
     * @param colName
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<TestDataLibData> readByCriteria(int start, int amount, String colName, String dir, String searchTerm, String individualSearch);

    /**
     * Finds all subdata entries (testdatalibdata) that are associated with an
     * entry name (testdatalib).
     *
     * @param testDataLibName - entry name used to filter the subdata entries
     * @return Answer indicating the status of the operation
     */
    AnswerList<TestDataLibData> readByName(String testDataLibName);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer create(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer update(TestDataLibData testDataLibData);

    /**
     *
     * @param testDataLibData
     * @return
     */
    Answer delete(TestDataLibData testDataLibData);

}
