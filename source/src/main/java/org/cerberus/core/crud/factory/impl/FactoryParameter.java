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
package org.cerberus.core.crud.factory.impl;

import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.factory.IFactoryParameter;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryParameter implements IFactoryParameter {

    @Override
    public Parameter create(String system, String param, String value, String description) {
        Parameter parameter = new Parameter();
        parameter.setSystem(system);
        parameter.setParam(param);
        parameter.setValue(value);
        parameter.setDescription(description);
        return parameter;
    }

    @Override
    public Parameter create(String system, String param, String value, String description, String system1, String system1value1) {
        Parameter parameter = this.create(system,param,value, description);
        parameter.setSystem1(system1);
        parameter.setSystem1value(system1value1);
        return parameter;
    }

}
