package org.cerberus.crud.dao;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.util.answer.AnswerList;

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

}
