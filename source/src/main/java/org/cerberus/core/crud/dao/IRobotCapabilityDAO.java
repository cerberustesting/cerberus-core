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
package org.cerberus.core.crud.dao;

import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {@link RobotCapability} data access object
 * 
 * @author Aurelien Bourdon
 */
public interface IRobotCapabilityDAO {

	/**
	 * Get a {@link RobotCapability} list from the given {@link Robot}'s name
	 * 
	 * @param robot
	 *            the {@link Robot}'s name
	 * @return a {@link RobotCapability} list associated with the given
	 *         {@link Robot}'s name
	 */
	AnswerList<RobotCapability> readByRobot(String robot);
        
        /**
         * Create a new {@link RobotCapability} to the database
         * 
         * @param capability the new {@link RobotCapability} to create
         * @return a {@link Answer} with the associated return code
         */
        Answer create(RobotCapability capability);
        
        /**
         * Update an existing {@link RobotCapability} from the database
         * 
         * @param capability the existing {@link RobotCapability} to update
         * @return a {@link Answer} with the associated return code
         */
        Answer update(RobotCapability capability);
        
        /**
         * Delete an exsiting {@link RobotCapability} from the database
         * 
         * @param capability the existing {@link RobotCapability} to delete
         * @return a {@link Answer} with the associated return code
         */
        Answer delete(RobotCapability capability);

}
