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
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author bcivel
 */
public interface IInvariantService {

    /**
     * Use readByIdName instead to avoid Answer
     *
     * @param idName
     * @return
     */
    AnswerList<Invariant> readByIdname(String idName);

    /**
     *
     * @param idName
     * @return
     * @throws CerberusException
     */
    List<Invariant> readByIdName(String idName) throws CerberusException;

    /**
     * Return a hashmap of Invariants with value as Key
     *
     * @param idName
     * @return
     * @throws org.cerberus.exception.CerberusException
     */
    HashMap<String, Invariant> readByIdNameToHash(String idName) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param testCaseCountries
     * @param countryInvariants
     * @return
     * @throws CerberusException
     */
    List<Invariant> findCountryInvariantsFromTestCase(String test, String testCase, HashMap<String, TestCaseCountry> testCaseCountries, HashMap<String, Invariant> countryInvariants) throws CerberusException;

    /**
     *
     * @param testCaseCountryproperties
     * @param countryInvariants
     * @return
     * @throws CerberusException
     */
    public List<Invariant> convertCountryPropertiesToCountryInvariants(TestCaseCountryProperties testCaseCountryproperties, HashMap<String, Invariant> countryInvariants) throws CerberusException;

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

    /**
     *
     * @param idName
     * @param gp
     * @return
     */
    AnswerList<Invariant> readByIdnameGp1(String idName, String gp);

    /**
     *
     * @param idName
     * @param gp
     * @return
     */
    AnswerList<Invariant> readByIdnameNotGp1(String idName, String gp);

    /**
     *
     * @param system
     * @param nbDays
     * @return
     */
    AnswerList<Invariant> readCountryListEnvironmentLastChanges(String system, Integer nbDays);

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<Invariant> readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByPublicByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<Invariant> readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList readDistinctValuesByPrivateByCriteria(String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<Invariant> readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

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
    AnswerItem<Invariant> readByKey(String id, String value);

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
    Invariant convert(AnswerItem<Invariant> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Invariant> convert(AnswerList<Invariant> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
