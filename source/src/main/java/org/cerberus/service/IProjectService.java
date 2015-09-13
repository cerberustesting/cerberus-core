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
package org.cerberus.service;

import java.util.List;
import org.cerberus.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IProjectService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem readByKey(String id);

    /**
     *
     * @return
     */
    AnswerList readAll();

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param project
     * @return true if project exist. false if not.
     */
    boolean exist(String project);

    /**
     *
     * @param project
     * @return
     */
    Answer create(Project project);

    /**
     *
     * @param project
     * @return
     */
    Answer delete(Project project);

    /**
     *
     * @param project
     * @return
     */
    Answer update(Project project);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Project convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Project> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
}
