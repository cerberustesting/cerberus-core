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

import java.util.List;

import org.cerberus.dao.IParameterSystemDAO;
import org.cerberus.entity.ParameterSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IParameterSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ParameterSystemService implements IParameterSystemService {

    @Autowired
    private IParameterSystemDAO parameterSystemDao;

    @Override
    public List<ParameterSystem> findAllParameterSystem(String system) throws CerberusException {
        return parameterSystemDao.findAllParameterSystem(system);
    }

}
