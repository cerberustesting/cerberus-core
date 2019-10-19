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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.cerberus.crud.entity.AppService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * Accès aux données de la table AppService
 *
 * @author cte
 */
public interface IAppServiceDAO {

    /**
     *
     * @param name
     * @return
     * @throws CerberusException
     */
    AppService findAppServiceByKey(String name) throws CerberusException;

    /**
     *
     * @param service
     * @param limit
     * @return
     */
    AnswerList<AppService> findAppServiceByLikeName(String service, int limit);

    /**
     * Get the {@link AppService} List of the given {@link System} with the
     * given Criteria
     *
     * @param startPosition the start index to look for
     * @param length the number of {@link AppService} to get
     * @param columnName the Column name to sort
     * @param sort
     * @param searchParameter the string to search in the {@link AppService}
     * @param individualSearch the string to search for each column
     * @param systems
     * @return
     */
    AnswerList<AppService> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems);

    /**
     * Get the {@link AppService} of the given key
     *
     * @param key the key of the {@link AppService} to get
     * @return
     */
    AnswerItem<AppService> readByKey(String key);

    /**
     * Get the distinctValue of the column
     *
     * @param columnName the Column name to get
     * @param searchParameter the string to search in the {@link AppService}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param object the {@link AppService} to Create
     * @return {@link AnswerItem}
     */
    Answer create(AppService object);

    /**
     * @param service
     * @param object the {@link AppService} to Update
     * @return {@link AnswerItem}
     */
    Answer update(String service, AppService object);

    /**
     * @param object the {@link AppService} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(AppService object);

    /*
     *  Load a {@link AppService} of a ResultSet
     *
     * @param rs the {@link ResultSet}
     */
    AppService loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param service
     * @param file
     * @return
     */
    Answer uploadFile(String service, FileItem file);
}
