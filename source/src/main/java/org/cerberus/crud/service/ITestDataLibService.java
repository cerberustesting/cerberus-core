/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.crud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
public interface ITestDataLibService {

    /**
     *
     * @param name
     * @param system
     * @param environment
     * @param country
     * @param type
     * @return
     */
    AnswerItem readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country, String type);

    /**
     *
     * @param name
     * @param system
     * @param environment
     * @param country
     * @return
     */
    AnswerItem readByKey(String name, String system, String environment, String country);

    /**
     *
     * @param testDatalib
     * @return
     */
    AnswerItem readByKey(int testDatalib);

    /**
     *
     * @param testDataLibName
     * @param limit
     * @return
     */
    AnswerList readNameListByName(String testDataLibName, int limit);

    /**
     *
     * @return All TestDataLib
     */
    AnswerList readAll();

    /**
     *
     * @param name
     * @param system
     * @param environment
     * @param country
     * @param type
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    AnswerList readByVariousByCriteria(String name, String system, String environment, String country, String type, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

     /**
     * Read distinct Value of specified column
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return 
     */
    AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName);

    /**
     * Auxiliary method that retrieves all the group names that were already
     * defined for a type.
     *
     * @return list of group values for the type
     */
    AnswerList readDistinctGroups();

    /**
     * Method that gets all the Static DataLib that match the criterias
     * dataName, dataSystem, dataCountry and dataEnviroenment and for each of
     * them feeds a HasMap with all the columns detail from dataLibData.
     *
     * @param dataName Name of the DataLib to take.
     * @param dataSystem System of the DataLib to take.
     * @param dataCountry Country of the DataLib to take.
     * @param dataEnvironment Envirnment of the DataLib to take.
     * @param rowLimit max number of row to be taken
     * @param system context system in order to get the calculation. It is used
     * to get the potencial parameters.
     * @return
     */
    AnswerList<HashMap<String, String>> readINTERNALWithSubdataByCriteria(String dataName, String dataSystem, String dataCountry, String dataEnvironment, int rowLimit, String system);

    /**
     *
     * @param testDataLib TestDataLib to insert
     * @throws CerberusException
     */
    void create(TestDataLib testDataLib) throws CerberusException;

    /**
     *
     * @param testDataLib
     * @param subDataList
     * @return
     */
    Answer create(TestDataLib testDataLib, List<TestDataLibData> subDataList);

    /**
     *
     * @param entries
     * @return
     */
    Answer create(HashMap<TestDataLib, List<TestDataLibData>> entries);

    /**
     * Deletes a testdatalib entry
     *
     * @param testDataLib
     * @return
     */
    Answer delete(TestDataLib testDataLib);

    /**
     *
     * @param testDataLib TestData to update using the key
     * @return
     */
    Answer update(TestDataLib testDataLib);

    /**
     *
     * @param lib
     * @return
     */
    Answer duplicate(TestDataLib lib);
}
