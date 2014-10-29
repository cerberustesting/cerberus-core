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

package org.cerberus.dao;

import org.cerberus.entity.BuildRevisionParameters;

import java.util.List;

public interface IBuildRevisionParametersDAO {

    List<BuildRevisionParameters> findBuildRevisionParametersFromMaxRevision(String build, String revision, String lastBuild, String lastRevision);

    public List<BuildRevisionParameters> findBuildRevisionParametersByCriteria(String system, String build, String revision);

    String getMaxBuildBySystem(String system);

    String getMaxRevisionBySystemAndBuild(String system, String build);

    void insertBuildRevisionParameters(BuildRevisionParameters brp);

    boolean deleteBuildRevisionParameters(int id);

    boolean updateBuildRevisionParameters(BuildRevisionParameters brp);

    BuildRevisionParameters findBuildRevisionParametersByKey(int id);
}
