package org.cerberus.crud.factory;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;

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
