/*
 * Cerberus  Copyright (C) 2016  vertigo17
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
package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.springframework.stereotype.Service;

@Service
public class FactoryRobotCapability implements IFactoryRobotCapability {

	@Override
	public RobotCapability create(int id, Robot robot, String capability, String value) {
		RobotCapability robotCapability = new RobotCapability();
		robotCapability.setId(id);
		robotCapability.setRobot(robot);
		robotCapability.setCapability(capability);
		robotCapability.setValue(value);
		return robotCapability;
	}

}
