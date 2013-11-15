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
package org.cerberus.service.impl;

import org.cerberus.dao.IBuildRevisionInvariantDAO;
import org.cerberus.entity.BuildRevisionInvariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IBuildRevisionInvariantService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BuildRevisionInvariantService implements IBuildRevisionInvariantService {

    @Autowired
    private IBuildRevisionInvariantDAO BuildRevisionInvariantDAO;

    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, Integer seq) throws CerberusException {
        return BuildRevisionInvariantDAO.findBuildRevisionInvariantByKey(system, level, seq);
    }

    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, String versionName) throws CerberusException {
        return BuildRevisionInvariantDAO.findBuildRevisionInvariantByKey(system, level, versionName);
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystemLevel(String system, Integer level) throws CerberusException {
        return BuildRevisionInvariantDAO.findAllBuildRevisionInvariantBySystemLevel(system, level);
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystem(String system) throws CerberusException {
        return BuildRevisionInvariantDAO.findAllBuildRevisionInvariantBySystem(system);
    }

    @Override
    public boolean insertBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.insertBuildRevisionInvariant(buildRevisionInvariant);
    }

    @Override
    public boolean deleteBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.deleteBuildRevisionInvariant(buildRevisionInvariant);
    }

    @Override
    public boolean updateBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.updateBuildRevisionInvariant(buildRevisionInvariant);
    }
    
}
