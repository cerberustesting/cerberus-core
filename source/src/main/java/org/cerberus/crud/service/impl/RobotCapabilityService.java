package org.cerberus.crud.service.impl;

import java.util.List;

import org.cerberus.crud.dao.IRobotCapabilityDAO;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.service.IRobotCapabilityService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link IRobotCapabilityService} default implementation
 * 
 * @author Aurelien Bourdon
 */
@Service
public class RobotCapabilityService implements IRobotCapabilityService {

	@Autowired
	private IRobotCapabilityDAO robotCapabilityDAO;

	@Override
	public AnswerList<RobotCapability> readByRobot(String robot) {
		return robotCapabilityDAO.readByRobot(robot);
	}

	@Override
	public List<RobotCapability> convert(AnswerList<RobotCapability> capabilityAnswers) throws CerberusException {
		if (capabilityAnswers != null && capabilityAnswers.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
			// if the service returns an OK message then we can get the item
			return (List<RobotCapability>) capabilityAnswers.getDataList();
		}
		throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
	}

}
