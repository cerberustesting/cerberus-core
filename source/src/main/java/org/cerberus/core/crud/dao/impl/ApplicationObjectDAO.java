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
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IApplicationObjectDAO;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.factory.IFactoryApplicationObject;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.database.DatabaseSpring;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.SqlUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.security.UserSecurity;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@AllArgsConstructor
@Repository
public class ApplicationObjectDAO implements IApplicationObjectDAO {

    private final DatabaseSpring databaseSpring;
    private final IFactoryApplicationObject factoryApplicationObject;
    private final IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectDAO.class);
    private static final String OBJECT_NAME = "ApplicationObject";
    private static final int MAX_ROW_SELECTED = 100000;

    // Create a new {@link ApplicationObject}
    private static final String CREATE = "INSERT INTO `applicationobject` (`application`,`object`,`value`,`screenshotfilename`,`XOffset`,`YOffset`,`usrcreated`,`datecreated`,`usrmodif`,`datemodif`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // Remove an existing {@link ApplicationObject}
    private static final String DELETE = "DELETE FROM `applicationobject` WHERE `ID` = ?";

    @Override
    public AnswerItem<ApplicationObject> readByKeyTech(int id) {
        AnswerItem<ApplicationObject> ans = new AnswerItem<>();
        MessageEvent msg;
        final String query = "SELECT * FROM `applicationobject` obj WHERE `ID` = ?";
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();
            ApplicationObject applicationObject = loadFromResultSet(rs);
            ans.setItem(applicationObject);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "READ_BY_KEY");
        } catch (Exception e) {
            LOG.error("Unable to read by key: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerItem<ApplicationObject> readByKey(String application, String object) {
        AnswerItem<ApplicationObject> ans = new AnswerItem<>();
        MessageEvent msg;
        final String query = "SELECT * FROM `applicationobject` obj WHERE `Application` = ? AND `Object` = ?";
        LOG.debug("SQL : {}", query);
        LOG.debug("SQL.param.app : {}", application);
        LOG.debug("SQL.param.obj : {}", object);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            ApplicationObject applicationObject = null;
            // Prepare and execute query
            preStat.setString(1, application);
            preStat.setString(2, object);

            try (ResultSet rs = preStat.executeQuery()) {
                while (rs.next()) {
                    applicationObject = loadFromResultSet(rs);
                }
                ans.setItem(applicationObject);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_KEY");
            }
        } catch (Exception e) {
            LOG.error("Unable to execute query: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<ApplicationObject> readByApplication(String application) {
        AnswerList<ApplicationObject> ans = new AnswerList<>();
        MessageEvent msg;
        final String query = "SELECT * FROM `applicationobject` obj WHERE `application` = ?";
        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            preStat.setString(1, application);
            try (ResultSet rs = preStat.executeQuery()) {
                List<ApplicationObject> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(loadFromResultSet(rs));
                }
                ans.setDataList(al);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_APP");
            }
        } catch (Exception e) {
            LOG.error("Unable to read by app: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public BufferedImage readImageByKey(String application, String object) {
        BufferedImage image = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_applicationobject_path Parameter not found");
        AnswerItem<Parameter> parameterAnswerItem = parameterService.readByKey("", "cerberus_applicationobject_path");
        if (parameterAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter parameter = parameterAnswerItem.getItem();
            String uploadPath = parameter.getValue();
            AnswerItem<ApplicationObject> applicationObjectAnswerItem = readByKey(application, object);
            if (applicationObjectAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                ApplicationObject applicationObject = applicationObjectAnswerItem.getItem();
                if ((applicationObject != null) && StringUtil.isNotEmptyOrNull(applicationObject.getScreenshotFilename())) {
                    String filePath = uploadPath + File.separator + applicationObject.getID() + File.separator + applicationObject.getScreenshotFilename();
                    File picture = new File(filePath);
                    try {
                        image = ImageIO.read(picture);
                    } catch (IOException e) {
                        LOG.warn("Impossible to read the image : " + picture, e.toString());
                    }
                }
            } else {
                LOG.warn("Application Object not found");
            }
        } else {
            LOG.warn("cerberus_applicationobject_path Parameter not found");
        }
        parameterAnswerItem.setResultMessage(msg);
        return image;
    }

    private static void deleteFolder(File folder, boolean deleteIt) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if (deleteIt) {
            folder.delete();
        }
    }

    @Override
    public Answer uploadFile(int id, FileItem file) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_applicationobject_path Parameter not found");
        AnswerItem<Parameter> parameterAnswerItem = parameterService.readByKey("", "cerberus_applicationobject_path");
        if (parameterAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter parameter = parameterAnswerItem.getItem();
            String uploadPath = parameter.getValue();
            File appDir = new File(uploadPath + File.separator + id);
            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (SecurityException se) {
                    LOG.error("Unable to create application dir: {}", se.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            se.toString());
                    parameterAnswerItem.setResultMessage(msg);
                }
            }
            if (parameterAnswerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                deleteFolder(appDir, false);
                File picture = new File(uploadPath + File.separator + id + File.separator + file.getName());
                try {
                    file.write(picture);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                            "Application Object file uploaded");
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Application Object").replace("%OPERATION%", "Upload"));
                } catch (Exception e) {
                    LOG.error("Unable to upload application object file: {}", e.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            e.toString());
                }
            }
        } else {
            LOG.warn("cerberus_applicationobject_path Parameter not found");
        }
        parameterAnswerItem.setResultMessage(msg);
        return parameterAnswerItem;
    }

    @Override
    public AnswerList<ApplicationObject> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<ApplicationObject> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<ApplicationObject> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disregarding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM applicationobject obj ");

        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
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

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
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

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
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
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else if (objectList.isEmpty()) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    response = new AnswerList<>(objectList, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    response = new AnswerList<>(objectList, nrTotalRows);
                }

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
    public AnswerList<ApplicationObject> readByApplicationByCriteria(String application, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch, List<String> systems) {
        AnswerList<ApplicationObject> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<ApplicationObject> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT SQL_CALC_FOUND_ROWS obj.* FROM applicationobject obj ");
        query.append(" left outer JOIN application app ON obj.Application = app.Application ");

        searchSQL.append(" where 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (obj.`Application` like ?");
            searchSQL.append(" or obj.`Object` like ?");
            searchSQL.append(" or obj.`Value` like ?");
            searchSQL.append(" or obj.`ScreenshotFileName` like ?");
            searchSQL.append(" or obj.`UsrCreated` like ?");
            searchSQL.append(" or obj.`DateCreated` like ?");
            searchSQL.append(" or obj.`UsrModif` like ?");
            searchSQL.append(" or obj.`DateModif` like ?)");
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

        if (StringUtil.isNotEmptyOrNull(application)) {
            searchSQL.append(" and (obj.`Application` = ? )");
        }

        if (CollectionUtils.isNotEmpty(systems)) {
            systems.add("");
            searchSQL.append(" and (").append(SqlUtil.generateInClause("app.`System`", systems)).append(") ");
        }

        searchSQL.append(" AND ").append(UserSecurity.getSystemAllowForSQL("app.`System`")).append(" ");

        query.append(searchSQL);

        if (StringUtil.isNotEmptyOrNull(column)) {
            query.append(" order by ").append(column).append(" ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        LOG.debug("SQL : {}", query);

        try (Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
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
            if (StringUtil.isNotEmptyOrNull(application)) {
                preStat.setString(i++, application);
            }

            if (CollectionUtils.isNotEmpty(systems)) {
                for (String system : systems) {
                    preStat.setString(i++, system);
                }
            }

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
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
    public Answer create(ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(CREATE)) {

            int i = 1;
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getObject());
            preStat.setString(i++, object.getValue());
            preStat.setString(i++, object.getScreenshotFilename());
            preStat.setString(i++, object.getXOffset());
            preStat.setString(i++, object.getYOffset());
            preStat.setString(i++, object.getUsrCreated());
            preStat.setString(i++, object.getDateCreated());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i, object.getDateModif());
            preStat.executeUpdate();

            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.error("Unable to create campaign object: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }

        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer delete(ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(DELETE)) {

            preStat.setInt(1, object.getID());
            preStat.executeUpdate();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.error("Unable to delete application object: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public Answer update(String application, String appObject, ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg;
        String query = "UPDATE `applicationobject` SET `application` = ?, `object` = ?, `value` = ?, `screenshotfilename` = ?, `XOffset` = ?, `YOffset` = ?, "
                + "`usrcreated` = ?, `datecreated` = ?, `usrmodif` = ?, `datemodif` = ? "
                + " WHERE `application` = ? AND `object` = ?";

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            int i = 1;
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getObject());
            preStat.setString(i++, object.getValue());
            preStat.setString(i++, object.getScreenshotFilename());
            preStat.setString(i++, object.getXOffset());
            preStat.setString(i++, object.getYOffset());
            preStat.setString(i++, object.getUsrCreated());
            preStat.setString(i++, object.getDateCreated());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i++, object.getDateModif());
            preStat.setString(i++, application);
            preStat.setString(i, appObject);
            preStat.executeUpdate();

            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.error("Unable to update campaign object: {}", e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        }
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM applicationobject obj ");

        searchSQL.append("WHERE 1=1 ");

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
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
        query.append(" order by ").append(columnName).append(" asc");

        LOG.debug("SQL : {}", query);
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
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

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {

                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
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

    @Override
    public AnswerList<String> readDistinctValuesByApplicationByCriteria(String application, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individualColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder()
                .append("SELECT distinct ")
                .append(columnName)
                .append(" as distinctValues FROM applicationobject obj ");

        searchSQL.append("WHERE 1=1");
        if (StringUtil.isNotEmptyOrNull(application)) {
            searchSQL.append(" and (`Application` = ? )");
        }

        if (StringUtil.isNotEmptyOrNull(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
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
        query.append(" order by ").append(columnName).append(" asc");

        LOG.debug("SQL : {}", query);

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
                Statement stm = connection.createStatement()) {

            int i = 1;
            if (StringUtil.isNotEmptyOrNull(application)) {
                preStat.setString(i++, application);
            }
            if (StringUtil.isNotEmptyOrNull(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
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

            try (ResultSet resultSet = preStat.executeQuery();
                    ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()")) {
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }

                int nrTotalRows = 0;
                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }

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

    private ApplicationObject loadFromResultSet(ResultSet rs) throws SQLException {
        int id = ParameterParserUtil.parseIntegerParam(rs.getString("ID"), -1);
        String application = ParameterParserUtil.parseStringParam(rs.getString("Application"), "");
        String object = ParameterParserUtil.parseStringParam(rs.getString("Object"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");
        String screenshotFilename = ParameterParserUtil.parseStringParam(rs.getString("ScreenshotFileName"), "");
        String xOffset = ParameterParserUtil.parseStringParam(rs.getString("XOffset"), "");
        String yOffset = ParameterParserUtil.parseStringParam(rs.getString("YOffset"), "");
        String usrCreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        String dateCreated = ParameterParserUtil.parseStringParam(rs.getString("DateCreated"), "");
        String usrModif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        String dateModif = ParameterParserUtil.parseStringParam(rs.getString("DateModif"), "");

        return factoryApplicationObject.create(id, application, object, value, screenshotFilename, xOffset, yOffset, usrCreated, dateCreated, usrModif, dateModif);
    }
}
