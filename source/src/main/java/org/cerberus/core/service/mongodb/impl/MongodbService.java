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
package org.cerberus.core.service.mongodb.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.result.UpdateResult;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import org.bson.Document;
import org.cerberus.core.service.mongodb.IMongodbService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author bcivel
 */
@Service
public class MongodbService implements IMongodbService {

    @Autowired
    IRecorderService recorderService;
    @Autowired
    IFactoryAppServiceHeader factoryAppServiceHeader;
    @Autowired
    IParameterService parameterService;
    @Autowired
    IFactoryAppService factoryAppService;
    @Autowired
    IAppServiceService AppServiceService;
    @Autowired
    IProxyService proxyService;

    private static final Logger LOG = LogManager.getLogger(MongodbService.class);

    @Override
    public AnswerItem<AppService> callMONGODB(String servicePath, String requestString, String requestExtra1String, String method, String operation, int timeOutMs,
            String system, TestCaseExecution tcexecution) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();

        AppService serviceMONGODB = factoryAppService.create("", AppService.TYPE_MONGODB, method, "", "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", null,
                "", null, "", null, null);
        serviceMONGODB.setProxy(false);
        serviceMONGODB.setProxyHost(null);
        serviceMONGODB.setProxyPort(0);
        serviceMONGODB.setServiceRequest(requestString);
        serviceMONGODB.setOperation(operation);
        serviceMONGODB.setServicePath(servicePath);
        serviceMONGODB.setTimeoutms(timeOutMs);
        serviceMONGODB.setStart(new Timestamp(new Date().getTime()));
        serviceMONGODB.setSecrets(tcexecution.getSecrets());
        String mongoDBResult = null;
        JSONArray mongoDBResultArray = new JSONArray();

        LOG.debug("Starting MONGODB Find. " + servicePath);

        try (MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyToSocketSettings(builder
                        -> builder.connectTimeout(timeOutMs, MILLISECONDS) // TODO Debug as still 30 sec.
                        .readTimeout(timeOutMs, MILLISECONDS)) // TODO Debug as still 30 sec.
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(timeOutMs, MILLISECONDS))
                .applyConnectionString(new ConnectionString(servicePath))
                .build())) {

            LOG.debug("Connection : " + operation);

            // Check connexion format.
            if (!operation.contains(".") || operation.startsWith(".") || operation.endsWith(".")) {
                result.setItem(serviceMONGODB);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_MONGO_COLLECTIONFORMAT);
                message.resolveDescription("SERVICEURL", servicePath);
                message.resolveDescription("COLLECTIONPATH", operation);
                result.setResultMessage(message);
                return result;
            }

            String MDBdtb = operation.split("\\.")[0];
            String MDBColl = operation.split("\\.")[1];
            LOG.debug("Connection : " + MDBdtb + " / " + MDBColl);

            // Does the database exist.
            MongoIterable<String> listDTB = mongoClient.listDatabaseNames();
            StringBuilder databaseExist = new StringBuilder();
            listDTB.forEach(databaseName -> {
                if (databaseName.equals(MDBdtb)) {
                    databaseExist.append(databaseName);
                }
            });
            if (databaseExist.toString().isEmpty()) {
                result.setItem(serviceMONGODB);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_MONGO_DATABASENOTEXIST);
                message.resolveDescription("SERVICEURL", servicePath);
                message.resolveDescription("DATABASE", MDBdtb);
                result.setResultMessage(message);
                return result;
            }

            MongoDatabase database = mongoClient.getDatabase(MDBdtb);

            // Does the collection exist.
            MongoIterable<String> listCOL = database.listCollectionNames();
            StringBuilder collectionExist = new StringBuilder();
            listCOL.forEach(collectionName -> {
                if (collectionName.equals(MDBColl)) {
                    collectionExist.append(collectionName);
                }
            });
            if (collectionExist.toString().isEmpty()) {
                result.setItem(serviceMONGODB);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_MONGO_COLLECTIONNOTEXIST);
                message.resolveDescription("SERVICEURL", servicePath);
                message.resolveDescription("COLLECTION", MDBColl);
                result.setResultMessage(message);
                return result;
            }

            MongoCollection<Document> collection = database.getCollection(MDBColl);

            switch (method) {

                case AppService.METHOD_MONGODBFIND:
                    try (
                            MongoCursor<Document> cursor = collection.find(BasicDBObject.parse(requestString))
                                    .iterator()) {
                                int i = 0;
                                while (cursor.hasNext() && i < 5) {
                                    LOG.debug("Results found.");
                                    mongoDBResult = cursor.next().toJson();
                                    i++;
                                    mongoDBResultArray.put(new JSONObject(mongoDBResult));
                                    LOG.debug(mongoDBResult);
//                    System.out.println(cursor.next().toJson());
                                }
                                serviceMONGODB.setResponseHTTPBody(mongoDBResultArray.toString());
                                serviceMONGODB.setResponseNb(i);
                                serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));

                            }
                    break;

                case AppService.METHOD_MONGODBUPDATEONE:
                    if ((StringUtil.isNotEmptyOrNULLString(requestString)) && (StringUtil.isNotEmptyOrNULLString(requestExtra1String))) {
                        UpdateResult resultUpdate = collection.updateOne(BasicDBObject.parse(requestString), BasicDBObject.parse(requestExtra1String));
                        LOG.debug("Results updated.");
//                        mongoDBResultArray.put(new JSONObject(resultUpdate.getUpsertedId()));
//                        LOG.debug(mongoDBResultArray);
//                    System.out.println(cursor.next().toJson());
//                        serviceMONGODB.setResponseHTTPBody(mongoDBResultArray.toString());
                        serviceMONGODB.setResponseNb(Integer.valueOf(String.valueOf(resultUpdate.getMatchedCount())));
                    } else {
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_MONGO_EMPTYREQUEST);
                        result.setResultMessage(message);
                        serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));
                        return result;
                    }
                    serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));
                    break;

            }

        } catch (MongoTimeoutException ex) {
            String localex = ex.toString();
            LOG.info("Exception when performing the MONGODB Call. " + localex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_TIMEOUT);
            message.resolveDescription("SERVICEURL", servicePath);
            message.resolveDescription("TIMEOUT", String.valueOf(timeOutMs));
            message.resolveDescription("DESCRIPTION", localex);
            result.setResultMessage(message);
            serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));
            return result;
            
        } catch (MongoSocketOpenException ex) {
            LOG.info("Exception when performing the MONGODB Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.resolveDescription("DESCRIPTION", ex.toString());
            result.setResultMessage(message);
            serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));
            return result;
            
        } catch (Exception ex) {
            LOG.error(ex, ex);
            LOG.info("Exception when performing the MONGODB Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.resolveDescription("DESCRIPTION", ex.toString());
            result.setResultMessage(message);
            serviceMONGODB.setEnd(new Timestamp(new Date().getTime()));
            return result;
        }

        // Get result Content Type.
        if (mongoDBResult != null) {
            serviceMONGODB.setResponseHTTPBodyContentType(AppServiceService.guessContentType(serviceMONGODB, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));
        }

        result.setItem(serviceMONGODB);
        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
        message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", method));
        message.setDescription(message.getDescription().replace("%SERVICEPATH%", servicePath));
        result.setResultMessage(message);
        return result;

    }

}
