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

import java.util.List;
import org.cerberus.core.crud.entity.CountryEnvDeployType;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

public interface ICountryEnvDeployTypeService {

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param deployType
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    public AnswerList<CountryEnvDeployType> readByVariousByCriteria(String system, String country, String environment, String deployType, int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param system
     * @param country
     * @param environment
     * @param deployType
     * @return
     */
    public AnswerList<CountryEnvDeployType> readByVarious(String system, String country, String environment, String deployType);

    /**
     *
     * @param object
     * @return
     */
    public Answer update(CountryEnvDeployType object);

    /**
     *
     * @param object
     * @return
     */
    public Answer create(CountryEnvDeployType object);

    /**
     *
     * @param object
     * @return
     */
    public Answer delete(CountryEnvDeployType object);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer createList(List<CountryEnvDeployType> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer deleteList(List<CountryEnvDeployType> objectList);

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
    public Answer compareListAndUpdateInsertDeleteElements(String system, String country, String environement, List<CountryEnvDeployType> newList);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CountryEnvDeployType convert(AnswerItem<CountryEnvDeployType> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CountryEnvDeployType> convert(AnswerList<CountryEnvDeployType> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
