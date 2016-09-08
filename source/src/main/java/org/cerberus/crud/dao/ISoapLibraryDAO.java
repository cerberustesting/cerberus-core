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

import org.cerberus.crud.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Accès aux données de la table SoapLibrary
 *
 * @author cte
 */
public interface ISoapLibraryDAO {

    SoapLibrary findSoapLibraryByKey(String name) throws CerberusException;

    /**
     *
     * @param sqlLibrary sqlLibrary to insert
     * @throws CerberusException
     */
    void createSoapLibrary(SoapLibrary sqlLibrary) throws CerberusException;

    /**
     *
     * @param sqlLibrary sqlLibrary to update using the key
     * @throws CerberusException
     */
    void updateSoapLibrary(SoapLibrary sqlLibrary) throws CerberusException;

    /**
     *
     * @param sqlLibrary sqlLibrary to delete
     * @throws CerberusException
     */
    void deleteSoapLibrary(SoapLibrary sqlLibrary) throws CerberusException;

    /**
     *
     * @return All SoapLibrary
     */
    List<SoapLibrary> findAllSoapLibrary();

    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    List<SoapLibrary> findSoapLibraryListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param name Key of the table
     * @param columnName Name of the column to update
     * @param value New value of the field columnName for the key name
     * @throws CerberusException
     */
    void updateSoapLibrary(String name, String columnName, String value) throws CerberusException;

    /**
     *
     * @param searchTerm words to be searched in every column (Exemple :
     * article)
     * @param inds part of the script to add to where clause (Exemple : `type` =
     * 'Article')
     * @return The number of records for these criterias
     */
    Integer getNumberOfSoapLibraryPerCrtiteria(String searchTerm, String inds);

    /**
     *  Get the {@link SoapLibrary} List of the given {@link System} with the given Criteria
     *
     * @param startPosition         the start index to look for
     * @param length                the number of {@link SoapLibrary} to get
     * @param columnName            the Column name to sort
     * @param searchParameter       the string to search in the {@link SoapLibrary}
     * @param individualSearch      the string to search for each column
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *  Get the {@link SoapLibrary} of the given key
     *
     * @param key                the key of the {@link SoapLibrary} to get
     */
    AnswerItem readByKey(String key);
    /**
     *  Get the distinctValue of the column
     *
     * @param columnName            the Column name to get
     * @param searchParameter       the string to search in the {@link SoapLibrary}
     * @param individualSearch      the string to search for each column
     */
    AnswerList readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param object the {@link SoapLibrary} to Create
     * @return {@link AnswerItem}
     */
    Answer create(SoapLibrary object);

    /**
     * @param object the {@link SoapLibrary} to Update
     * @return {@link AnswerItem}
     */
    Answer update(SoapLibrary object);

    /**
     * @param object the {@link SoapLibrary} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(SoapLibrary object);

    /*
     *  Load a {@link SoapLibrary} of a ResultSet
     *
     * @param rs the {@link ResultSet}
     */
    SoapLibrary loadFromResultSet(ResultSet rs) throws SQLException;
}
