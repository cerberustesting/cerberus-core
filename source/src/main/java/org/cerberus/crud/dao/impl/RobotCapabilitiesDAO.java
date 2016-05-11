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
package org.cerberus.crud.dao.impl;

import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IRobotCapabilitiesDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapabilities;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * Default {@link IRobotCapabilitiesDAO} implementation
 *
 * @author Aurelien Bourdon.
 */
@Repository
public class RobotCapabilitiesDAO implements IRobotCapabilitiesDAO {

    private interface Table {
        String NAME = "robotcapability";

        interface Column {
            String ID = "id";
            String ROBOT = "robot";
            String CAPABILITY = "capability";
            String VALUE = "value";
        }
    }

    private interface Query {
        /**
         * Find all capabilities from a given {@link Robot}
         */
        String FIND_FROM_ROBOT = String.format(
                "SELECT * FROM %s WHERE %s = ?",
                Table.NAME,
                Table.Column.ROBOT
        );

        /**
         * Create a robot capability line
         */
        String CREATE = String.format(
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                Table.NAME,
                Table.Column.ROBOT,
                Table.Column.CAPABILITY,
                Table.Column.VALUE
        );

        /**
         * Update a robot capability line
         */
        String UPDATE = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
                Table.NAME,
                Table.Column.VALUE,
                Table.Column.ROBOT,
                Table.Column.CAPABILITY
        );

        /**
         * Update a robot capability line
         */
        String DELETE = String.format(
                "DELETE FROM %s WHERE %s = ? AND %s = ?",
                Table.NAME,
                Table.Column.ROBOT,
                Table.Column.CAPABILITY
        );
    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(RobotCapabilitiesDAO.class);

    @Autowired
    private DatabaseSpring database;

