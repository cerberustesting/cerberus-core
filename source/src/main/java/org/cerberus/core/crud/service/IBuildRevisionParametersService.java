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

import org.cerberus.core.crud.entity.BuildRevisionParameters;

import java.util.List;
import java.util.Map;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

public interface IBuildRevisionParametersService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem readByKeyTech(int id);

    /**
     *
     * @param system
     * @return
     */
    AnswerItem readLastBySystem(String system);

    /**
     * This service is used to retrieve the buildRevisionParameters from
     * database knowing the build, revision, release and application
     *
     * @param build
     * @param revision
     * @param release
     * @param application
     * @return buildRevisionParameters Object
     */
    AnswerItem readByVarious2(String build, String revision, String release, String application);

    /**
     *
     * @param system
     * @param application
     * @param build
     * @param start
     * @param revision
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<BuildRevisionParameters> readByVarious1ByCriteria(String system, String application, String build, String revision, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param system
     * @param build
     * @param revision
     * @param lastBuild
     * @param lastRevision
     * @return
     */
    AnswerList<BuildRevisionParameters> readMaxSVNReleasePerApplication(String system, String build, String revision, String lastBuild, String lastRevision);

    /**
     *
     * @param system
     * @param build
     * @param revision
     * @param lastBuild
     * @param lastRevision
     * @return
     */
    AnswerList<BuildRevisionParameters> readNonSVNRelease(String system, String build, String revision, String lastBuild, String lastRevision);

    /**
     *
     * @param brp
     * @return
     */
    Answer create(BuildRevisionParameters brp);

    /**
     *
     * @param brp
     * @return
     */
    Answer delete(BuildRevisionParameters brp);

    /**
     *
     * @param brp
     * @return
     */
    Answer update(BuildRevisionParameters brp);

    /**
     * This service is used to control that a given build and revision is
     * already used inside a system (ie a deploy has already been made on at
     * least 1 environment).
     *
     * @param application
     * @param build
     * @param revision
     * @return true if the build revision of the system is already used
     * (deployed in any environment)
     */
    boolean check_buildRevisionAlreadyUsed(String application, String build, String revision);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    BuildRevisionParameters convert(AnswerItem<BuildRevisionParameters> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<BuildRevisionParameters> convert(AnswerList<BuildRevisionParameters> answerList) throws CerberusException;

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
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
