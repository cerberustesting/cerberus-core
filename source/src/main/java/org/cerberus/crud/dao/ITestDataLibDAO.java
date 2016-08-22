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
package org.cerberus.crud.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 * @author FNogueira
 */
public interface ITestDataLibDAO {

    /**
     *
     * @param testDataLibID
     * @return
     */
    AnswerItem readByKey(int testDataLibID);

    /**
     *
     * @param name
     * @param system
     * @param environment
     * @param country
     * @return
     */
    AnswerItem readByNameBySystemByEnvironmentByCountry(String name, String system, String environment, String country);

    /**
     *
     * @param testDataLibName
     * @param limit
     * @return
     */
    AnswerList readNameListByName(String testDataLibName, int limit);

    /**
     *
     * @return All TestData
     */
    AnswerList readAll();

    /**
     *
     * @param name filter by Name. null to disable the filter.
     * @param system filter by System. null to disable the filter.
     * @param environment filter by Environment. null to disable the filter.
     * @param country filter by Country. null to disable the filter.
     * @param type filter by Type. null to disable the filter.
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param colName order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    AnswerList readByVariousByCriteria(String name, String system, String environment, String country, String type, int start, int amount, String colName, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @return
     */
    AnswerList readDistinctGroups();

    /**
     *
     * @param testDataLib
     * @return
     */
    Answer create(TestDataLib testDataLib);

    /**
     *
     * @param testDataLib
     * @return
     */
    Answer delete(TestDataLib testDataLib);

    /**
     *
     * @param testDataLib
     * @return
     */
    Answer update(TestDataLib testDataLib);

    /**
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    TestDataLib loadFromResultSet(ResultSet resultSet) throws SQLException;

    /**
     * Read distinct Value of specified column
     *
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<List<String>> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName);
}
