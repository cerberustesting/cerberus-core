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
package org.cerberus.statistics;

import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITestCaseExecutionStatisticsService {

    BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment);

    BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, List<String> environment);

    BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, String environment);

    BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, String environment);

    List<BuildRevisionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize);
    
    List<BuildRevisionStatistics> getListOfXLastBuildAndRev(String system, int listSize);
}
