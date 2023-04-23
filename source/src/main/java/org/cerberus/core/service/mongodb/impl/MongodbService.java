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

import java.util.Iterator;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import org.bson.Document;
import org.cerberus.core.service.mongodb.IMongodbService;
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
    public AnswerItem<AppService> callMONGODB(String servicePath, String requestString, String method, String operation, int timeOutMs,
            String system, TestCaseExecution tcexecution) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();

        AppService serviceMONGODB = factoryAppService.create("", AppService.TYPE_MONGODB, method, "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", null,
                "", null, "", null, null);
        serviceMONGODB.setProxy(false);
        serviceMONGODB.setProxyHost(null);
        serviceMONGODB.setProxyPort(0);
        serviceMONGODB.setServiceRequest(requestString);
        serviceMONGODB.setOperation(operation);
        serviceMONGODB.setServicePath(servicePath);
        serviceMONGODB.setTimeoutms(timeOutMs);
        String mongoDBResult = null;
        JSONArray mongoDBResultArray = new JSONArray();

        // Replace the uri string with your MongoDB deployment's connection string
//        String uri = "mongodb+srv://<username>:<password>@tracking.6zo4t.mongodb.net/?retryWrites=true&w=majority";
        LOG.debug("Starting MONGODB Find. " + servicePath);

//        servicePath = "mongodb+srv://cerberus-test:hZpR78LHEMdi4@tracking.6zo4t.mongodb.net/?retryWrites=true&w=majority";
//        try (MongoClient mongoClient = MongoClients.create(servicePath)) {
        try (MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(servicePath))
                .applyToSocketSettings(builder
                        -> builder.connectTimeout(timeOutMs, MILLISECONDS) // TODO Debug as still 30 sec.
                        .readTimeout(timeOutMs, MILLISECONDS)) // TODO Debug as still 30 sec.
                .build())) {

            LOG.debug("Connection : " + operation);
            String MDBdtb = operation.split("\\.")[0];
            String MDBColl = operation.split("\\.")[1];
            LOG.debug("Connection : " + MDBdtb + " / " + MDBColl);
            MongoDatabase database = mongoClient.getDatabase(MDBdtb);
            MongoCollection<Document> collection = database.getCollection(MDBColl);
//            Bson projectionFields = Projections.fields(
            //                    Projections.include("title", "imdb"),
//                    Projections.excludeId());
//            Document doc = collection.find(eq("ID_CDE_GET", "7883"))
//                    .projection(projectionFields)
//                    //                    .sort(Sorts.descending("imdb.rating"))
//                    .first();
//            Document doc = collection.find(eq("ID_CDE_GET", "7883")).first();
//                    .projection(projectionFields)
            //                    .sort(Sorts.descending("imdb.rating"))
//                    .first();
            BasicDBObject whereQuery = new BasicDBObject();

            JSONObject requestObject = new JSONObject(requestString);

            @SuppressWarnings("unchecked")
            Iterator<String> keys = requestObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
//                if (requestObject.get(key) instanceof String) {
                whereQuery.put(key, requestObject.get(key));
                // do something with jsonObject here      
//                }
            }
            LOG.debug("Parsed : " + requestObject);
            LOG.debug("Parsed : " + whereQuery);

            MongoCursor<Document> cursor = collection.find(whereQuery)
                    //                    .projection(projectionFields)
                    .iterator();
            try {
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

            } finally {
                cursor.close();
            }

//            if (doc == null) {
//                LOG.debug("No results found.");
//            } else {
//                LOG.debug("Results found.");
//                LOG.debug(doc.toJson());
//                mongoDBResult = doc.toJson();
//                serviceMONGODB.setResponseHTTPBody(mongoDBResult);
//                serviceMONGODB.setResponseHTTPCode(0);
//                serviceMONGODB.setResponseHTTPVersion("");
////                serviceMONGODB.setResponseHeaderList(responseHttp.getResponseHeaderList());
//            }
        } catch (MongoTimeoutException ex) {
            LOG.info("Exception when performing the MONGODB Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_TIMEOUT);
            message.resolveDescription("SERVICEURL", servicePath);
            message.resolveDescription("TIMEOUT", String.valueOf(timeOutMs));
            message.resolveDescription("DESCRIPTION", ex.toString());
            result.setResultMessage(message);
            return result;
        } catch (MongoSocketOpenException ex) {
            LOG.info("Exception when performing the MONGODB Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.resolveDescription("DESCRIPTION", ex.toString());
            result.setResultMessage(message);
            return result;
        } catch (Exception ex) {
            LOG.info("Exception when performing the MONGODB Call. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.resolveDescription("DESCRIPTION", ex.toString());
            result.setResultMessage(message);
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
