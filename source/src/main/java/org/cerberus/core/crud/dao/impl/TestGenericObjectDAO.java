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
package org.cerberus.core.crud.dao.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.dao.ITestGenericObjectDAO;
import org.cerberus.core.crud.entity.TestGenericObject;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@AllArgsConstructor
@Repository
public class TestGenericObjectDAO implements ITestGenericObjectDAO {

    private final DatabaseSpring databaseSpring;
    private static final Logger LOG = LogManager.getLogger(TestGenericObjectDAO.class);
    private static final String OBJECT_NAME = "Test Generic Object";
    private static final String SQL_DUPLICATED_CODE = "23000";
    private static final int MAX_ROW_SELECTED = 100000;

    @Override
    public AnswerList<TestGenericObject> readBySystemByCriteria(List<String> systems, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<TestGenericObject> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestGenericObject> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM\n"
                + "(\n"
                + "SELECT * FROM \n"
                + "(select Test, Testcase , \'HEADER\' Object, t.Application, `System`, t.status, \'\' country, t.isactive, \n"
                + "\'\' StepId, \'\' ActionId, \'\' ControlId, \'\' `Loop`, \n"
                + "ConditionOperator, ConditionValue1, ConditionValue2, ConditionValue3, \n"
                + "\'\' property, \'\' ActionControl, \'\' Value1, \'\' Value2, \'\' Value3, \n"
                + "\'\' IsFatal, \'\' doScreenshotBefore, \'\' doScreenshotAfter, \'\' waitBefore, \'\' waitAfter, \n"
                + "t.Description, \n"
                + "t.UsrCreated, t.DateCreated, t.UsrModif, t.DateModif FROM testcase t\n"
                + "JOIN application a ON t.application=a.application\n"
                + ") tco0\n"
                + "UNION ALL\n"
                + "SELECT * FROM \n"
                + "(select ts.Test, ts.Testcase, \'STEP\' Object, t.Application, `System`, t.status, \'\' country, t.isactive, \n"
                + "StepId, \'\' ActionId, \'\' ControlId, `Loop`, \n"
                + "ts.ConditionOperator, ts.ConditionValue1, ts.ConditionValue2, ts.ConditionValue3, \n"
                + "\'\' property, \'\' ActionControl, \'\' Value1, \'\' Value2, \'\' Value3, \n"
                + "\'\' IsFatal, \'\' doScreenshotBefore, \'\' doScreenshotAfter, \'\' waitBefore, \'\' waitAfter, \n"
                + "ts.Description, \n"
                + "ts.UsrCreated, ts.DateCreated, ts.UsrModif, ts.DateModif  FROM testcasestep ts \n"
                + "JOIN testcase t ON t.test=ts.test and t.testcase=ts.testcase\n"
                + "JOIN application a ON t.application=a.application\n"
                + ") tco1\n"
                + "UNION ALL\n"
                + "SELECT * FROM \n"
                + "(select ta.Test, ta.Testcase , \'ACTION\' Object, t.Application, `System`, t.status, \'\' country, t.isactive, \n"
                + "StepId , ActionId  , \'\' as ControlId, \'\' `Loop` ,\n"
                + "ta.ConditionOperator , ta.ConditionValue1 ,ta.ConditionValue2 , ta.ConditionValue3 , \n"
                + "\'\' property, `Action` ActionControl, Value1, Value2, Value3 ,\n"
                + "IsFatal , doScreenshotBefore , doScreenshotAfter , waitBefore , waitAfter ,\n"
                + "ta.Description , \n"
                + "ta.UsrCreated , ta.DateCreated , ta.UsrModif , ta.DateModif  FROM testcasestepaction ta\n"
                + "JOIN testcase t ON t.test=ta.test and t.testcase=ta.testcase\n"
                + "JOIN application a ON t.application=a.application\n"
                + ") tco2\n"
                + "UNION ALL\n"
                + "SELECT * FROM \n"
                + "(select tc.Test, tc.Testcase , \'CONTROL\' Object, t.Application, `System`, t.status, \'\' country, t.isactive, \n"
                + "StepId , ActionId  , ControlId  , \'\' `Loop`, \n"
                + "tc.ConditionOperator , tc.ConditionValue1 ,tc.ConditionValue2 , tc.ConditionValue3 , \n"
                + "\'\' property, Control  ActionControl, Value1, Value2, Value3 ,\n"
                + "IsFatal , doScreenshotBefore , doScreenshotAfter , waitBefore , waitAfter ,\n"
                + "tc.Description , \n"
                + "tc.UsrCreated , tc.DateCreated , tc.UsrModif , tc.DateModif  FROM testcasestepactioncontrol tc\n"
                + "JOIN testcase t ON t.test=tc.test and t.testcase=tc.testcase\n"
                + "JOIN application a ON t.application=a.application\n"
                + ") tco3\n"
                + "UNION ALL\n"
                + "SELECT * FROM \n"
                + "(select tp.Test, tp.Testcase , \'PROPERTY\' Object, t.Application, `System`, t.status, tp.country, t.isactive, \n"
                + "\'\' StepId, \'\' ActionId, \'\' ControlId, \'\' `Loop`, \n"
                + "\'\' ConditionOperator, \'\' ConditionValue1, \'\' ConditionValue2, \'\' ConditionValue3, \n"
                + "tp.property, tp.Type  ActionControl, Value1, Value2, \'\' Value3 ,\n"
                + "\'\' IsFatal , \'\' doScreenshotBefore , \'\' doScreenshotAfter , \'\' waitBefore , \'\' waitAfter ,\n"
                + "tp.Description , \n"
                + "tp.UsrCreated , tp.DateCreated , tp.UsrModif , tp.DateModif  FROM testcasecountryproperties tp\n"
                + "JOIN testcase t ON t.test=tp.test and t.testcase=tp.testcase\n"
                + "JOIN application a ON t.application=a.application\n"
                + ") tco4\n"
                + ") a");
        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Value1` like ?");
            searchSQL.append(" or `Value2` like ?");
            searchSQL.append(" or `Value3` like ?");
            searchSQL.append(" or `Description` like ?");
            searchSQL.append(" or `conditionValue1` like ?");
            searchSQL.append(" or `conditionValue2` like ?");
            searchSQL.append(" or `conditionValue2` like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }
        query.append(searchSQL);

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.systems : {}", systems);

        try (Connection connection = this.databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {
            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

                if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                }
                response = new AnswerList<>(objectList, nrTotalRows);
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : {}", exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(List<String> systems, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct `")
                .append(columnName)
                .append("` as distinctValues FROM (\n"
                        + "SELECT * FROM \n"
                        + "(select Test, Testcase , \"HEADER\" Object, t.Application, `System`, t.status, \"\" country, t.isactive, \n"
                        + "\"\" StepId, \"\" ActionId, \"\" ControlId, \"\" `Loop`, \n"
                        + "ConditionOperator, ConditionValue1, ConditionValue2, ConditionValue3, \n"
                        + "\"\" property, \"\" ActionControl, \"\" Value1, \"\" Value2, \"\" Value3, \n"
                        + "\"\" IsFatal, \"\" doScreenshotBefore, \"\" doScreenshotAfter, \"\" waitBefore, \"\" waitAfter, \n"
                        + "t.Description, \n"
                        + "t.UsrCreated, t.DateCreated, t.UsrModif, t.DateModif FROM testcase t\n"
                        + "JOIN application a ON t.application=a.application\n"
                        + ") tco0\n"
                        + "UNION ALL\n"
                        + "SELECT * FROM \n"
                        + "(select ts.Test, ts.Testcase, \"STEP\" Object, t.Application, `System`, t.status, \"\" country, t.isactive, \n"
                        + "StepId, \"\" ActionId, \"\" ControlId, `Loop`, \n"
                        + "ts.ConditionOperator, ts.ConditionValue1, ts.ConditionValue2, ts.ConditionValue3, \n"
                        + "\"\" property, \"\" ActionControl, \"\" Value1, \"\" Value2, \"\" Value3, \n"
                        + "\"\" IsFatal, \"\" doScreenshotBefore, \"\" doScreenshotAfter, \"\" waitBefore, \"\" waitAfter, \n"
                        + "ts.Description, \n"
                        + "ts.UsrCreated, ts.DateCreated, ts.UsrModif, ts.DateModif  FROM testcasestep ts \n"
                        + "JOIN testcase t ON t.test=ts.test and t.testcase=ts.testcase\n"
                        + "JOIN application a ON t.application=a.application\n"
                        + ") tco1\n"
                        + "UNION ALL\n"
                        + "SELECT * FROM \n"
                        + "(select ta.Test, ta.Testcase , \"ACTION\" Object, t.Application, `System`, t.status, \"\" country, t.isactive, \n"
                        + "StepId , ActionId  , \"\" as ControlId, \"\" `Loop` ,\n"
                        + "ta.ConditionOperator , ta.ConditionValue1 ,ta.ConditionValue2 , ta.ConditionValue3 , \n"
                        + "\"\" property, `Action` ActionControl, Value1, Value2, Value3 ,\n"
                        + "IsFatal , doScreenshotBefore , doScreenshotAfter , waitBefore , waitAfter ,\n"
                        + "ta.Description , \n"
                        + "ta.UsrCreated , ta.DateCreated , ta.UsrModif , ta.DateModif  FROM testcasestepaction ta\n"
                        + "JOIN testcase t ON t.test=ta.test and t.testcase=ta.testcase\n"
                        + "JOIN application a ON t.application=a.application\n"
                        + ") tco2\n"
                        + "UNION ALL\n"
                        + "SELECT * FROM \n"
                        + "(select tc.Test, tc.Testcase , \"CONTROL\" Object, t.Application, `System`, t.status, \"\" country, t.isactive, \n"
                        + "StepId , ActionId  , ControlId  , \"\" `Loop`, \n"
                        + "tc.ConditionOperator , tc.ConditionValue1 ,tc.ConditionValue2 , tc.ConditionValue3 , \n"
                        + "\"\" property, Control  ActionControl, Value1, Value2, Value3 ,\n"
                        + "IsFatal , doScreenshotBefore , doScreenshotAfter , waitBefore , waitAfter ,\n"
                        + "tc.Description , \n"
                        + "tc.UsrCreated , tc.DateCreated , tc.UsrModif , tc.DateModif  FROM testcasestepactioncontrol tc\n"
                        + "JOIN testcase t ON t.test=tc.test and t.testcase=tc.testcase\n"
                        + "JOIN application a ON t.application=a.application\n"
                        + ") tco3\n"
                        + "UNION ALL\n"
                        + "SELECT * FROM \n"
                        + "(select tp.Test, tp.Testcase , \"PROPERTY\" Object, t.Application, `System`, t.status, tp.country, t.isactive, \n"
                        + "\"\" StepId, \"\" ActionId, \"\" ControlId, \"\" `Loop`, \n"
                        + "\"\" ConditionOperator, \"\" ConditionValue1, \"\" ConditionValue2, \"\" ConditionValue3, \n"
                        + "tp.property, tp.Type  ActionControl, Value1, Value2, \"\" Value3 ,\n"
                        + "\"\" IsFatal , \"\" doScreenshotBefore , \"\" doScreenshotAfter , \"\" waitBefore , \"\" waitAfter ,\n"
                        + "tp.Description , \n"
                        + "tp.UsrCreated , tp.DateCreated , tp.UsrModif , tp.DateModif  FROM testcasecountryproperties tp\n"
                        + "JOIN testcase t ON t.test=tp.test and t.testcase=tp.testcase\n"
                        + "JOIN application a ON t.application=a.application\n"
                        + ") tco4\n"
                        + ") a ");

