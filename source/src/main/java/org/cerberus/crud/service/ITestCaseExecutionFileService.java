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
import java.util.Map;

import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ITestCaseExecutionFileService {

    /**
     *
     * @param id
     * @return
     */
    public AnswerItem<TestCaseExecutionFile> readByKey(long id);
    
    /**
     *
     * @param id
     * @param level
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<List<TestCaseExecutionFile>> readByVariousByCriteria(long id, String level, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param id
     * @param level
     * @return
     */
    AnswerList<List<TestCaseExecutionFile>> readByVarious(long id, String level);
    
    /**
     *
     * @param exeid
     * @param level
     * @param fileDesc
     * @param fileName
     * @param fileType
     * @param usrCreated
     * @return
     */
    Answer create(long exeid, String level, String fileDesc, String fileName, String fileType, String usrCreated);
    
    /**
     *
     * @param id
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(long id);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseExecutionFile object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TestCaseExecutionFile object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(TestCaseExecutionFile object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionFile convert(AnswerItem answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionFile> convert(AnswerList answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
