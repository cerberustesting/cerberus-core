/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentParametersService {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param application
     * @return
     * @throws CerberusException
     */
    CountryEnvironmentParameters findCountryEnvironmentParameterByKey(String system, String country, String environment, String application) throws CerberusException;

    /**
     *
     * @param country
     * @param application
     * @return
     */
    public List<String[]> getEnvironmentAvailable(String country, String application);

    /**
     *
     * @param countryEnvironmentParameter
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentParameters> findCountryEnvironmentParametersByCriteria(CountryEnvironmentParameters countryEnvironmentParameter) throws CerberusException;

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param application
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList readByVariousByCriteria(String system, String country, String environment, String application, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param application
     * @return
     */
    public AnswerList readByVarious(String system, String country, String environment, String application);

    /**
     *
     * @return
     * @throws CerberusException
     */
    public List<String> getDistinctEnvironmentNames() throws CerberusException;

    /**
     * Find all CountryEnvironmentParameters by System
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentParameters> findAll(String system) throws CerberusException;

    /**
     * Update CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void update_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    /**
     * Delete CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void delete_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    /**
     * Create CountryEnvironmentParameters
     *
     * @param cea
     * @throws CerberusException
     */
    void create_deprecated(CountryEnvironmentParameters cea) throws CerberusException;

    /**
     * Find List of CountryEnvironmentParameters by Criteria
     *
     * @param system
     * @param country
     * @param env
     * @param start row number of the resulset where start the List
     * (limit(start,amount))
     * @param amount number of row returned
     * @param column column used for the sort (sort by column dir >become> sort
     * by country asc)
     * @param dir asc or desc
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public List<CountryEnvironmentParameters> findListByCriteria(String system, String country, String env, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     * Find the number of CountryEnvironmentParameters found respecting the
     * search criteria
     *
     * @param searchTerm
     * @param inds
     * @return
     */
    public Integer countPerCriteria(String searchTerm, String inds);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     */
    public List<CountryEnvironmentParameters> findListByCriteria(String system, String country, String environment);

    /**
     *
     * @param object
     * @return
     */
    public Answer update(CountryEnvironmentParameters object);

    /**
     *
     * @param object
     * @return
     */
    public Answer delete(CountryEnvironmentParameters object);

    /**
     *
     * @param object
     * @return
     */
    public Answer create(CountryEnvironmentParameters object);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer createList(List<CountryEnvironmentParameters> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer deleteList(List<CountryEnvironmentParameters> objectList);

    /**
     * Update all CountryEnvironmentDatabase from the sourceList to the
     * perimeter of system, country and environment list. All existing databases
     * from newList will be updated, the new ones added and missing ones
     * deleted.
     *
     * @param system
     * @param country
     * @param environement
     * @param newList
     * @return
     */
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvironmentParameters> newList);

    /**
     * Update all CountryEnvironmentDatabase from the sourceList to the
     * perimeter of the application list. All existing databases
     * from newList will be updated, the new ones added and missing ones
     * deleted.
     *
     * @param system
     * @param application
     * @param newList
     * @return
     */
    public Answer compareListAndUpdateInsertDeleteElements(String system, String application, List<CountryEnvironmentParameters> newList);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CountryEnvironmentParameters convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentParameters> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
}
