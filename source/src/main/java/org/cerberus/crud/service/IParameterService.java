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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.observe.Observable;

/**
 * @author bcivel
 */
public interface IParameterService extends Observable<String, Parameter> {

    String DEFAULT_SYSTEM = "";

    /**
     * Getting the parameter from database with system (priority) rule. If
     * parameter exist for system, we get that value. If it does not exist, we
     * get the default value (system="") value .
     *
     * @param key
     * @param system
     * @return
     * @throws CerberusException
     */
    Parameter findParameterByKey(String key, String system) throws CerberusException;

    /**
     * This method can be used in order to retrieve a parameter directly in
     * boolean format.
     *
     * @param key
     * @param system
     * @param defaultValue
     * @return
     */
    boolean getParameterBooleanByKey(String key, String system, boolean defaultValue);

    /**
     * This method can be used in order to retrieve a parameter directly in
     * integer format.
     *
     * @param key
     * @param system
     * @param defaultValue
     * @return
     */
    Integer getParameterIntegerByKey(String key, String system, Integer defaultValue);

    /**
     * This method can be used in order to retrieve a parameter directly in long
     * format.
     *
     * @param key
     * @param system
     * @param defaultValue
     * @return
     */
    long getParameterLongByKey(String key, String system, long defaultValue);

    /**
     * This method can be used in order to retrieve a parameter directly in
     * float format.
     *
     * @param key
     * @param system
     * @param defaultValue
     * @return
     */
    float getParameterFloatByKey(String key, String system, float defaultValue);

    /**
     * This method can be used in order to retrieve a parameter directly in
     * String format.
     *
     * @param key
     * @param system
     * @param defaultValue
     * @return
     */
    String getParameterStringByKey(String key, String system, String defaultValue);

    List<Parameter> findAllParameter() throws CerberusException;

    void updateParameter(Parameter parameter) throws CerberusException;

    void insertParameter(Parameter parameter) throws CerberusException;

    void saveParameter(Parameter parameter) throws CerberusException;

    /**
     * Get the {@link Parameter} List of the given {@link System}
     *
     * @param system the {@link System} To look for
     * @param system1 the {@link System} To add the value of the same paramater
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    List<Parameter> findAllParameterWithSystem1(String system, String system1) throws CerberusException;

    /**
     * Get the {@link Parameter} List of the given {@link System} with the given
     * Criteria
     *
     * @param system the {@link System} To look for
     * @param system1 the {@link System} To add the value of the same paramater
     * @param startPosition the start index to look for
     * @param length the number of {@link Parameter} to get
     * @param columnName the Column name to sort
     * @param sort
     * @param searchParameter the string to search in the {@link Parameter}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList readWithSystem1BySystemByCriteria(String system, String system1, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * Get the {@link Parameter} with the given {@link System} and the given key
     *
     * @param system the {@link System} To look for
     * @param system1 the {@link System} To add the value of the same paramater
     * @param key the key of the {@link Parameter}
     * @return
     */
    AnswerItem readWithSystem1ByKey(String system, String key, String system1);

    /**
     * @param system
     * @param system1
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * Get the {@link Parameter} of the given key
     *
     * @param system the system of the {@link Parameter} to get
     * @param param the param of the {@link Parameter} to get
     * @return 
     */
    AnswerItem readByKey(String system, String param);

    /**
     * @param object the {@link Parameter} to Create
     * @return {@link AnswerItem}
     */
    Answer create(Parameter object);

    /**
     * @param object the {@link Parameter} to Update
     * @return {@link AnswerItem}
     */
    Answer update(Parameter object);

    /**
     * @param object the {@link Parameter} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(Parameter object);

    /**
     * @param object the {@link Parameter} to Save
     * @return {@link AnswerItem}
     */
    Answer save(Parameter object);

    /**
     *
     * @param parameter
     * @param request
     * @return
     */
    boolean hasPermissionsRead(Parameter parameter, HttpServletRequest request);

    /**
     *
     * @param parameter
     * @param request
     * @return
     */
    boolean hasPermissionsUpdate(Parameter parameter, HttpServletRequest request);

    /**
     *
     * @param parameter
     * @param request
     * @return
     */
    boolean hasPermissionsUpdate(String parameter, HttpServletRequest request);

    /**
     *
     * @param parameter
     * @param request
     * @return
     */
    boolean hasPermissionsCreate(Parameter parameter, HttpServletRequest request);

    /**
     *
     * @param parameter
     * @param request
     * @return
     */
    boolean hasPermissionsDelete(Parameter parameter, HttpServletRequest request);

}
