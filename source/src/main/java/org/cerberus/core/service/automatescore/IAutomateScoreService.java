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
package org.cerberus.core.service.automatescore;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 *
 * @author vertigo17
 */
public interface IAutomateScoreService {

    /**
     *
     * @param request
     * @param systems
     * @param to
     * @param campaigns
     * @param nbWeeks
     * @return
     */
    public JSONObject generateAutomateScore(HttpServletRequest request, List<String> systems, List<String> campaigns, String to, int nbWeeks);

}
