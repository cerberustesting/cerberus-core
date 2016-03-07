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
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ICountryEnvironmentDatabaseService {

    public AnswerItem readByKey(String system, String country, String environment, String database);

    public CountryEnvironmentDatabase findCountryEnvironmentDatabaseByKey(String system, String country, String environment, String database) throws CerberusException;

    /**
     * Find all countryEnvironmentDatabase by System
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    public List<CountryEnvironmentDatabase> findAll(String system) throws CerberusException;

    /**
     * Find List of CountryEnvironmentDatabase by Criteria
     *
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
    public List<CountryEnvironmentDatabase> findListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     */
    public AnswerList readByVarious(String system, String country, String environment);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList readByVariousByCriteria(String system, String country, String environment, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     * Find the number of CountryEnvironmentDatabase found respecting the search
     * criteria
     *
     * @param searchTerm
     * @return
     */
    public Integer count(String searchTerm);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @return
     * @throws CerberusException
     */
    public List<CountryEnvironmentDatabase> findListByCriteria(String system, String country, String environment) throws CerberusException;

    /**
     * Update countryEnvironmentDatabase
     *
     * @param ced
     * @throws CerberusException
     */
    void update_deprecated(CountryEnvironmentDatabase ced) throws CerberusException;

    /**
     * Delete countryEnvironmentDatabase
     *
     * @param ced
     * @throws CerberusException
     */
    void delete_deprecated(CountryEnvironmentDatabase ced) throws CerberusException;

    /**
     * Create countryEnvironmentDatabase
     *
     * @param ced
     * @throws CerberusException
     */
    void create_deprecated(CountryEnvironmentDatabase ced) throws CerberusException;

    /**
     *
     * @param object
     * @return
     */
    public Answer create(CountryEnvironmentDatabase object);

    /**
     *
     * @param object
     * @return
     */
    public Answer delete(CountryEnvironmentDatabase object);

    /**
     *
     * @param object
     * @return
     */
    public Answer update(CountryEnvironmentDatabase object);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer createList(List<CountryEnvironmentDatabase> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer deleteList(List<CountryEnvironmentDatabase> objectList);

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
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvironmentDatabase> newList);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param database
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String system, String country, String environment, String database);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CountryEnvironmentDatabase convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CountryEnvironmentDatabase> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
}
