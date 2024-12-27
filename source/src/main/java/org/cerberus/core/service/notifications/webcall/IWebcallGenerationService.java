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
package org.cerberus.core.service.notifications.webcall;

import java.util.List;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface IWebcallGenerationService {

    /**
     *
     * @param tag
     * @param ceberusEventMessage
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyStartTagExecution(Tag tag, JSONObject ceberusEventMessage) throws Exception;

    /**
     *
     * @param tag
     * @param ceberusEventMessage
     * @param prioritiesList
     * @param countriesList
     * @param environmentsList
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyEndTagExecution(Tag tag, JSONObject ceberusEventMessage, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) throws Exception;

    /**
     *
     * @param exe
     * @param ceberusEventMessage
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyStartExecution(TestCaseExecution exe, JSONObject ceberusEventMessage) throws Exception;

    /**
     *
     * @param exe
     * @param ceberusEventMessage
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyEndExecution(TestCaseExecution exe, JSONObject ceberusEventMessage) throws Exception;

    /**
     *
     * @param testCase
     * @param eventReference
     * @param ceberusEventMessage
     * @return
     * @throws Exception
     */
    public JSONObject generateNotifyTestCaseChange(TestCase testCase, String originalTest, String originalTestcase, String eventReference, JSONObject ceberusEventMessage) throws Exception;
}
