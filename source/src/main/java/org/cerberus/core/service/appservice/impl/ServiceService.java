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
package org.cerberus.core.service.appservice.impl;

import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.entity.CountryEnvironmentDatabase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.ICountryEnvironmentDatabaseService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.appservice.IServiceService;
import org.cerberus.core.service.ftp.IFtpService;
import org.cerberus.core.service.kafka.IKafkaService;
import org.cerberus.core.service.rest.IRestService;
import org.cerberus.core.service.soap.ISoapService;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.TopicPartition;
import org.cerberus.core.api.dto.appservice.AppServiceCallPropertyDTO;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.factory.IFactoryCountryEnvParam;
import org.cerberus.core.crud.factory.IFactoryInvariant;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.service.csvfile.ICsvFileService;
import org.cerberus.core.service.mongodb.IMongodbService;

/**
 * @author bcivel
 * @author vertigo17
 */
@Service
public class ServiceService implements IServiceService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ServiceService.class);

    @Autowired
    ICsvFileService fileService;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IAppServiceService appServiceService;
    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private IFactoryTestCaseExecution factoryExecution;
    @Autowired
    private IFactoryInvariant factoryInvariant;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private ICountryEnvironmentParametersService cepService;
    @Autowired
    private ICountryEnvParamService ceparamService;
    @Autowired
    private IFactoryCountryEnvParam factoryCountryEnvParam;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ISoapService soapService;
    @Autowired
    private IVariableService variableService;
    @Autowired
    private IRestService restService;
    @Autowired
    private IMongodbService mongodbService;
    @Autowired
    private IKafkaService kafkaService;
    @Autowired
    private IFtpService ftpService;
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;

    @Override
    public AnswerItem<AppService> callService(String service, String targetNbEvents, String targetNbSec, String database, String manualRequest, String manualServicePathParam, String manualOperation, TestCaseExecution execution, int timeoutMs) {
        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
        String decodedRequest;
        String decodedServicePath = null;
        String decodedOperation;
        String decodedAttachement;
        AnswerItem<AppService> result = new AnswerItem<>();
        AnswerItem<String> answerDecode = new AnswerItem<>();
        String system = execution.getApplicationObj().getSystem();
        String country = execution.getCountry();
        String environment = execution.getEnvironment();
        LOG.debug("Starting callService : " + service + " with database : " + database);

        AppService appService = null;

        try {

            // If Service information is not defined, we create it from request, servicePath and operation parameters forcing in SOAP mode.
            if (StringUtil.isEmptyOrNull(service)) {
                LOG.debug("Creating AppService from parameters.");
                appService = factoryAppService.create("null", AppService.TYPE_REST, "", null, "", "", manualRequest, "", "", "", "", "", "", "Automatically created Service from datalib.",
                        manualServicePathParam, true, "", manualOperation, false, "", false, "", false, "", null, null, null, null, null, null);
                service = "null";

            } else {
                // If Service information is defined, we get it from database.
                LOG.debug("Getting AppService from service : " + service);
                appService = appServiceService.convert(appServiceService.readByKeyWithDependency(service, true));

            }
            execution.addSecret(appService.getAuthPassword());
            execution.setCurrentApplication(appService.getApplication());

            String servicePath;

            if (appService == null) {

                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICENAME", service)
                        .resolveDescription("DESCRIPTION", "Service does not exist !!");

            } else if (StringUtil.isEmptyOrNull(appService.getServicePath())) {
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICENAME", service)
                        .resolveDescription("DESCRIPTION", "Service path is not defined");

            } else {

                // We start by calculating the servicePath and decode it.
                servicePath = appService.getServicePath();

                try {

                    // Decode Service Path
                    answerDecode = variableService.decodeStringCompletly(servicePath, execution, null, false);
                    servicePath = answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                .resolveDescription("SERVICENAME", service)
                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", "Service Path").getDescription());
                        LOG.debug("Service Call interupted due to decode 'Service Path'.");
                        result.setResultMessage(message);
                        return result;
                    }

                } catch (CerberusEventException cee) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICEWITHPATH)
                            .resolveDescription("SERVICENAME", service)
                            .resolveDescription("SERVICEPATH", decodedServicePath)
                            .resolveDescription("DESCRIPTION", cee.getMessageError().getDescription());
                    result.setResultMessage(message);
                    return result;
                }

                execution.addSecret(StringUtil.getPasswordFromAnyUrl(servicePath));

                // Autocomplete of service path is disable for KAFKA and MONGODB service (this is because there could be a list of host).
                if (!appService.getType().equals(AppService.TYPE_KAFKA) && !appService.getType().equals(AppService.TYPE_MONGODB)) {

                    if (!(StringUtil.isURL(servicePath))) {
                        // The URL defined inside the Service or directly from parameter is not complete and we need to add the first part taken either 
                        // the data from tCExecution of related database.

                        if (StringUtil.isEmptyOrNull(database)) {

                            // We reformat servicePath in order to add the context from the application execution.
                            String targetHost = "";
                            CountryEnvironmentParameters envappli;
                            if (execution.getCurrentApplication() != null) {
                                envappli = execution.getCountryEnvApplicationParams().getOrDefault(execution.getCurrentApplication(), execution.getCountryEnvApplicationParam());
                            } else {
                                envappli = execution.getCountryEnvApplicationParam();
                            }

                            if (StringUtil.isNotEmptyOrNull(envappli.getIp())) {
                                targetHost = StringUtil.getURLFromString(envappli.getIp(), envappli.getUrl(), "", "");
                            } else {
                                targetHost = execution.getUrl();
                            }
                            servicePath = StringUtil.getURLFromString(targetHost, "", appService.getServicePath(), "http://");

                        } else {

                            // We reformat servicePath in order to add the context from the databaseUrl definition and corresponding from the country and environment of the execution.
                            try {
                                CountryEnvironmentDatabase countryEnvironmentDatabase;
                                countryEnvironmentDatabase = countryEnvironmentDatabaseService.convert(this.countryEnvironmentDatabaseService.readByKey(system,
                                        country, environment, database));
                                if (countryEnvironmentDatabase == null) {
                                    message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLNOTEXIST)
                                            .resolveDescription("SERVICEURL", appService.getServicePath())
                                            .resolveDescription("SYSTEM", system)
                                            .resolveDescription("COUNTRY", country)
                                            .resolveDescription("ENV", environment)
                                            .resolveDescription("DATABASE", database);
                                    result.setResultMessage(message);
                                    return result;

                                } else {
                                    String soapURL = countryEnvironmentDatabase.getSoapUrl();
                                    if (StringUtil.isEmptyOrNull(soapURL)) {
                                        message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLEMPTY)
                                                .resolveDescription("SERVICEURL", appService.getServicePath())
                                                .resolveDescription("SYSTEM", system)
                                                .resolveDescription("COUNTRY", country)
                                                .resolveDescription("ENV", environment)
                                                .resolveDescription("DATABASE", database);
                                        result.setResultMessage(message);
                                        return result;
                                    }
                                    // soapURL from database is not empty so we prefix the Service URL with it.
                                    servicePath = StringUtil.getURLFromString(soapURL, "", servicePath, "");

                                    if (!StringUtil.isURL(servicePath)) {
                                        message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKO)
                                                .resolveDescription("SERVICEURL", servicePath)
                                                .resolveDescription("SOAPURL", soapURL)
                                                .resolveDescription("SERVICEPATH", appService.getServicePath());
                                        result.setResultMessage(message);
                                        return result;

                                    }

                                }

                            } catch (CerberusException ex) {
                                message = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_SERVICE_URLKOANDDATABASESOAPURLNOTEXIST)
                                        .resolveDescription("SERVICEURL", servicePath)
                                        .resolveDescription("SYSTEM", system)
                                        .resolveDescription("COUNTRY", country)
                                        .resolveDescription("ENV", environment)
                                        .resolveDescription("DATABASE", database);
                                result.setResultMessage(message);
                                return result;
                            }

                        }

                    }
                }

                // appService object and target servicePath is now clean. We can start to decode.
                decodedServicePath = servicePath;
                decodedRequest = appService.getServiceRequest();
                LOG.debug("AppService with correct path is now OK : " + servicePath);

                try {

                    // Decode Service Path again as the change done by automatic complete of it following application configuration could have inserted some new variables.
                    answerDecode = variableService.decodeStringCompletly(decodedServicePath, execution, null, false);
                    decodedServicePath = answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                .resolveDescription("SERVICENAME", service)
                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", "Service Path").getDescription());
                        LOG.debug("Service Call interupted due to decode 'Service Path'.");
                        result.setResultMessage(message);
                        return result;
                    }

                    // Decode Request
                    answerDecode = variableService.decodeStringCompletly(decodedRequest, execution, null, false);
                    decodedRequest = answerDecode.getItem();
                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                .resolveDescription("SERVICENAME", service)
                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", "Service Request").getDescription());
                        LOG.debug("Service Call interupted due to decode 'Service Request'.");
                        result.setResultMessage(message);
                        return result;
                    }

                    // Decode Header List
                    List<AppServiceHeader> objectHeaderList = new ArrayList<>();
                    for (AppServiceHeader object : appService.getHeaderList()) {
                        answerDecode = variableService.decodeStringCompletly(object.getKey(), execution, null, false);
                        object.setKey(answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Header Key '" + object.getKey() + "'";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        answerDecode = variableService.decodeStringCompletly(object.getValue(), execution, null, false);
                        object.setValue(answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Header Value '" + object.getKey() + "'";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        objectHeaderList.add(object);
                    }
                    appService.setHeaderList(objectHeaderList);

                    // Decode ContentDetail List
                    List<AppServiceContent> objectContentList = new ArrayList<>();
                    for (AppServiceContent object : appService.getContentList()) {
                        answerDecode = variableService.decodeStringCompletly(object.getKey(), execution, null, false);
                        object.setKey(answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Content Key '" + object.getKey() + "'";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        answerDecode = variableService.decodeStringCompletly(object.getValue(), execution, null, false);
                        object.setValue(answerDecode.getItem());
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Content Value '" + object.getKey() + "'";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        objectContentList.add(object);
                    }
                    appService.setContentList(objectContentList);

                } catch (CerberusEventException cee) {
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICEWITHPATH)
                            .resolveDescription("SERVICENAME", service)
                            .resolveDescription("SERVICEPATH", decodedServicePath)
                            .resolveDescription("DESCRIPTION", cee.getMessageError().getDescription());
                    result.setResultMessage(message);
                    return result;
                }

                // Get from parameter whether we define a token or not (in order to trace the cerberus calls in http header)
                String token = null;
                if (parameterService.getParameterBooleanByKey("cerberus_callservice_enablehttpheadertoken", system, true)) {
                    token = String.valueOf(execution.getId());
                }
                // Get from parameter the call timeout to be used.
                if (timeoutMs == 0) {
                    timeoutMs = parameterService.getParameterIntegerByKey("cerberus_callservice_timeoutms", system, 60000);
                }
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

                            answerDecode = variableService.decodeStringCompletly(decodedOperation, execution, null, false);
                            decodedOperation = answerDecode.getItem();
                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                String field = "Operation";
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                        .resolveDescription("SERVICENAME", service)
                                        .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                result.setResultMessage(message);
                                return result;
                            }

                            answerDecode = variableService.decodeStringCompletly(decodedAttachement, execution, null, false);
                            decodedAttachement = answerDecode.getItem();
                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                String field = "Attachement URL";
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                        .resolveDescription("SERVICENAME", service)
                                        .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                result.setResultMessage(message);
                                return result;
                            }

                        } catch (CerberusEventException cee) {
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("SERVICEPATH", decodedServicePath)
                                    .resolveDescription("DESCRIPTION", cee.getMessageError().getDescription());
                            result.setResultMessage(message);
                            return result;
                        }

                        /**
                         * Call SOAP and store it into the execution.
                         */
                        result = soapService.callSOAP(decodedRequest, decodedServicePath, decodedOperation, decodedAttachement,
                                appService.getHeaderList(), token, timeoutMs, system);
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
                            case AppService.METHOD_HTTPDELETE:
                            case AppService.METHOD_HTTPPUT:
                            case AppService.METHOD_HTTPPATCH:
                                /**
                                 * Call REST and store it into the execution.
                                 */
                                result = restService.callREST(decodedServicePath, decodedRequest, appService.getMethod(), appService.getBodyType(),
                                        appService.getHeaderList(), appService.getContentList(), token, timeoutMs, system, appService.isFollowRedir(), execution, appService.getDescription(),
                                        appService.getAuthType(), appService.getAuthUser(), appService.getAuthPassword(), appService.getAuthAddTo());
                                message = result.getResultMessage()
                                        .resolveDescription("SERVICENAME", service);
                                result.setResultMessage(message);
                                break;

                            default:
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                        .resolveDescription("SERVICENAME", service)
                                        .resolveDescription("DESCRIPTION", "Method : '" + appService.getMethod() + "' for REST Service is not supported by the engine");
                                result.setResultMessage(message);
                        }

                        break;

                    /**
                     * MONGODB.
                     */
                    case AppService.TYPE_MONGODB:

                        /**
                         * MONGODB.
                         */
                        switch (appService.getMethod()) {

                            case AppService.METHOD_MONGODBFIND:
                                /**
                                 * Call MONGODB and store it into the execution.
                                 */
                                result = mongodbService.callMONGODB(decodedServicePath, decodedRequest, appService.getMethod(),
                                        appService.getOperation(), timeoutMs, system, execution);
                                message = result.getResultMessage();
                                break;

                            default:
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Method : '" + appService.getMethod() + "' for MONGODB Service is not supported by the engine."));
                                result.setResultMessage(message);
                        }

                        break;

                    /**
                     * KAFKA.
                     */
                    case AppService.TYPE_KAFKA:

                        String decodedKey = appService.getKafkaKey();
                        answerDecode = variableService.decodeStringCompletly(decodedKey, execution, null, false);
                        decodedKey = answerDecode.getItem();
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Kafka Key";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        String decodedTopic = appService.getKafkaTopic();
                        answerDecode = variableService.decodeStringCompletly(decodedTopic, execution, null, false);
                        decodedTopic = answerDecode.getItem();
                        if (!(answerDecode.isCodeStringEquals("OK"))) {
                            // If anything wrong with the decode --> we stop here with decode message in the action result.
                            String field = "Kafka topic";
                            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                    .resolveDescription("SERVICENAME", service)
                                    .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                            LOG.debug("Service Call interupted due to decode '" + field + "'.");
                            result.setResultMessage(message);
                            return result;
                        }

                        String decodedSchemaRegistryURL = appService.getSchemaRegistryURL();
                        if (appService.isAvroEnable()) {
                            answerDecode = variableService.decodeStringCompletly(decodedSchemaRegistryURL, execution, null, false);
                            decodedSchemaRegistryURL = answerDecode.getItem();
                            if (!(answerDecode.isCodeStringEquals("OK"))) {
                                // If anything wrong with the decode --> we stop here with decode message in the action result.
                                String field = "Kafka Schema Registry URL";
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                        .resolveDescription("SERVICENAME", service)
                                        .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                result.setResultMessage(message);
                                return result;
                            }
                        }

                        switch (appService.getMethod()) {

                            case AppService.METHOD_KAFKAPRODUCE:
                                /**
                                 * Call REST and store it into the execution.
                                 */
                                result = kafkaService.produceEvent(decodedTopic, decodedKey, decodedRequest, decodedServicePath, appService.getHeaderList(), appService.getContentList(),
                                        token, appService.isAvroEnable(), decodedSchemaRegistryURL, appService.isAvroEnableKey(), appService.getAvroSchemaKey(), appService.isAvroEnableValue(), appService.getAvroSchemaValue(), timeoutMs);
                                message = result.getResultMessage();
                                break;

                            case AppService.METHOD_KAFKASEARCH:

                                int targetNbEventsInt = ParameterParserUtil.parseIntegerParam(targetNbEvents, 1);
                                int targetNbSecInt = ParameterParserUtil.parseIntegerParam(targetNbSec, 30);
                                if (targetNbEventsInt <= 0) {
                                    // We get at least 1 Event.
                                    targetNbEventsInt = 1;
                                }
                                if (targetNbSecInt <= 4) {
                                    // We wait at least 1 second.
                                    targetNbSecInt = 5;
                                }

                                appService.setServicePath(decodedServicePath);
                                appService.setKafkaTopic(decodedTopic);
                                appService.setKafkaKey(null);
                                appService.setServiceRequest(null);
                                appService.setKafkaWaitNbEvent(targetNbEventsInt);
                                appService.setKafkaWaitSecond(targetNbSecInt);
                                appService.setKafkaResponsePartition(-1);
                                appService.setKafkaResponseOffset(-1);

                                // Control Latest Offset are defined.
                                AnswerItem<Map<TopicPartition, Long>> resultConsume = new AnswerItem<>();
                                HashMap<String, Map<TopicPartition, Long>> tempKafka = new HashMap<>();

                                String decodedFilterPath = appService.getKafkaFilterPath();
                                String decodedFilterValue = appService.getKafkaFilterValue();
                                String decodedFilterHeaderPath = appService.getKafkaFilterHeaderPath();
                                String decodedFilterHeaderValue = appService.getKafkaFilterHeaderValue();

                                try {

                                    if (execution.getKafkaLatestOffset() == null) {

                                        resultConsume = kafkaService.seekEvent(decodedTopic, decodedServicePath, appService.getContentList(), timeoutMs);

                                        if (!(resultConsume.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEARCHKAFKA.getCode()))) {
                                            LOG.debug("Call interupted due to error when opening Kafka consume. " + resultConsume.getMessageDescription());
                                            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_KAFKACONSUMERSEEK)
                                                    .resolveDescription("SERVICE", service)
                                                    .resolveDescription("DETAIL", resultConsume.getMessageDescription()));
                                        }
                                        LOG.debug("Saving Map to key : " + kafkaService.getKafkaConsumerKey(decodedTopic, decodedServicePath));
                                        tempKafka.put(kafkaService.getKafkaConsumerKey(decodedTopic, decodedServicePath), resultConsume.getItem());

                                    }

                                    answerDecode = variableService.decodeStringCompletly(decodedFilterPath, execution, null, false);
                                    decodedFilterPath = answerDecode.getItem();
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Filter Path";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                                .resolveDescription("SERVICENAME", service)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                        result.setResultMessage(message);
                                        return result;
                                    }

                                    answerDecode = variableService.decodeStringCompletly(decodedFilterValue, execution, null, false);
                                    decodedFilterValue = answerDecode.getItem();
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Filter Value";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                                .resolveDescription("SERVICENAME", service)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                        result.setResultMessage(message);
                                        return result;
                                    }

                                    answerDecode = variableService.decodeStringCompletly(decodedFilterHeaderPath, execution, null, false);
                                    decodedFilterHeaderPath = answerDecode.getItem();
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Filter Header Path";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                                .resolveDescription("SERVICENAME", service)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                        result.setResultMessage(message);
                                        return result;
                                    }

                                    answerDecode = variableService.decodeStringCompletly(decodedFilterHeaderValue, execution, null, false);
                                    decodedFilterHeaderValue = answerDecode.getItem();
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Filter Header Value";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                                                .resolveDescription("SERVICENAME", service)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Service Call interupted due to decode '" + field + "'.");
                                        result.setResultMessage(message);
                                        return result;
                                    }

                                } catch (CerberusEventException cee) {
                                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEARCHKAFKA)
                                            .resolveDescription("TOPIC", decodedTopic)
                                            .resolveDescription("HOSTS", decodedServicePath)
                                            .resolveDescription("EX", cee.getMessageError().getDescription());
                                    result.setResultMessage(message);
                                    result.setItem(appService);
                                    return result;
                                }

                                appService.setKafkaFilterPath(decodedFilterPath);
                                appService.setKafkaFilterValue(decodedFilterValue);
                                appService.setKafkaFilterHeaderPath(decodedFilterHeaderPath);
                                appService.setKafkaFilterHeaderValue(decodedFilterHeaderValue);

                                String kafkaKey = kafkaService.getKafkaConsumerKey(decodedTopic, decodedServicePath);
                                AnswerItem<String> resultSearch = kafkaService.searchEvent(execution.getKafkaLatestOffset().get(kafkaKey), decodedTopic, decodedServicePath,
                                        appService.getHeaderList(), appService.getContentList(), decodedFilterPath, decodedFilterValue, decodedFilterHeaderPath, decodedFilterHeaderValue,
                                        appService.isAvroEnable(), decodedSchemaRegistryURL, appService.isAvroEnableKey(), appService.isAvroEnableValue(), targetNbEventsInt, targetNbSecInt);

                                if (!(resultSearch.isCodeStringEquals("OK"))) {
                                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                    message = message.resolveDescription("DESCRIPTION", resultSearch.getMessageDescription());
                                } else {
                                    message = resultSearch.getResultMessage();
                                }

                                appService.setResponseHTTPBody(resultSearch.getItem());
                                appService.setResponseHTTPBodyContentType(appServiceService.guessContentType(appService, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));

                                result.setItem(appService);
                                result.setResultMessage(message);

                                break;

                            default:
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Method : '" + appService.getMethod() + "' for KAFKA Service is not supported by the engine (Use " + AppService.METHOD_KAFKAPRODUCE + " or " + AppService.METHOD_KAFKASEARCH + ")."));
                                result.setResultMessage(message);
                        }

                        break;

                    case AppService.TYPE_FTP:
                        /**
                         * FTP.
                         */
                        switch (appService.getMethod()) {
                            case AppService.METHOD_HTTPGET:
                            case AppService.METHOD_HTTPPOST:
                                appService.setTimeoutms(timeoutMs);
                                result = ftpService.callFTP(decodedServicePath, system, decodedRequest, appService.getMethod(), appService.getFileName(), appService.getService(), timeoutMs);
                                message = result.getResultMessage();
                                break;
                            default:
                                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                                message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Method : '" + appService.getMethod() + "' for FTP Service is not supported by the engine."));
                                result.setResultMessage(message);
                        }
                        break;

                    default:
                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                        message.setDescription(message.getDescription().replace("%SERVICE%", service));
                        message.setDescription(message.getDescription().replace("%DESCRIPTION%", "Service Type : '" + appService.getType() + "' is not supported by the engine."));
                        result.setResultMessage(message);
                }
                message.setDescription(message.getDescription().replace("%TOPIC%", appService.getKafkaTopic()));
                message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", appService.getType()));

            }

        } catch (CerberusException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("SERVICENAME", service)
                    .resolveDescription_NoLimit("DESCRIPTION", "Cerberus exception on CallService : " + ex.getMessageError().getDescription());
            result.setResultMessage(message);
            result.setItem(appService);
            return result;
        } catch (Exception ex) {
            LOG.error("Exception when performing CallService Action. " + ex.toString(), ex);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("SERVICENAME", service)
                    .resolveDescription("DESCRIPTION", "Exception on CallService : " + ex.toString());
            result.setResultMessage(message);
            result.setItem(appService);
            return result;
        }

        result.setResultMessage(message);
        LOG.debug("Ended callService : " + service + " with database : " + database + " Result : " + message.getDescription());
        return result;
    }

    @Override
    public AnswerItem<AppService> callAPI(String service, String country, String environment, String application, String system, int timeout,
            String kafkaNb, String kafkaTime, List<AppServiceCallPropertyDTO> props, String login) {
        AnswerItem<AppService> ans = null;

        // Secure non null parameters.
        application = (application == null) ? "" : application;
        country = (country == null) ? "" : country;
        environment = (environment == null) ? "" : environment;
        system = (system == null) ? "" : system;
        kafkaNb = (kafkaNb == null) ? "" : kafkaNb;
        kafkaTime = (kafkaTime == null) ? "" : kafkaTime;

        // Simulation Execution
        TestCaseExecution execution = factoryExecution.create(0, "test-folderid", "testid", "test-description", "", "", environment, country, "", "", "", "", "", "", "", "", 0, 0,
                "", "", application, null, "", "", 0, 0, 0, 0, 0, 0, true, "", "", "", "", null, null, null, 0, "", "", "", "", "", "", null, null,
                "", 0, "", null, "", "", "", "", "", "", "", "", "", "", "", 0, 0, "system", "", null, "", null);
        execution.setEnvironmentData(environment);

        // Simulation Application
        Application applicationObj = null;
        try {
            applicationObj = applicationService.convert(applicationService.readByKey(application));
        } catch (CerberusException e) {
            LOG.error(e, e);
        }
        if (applicationObj == null) {
            applicationObj = Application.builder()
                    .application(application)
                    .system(system)
                    .type(Application.TYPE_SRV) // TODO System
                    .build();
        }
        execution.setApplicationObj(applicationObj);
        LOG.debug(applicationObj);

        // Simulation CountryEnvironment
        CountryEnvParam envParam = null;
        try {
            envParam = ceparamService.convert(ceparamService.readByKey(system, country, environment));
        } catch (CerberusException e) {
            LOG.error(e, e);
        }
        if (envParam == null) {
            envParam = factoryCountryEnvParam.create(system, country, environment);
        }
        execution.setCountryEnvParam(envParam);

        // Simulation CountryEnvironment
        CountryEnvironmentParameters env = null;
        try {
            env = cepService.convert(cepService.readByKey(system, country, environment, application));
            if ((env != null) && StringUtil.isNotEmptyOrNull(env.getIp())) {
                execution.setUrl(env.getIp());
            }
        } catch (CerberusException e) {
            LOG.error(e, e);
        }
        if (env == null) {
            env = CountryEnvironmentParameters.builder()
                    .application(application)
                    .country(country)
                    .url("").ip("")
                    .var1("").var2("").var3("").var4("")
                    .domain("")
                    .build();
        }
        execution.setCountryEnvApplicationParam(env);

        // Simulation Invariant Environment
        Invariant envObj = null;
        try {
            envObj = invariantService.convert(invariantService.readByKey(Invariant.IDNAME_ENVIRONMENT, environment));
        } catch (CerberusException e) {
            LOG.error(e, e);
        }
        if (envObj == null) {
            envObj = factoryInvariant.create(Invariant.IDNAME_ENVIRONMENT, environment, 10, "", "", "", "", "", "", "", "", "", "", "");
        }
        execution.setEnvironmentDataObj(envObj);
        execution.setEnvironmentObj(envObj);

        // Simulation Invariant Country
        Invariant countryObj = null;
        try {
            countryObj = invariantService.convert(invariantService.readByKey(Invariant.IDNAME_COUNTRY, country));
        } catch (CerberusException e) {
            LOG.error(e, e);
        }
        if (countryObj == null) {
            countryObj = factoryInvariant.create(Invariant.IDNAME_COUNTRY, country, 10, "", "", "", "", "", "", "", "", "", "", "");
        }
        execution.setCountryObj(countryObj);

        TestCaseCountryProperties prop;
        for (AppServiceCallPropertyDTO callProp : props) {
            if (callProp.isActive() && !callProp.isToDelete()) {
                prop = TestCaseCountryProperties.builder()
                        .country(country).property(callProp.getKey()).value1(callProp.getValue())
                        .type(TestCaseCountryProperties.TYPE_TEXT).nature(TestCaseCountryProperties.NATURE_STATIC).build();
                execution.addTestCaseCountryPropertyList(prop);
            }
        }

        // We can now start the call with all data prepared.
        try {

            ans = this.callService(service, kafkaNb, kafkaTime, null, "", "", "", execution, timeout);

        } catch (Exception e) {
            LOG.error(e, e);
        }

        return ans;
    }

}
