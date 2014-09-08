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
package org.cerberus.dao;

import java.util.List;
import org.cerberus.entity.TestDataLib;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author vertigo17
 */
public interface ITestDataLibDataDAO {

    /**
     *
     * @param testDataLibData
     * @throws CerberusException
     */
    void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibData
     * @throws CerberusException
     */
    void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibData
     * @throws CerberusException
     */
    void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException;

    /**
     *
     * @param testDataLibID
     * @param subData
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException;

    /**
     *
     * @return All TestData
     */
    List<TestDataLibData> findAllTestDataLibData();

    /**
     *
     * @param testDataLibID 
     * resultSet
     * @return
     */
    List<TestDataLibData> findTestDataLibDataListByTestDataLib(Integer testDataLibID);

    List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException;
}
