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

import org.apache.commons.fileupload.FileItem;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vertigo
 */
public interface IApplicationObjectService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<ApplicationObject> readByKeyTech(int id);

    /**
     *
     * @param application
     * @param object
     * @return
     */
    AnswerItem<ApplicationObject> readByKey(String application, String object);

    /**
     *
     * @param Application
     * @return
     */
    AnswerList<ApplicationObject> readByApplication(String Application);

    /**
     *
     * @param id
     * @param file
     * @return
     */
    Answer uploadFile(int id, FileItem file);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<ApplicationObject> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param application
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<ApplicationObject> readByApplicationByCriteria(String application, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> systems);

    /**
     *
     * @param application
     * @param object
     * @return
     */
    BufferedImage readImageByKey(String application, String object);

    /**
     *
     * @param object
     * @return
     */
    Answer create(ApplicationObject object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(ApplicationObject object);

    /**
     *
     * @param application
     * @param appObject
     * @param object
     * @return
     */
    Answer update(String application, String appObject, ApplicationObject object);

    /**
     *
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param application
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByApplicationByCriteria(String application, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
