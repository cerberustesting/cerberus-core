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

import org.cerberus.entity.Application;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface IApplicationService {

    /**
     *
     * @param application
     * @return Application object with all properties feeded.
     * @throws CerberusException if Application not found.
     */
    Application readByKey_Deprecated(String application) throws CerberusException;

    /**
     *
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> readAll_Deprecated() throws CerberusException;

    /**
     *
     * @param system
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> readBySystem_Deprecated(String system) throws CerberusException;

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
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param system
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    public AnswerList readBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param id
     * @return
     */
    public AnswerItem readByKey(String id);

    public Answer create(Application application);

    public Answer delete(Application application);

    public Answer update(Application application);

    /**
     *
     * @param application
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String application);

    /**
     *
     * @return @throws CerberusException
     * @since 0.9.1
     */
    List<String> readDistinctSystem();
}
