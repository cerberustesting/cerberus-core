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

import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.AppService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author cte
 */
public interface IAppServiceService {

    AppService findAppServiceByKey(String name) throws CerberusException;

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
     * @return
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * Get the {@link AppService} of the given key
     *
     * @param key the key of the {@link AppService} to get
     * @return
     */
    AnswerItem readByKey(String key);

    /**
     * Get the distinctValue of the column
     *
     * @param columnName the Column name to get
     * @param searchParameter the string to search in the {@link AppService}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param object the {@link AppService} to Create
     * @return {@link AnswerItem}
     */
    Answer create(AppService object);

    /**
     * @param object the {@link AppService} to Update
     * @return {@link AnswerItem}
     */
    Answer update(AppService object);

    /**
     * @param object the {@link AppService} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(AppService object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    AppService convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<AppService> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
