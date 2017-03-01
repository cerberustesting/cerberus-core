package org.cerberus.crud.service.impl;

import org.apache.log4j.Logger;
import org.cerberus.api.repository.specification.VariousSpecification;
import org.cerberus.crud.repository.IRobotRepository;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.service.IRobotService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RobotService implements IRobotService {

    private static final Logger LOGGER = Logger.getLogger(RobotService.class);

    private final IRobotRepository robotRepository;

    @Autowired
    public RobotService(final IRobotRepository robotRepository) {
        this.robotRepository = robotRepository;
    }

    @Override
    public AnswerItem<Robot> readByKeyTech(final Integer robotid) {
        try {
            return convert(robotRepository.findOne(robotid));
        } catch (final Exception e) {
            LOGGER.warn("Unable to read robot by technical key", e);
            return answerItemError(e);
        }
    }

    @Override
    public AnswerItem<Robot> readByKey(final String robot) {
        try {
            return convert(robotRepository.findByRobot(robot));
        } catch (final Exception e) {
            LOGGER.warn("Unable to read robot by key", e);
            return answerItemError(e);
        }
    }

    @Override
    public AnswerList<Robot> readAll() {
        try {
            return convert(robotRepository.findAll());
        } catch (final Exception e) {
            LOGGER.warn("Unable to read all robots", e);
            return answerListError(e);
        }
    }

    @Override
    public AnswerList<Robot> readByCriteria(final int startPosition, final int length, final String columnName, final String sort, final String searchParameter, final Map<String, List<?>> individualSearch) {
        try {
            final Page<Robot> page = robotRepository.findAll(
                    new VariousSpecification<Robot>(individualSearch, searchParameter),
                    new PageRequest(startPosition, length == 0 ? Integer.MAX_VALUE : length, new Sort(Sort.Direction.fromStringOrNull(sort), columnName))
            );
            return convert(page.getContent(), page.getTotalPages());
        } catch (final Exception e) {
            LOGGER.warn("Unable to read robots by criteria", e);
            return answerListError(e);
        }
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(final String searchParameter, final Map<String, List<?>> individualSearch, final String columnName) {
        try {
            return (AnswerList<String>) convert(robotRepository.findSingleAttribute(columnName, new VariousSpecification<Robot>(individualSearch, searchParameter)));
        } catch (final Exception e) {
            LOGGER.warn("Unable to read distinct values by criteria", e);
            return answerListError(e);
        }
    }

    @Override
    public Answer create(final Robot robot) {
        try {
            return convert(robotRepository.save(robot));
        } catch (final Exception e) {
            LOGGER.warn("Unable to create robot", e);
            return answerItemError(e);
        }
    }

    @Override
    public Answer delete(final Robot robot) {
        try {
            robotRepository.delete(robot.getRobotID());
            return new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        } catch (final Exception e) {
            LOGGER.warn("Unable to delete robot", e);
            return answerItemError(e);
        }
    }

    @Override
    public Answer update(final Robot robot) {
        try {
            return convert(robotRepository.save(robot));
        } catch (final Exception e) {
            LOGGER.warn("Unable to update robot", e);
            return answerItemError(e);
        }
    }

    @Override
    public Robot convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return (Robot) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<Robot> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            return (List<Robot>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    private AnswerItem<Robot> convert(Robot robot) {
        return new AnswerItem<>(robot, new MessageEvent(robot == null ? MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND : MessageEventEnum.DATA_OPERATION_OK));
    }

    private AnswerItem<Robot> answerItemError(Throwable error) {
        return new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", error.getMessage()));
    }

    private <T> AnswerList<T> convert(List<T> items) {
        return convert(items, items.size());
    }

    private <T> AnswerList<T> answerListError(Throwable error) {
        return new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION", error.getMessage()));
    }

    private <T> AnswerList<T> convert(List<T> items, int totalRows) {
        return new AnswerList<>(items, totalRows, new MessageEvent(items == null ? MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND : MessageEventEnum.DATA_OPERATION_OK));
    }
}
