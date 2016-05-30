package org.cerberus.crud.factory.impl;

import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.springframework.stereotype.Service;

@Service
public class FactoryRobotCapability implements IFactoryRobotCapability {

	@Override
	public RobotCapability create(int id, String robot, String capability, String value) {
		RobotCapability robotCapability = new RobotCapability();
		robotCapability.setId(id);
		robotCapability.setRobot(robot);
		robotCapability.setCapability(capability);
		robotCapability.setValue(value);
		return robotCapability;
	}

}
