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
package org.cerberus.core.service.notifications.teams;

import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface ITeamsGenerationService {

    /**
     *
     * @param tag
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyStartTagExecution(Tag tag) throws Exception;

    /**
     *
     * @param tag
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyEndTagExecution(Tag tag) throws Exception;

    /**
     *
     * @param exe
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe) throws Exception;

    /**
     *
     * @param exe
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe) throws Exception;

    /**
     *
     * @param testCase
     * @param eventReference
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String eventReference) throws Exception;
}
