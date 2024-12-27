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
package org.cerberus.core.crud.factory;

import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;

/**
 * {@link RobotCapability} factory
 * 
 * @author Aurelien Bourdon
 */
public interface IFactoryRobotCapability {

	/**
	 * Create a new {@link RobotCapability} with its associated attributes
	 * 
	 * @param id
	 *            the {@link RobotCapability}'s technical identifier
	 * @param robot
	 *            the {@link RobotCapability}'s associated {@link Robot} name
	 * @param capability
	 *            the {@link RobotCapability}' capability key
	 * @param value
	 *            the {@link RobotCapability}'s capability value
	 * @return a new {@link RobotCapability} instance based on the given
	 *         attributes
	 */
	RobotCapability create(int id, String robot, String capability, String value);

}
