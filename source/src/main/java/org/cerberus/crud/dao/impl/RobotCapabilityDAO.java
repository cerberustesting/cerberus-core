package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IRobotCapabilityDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Robot;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.factory.IFactoryRobotCapability;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Default {@link IRobotCapabilityDAO} implementation
 *
 * @author Aurelien Bourdon
 */
@Repository
public class RobotCapabilityDAO implements IRobotCapabilityDAO {

    /**
     * Declare SQL queries used by this {@link RobotCapabilityDAO}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Get list of {@link RobotCapability} associated with the given
         * {@link Robot}'s name
         */
        String READ_BY_ROBOT = "SELECT * FROM `robotcapability` WHERE `robot` = ?";

        /**
         * Create a new {@link RobotCapability}
         */
        String CREATE = "INSERT INTO `robotcapability` (`robot`, `capability`, `value`) VALUES (?, ?, ?)";

        /**
         * Update an existing {@link RobotCapability}
         */
        String UPDATE = "UPDATE `robotcapability` SET `value` = ? WHERE `robot` = ? AND `capability` = ?";

        /**
         * Remove an existing {@link RobotCapability}
         */
        String DELETE = "DELETE FROM `robotcapability` WHERE `robot` = ? AND `capability` = ?";
    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = Logger.getLogger(RobotCapabilityDAO.class);

    /**
     * The associated entity name to this DAO
     */
    private static final String OBJECT_NAME = RobotCapability.class.getSimpleName();

    @Autowired
    private DatabaseSpring databaseSpring;

    @Autowired
    private IFactoryRobotCapability robotCapabilityFactory;

    @Override
    public AnswerList<RobotCapability> readByRobot(String robot) {
        AnswerList<RobotCapability> ans = new AnswerList<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_ROBOT)) {
            // Prepare and execute query
            preStat.setString(1, robot);
            ResultSet resultSet = preStat.executeQuery();

            // Parse query
            List<RobotCapability> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(loadFromResultSet(resultSet));
            }
            ans.setDataList(result);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "SELECT");
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer create(RobotCapability capability) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.CREATE)) {
            // Prepare and execute query
            preStat.setString(1, capability.getRobot());
            preStat.setString(2, capability.getCapability());
            preStat.setString(3, capability.getValue());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create robot capability: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer update(RobotCapability capability) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.UPDATE)) {
            // Prepare and execute query
            preStat.setString(1, capability.getValue());
            preStat.setString(2, capability.getRobot());
            preStat.setString(3, capability.getCapability());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to update robot capability: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer delete(RobotCapability capability) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setString(1, capability.getRobot());
            preStat.setString(2, capability.getCapability());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "DELETE");
        } catch (Exception e) {
            LOG.warn("Unable to delete robot capability: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    /**
     * Load a {@link RobotCapability} instance from the given {@link ResultSet}
     *
     * @param resultSet the {@link ResultSet} from which get the
     * {@link RobotCapability} instance
     * @return a {@link RobotCapability} instance from the given
     * {@link ResultSet}
     * @throws SQLException if a SQL error occurred
     */
    private RobotCapability loadFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String robot = resultSet.getString("robot");
        String capability = resultSet.getString("capability");
        String value = resultSet.getString("value");
        return robotCapabilityFactory.create(id, robot, capability, value);
    }

}