    @Override
    public Answer create(RobotCapabilities capabilities) {
        // The final answer
        Answer answer = new Answer();

        // Check argument
        if (capabilities == null || capabilities.getCapabilities() == null) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR)
                    .resolveDescription("DESCRIPTION", "Unable to create null robot capabilities"));
            return answer;
        }

        // Execute update
        try (Connection connection = database.connect()) {
            for (Map.Entry<String, String> capability : capabilities.getCapabilities().entrySet()) {
                try (PreparedStatement statement = connection.prepareStatement(Query.CREATE)) {
                    // Fill statement
                    statement.setString(1, capabilities.getRobot().getRobot());
                    statement.setString(2, capability.getKey());
                    statement.setString(3, capability.getValue());

                    // Execute query
                    statement.executeUpdate();
                } catch (SQLException e) {
                    LOGGER.warn(String.format(
                            "Unable to insert capability (%s, %s) for robot %s due to %s",
                            capability.getKey(),
                            capability.getValue(),
                            capabilities.getRobot().getRobot(),
                            e.getMessage())
                    );
                }
            }
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                    .resolveDescription("ITEM", "Robot capabilities")
                    .resolveDescription("OPERATION", "Create"));
        } catch (SQLException e) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("REASON", e.getMessage()));
        }

        // Finally return the final answer
        return answer;
    }

    @Override
    public Answer update(RobotCapabilities capabilities) {
        Answer answer = new Answer();

        // Check argument
        if (capabilities == null) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR)
                    .resolveDescription("DESCRIPTION", "Unable to delete null capabilities"));
            return answer;
        }

        // Execute update
        try (Connection connection = database.connect()) {
            for (Map.Entry<String, String> capability : capabilities.getCapabilities().entrySet()) {
                try (PreparedStatement statement = connection.prepareStatement(Query.UPDATE)) {
                    // Fill statement
                    statement.setString(1, capability.getValue());
                    statement.setString(2, capabilities.getRobot().getRobot());
                    statement.setString(3, capability.getKey());

                    // Execute query
                    statement.executeUpdate();
                } catch (SQLException e) {
                    LOGGER.warn(String.format(
                            "Unable to update capability (%s, %s) for robot %s due to %s",
                            capability.getKey(),
                            capability.getValue(),
                            capabilities.getRobot().getRobot(),
                            e.getMessage())
                    );
                }
            }
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                    .resolveDescription("ITEM", "Robot capabilities")
                    .resolveDescription("OPERATION", "Update"));
        } catch (SQLException e) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("REASON", e.getMessage()));
        }
        return answer;
    }

    @Override
    public Answer delete(RobotCapabilities capabilities) {
        Answer answer = new Answer();

        // Check argument
        if (capabilities == null) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR)
                    .resolveDescription("DESCRIPTION", "Unable to delete null capabilities"));
            return answer;
        }

        // Execute update
        try (Connection connection = database.connect()) {
            for (Map.Entry<String, String> capability : capabilities.getCapabilities().entrySet()) {
                try (PreparedStatement statement = connection.prepareStatement(Query.DELETE)) {
                    // Fill statement
                    statement.setString(1, capabilities.getRobot().getRobot());
                    statement.setString(2, capability.getKey());

                    // Execute query
                    statement.executeUpdate();
                } catch (SQLException e) {
                    LOGGER.warn(String.format(
                            "Unable to delete capability (%s, %s) for robot %s due to %s",
                            capability.getKey(),
                            capability.getValue(),
                            capabilities.getRobot().getRobot(),
                            e.getMessage())
                    );
                }
            }
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                    .resolveDescription("ITEM", "Robot capabilities")
                    .resolveDescription("OPERATION", "Delete"));
        } catch (SQLException e) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("REASON", e.getMessage()));
        }
        return answer;
    }

    @Override
    public AnswerItem<RobotCapabilities> findFromRobot(Robot robot) {
        // The final answer item
        AnswerItem<RobotCapabilities> answer = new AnswerItem<>();

        // Check argument
        if (robot == null) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR)
                    .resolveDescription("DESCRIPTION", "Unable to find capabilities from a null robot"));
            return answer;
        }

        // Try to find capabilities from the given robot
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(Query.FIND_FROM_ROBOT)) {
            statement.setString(1, robot.getRobot());
            RobotCapabilities capabilities = new RobotCapabilities(robot);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    capabilities.putCapability(result.getString(Table.Column.CAPABILITY), result.getString(Table.Column.VALUE));
                }
            }
            // FIXME legacy code compliance: do not override capabilities from Robot table values but only from RobotCapability one
            capabilities = legacyCompliance(capabilities, robot);
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK)
                    .resolveDescription("ITEM", "Robot capabilities")
                    .resolveDescription("OPERATION", "Find"));
            answer.setItem(capabilities);
        } catch (SQLException e) {
            answer.setResultMessage(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                    .resolveDescription("REASON", e.getMessage()));
        }

        // Finally return the final answer item
        return answer;
    }

    /**
     * Make the given {@link RobotCapabilities} compliant with legacy version of {@link Robot}.
     * <p>
     * Legacy version of {@link Robot} directly use capability attributes on the Robot object, instead of getting them from the {@link RobotCapabilities} structure.
     *
     * @param capabilities the {@link RobotCapabilities} to make compliant
     * @param robot        the {@link Robot} to retrieve legacy capability values
     * @return the same {@link RobotCapabilities} as given, updated with {@link Robot}'s capabilities if necessary
     */
    @Deprecated
    private RobotCapabilities legacyCompliance(RobotCapabilities capabilities, Robot robot) {
        if (robot == null || capabilities == null) {
            LOGGER.warn("Unable to make robot compliant with legacy capabilities with null robot or capabilities");
            return capabilities;
        }

        // If there is a Robot#getPlatform() value and no capability is found into the capabilities structure, then use it.
        if (!StringUtil.isNullOrEmpty(robot.getPlatform()) && !capabilities.hasCapability(RobotCapabilities.Capability.PLATFORM)) {
            capabilities.putCapability(RobotCapabilities.Capability.PLATFORM, robot.getPlatform());
        }

        // If there is a Robot#getBrowser() value and no capability is found into the capabilities structure, then use it.
        if (!StringUtil.isNullOrEmpty(robot.getBrowser()) && !capabilities.hasCapability(RobotCapabilities.Capability.BROWSER)) {
            capabilities.putCapability(RobotCapabilities.Capability.BROWSER, robot.getBrowser());
        }

        // If there is a Robot#getVersion() value and no capability is found into the capabilities structure, then use it.
        if (!StringUtil.isNullOrEmpty(robot.getVersion()) && !capabilities.hasCapability(RobotCapabilities.Capability.VERSION)) {
            capabilities.putCapability(RobotCapabilities.Capability.VERSION, robot.getVersion());
        }

        return capabilities;
    }

}
