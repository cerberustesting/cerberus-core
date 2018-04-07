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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface IInvariantService {

    /**
     *
     * @param idName
     * @return
     */
    AnswerList readByIdname(String idName);

    /**
     *
     * @param idName
     * @param defaultValue
     * @return
     */
    HashMap<String, Integer> readToHashMapGp1IntegerByIdname(String idName, Integer defaultValue);

    /**
     *
     * @param idName
     * @param defaultValue
     * @return
     */
    HashMap<String, String> readToHashMapGp1StringByIdname(String idName, String defaultValue);

    AnswerList readByIdnameGp1(String idName, String gp);

    AnswerList readCountryListEnvironmentLastChanges(String system, Integer nbDays);

    AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    AnswerList readDistinctValuesByPublicByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName);

    AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    AnswerList readDistinctValuesByPrivateByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName);

    AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param idName
     * @param value
     * @return
     */
    boolean isInvariantExist(String idName, String value);

    /**
     *
     * @param object
     * @return
     */
    boolean isInvariantPublic(Invariant object);

    /**
     *
     * @param id
     * @param value
     * @return
     */
    AnswerItem readByKey(String id, String value);

    /**
     *
     * @param invariant
     * @return
     */
    Answer create(Invariant invariant);

    /**
     *
     * @param invariant
     * @return
     */
    Answer delete(Invariant invariant);

    /**
     *
     * @param idname
     * @param value
     * @param invariant
     * @return
     */
    Answer update(String idname, String value, Invariant invariant);

    /**
     *
     * @param filter
     * @return
     */
    String getPublicPrivateFilter(String filter);

    /**
     *
     * @param invariant
     * @param request
     * @return
     */
    boolean hasPermissionsRead(Invariant invariant, HttpServletRequest request);

    /**
     *
     * @param invariant
     * @param request
     * @return
     */
    boolean hasPermissionsUpdate(Invariant invariant, HttpServletRequest request);

    /**
     *
     * @param invariant
     * @param request
     * @return
     */
    boolean hasPermissionsCreate(Invariant invariant, HttpServletRequest request);

    /**
     *
     * @param invariant
     * @param request
     * @return
     */
    boolean hasPermissionsDelete(Invariant invariant, HttpServletRequest request);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Invariant convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Invariant> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
