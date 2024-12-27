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
package org.cerberus.core.api.entity;

import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;

/**
 * @author lucashimpens
 */
@Data
@Builder
public class CICampaignResult {
    private String globalResult;
    private String campaignExecutionId;
    private int calculatedResult;
    private int resultThreshold;
    private JSONArray detailByDeclinations;
    private JSONArray environments;
    private JSONArray countries;
    private JSONArray robotDeclinations;
    private JSONArray systems;
    private JSONArray applications;
    private CampaignExecutionResult result;
    private CampaignExecutionResultPriority resultByPriority;
    private String executionStart;
    private String executionEnd;
}
