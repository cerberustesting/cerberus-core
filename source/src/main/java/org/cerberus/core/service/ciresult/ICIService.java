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
package org.cerberus.core.service.ciresult;

import org.cerberus.core.api.entity.CICampaignResult;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONObject;

import java.util.List;

/**
 * @author bcivel
 */
public interface ICIService {

    /**
     * @param tag
     * @param campaign
     * @param executions
     * @return
     */
    JSONObject getCIResult(String tag, String campaign, List<TestCaseExecution> executions);

    /**
     * @param resultCal
     * @param resultCalThreshold
     * @param nbtotal
     * @param nbok
     * @return
     */
    String getFinalResult(int resultCal, int resultCalThreshold, int nbtotal, int nbok);

    /**
     * @param tag
     * @param campaign
     * @return
     */
    CICampaignResult getCIResultApi(String tag, String campaign);

    /**
     * @param id           campaign or execution id
     * @param globalResult
     * @return
     */
    String generateSvg(String id, String globalResult);
}
