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

/**
 * @author lucashimpens
 */
@Builder
@Data
public class CampaignExecutionResultPriority {

    private int okCoefficientPriorityLevel1;
    private int okCoefficientPriorityLevel2;
    private int okCoefficientPriorityLevel3;
    private int okCoefficientPriorityLevel4;
    private int okCoefficientPriorityLevel5;
    private int nonOkExecutionsPriorityLevel1;
    private int nonOkExecutionsPriorityLevel2;
    private int nonOkExecutionsPriorityLevel3;
    private int nonOkExecutionsPriorityLevel4;
    private int nonOkExecutionsPriorityLevel5;
}
