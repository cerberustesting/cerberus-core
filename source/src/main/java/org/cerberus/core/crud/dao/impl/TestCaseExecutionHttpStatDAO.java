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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionHttpStatDAO;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionHttpStat;
import org.cerberus.core.crud.factory.impl.FactoryTestCaseExecutionHttpStat;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.har.IHarService;
import org.cerberus.core.service.har.entity.HarStat;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Implements methods defined on ITestCaseExecutionHttpStatDAO
 *
 * @author vertigo17
 */
@Repository
public class TestCaseExecutionHttpStatDAO implements ITestCaseExecutionHttpStatDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseExecutionHttpStat factoryTestCaseExecutionHttpStat;
    @Autowired
    private IFactoryTestCase factoryTestCase;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private IHarService harService;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionHttpStatDAO.class);

    private final String OBJECT_NAME = "TestCaseExecutionHttpStat";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;
    private final int MAX_SIZE_SELECTED = 50000000;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";

    @Override
    public Answer create(TestCaseExecutionHttpStat object) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcaseexecutionhttpstat (`id`, `start`, `controlstatus`, `system`, `application`, `test`, `testcase`, `country`, `environment`, `robotDecli`");
        query.append(", total_hits, total_size, total_time");
        query.append(", internal_hits, internal_size, internal_time");
        query.append(", img_size, img_size_max, img_hits");
        query.append(", js_size, js_size_max, js_hits");
        query.append(", css_size, css_size_max, css_hits");
        query.append(", html_size, html_size_max, html_hits");
        query.append(", media_size, media_size_max, media_hits");
        query.append(", nb_thirdparty, crbversion");
        query.append(", statDetail ");
        query.append(", UsrCreated");
        query.append(") VALUES (?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                preStat.setLong(i++, object.getId());
//                preStat.setTimestamp(i++, object.getStart());
                preStat.setString(i++, object.getControlStatus());
                preStat.setString(i++, object.getSystem());
                preStat.setString(i++, object.getApplication());
                preStat.setString(i++, object.getTest());
                preStat.setString(i++, object.getTestcase());
                preStat.setString(i++, object.getCountry());
                preStat.setString(i++, object.getEnvironment());
                preStat.setString(i++, object.getRobotDecli());
                preStat.setInt(i++, object.getTotal_hits());
                preStat.setInt(i++, object.getTotal_size());
                preStat.setInt(i++, object.getTotal_time());
                preStat.setInt(i++, object.getInternal_hits());
                preStat.setInt(i++, object.getInternal_size());
                preStat.setInt(i++, object.getInternal_time());
                preStat.setInt(i++, object.getImg_size());
                preStat.setInt(i++, object.getImg_size_max());
                preStat.setInt(i++, object.getImg_hits());
                preStat.setInt(i++, object.getJs_size());
                preStat.setInt(i++, object.getJs_size_max());
                preStat.setInt(i++, object.getJs_hits());
                preStat.setInt(i++, object.getCss_size());
                preStat.setInt(i++, object.getCss_size_max());
                preStat.setInt(i++, object.getCss_hits());
                preStat.setInt(i++, object.getHtml_size());
                preStat.setInt(i++, object.getHtml_size_max());
                preStat.setInt(i++, object.getHtml_hits());
                preStat.setInt(i++, object.getMedia_size());
                preStat.setInt(i++, object.getMedia_size_max());
                preStat.setInt(i++, object.getMedia_hits());
                preStat.setInt(i++, object.getNb_thirdparty());
                preStat.setString(i++, object.getCrbVersion());
                preStat.setString(i++, object.getStatDetail().toString());
                preStat.setString(i++, object.getUsrCreated());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public AnswerItem<TestCaseExecutionHttpStat> readByKey(long exeId) {
        AnswerItem<TestCaseExecutionHttpStat> ans = new AnswerItem<>();
        TestCaseExecutionHttpStat result = null;
        final String query = "SELECT * FROM testcaseexecutionhttpstat ehs WHERE `id` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
            LOG.debug("SQL.param.id : " + exeId);
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                preStat.setLong(1, exeId);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<TestCaseExecutionHttpStat> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to,
                                                                List<String> system, List<String> countries, List<String> environments, List<String> robotDecli) {
        AnswerList<TestCaseExecutionHttpStat> response = new AnswerList<>();
        List<TestCaseExecutionHttpStat> objectList = new ArrayList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionhttpstat ehs ");

        searchSQL.append(" where 1=1 ");

        // System
        if (system != null && !system.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", system));
        }
        // Country
        if (countries != null && !countries.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Country`", countries));
        }
        // System
        if (environments != null && !environments.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Environment`", environments));
        }
        // System
        if (robotDecli != null && !robotDecli.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`RobotDecli`", robotDecli));
        }
        // from and to
        searchSQL.append(" and start >= ? and start <= ? ");
        // testcase
        StringBuilder testcaseSQL = new StringBuilder();
        for (TestCase testcase : testcases) {
            testcaseSQL.append(" (test = ? and testcase = ?) or ");
        }
        if (!StringUtil.isEmptyOrNull(testcaseSQL.toString())) {
            searchSQL.append("and (").append(testcaseSQL).append(" (0=1) ").append(")");
        }
        // controlStatus
        if (controlStatus != null) {
            searchSQL.append(" and ControlStatus = ? ");
        }

        query.append(searchSQL);

        query.append(" order by id desc ");

        query.append(" limit ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null && !system.isEmpty()) {
                    for (String syst : system) {
                        preStat.setString(i++, syst);
                    }
                }
                if (countries != null && !countries.isEmpty()) {
                    for (String val : countries) {
                        preStat.setString(i++, val);
                    }
                }
                if (environments != null && !environments.isEmpty()) {
                    for (String val : environments) {
                        preStat.setString(i++, val);
                    }
                }
                if (robotDecli != null && !robotDecli.isEmpty()) {
                    for (String val : robotDecli) {
                        preStat.setString(i++, val);
                    }
                }
                t1 = new Timestamp(from.getTime());
                preStat.setTimestamp(i++, t1);
                t1 = new Timestamp(to.getTime());
                preStat.setTimestamp(i++, t1);
                for (TestCase testcase : testcases) {
                    preStat.setString(i++, testcase.getTest());
                    preStat.setString(i++, testcase.getTestcase());
                }
                if (controlStatus != null) {
                    preStat.setString(i++, controlStatus);
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    int currentSize = 0;
                    //gets the data
                    while (resultSet.next()) {
                        TestCaseExecutionHttpStat curStat = this.loadFromResultSet(resultSet);
                        currentSize += curStat.getStatDetail().toString().length();
                        if (currentSize < MAX_SIZE_SELECTED) {
                            objectList.add(curStat);
                        } else {
                            LOG.debug("Over Size !!! " + curStat.getDateCreated().toString());
                            curStat.setStatDetail(new JSONObject());
                            objectList.add(curStat);
                        }
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (objectList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else if (objectList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList<>(objectList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList<>(objectList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setDataList(objectList);
        return response;
    }

    @Override
    public AnswerItem<JSONObject> readByCriteria(String controlStatus, List<TestCase> testcases, Date from, Date to,
                                                 List<String> system, List<String> countries, List<String> environments, List<String> robotDecli,
                                                 List<String> parties, List<String> types, List<String> units) {

        JSONObject object = new JSONObject();
        AnswerItem<JSONObject> response = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        StringBuilder searchSQL = new StringBuilder();
        Timestamp t1;

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcaseexecutionhttpstat ehs ");

        searchSQL.append(" where 1=1 ");

        // System
        if (system != null && !system.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`System`", system));
        }
        // Country
        if (countries != null && !countries.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Country`", countries));
        }
        // System
        if (environments != null && !environments.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`Environment`", environments));
        }
        // System
        if (robotDecli != null && !robotDecli.isEmpty()) {
            searchSQL.append(" and ");
            searchSQL.append(SqlUtil.generateInClause("`RobotDecli`", robotDecli));
        }
        // from and to
        searchSQL.append(" and start >= ? and start <= ? ");
        // testcase
        StringBuilder testcaseSQL = new StringBuilder();
        for (TestCase testcase : testcases) {
            testcaseSQL.append(" (test = ? and testcase = ?) or ");
        }
        if (!StringUtil.isEmptyOrNull(testcaseSQL.toString())) {
            searchSQL.append("and (").append(testcaseSQL).append(" (0=1) ").append(")");
        }
        // controlStatus
        if (controlStatus != null) {
            searchSQL.append(" and ControlStatus = ? ");
        }

        query.append(searchSQL);

        query.append(" order by id desc ");

        query.append(" limit ").append(MAX_ROW_SELECTED);

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (system != null && !system.isEmpty()) {
                    for (String syst : system) {
                        preStat.setString(i++, syst);
                    }
                }
                if (countries != null && !countries.isEmpty()) {
                    for (String val : countries) {
                        preStat.setString(i++, val);
                    }
                }
                if (environments != null && !environments.isEmpty()) {
                    for (String val : environments) {
                        preStat.setString(i++, val);
                    }
                }
                if (robotDecli != null && !robotDecli.isEmpty()) {
                    for (String val : robotDecli) {
                        preStat.setString(i++, val);
                    }
                }
                t1 = new Timestamp(from.getTime());
                preStat.setTimestamp(i++, t1);
                t1 = new Timestamp(to.getTime());
                preStat.setTimestamp(i++, t1);
                for (TestCase testcase : testcases) {
                    preStat.setString(i++, testcase.getTest());
                    preStat.setString(i++, testcase.getTestcase());
                }
                if (controlStatus != null) {
                    preStat.setString(i++, controlStatus);
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    HashMap<String, JSONArray> curveMap = new HashMap<>();
                    HashMap<String, JSONObject> curveObjMap = new HashMap<>();
                    // Indicator Map
                    HashMap<String, Boolean> partyMap = new HashMap<>();
                    partyMap.put("total", false);
                    partyMap.put("internal", false);
                    HashMap<String, Boolean> typeMap = new HashMap<>();
                    HashMap<String, Boolean> unitMap = new HashMap<>();
                    String curveKey;
                    JSONArray curArray = new JSONArray();
                    JSONObject curveObj = new JSONObject();
                    JSONObject pointObj = new JSONObject();

                    int nbFetch = 0;
                    //gets the data
                    while (resultSet.next()) {
                        TestCaseExecutionHttpStat curStat = this.loadFromResultSet(resultSet);
                        nbFetch++;

                        if (curStat.getStatDetail().has("thirdparty")) {

                            // Get List of Third Party
                            JSONObject partiesA = curStat.getStatDetail().getJSONObject("thirdparty");
                            @SuppressWarnings("unchecked")
                            Iterator<String> jsonObjectIterator = partiesA.keys();
                            jsonObjectIterator.forEachRemaining(key -> {
                                partyMap.put(key, false);
                            });

                            for (String party : parties) {
                                for (String type : types) {
                                    for (String unit : units) {
                                        curveKey = getKeyCurve(curStat, party, type, unit);
                                        int x = harService.getValue(curStat, party, type, unit);
                                        if (x != -1) {
                                            partyMap.put(party, true);
                                            typeMap.put(type, true);
                                            unitMap.put(unit, true);

                                            pointObj = new JSONObject();
                                            Date d = new Date(curStat.getStart().getTime());
                                            TimeZone tz = TimeZone.getTimeZone("UTC");
                                            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                                            df.setTimeZone(tz);
                                            pointObj.put("x", df.format(d));

                                            pointObj.put("y", x);
                                            pointObj.put("exe", curStat.getId());
                                            pointObj.put("exeControlStatus", curStat.getControlStatus());

                                            if (curveMap.containsKey(curveKey)) {
                                                curArray = curveMap.get(curveKey);
                                            } else {
                                                curArray = new JSONArray();

                                                curveObj = new JSONObject();
                                                curveObj.put("key", curveKey);
                                                TestCase a = factoryTestCase.create(curStat.getTest(), curStat.getTestcase());
                                                try {
                                                    a = testCaseService.convert(testCaseService.readByKey(curStat.getTest(), curStat.getTestcase()));
                                                } catch (CerberusException ex) {
                                                    LOG.error("Exception when getting TestCase details", ex);
                                                }
                                                curveObj.put("testcase", a.toJson());

                                                curveObj.put("country", curStat.getCountry());
                                                curveObj.put("environment", curStat.getEnvironment());
                                                curveObj.put("robotdecli", curStat.getRobotDecli());
                                                curveObj.put("system", curStat.getSystem());
                                                curveObj.put("application", curStat.getApplication());
                                                curveObj.put("unit", unit);
                                                curveObj.put("party", party);
                                                curveObj.put("type", type);

                                                curveObjMap.put(curveKey, curveObj);
                                            }
                                            curArray.put(pointObj);
                                            curveMap.put(curveKey, curArray);

                                        }

                                    }
                                }

                            }
                        }

                    }

                    object.put("hasPerfdata", (curveObjMap.size() > 0));

                    JSONArray curvesArray = new JSONArray();
                    for (Map.Entry<String, JSONObject> entry : curveObjMap.entrySet()) {
                        String key = entry.getKey();
                        JSONObject val = entry.getValue();
                        JSONObject localcur = new JSONObject();
                        localcur.put("key", val);
                        localcur.put("points", curveMap.get(key));
                        curvesArray.put(localcur);
                    }
                    object.put("datasetPerf", curvesArray);

                    JSONArray objectdinst = new JSONArray();
                    objectdinst = new JSONArray();
                    for (HarStat.Units v : HarStat.Units.values()) {
                        JSONObject objectcount = new JSONObject();
                        objectcount.put("name", v.name().toLowerCase());
                        objectcount.put("hasData", unitMap.containsKey(v.name().toLowerCase()));
                        objectcount.put("isRequested", units.contains(v.name().toLowerCase()));
                        objectdinst.put(objectcount);
                    }
                    object.put("distinctUnits", objectdinst);

                    objectdinst = new JSONArray();
                    for (HarStat.Types v : HarStat.Types.values()) {
                        JSONObject objectcount = new JSONObject();
                        objectcount.put("name", v.name().toLowerCase());
                        objectcount.put("hasData", typeMap.containsKey(v.name().toLowerCase()));
                        objectcount.put("isRequested", types.contains(v.name().toLowerCase()));
                        objectdinst.put(objectcount);
                    }
                    object.put("distinctTypes", objectdinst);

                    objectdinst = new JSONArray();
                    for (Map.Entry<String, Boolean> entry : partyMap.entrySet()) {
                        String key = entry.getKey();
                        Boolean val = entry.getValue();
                        JSONObject objectcount = new JSONObject();
                        objectcount.put("name", key);
                        objectcount.put("hasData", val);
                        objectcount.put("isRequested", parties.contains(key));
                        objectdinst.put(objectcount);
                    }
                    object.put("distinctParties", objectdinst);

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (nbFetch >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    } else if (nbFetch <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    }

                    object.put("message", msg.getDescription());
                    object.put("messageType", msg.getCodeString());
                    object.put("iTotalRecords", nrTotalRows);
                    object.put("iTotalDisplayRecords", nrTotalRows);

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } catch (JSONException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        response.setResultMessage(msg);
        response.setItem(object);
        return response;
    }

    private String getKeyCurve(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
        return type + "/" + party + "/" + unit + "/" + stat.getTest() + "/" + stat.getTestcase() + "/" + stat.getCountry() + "/" + stat.getEnvironment() + "/" + stat.getRobotDecli() + "/" + stat.getSystem() + "/" + stat.getApplication();
    }

    @Override
    public TestCaseExecutionHttpStat loadFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("ehs.id");
        Timestamp time = rs.getTimestamp("start");
        String controlStatus = ParameterParserUtil.parseStringParam(rs.getString("ehs.controlstatus"), "");
        String system = ParameterParserUtil.parseStringParam(rs.getString("ehs.system"), "");
        String application = ParameterParserUtil.parseStringParam(rs.getString("ehs.application"), "");
        String test = ParameterParserUtil.parseStringParam(rs.getString("ehs.test"), "");
        String testcase = ParameterParserUtil.parseStringParam(rs.getString("ehs.testcase"), "");
        String country = ParameterParserUtil.parseStringParam(rs.getString("ehs.country"), "");
        String environment = ParameterParserUtil.parseStringParam(rs.getString("ehs.environment"), "");
        String robotdecli = ParameterParserUtil.parseStringParam(rs.getString("ehs.robotdecli"), "");
        String stat = ParameterParserUtil.parseStringParam(rs.getString("ehs.statdetail"), "");
        String crbVersion = ParameterParserUtil.parseStringParam(rs.getString("ehs.crbversion"), "");
        int tothits = rs.getInt("ehs.total_hits");
        int totsize = rs.getInt("ehs.total_size");
        int tottime = rs.getInt("ehs.total_time");
        int inthits = rs.getInt("ehs.internal_hits");
        int intsize = rs.getInt("ehs.internal_size");
        int inttime = rs.getInt("ehs.internal_time");
        int imghits = rs.getInt("ehs.img_hits");
        int imgsize = rs.getInt("ehs.img_size");
        int imgsizem = rs.getInt("ehs.img_size_max");
        int jshits = rs.getInt("ehs.js_hits");
        int jssize = rs.getInt("ehs.js_size");
        int jssizem = rs.getInt("ehs.js_size_max");
        int csshits = rs.getInt("ehs.css_hits");
        int csssize = rs.getInt("ehs.css_size");
        int csssizem = rs.getInt("ehs.css_size_max");
        int htmlhits = rs.getInt("ehs.html_hits");
        int htmlsize = rs.getInt("ehs.html_size");
        int htmlsizem = rs.getInt("ehs.html_size_max");
        int mediahits = rs.getInt("ehs.media_hits");
        int mediasize = rs.getInt("ehs.media_size");
        int mediasizem = rs.getInt("ehs.media_size_max");
        int nbt = rs.getInt("ehs.nb_thirdparty");

        //TODO remove when working in test with mockito and autowired
        factoryTestCaseExecutionHttpStat = new FactoryTestCaseExecutionHttpStat();

        JSONObject statJS = new JSONObject();
        try {
            statJS = new JSONObject(stat);
        } catch (JSONException ex) {
            LOG.warn("Exception when parsing statdetail column to JSON.", ex);
        }

        return factoryTestCaseExecutionHttpStat.create(id, time, controlStatus, system, application, test, testcase, country, environment, robotdecli,
                tothits, totsize, tottime,
                inthits, intsize, inttime,
                imgsize, imgsizem, imghits,
                jssize, jssizem, jshits,
                csssize, csssizem, csshits,
                htmlsize, htmlsizem, htmlhits,
                mediasize, mediasizem, mediahits,
                nbt, crbVersion, statJS, testcase, time, system, time);
    }

}