        searchSQL.append("WHERE 1=1");
        if (CollectionUtils.isNotEmpty(systems)) {
            searchSQL.append(" AND ");
            searchSQL.append(SqlUtil.generateInClause("`System`", systems));
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Value1` like ?");
            searchSQL.append(" or `Value2` like ?");
            searchSQL.append(" or `Value3` like ?");
            searchSQL.append(" or `Description` like ?");
            searchSQL.append(" or `conditionValue1` like ?");
            searchSQL.append(" or `conditionValue2` like ?");
            searchSQL.append(" or `conditionValue2` like ?)");
        }
        if (MapUtils.isNotEmpty(individualSearch)) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individualColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by `").append(columnName).append("` asc");

        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.systems : {}", systems);

        try (Connection connection = databaseSpring.connect(); PreparedStatement preStat = connection.prepareStatement(query.toString()); Statement stm = connection.createStatement()) {

            int i = 1;
            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individualColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try (ResultSet resultSet = preStat.executeQuery(); ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }
                LOG.debug(distinctValues.size());
                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
                LOG.debug(nrTotalRows);

                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to execute query : {}", e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    private TestGenericObject loadFromResultSet(ResultSet rs) throws SQLException {

        String test = ParameterParserUtil.parseStringParam(rs.getString("Test"), "");
        String testCase = ParameterParserUtil.parseStringParam(rs.getString("Testcase"), "");
        String country = ParameterParserUtil.parseStringParam(rs.getString("Country"), "");
        String active = ParameterParserUtil.parseStringParam(rs.getString("IsActive"), "");
        String object = ParameterParserUtil.parseStringParam(rs.getString("Object"), "");
        String app = ParameterParserUtil.parseStringParam(rs.getString("Application"), "");
        String status = ParameterParserUtil.parseStringParam(rs.getString("Status"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("System"), "");
        int stepid = ParameterParserUtil.parseIntegerParam(rs.getString("stepid"), -1);
        int actionid = ParameterParserUtil.parseIntegerParam(rs.getString("actionid"), -1);
        int controlid = ParameterParserUtil.parseIntegerParam(rs.getString("controlid"), -1);
        String loop = ParameterParserUtil.parseStringParam(rs.getString("loop"), "");

        String conditionOperator = ParameterParserUtil.parseStringParam(rs.getString("conditionOperator"), "");
        String conditionValue1 = ParameterParserUtil.parseStringParam(rs.getString("conditionValue1"), "");
        String conditionValue2 = ParameterParserUtil.parseStringParam(rs.getString("conditionValue2"), "");
        String conditionValue3 = ParameterParserUtil.parseStringParam(rs.getString("conditionValue3"), "");
        String property = ParameterParserUtil.parseStringParam(rs.getString("Property"), "");
        String actionControl = ParameterParserUtil.parseStringParam(rs.getString("actionControl"), "");
        String value1 = ParameterParserUtil.parseStringParam(rs.getString("value1"), "");
        String value2 = ParameterParserUtil.parseStringParam(rs.getString("value2"), "");
        String value3 = ParameterParserUtil.parseStringParam(rs.getString("value3"), "");

        String isFatal = ParameterParserUtil.parseStringParam(rs.getString("isFatal"), "");
        String doScreenshotBefore = ParameterParserUtil.parseStringParam(rs.getString("doScreenshotBefore"), "");
        String doScreenshotAfter = ParameterParserUtil.parseStringParam(rs.getString("doScreenshotAfter"), "");

        int waitBefore = ParameterParserUtil.parseIntegerParam(rs.getString("waitBefore"), -1);
        int waitAfter = ParameterParserUtil.parseIntegerParam(rs.getString("waitAfter"), -1);

        String description = ParameterParserUtil.parseStringParam(rs.getString("Description"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        Timestamp dateModif = rs.getTimestamp("DateModif");
        Timestamp dateCreated = rs.getTimestamp("DateCreated");

        return TestGenericObject.builder()
                .test(test)
                .testcase(testCase)
                .active(active)
                .object(object)
                .application(app)
                .country(country)
                .system(system)
                .status(status)
                .stepId(stepid)
                .actionId(actionid)
                .controlId(controlid)
                .loop(loop)
                .conditionOperator(conditionOperator)
                .conditionValue1(conditionValue1)
                .conditionValue2(conditionValue2)
                .conditionValue3(conditionValue3)
                .property(property)
                .actionControl(actionControl)
                .value1(value1)
                .value2(value2)
                .value3(value3)
                .isFatal(isFatal)
                .doScreenshotBefore(doScreenshotBefore)
                .doScreenshotAfter(doScreenshotAfter)
                .waitBefore(waitBefore)
                .waitAfter(waitAfter)
                .description(description)
                .usrCreated(usrCreated)
                .dateCreated(dateCreated)
                .usrModif(usrModif)
                .dateModif(dateModif)
                .build();
    }
}
