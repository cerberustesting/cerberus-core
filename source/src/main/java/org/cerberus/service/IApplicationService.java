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
import org.cerberus.entity.Project;
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
    Application findApplicationByKey(String application) throws CerberusException;

    /**
     *
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> findAllApplication() throws CerberusException;

    /**
     *
     * @param system
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> findApplicationBySystem(String system) throws CerberusException;

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
    public AnswerList findApplicationListByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

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
    public AnswerList findApplicationListBySystemByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param id
     * @return
     */
    public AnswerItem findApplicationByString(String id);

    public void createApplication(Application application) throws CerberusException;

    public void deleteApplication(Application application) throws CerberusException;

    boolean updateApplication(Application application) throws CerberusException;

    public Answer createApplication1(Application application);

    public Answer deleteApplication1(Application application);

    public Answer updateApplication1(Application application);

    /**
     *
     * @param application
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean isApplicationExist(String application);

    /**
     *
     * @return @throws CerberusException
     * @since 0.9.1
     */
    List<String> findDistinctSystem();
}
