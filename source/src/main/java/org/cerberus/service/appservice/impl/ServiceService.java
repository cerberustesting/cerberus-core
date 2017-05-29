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
package org.cerberus.service.appservice.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.appservice.IServiceService;
import org.cerberus.service.file.IFileService;
import org.cerberus.service.rest.IRestService;
import org.cerberus.service.soap.ISoapService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 * @author vertigo17
 *
 */
@Service
public class ServiceService implements IServiceService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ServiceService.class);

    @Autowired
    IFileService fileService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IRestService restService;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;

    @Override
    public AnswerItem<AppService> callService(String service, String database, String request, String servicePathParam, String operation, TestCaseExecution tCExecution) {
        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
        String decodedRequest;
        String decodedServicePath = null;
        String decodedOperation;
        String decodedAttachement;
        AnswerItem result = new AnswerItem();
        String system = tCExecution.getApplicationObj().getSystem();
        String country = tCExecution.getCountry();
        String environment = tCExecution.getEnvironment();
        LOG.debug("Starting callService : " + service + " with database : " + database);
        try {

            AppService appService;

            // If Service information is not defined, we create it from request, servicePath and operation parameters forcing in SOAP mode.
            if (StringUtil.isNullOrEmpty(service)) {
                LOG.debug("Creating AppService from parameters.");
                appService = factoryAppService.create("null", AppService.TYPE_SOAP, "", "", "", request, "Automatically created Service from datalib.",
                        servicePathParam, "", operation, null, null, null, null);
                service = "null";

            } else {
                // If Service information is defined, we get it from database.
                LOG.debug("Getting AppService from service : " + service);
                appService = appServiceService.convert(appServiceService.readByKeyWithDependency(service, "Y"));

            }

            String servicePath;

            if (appService == null) {

                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Service does not exist !!"));

            } else if (StringUtil.isNullOrEmpty(appService.getServicePath())) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE).resolveDescription("DESCRIPTION", "Service path is not defined");
            } else {

                // We start by calculating the servicePath and decode it.
                servicePath = appService.getServicePath();
                if (!(StringUtil.isURL(servicePath))) {
                    // The URL defined inside the Service or directly from parameter is not complete and we need to add the first part taken either 
                    // the data from tCExecution of related database.

                    if (StringUtil.isNullOrEmpty(database)) {

                        // We reformat servicePath in order to add the context from the application execution.
                        servicePath = StringUtil.getURLFromString(tCExecution.getCountryEnvironmentParameters().getIp(),
                                tCExecution.getCountryEnvironmentParameters().getUrl(), appService.getServicePath(), "http://");

                    } else {

                        // We reformat servicePath in order to add the context from the databaseUrl definition and corresponding from the country and environment of the execution.
                        try {
                            CountryEnvironmentDatabase countryEnvironmentDatabase;
                            countryEnvironmentDatabase = countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                                    country, environment, database));
                            if (countryEnvironmentDatabase == null) {
                                message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLNOTEXIST);
                                message.setDescription(message.getDescription()
                                        .replace("%SERVICEURL%", appService.getServicePath())
                                        .replace("%SYSTEM%", system)
                                        .replace("%COUNTRY%", country)
                                        .replace("%ENV%", environment)
                                        .replace("%DATABASE%", database));
                                result.setResultMessage(message);
                                return result;

                            } else {
                                String soapURL = countryEnvironmentDatabase.getSoapUrl();
                                if (StringUtil.isNullOrEmpty(soapURL)) {
                                    message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLEMPTY);
                                    message.setDescription(message.getDescription()
                                            .replace("%SERVICEURL%", appService.getServicePath())
                                            .replace("%SYSTEM%", system)
                                            .replace("%COUNTRY%", country)
                                            .replace("%ENV%", environment)
                                            .replace("%DATABASE%", database));
                                    result.setResultMessage(message);
                                    return result;
                                }
                                // soapURL from database is not empty so we prefix the Service URL with it.
                                servicePath = StringUtil.getURLFromString(soapURL, "", servicePath, "");

                                if (!StringUtil.isURL(servicePath)) {
                                    message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKO);
                                    message.setDescription(message.getDescription()
                                            .replace("%SERVICEURL%", servicePath)
                                            .replace("%SOAPURL%", soapURL)
                                            .replace("%SERVICEPATH%", appService.getServicePath()));
                                    result.setResultMessage(message);
                                    return result;

                                }

                            }

                        } catch (CerberusException ex) {
                            message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLNOTEXIST);
                            message.setDescription(message.getDescription()
                                    .replace("%SERVICEURL%", servicePath)
                                    .replace("%SYSTEM%", system)
                                    .replace("%COUNTRY%", country)
                                    .replace("%ENV%", environment)
                                    .replace("%DATABASE%", database));
                            result.setResultMessage(message);
                            return result;
                        }

                    }

                }

                // appService object and target servicePath is now clean. We can start to decode.
                decodedServicePath = servicePath;
                decodedRequest = appService.getServiceRequest();
                LOG.debug("AppService with correct path is  now OK : " + servicePath);
                AnswerItem<String> answerDecode = new AnswerItem();

                try {

                    // Decode Service Path
                    answerDecode = variableService.decodeStringCompletly(decodedServicePath, tCExecution, null, false);
                    decodedServicePath = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        message = answerDecode.getResultMessage().resolveDescription("FIELD", "Service Path");
                        LOG.debug("Property interupted due to decode 'Service Path'.");
                        result.setResultMessage(message);
                        return result;
                    }
                    // Decode Request
                    answerDecode = variableService.decodeStringCompletly(decodedRequest, tCExecution, null, false);
                    decodedRequest = (String) answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        message = answerDecode.getResultMessage().resolveDescription("FIELD", "Service Request");
                        LOG.debug("Property interupted due to decode 'Service Request'.");
                        result.setResultMessage(message);
                        return result;
                    }
                    // Decode Header List
                    List<AppServiceHeader> objectResponseHeaderList = new ArrayList<>();
                    for (AppServiceHeader object : appService.getHeaderList()) {
                        answerDecode = variableService.decodeStringCompletly(object.getKey(), tCExecution, null, false);
                        object.setKey((String) answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Header Key " + object.getKey();
                            message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                            LOG.debug("Property interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        answerDecode = variableService.decodeStringCompletly(object.getValue(), tCExecution, null, false);
                        object.setValue((String) answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Header Value " + object.getKey();
                            message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                            LOG.debug("Property interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        objectResponseHeaderList.add(object);
                    }
                    // Decode ContentDetail List
                    appService.setResponseHeaderList(objectResponseHeaderList);
                    List<AppServiceContent> objectResponseContentList = new ArrayList<>();
                    for (AppServiceContent object : appService.getContentList()) {
                        answerDecode = variableService.decodeStringCompletly(object.getKey(), tCExecution, null, false);
                        object.setKey((String) answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Content Key " + object.getKey();
                            message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                            LOG.debug("Property interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        answerDecode = variableService.decodeStringCompletly(object.getValue(), tCExecution, null, false);
                        object.setValue((String) answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Content Value " + object.getKey();
                            message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                            LOG.debug("Property interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        objectResponseContentList.add(object);
                    }
                    appService.setContentList(objectResponseContentList);

                } catch (CerberusEventException cee) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICEWITHPATH);
                    message.setDescription(message.getDescription().replace("%SERVICENAME%", service));
                    message.setDescription(message.getDescription().replace("%SERVICEPATH%", decodedServicePath));
                    message.setDescription(message.getDescription().replace("%DESCRIPTION%", cee.getMessageError().getDescription()));
                    result.setResultMessage(message);
                    return result;
                }

                // Get from parameter whether we define a token or not (in order to trace the cerberus calls in http header)
                String token = null;
                if (parameterService.getParameterBooleanByKey("cerberus_callservice_enablehttpheadertoken", system, true)) {
                    token = String.valueOf(tCExecution.getId());
                }
                // Get from parameter the call timeout to be used.
                int timeOutMs = parameterService.getParameterIntegerByKey("cerberus_callservice_timeoutms", system, 60000);
                // The rest of the data will be prepared depending on the TYPE and METHOD used.
                switch (appService.getType()) {
                    case AppService.TYPE_SOAP:
                        LOG.debug("This is a SOAP Service");

                        /**
                         * SOAP. Decode Envelope and Operation replacing
                         * properties encapsulated with %
                         */
                        decodedOperation = appService.getOperation();
                        decodedAttachement = appService.getAttachementURL();
                        try {

                            answerDecode = variableService.decodeStringCompletly(decodedOperation, tCExecution, null, false);
                            decodedOperation = (String) answerDecode.getItem();
                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                String field = "Operation";
                                message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                                LOG.debug("Property interupted due to decode '" + field + "'.");
                                result.setResultMessage(message);
                                return result;
                            }
                            
                            answerDecode = variableService.decodeStringCompletly(decodedAttachement, tCExecution, null, false);
                            decodedAttachement = (String) answerDecode.getItem();
                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                String field = "Attachement URL";
                                message = answerDecode.getResultMessage().resolveDescription("FIELD", field);
                                LOG.debug("Property interupted due to decode '" + field + "'.");
                                result.setResultMessage(message);
                                return result;
                            }

                        } catch (CerberusEventException cee) {
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                            message.setDescription(message.getDescription().replace("%SERVICENAME%", service));
                            message.setDescription(message.getDescription().replace("%SERVICEPATH%", decodedServicePath));
                            message.setDescription(message.getDescription().replace("%DESCRIPTION%", cee.getMessageError().getDescription()));
                            result.setResultMessage(message);
                            return result;
                        }

                        /**
                         * Call SOAP and store it into the execution.
                         */
                        result = soapService.callSOAP(decodedRequest, decodedServicePath, decodedOperation, decodedAttachement,
                                appService.getHeaderList(), token, timeOutMs, system);
                        LOG.debug("SOAP Called done.");

                        LOG.debug("Result message." + result.getResultMessage());
                        message = result.getResultMessage();

                        break;

                    case AppService.TYPE_REST:

                        /**
                         * REST.
                         */
                        switch (appService.getMethod()) {
                            case AppService.METHOD_HTTPGET:
                            case AppService.METHOD_HTTPPOST:

                                /**
                                 * Call REST and store it into the execution.
                                 */
                                result = restService.callREST(decodedServicePath, decodedRequest, appService.getMethod(),
                                        appService.getHeaderList(), appService.getContentList(), token, timeOutMs, system);
                                message = result.getResultMessage();
                                break;

                            default:
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Method : '" + appService.getMethod() + "' for REST Service is not supported by the engine."));
                                result.setResultMessage(message);
                        }

                        break;

                    default:
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%SERVICE%", service));
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Service Type : '" + appService.getType() + "' is not supported by the engine."));
                        result.setResultMessage(message);
                }
            }

        } catch (CerberusException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICENAME%", service));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Cerberus exception on CallService : " + ex.getMessageError().getDescription()));
            result.setResultMessage(message);
            return result;
        } catch (Exception ex) {
            LOG.error("Exception when performing CallService Action. " + ex.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICENAME%", service));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Cerberus exception on CallService : " + ex.toString()));
            return result;
        }

        message.setDescription(message.getDescription().replace("%SERVICENAME%", service));
        result.setResultMessage(message);
        LOG.debug("Ended callService : " + service + " with database : " + database + " Result : " + message.getDescription());
        return result;
    }

}
