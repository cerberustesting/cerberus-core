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
import java.util.Map;

import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

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
    AnswerItem<TestCaseExecutionFile> readByKey(long id);

    /**
     *
     * @param exeId
     * @param level
     * @param fileDesc
     * @return
     */
    AnswerItem<TestCaseExecutionFile> readByKey(long exeId, String level, String fileDesc);

    /**
     *
     * @param exeId
     * @param level
     * @param start
     * @param amount
     * @param column
     * @param dir
     * @param searchTerm
     * @param individualSearch
     * @return
     */
    AnswerList<TestCaseExecutionFile> readByVariousByCriteria(long exeId, String level, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param exeId
     * @param level
     * @return
     */
    AnswerList<TestCaseExecutionFile> readByVarious(long exeId, String level);


    /**
     * Return all File correponding to fileDesc
     * @param exeId
     * @param fileDesc
     * @return
     */
    List<TestCaseExecutionFile> getListByFileDesc(long exeId, String fileDesc) throws CerberusException;

    /**
     *
     * @param exeId
     * @param level
     * @param fileDesc
     * @param fileName
     * @param fileType
     * @param usrCreated
     * @return
     */
    Answer save(long exeId, String level, String fileDesc, String fileName, String fileType, String usrCreated);

    /**
     *
     * @param id
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(long id);

    /**
     *
     * @param exeId
     * @param level
     * @param fileDesc
     * @return
     */
    boolean exist(long exeId, String level, String fileDesc);
    
    /**
    *
    * @param exeId
    * @param level
    * @return
    */
   boolean exist(long exeId, String level);


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
     * @param folder
     */
    void deleteFile(String root, String fileName);

    /**
     *
     * @param object
     * @return
     */
    Answer save(TestCaseExecutionFile object);
    
    /**
    *
    * @param object
    * @return
    */
   Answer saveManual(TestCaseExecutionFile object);
    
    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TestCaseExecutionFile convert(AnswerItem<TestCaseExecutionFile> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TestCaseExecutionFile> convert(AnswerList<TestCaseExecutionFile> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;
    
    /**
     * 
     * @param fileName
     * @param extension
     * @return
     */
    public String checkExtension(String fileName, String extension);

}
