package org.cerberus.crud.service;

import java.util.List;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 * {@link RobotCapability} service
 * 
 * @author Aurelien Bourdon
 */
public interface IRobotCapabilityService {

	/**
	 * Get all {@link RobotCapability} which are associated to the given
	 * {@link Robot}'s name
	 * 
	 * @param robot
	 *            the {@link Robot}'s name from which getting all associated
	 *            {@link RobotCapability}
	 * @return a list of {@link RobotCapability} which are associated to the
	 *         given {@link Robot}'s name
	 */
	AnswerList<RobotCapability> readByRobot(String robot);

	/**
	 * Convert given capability answers to list of {@link RobotCapability}
	 * 
	 * @param capabilityAnswers
	 *            the {@link AnswerList} of {@link RobotCapability} to convert
	 * @return a list of {@link RobotCapability}
	 * @throws CerberusException
	 *             if an error occurred
	 */
	List<RobotCapability> convert(AnswerList<RobotCapability> capabilityAnswers) throws CerberusException;

}
