/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
import javax.imageio.ImageIO;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.crud.dao.IApplicationObjectDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.ApplicationObject;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.factory.IFactoryApplicationObject;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.security.UserSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Implements methods defined on IApplicationDAO
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
@Repository
public class ApplicationObjectDAO implements IApplicationObjectDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryApplicationObject factoryApplicationObject;
    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectDAO.class);

    private final String OBJECT_NAME = "ApplicationObject";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    /**
     * Declare SQL queries used by this {@link ApplicationObject}
     *
     * @author Aurelien Bourdon
     */
    private static interface Query {

        /**
         * Get list of {@link ApplicationObject} associated with the given id
         */
        String READ_BY_KEY = "SELECT * FROM `applicationobject` WHERE `ID` = ?";

        /**
         * Get list of {@link ApplicationObject} associated with the given key
         */
        String READ_BY_KEY1 = "SELECT * FROM `applicationobject` WHERE `Application` = ? AND `Object` = ?";

        /**
         * Get list of {@link ApplicationObject} associated with the given
         * {@link Application}
         */
        String READ_BY_APP = "SELECT * FROM `applicationobject` WHERE `application` = ?";

        /**
         * Create a new {@link ApplicationObject}
         */
        String CREATE = "INSERT INTO `applicationobject` (`application`,`object`,`value`,`screenshotfilename`,`usrcreated`,`datecreated`,`usrmodif`,`datemodif`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        /**
         * Remove an existing {@link ApplicationObject}
         */
        String DELETE = "DELETE FROM `applicationobject` WHERE `ID` = ?";

        /**
         * Remove all {@link ApplicationObject} of a {@link Application}
         */
        String DELETE_BY_APP = "DELETE FROM `applicationobject` WHERE `application` = ?";
    }

    @Override
    public AnswerItem<ApplicationObject> readByKeyTech(int id) {
        AnswerItem<ApplicationObject> ans = new AnswerItem<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_KEY)) {
            // Prepare and execute query
            preStat.setInt(1, id);
            ResultSet rs = preStat.executeQuery();
            ApplicationObject ao = loadFromResultSet(rs);
            ans.setItem(ao);
            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "READ_BY_KEY");
        } catch (Exception e) {
            LOG.warn("Unable to read by key: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerItem<ApplicationObject> readByKey(String application, String object) {
        AnswerItem<ApplicationObject> ans = new AnswerItem<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_KEY1)) {
            ApplicationObject ao = null;
            // Prepare and execute query
            preStat.setString(1, application);
            preStat.setString(2, object);
            ResultSet rs = preStat.executeQuery();
            try {
                while (rs.next()) {
                    ao = loadFromResultSet(rs);
                }
                ans.setItem(ao);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_KEY");
            }catch (Exception e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
            } finally {
            	if(rs != null) {
            		rs.close();
            	}
            }
        } catch (Exception e) {
            LOG.warn("Unable to read by key: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public AnswerList<ApplicationObject> readByApplication(String Application) {
        AnswerList<ApplicationObject> ans = new AnswerList<>();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.READ_BY_APP)) {
            // Prepare and execute query
            preStat.setString(1, Application);
            ResultSet rs = preStat.executeQuery();
            try {
            	List<ApplicationObject> al = new ArrayList<>();
                while (rs.next()) {
                    al.add(loadFromResultSet(rs));
                }
                ans.setDataList(al);
                // Set the final message
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                        .resolveDescription("OPERATION", "READ_BY_APP");
            }catch (Exception e) {
                LOG.warn("Unable to execute query : " + e.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        e.toString());
            } finally {
            	if(rs != null) {
            		rs.close();
            	}
            }
        } catch (Exception e) {
            LOG.warn("Unable to read by app: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }
        return ans;
    }

    @Override
    public BufferedImage readImageByKey(String application, String object) {
        BufferedImage image = null;
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_applicationobject_path Parameter not found");
        AnswerItem a = parameterService.readByKey("", "cerberus_applicationobject_path");
        if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter p = (Parameter) a.getItem();
            String uploadPath = p.getValue();
            a = readByKey(application, object);
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                ApplicationObject ao = (ApplicationObject) a.getItem();
                if (ao != null) {
                    File picture = new File(uploadPath + File.separator + ao.getID() + File.separator + ao.getScreenShotFileName());
                    try {
                        image = ImageIO.read(picture);
                    } catch (IOException e) {
                        LOG.warn("Impossible to read the image");
                    }
                }
            } else {
                LOG.warn("Application Object not found");
            }
        } else {
            LOG.warn("cerberus_applicationobject_path Parameter not found");
        }
        a.setResultMessage(msg);
        return image;
    }

    private static void deleteFolder(File folder, boolean deleteit) {
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
        if (deleteit) {
            folder.delete();
        }
    }

    @Override
    public Answer uploadFile(int id, FileItem file) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                "cerberus_applicationobject_path Parameter not found");
        AnswerItem a = parameterService.readByKey("", "cerberus_applicationobject_path");
        if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            Parameter p = (Parameter) a.getItem();
            String uploadPath = p.getValue();
            File appDir = new File(uploadPath + File.separator + id);
            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (SecurityException se) {
                    LOG.warn("Unable to create application dir: " + se.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            se.toString());
                    a.setResultMessage(msg);
                }
            }
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                deleteFolder(appDir, false);
                File picture = new File(uploadPath + File.separator + id + File.separator + file.getName());
                try {
                    file.write(picture);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("DESCRIPTION",
                            "Application Object file uploaded");
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "Application Object").replace("%OPERATION%", "Upload"));
                } catch (Exception e) {
                    LOG.warn("Unable to upload application object file: " + e.getMessage());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                            e.toString());
                }
            }
        } else {
            LOG.warn("cerberus_applicationobject_path Parameter not found");
        }
        a.setResultMessage(msg);
        return a;
    }

    @Override
    public AnswerList<ApplicationObject> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList<ApplicationObject> response = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<ApplicationObject> objectList = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM applicationobject ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        query.append(searchSQL);



        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        
        try(Connection connection = this.databaseSpring.connect();
        		PreparedStatement preStat = connection.prepareStatement(query.toString());
        		Statement stm = connection.createStatement();) {
            
            int i = 1;
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            
            try(ResultSet resultSet = preStat.executeQuery();
            		ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
                //gets the data
                while (resultSet.next()) {
                    objectList.add(this.loadFromResultSet(resultSet));
                }

                //get the total number of rows
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
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

            } 
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
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
        List<ApplicationObject> objectList = new ArrayList<ApplicationObject>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS obj.* FROM applicationobject obj ");
        query.append(" left outer JOIN application app ON obj.Application = app.Application ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (obj.`Application` like ?");
            searchSQL.append(" or obj.`Object` like ?");
            searchSQL.append(" or obj.`Value` like ?");
            searchSQL.append(" or obj.`ScreenshotFileName` like ?");
            searchSQL.append(" or obj.`UsrCreated` like ?");
            searchSQL.append(" or obj.`DateCreated` like ?");
            searchSQL.append(" or obj.`UsrModif` like ?");
            searchSQL.append(" or obj.`DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }

        if (!StringUtil.isNullOrEmpty(application)) {
            searchSQL.append(" and (obj.`Application` = ? )");
        }

        if ((systems != null) && (!systems.isEmpty())) {
            systems.add("");
            searchSQL.append(" and (").append(SqlUtil.generateInClause("app.`System`", systems)).append(") ");
        }

        searchSQL.append( " AND ").append(UserSecurity.getSystemAllowForSQL("app.`System`")).append(" ");

        query.append(searchSQL);


        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }
                if (!StringUtil.isNullOrEmpty(application)) {
                    preStat.setString(i++, application);
                }

                if ((systems != null) && (!systems.isEmpty())) {
                    for (String mysystem : systems) {
                        preStat.setString(i++, mysystem);
                    }
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        objectList.add(this.loadFromResultSet(resultSet));
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
    public Answer create(ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.CREATE)) {
            // Prepare and execute query
            preStat.setString(1, object.getApplication());
            preStat.setString(2, object.getObject());
            preStat.setString(3, object.getValue());
            preStat.setString(4, object.getScreenShotFileName());
            preStat.setString(5, object.getUsrCreated());
            preStat.setString(6, object.getDateCreated());
            preStat.setString(7, object.getUsrModif());
            preStat.setString(8, object.getDateModif());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create campaign object: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer delete(ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg = null;

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(Query.DELETE)) {
            // Prepare and execute query
            preStat.setInt(1, object.getID());
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "CREATE");
        } catch (Exception e) {
            LOG.warn("Unable to create campaign object: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public Answer update(String application, String appObject, ApplicationObject object) {
        Answer ans = new Answer();
        MessageEvent msg = null;
        String query = "UPDATE `applicationobject` SET `application` = ?, `object` = ?, `value` = ?, `screenshotfilename` = ?, `usrcreated` = ?, `datecreated` = ?, `usrmodif` = ?, `datemodif` = ? "
                + " WHERE `application` = ? AND `object` = ?";

        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query)) {
            // Prepare and execute query
            int i = 1;
            preStat.setString(i++, object.getApplication());
            preStat.setString(i++, object.getObject());
            preStat.setString(i++, object.getValue());
            preStat.setString(i++, object.getScreenShotFileName());
            preStat.setString(i++, object.getUsrCreated());
            preStat.setString(i++, object.getDateCreated());
            preStat.setString(i++, object.getUsrModif());
            preStat.setString(i++, object.getDateModif());
            preStat.setString(i++, application);
            preStat.setString(i++, appObject);
            preStat.executeUpdate();

            // Set the final message
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                    .resolveDescription("OPERATION", "UPDATE");
        } catch (Exception e) {
            LOG.warn("Unable to update campaign object: " + e.getMessage());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            ans.setResultMessage(msg);
        }

        return ans;
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList<String> answer = new AnswerList<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct `");
        query.append(columnName);
        query.append("` as distinctValues FROM applicationobject ");

        searchSQL.append("WHERE 1=1 ");

    	if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        
        query.append(searchSQL);
        query.append(" order by `").append(columnName).append("` asc");
        
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
        		Statement stm = connection.createStatement();) {

            int i = 1;
            
        	if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }
            
            try(ResultSet resultSet = preStat.executeQuery();
            		ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
            	//gets the data
                while (resultSet.next()) {
                    distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
                }
                //get the total number of rows
                
                int nrTotalRows = 0;

                if (rowSet != null && rowSet.next()) {
                    nrTotalRows = rowSet.getInt(1);
                }
                if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                    LOG.error("Partial Result in the query.");
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            }catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            } 
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
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
        List<String> individalColumnSearchValues = new ArrayList<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM applicationobject ");

        searchSQL.append("WHERE 1=1");
        if (!StringUtil.isNullOrEmpty(application)) {
            searchSQL.append(" and (`Application` = ? )");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`Application` like ?");
            searchSQL.append(" or `Object` like ?");
            searchSQL.append(" or `Value` like ?");
            searchSQL.append(" or `ScreenshotFileName` like ?");
            searchSQL.append(" or `UsrCreated` like ?");
            searchSQL.append(" or `DateCreated` like ?");
            searchSQL.append(" or `UsrModif` like ?");
            searchSQL.append(" or `DateModif` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString());
        		Statement stm = connection.createStatement();) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(application)) {
                preStat.setString(i++, application);
            }
            if (!StringUtil.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            try(ResultSet resultSet = preStat.executeQuery();
            		ResultSet rowSet = stm.executeQuery("SELECT FOUND_ROWS()");) {
            	//gets the data
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
                } else if (distinctValues.size() <= 0) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList<>(distinctValues, nrTotalRows);
                }
            }catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    private ApplicationObject loadFromResultSet(ResultSet rs) throws SQLException {
        Integer ID = ParameterParserUtil.parseIntegerParam(rs.getString("ID"), -1);
        String application = ParameterParserUtil.parseStringParam(rs.getString("Application"), "");
        String object = ParameterParserUtil.parseStringParam(rs.getString("Object"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");
        String screenshotfilename = ParameterParserUtil.parseStringParam(rs.getString("ScreenshotFileName"), "");
        String usrcreated = ParameterParserUtil.parseStringParam(rs.getString("UsrCreated"), "");
        String datecreated = ParameterParserUtil.parseStringParam(rs.getString("DateCreated"), "");
        String usrmodif = ParameterParserUtil.parseStringParam(rs.getString("UsrModif"), "");
        String datemodif = ParameterParserUtil.parseStringParam(rs.getString("DateModif"), "");

        return factoryApplicationObject.create(ID, application, object, value, screenshotfilename, usrcreated, datecreated, usrmodif, datemodif);
    }
}
