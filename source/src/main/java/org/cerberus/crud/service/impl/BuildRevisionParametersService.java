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

package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.IBuildRevisionParametersDAO;
import org.cerberus.crud.entity.BuildRevisionParameters;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildRevisionParametersService implements IBuildRevisionParametersService {

    @Autowired
    IBuildRevisionParametersDAO buildRevisionParametersDAO;

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersFromMaxRevision(String build, String revision, String lastBuild, String lastRevision) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersFromMaxRevision(build, revision, lastBuild, lastRevision);
    }

    @Override
    public List<BuildRevisionParameters> findBuildRevisionParametersByCriteria(String system, String build, String revision) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersByCriteria(system, build, revision);
    }

    @Override
    public String getMaxBuildBySystem(String system) {
        return this.buildRevisionParametersDAO.getMaxBuildBySystem(system);
    }

    @Override
    public String getMaxRevisionBySystemAndBuild(String system, String build) {
        return this.buildRevisionParametersDAO.getMaxRevisionBySystemAndBuild(system, build);
    }

    @Override
    public void insertBuildRevisionParameters(BuildRevisionParameters brp) {
        this.buildRevisionParametersDAO.insertBuildRevisionParameters(brp);
    }

    @Override
    public void deleteBuildRevisionParameters(int id) {
        this.buildRevisionParametersDAO.deleteBuildRevisionParameters(id);
    }

    @Override
    public void updateBuildRevisionParameters(BuildRevisionParameters brp) {
        this.buildRevisionParametersDAO.updateBuildRevisionParameters(brp);
    }

    @Override
    public BuildRevisionParameters findBuildRevisionParametersByKey(int id) {
        return this.buildRevisionParametersDAO.findBuildRevisionParametersByKey(id);
    }
}
