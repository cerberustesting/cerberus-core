package org.cerberus.crud.dao;

import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.util.answer.Answer;
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
