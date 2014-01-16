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


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionStatisticsServiceImpl implements ITestCaseExecutionStatisticsService {

    @Autowired
    ITestCaseExecutionStatisticsDAO testCaseExecutionStatisticsDAO;

    @Override
    public BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, List<String> environment) {
        return testCaseExecutionStatisticsDAO.getStatisticsOfExecution(MySystem, build, revision, environment);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, List<String> environment) {
        return testCaseExecutionStatisticsDAO.getStatisticsOfExternalExecution(MySystem, build, revision, environment);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExecution(String MySystem, String build, String revision, String environment) {
        List<String> env = new ArrayList<String>();
        env.add(environment);
        return this.getStatisticsOfExecution(MySystem, build, revision, env);
    }

    @Override
    public BuildRevisionStatistics getStatisticsOfExternalExecution(String MySystem, String build, String revision, String environment) {
        List<String> env = new ArrayList<String>();
        env.add(environment);
        return this.getStatisticsOfExternalExecution(MySystem, build, revision, env);
    }

    @Override
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRevExecuted(String system, int listSize) {
        return testCaseExecutionStatisticsDAO.getListOfXLastBuildAndRevExecuted(system, listSize);
    }

    @Override
    public List<BuildRevisionStatistics> getListOfXLastBuildAndRev(String system, int listSize) {
        return testCaseExecutionStatisticsDAO.getListOfXLastBuildAndRev(system, listSize);
    }
    
}
