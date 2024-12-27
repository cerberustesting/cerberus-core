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
package org.cerberus.core.crud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface IApplicationService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<Application> readByKey(String id);

    /**
     *
     * @param id
     * @return
     * @throws org.cerberus.core.exception.CerberusException
     */
    Application readByKeyWithDependency(String id) throws CerberusException;

    /**
     *
     * @return
     */
    AnswerList<Application> readAll();

    /**
     *
     * @param System
     * @return
     */
    Integer getNbApplications(List<String> System);

    /**
     *
     * @param System
     * @return
     */
    AnswerList<Application> readBySystem(List<String> System);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<Application> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<Application> readBySystemByCriteria(List<String> system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @return
     */
    AnswerItem<HashMap<String, HashMap<String, Integer>>> readTestCaseCountersBySystemByStatus(List<String> system);

    /**
     *
     * @param application
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String application);

    /**
     *
     * @param object
     * @return
     */
    Answer create(Application object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(Application object);

    /**
     *
     * @param application
     * @param object
     * @return
     */
    Answer update(String application, Application object);

    /**
     *
     * @return @since 0.9.1
     */
    AnswerList readDistinctSystem();

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Application convert(AnswerItem<Application> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Application> convert(AnswerList<Application> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(List<String> system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
