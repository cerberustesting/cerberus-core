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
package org.cerberus.core.service.robotextension;

import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public interface IFilemanagementService {

    /**
     *
     * @param session
     * @param action
     * @param nbFiles
     * @param filename
     * @param option
     * @param contentBase64
     * @return
     */
    public AnswerItem<JSONObject> doFilemanagementAction(Session session, String action, int  nbFiles, String filename, String option, String contentBase64);

    /**
     *
     * @param session
     * @param filename
     * @return
     */
    public MessageEvent doFilemanagementActionCleanRobotFile(Session session, String filename);

    /**
     *
     * @param session
     * @param filename
     * @param contentBase64
     * @param option
     * @return
     */
    public MessageEvent doFilemanagementActionUploadRobotFile(Session session, String filename, String contentBase64, String option);

    /**
     *
     * @param session
     * @param filename
     * @param nbFiles
     * @param option
     * @return
     */
    public AnswerItem<JSONObject> doFilemanagementActionGetRobotFile(Session session, String filename, int nbFiles, String option);

}
