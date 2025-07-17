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
package org.cerberus.core.crud.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ConcurrentModificationException;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.config.cerberus.Property;
import org.cerberus.core.crud.dao.IParameterDAO;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.factory.IFactoryParameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class ParameterService implements IParameterService {

    @Autowired
    private IParameterDAO parameterDao;
    @Autowired
    private IFactoryParameter factoryParameter;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;

    private HashMap<String, Parameter> cacheEntry = new HashMap<>();

    private static final Logger LOG = LogManager.getLogger(ParameterService.class);

    @Override
    public HashMap<String, Parameter> getCacheEntry() {
        return cacheEntry;
    }

    @Override
    public void purgeCacheEntry(String parameter) {
        if (StringUtil.isEmptyOrNull(parameter)) {
            cacheEntry.clear();
            LOG.debug("All Parameter cache entries purged.");
        } else {
            try {
                for (Map.Entry<String, Parameter> entry : cacheEntry.entrySet()) {
                    String key = entry.getKey();
                    Parameter value = entry.getValue();
                    if (parameter == null || key.contains(parameter)) {
                        cacheEntry.remove(key);
                        LOG.debug("Purged Parameter " + key + " from cache entries.");
                    }
                }
            } catch (ConcurrentModificationException e) {
                // If we failed to removed the parameter entries (ConcurrentModificationException) , we purge everything ;-).
                cacheEntry.clear();
                LOG.debug("All Parameter cache entries purged. (specific parameter execution failed).");
            }
        }
    }

    @Override
    public Parameter findParameterByKey(String key, String system) throws CerberusException {
        Parameter myParameter;
        /**
         * We try to get the parameter using the system parameter but if it does
         * not exist or empty, we get it with system="" which correspond to the
         * default global Cerberus Parameter.
         */
        LocalDateTime currentTime = LocalDateTime.now();
        String cacheKey = key + '#' + system;
        if (cacheEntry == null) {
            cacheEntry = new HashMap<>();
        }

        /*
        if (Parameter.SHORT_CACHE_DURATION > 0 && Parameter.VALUE_cerberus_splashpage_enable.equals(key)) {
            if (cacheEntry.containsKey(cacheKey)
                    && cacheEntry.get(cacheKey) != null
                    && cacheEntry.get(cacheKey).getCacheEntryCreation() != null
                    && cacheEntry.get(cacheKey).getCacheEntryCreation().plusSeconds(Parameter.SHORT_CACHE_DURATION).isAfter(currentTime)) {
                LOG.debug("Return parameter from short cache Value.");
                return cacheEntry.get(cacheKey);
            }
        }*/
        if (Parameter.CACHE_DURATION > 0 && !Parameter.VALUE_cerberus_queueexecution_enable.equals(key)) {
            if (cacheEntry.containsKey(cacheKey)
                    && cacheEntry.get(cacheKey) != null
                    && cacheEntry.get(cacheKey).getCacheEntryCreation() != null
                    && cacheEntry.get(cacheKey).getCacheEntryCreation().plusSeconds(Parameter.CACHE_DURATION).isAfter(currentTime)) {
                LOG.debug("Return parameter '" + key + "' from cache Value.");
                return cacheEntry.get(cacheKey);
            }
        }

        try {
            LOG.debug("Trying to retrieve parameter : " + key + " - [" + system + "]");
            myParameter = parameterDao.findParameterByKey(system, key);
            if (myParameter != null && myParameter.getValue().isEmpty()) {
                myParameter = parameterDao.findParameterByKey("", key);
            }
        } catch (CerberusException ex) {
            LOG.debug("Trying to retrieve parameter (default value) : " + key + " - []");
            myParameter = parameterDao.findParameterByKey("", key);
            if (myParameter != null) {
                LOG.debug("Insert parameter '" + cacheKey + "' to cache.");
                myParameter.setCacheEntryCreation(currentTime);
                cacheEntry.put(cacheKey, myParameter);
            }
            return myParameter;
        }
        if (myParameter != null) {
            LOG.debug("Insert parameter '" + cacheKey + "' to cache.");
            myParameter.setCacheEntryCreation(currentTime);
            cacheEntry.put(cacheKey, myParameter);
        }
        return myParameter;
    }

    @Override
    public boolean getParameterBooleanByKey(String key, String system, boolean defaultValue) {
        Parameter myParameter;
        boolean outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = StringUtil.parseBoolean(myParameter.getValue());
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error("Error when trying to retreive parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
        }
        LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
        return outPutResult;
    }

    @Override
    public Integer getParameterIntegerByKey(String key, String system, Integer defaultValue) {
        Parameter myParameter;
        Integer outPutResult = defaultValue;
        if (Property.isSaaS() && Parameter.VALUE_queueexecution_global_threadpoolsize.equalsIgnoreCase(key)) {
            LOG.debug("Saas Mode is activated so parameter retrieved will be the master one.");
            key = Parameter.VALUE_queueexecution_global_threadpoolsize_master;
            try {
                myParameter = this.findParameterByKey(key, system);
                if (myParameter == null) {
                    return outPutResult;
                }
                outPutResult = Integer.valueOf(myParameter.getValue());
                LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
            } catch (CerberusException | NumberFormatException ex) {
                LOG.debug("Error when trying to retrieve parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
            }
            return outPutResult;
        }
        try {
            myParameter = this.findParameterByKey(key, system);
            if (myParameter == null) {
                return outPutResult;
            }
            outPutResult = Integer.valueOf(myParameter.getValue());
            LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error("Error when trying to retrieve parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
        }
        return outPutResult;
    }

    @Override
    public long getParameterLongByKey(String key, String system, long defaultValue) {
        Parameter myParameter;
        long outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = Long.parseLong(myParameter.getValue());
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error("Error when trying to retrieve parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
        }
        LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
        return outPutResult;
    }

    @Override
    public float getParameterFloatByKey(String key, String system, float defaultValue) {
        Parameter myParameter;
        float outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = Float.valueOf(myParameter.getValue());
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error("Error when trying to retrieve parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
        }
        LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
        return outPutResult;
    }

    @Override
    public String getParameterStringByKey(String key, String system, String defaultValue) {
        Parameter myParameter;
        String outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = myParameter.getValue();
        } catch (CerberusException ex) {
            LOG.error("Error when trying to retrieve parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex, ex);
        }
        LOG.debug("Success loading parameter : '" + key + "' for system : '" + system + "'. Value returned : '" + outPutResult + "'");
        return outPutResult;
    }

    @Override
    public List<Parameter> findAllParameter() throws CerberusException {
        return parameterDao.findAllParameter();
    }

    @Override
    public List<Parameter> findAllParameterWithSystem1(String system, String system1) throws CerberusException {
        return parameterDao.findAllParameterWithSystem1(system, system1);
    }

    @Override
    public AnswerList<Parameter> readWithSystem1BySystemByCriteria(String system, String system1, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return parameterDao.readWithSystem1BySystemByCriteria(system, system1, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem<Parameter> readWithSystem1ByKey(String system, String key, String system1) {
        return parameterDao.readWithSystem1ByKey(system, key, system1);
    }

    @Override
    public AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return parameterDao.readDistinctValuesWithSystem1ByCriteria(system, system1, searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerItem<Parameter> readByKey(String system, String param) {
        return parameterDao.readByKey(system, param);
    }

    @Override
    public Answer update(Parameter object) {
        Answer answer = parameterDao.update(object);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {

            // A parameter is changed so we purge the cache.
            purgeCacheEntry(object.getParam());

            // If we activate the parameter to active the job, we trigger it directly.
            if (Parameter.VALUE_cerberus_queueexecution_enable.equals(object.getParam()) && ParameterParserUtil.parseBooleanParam(object.getValue(), false)) {
                try {
                    // Run the Execution pool Job.
                    executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex) {
                    LOG.error("Exeption triggering the ThreadPool job.", ex);
                }
            }
        }
        return answer;
    }

    @Override
    public Answer setParameter(String parameterKey, String system, String value) {
        Answer answer = parameterDao.setParameter(parameterKey, system, value);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {

            // A parameter is changed so we purge the cache.
            purgeCacheEntry(parameterKey);

            // If we activate the parameter to active the job, we trigger it directly.
            if (Parameter.VALUE_cerberus_queueexecution_enable.equals(parameterKey) && ParameterParserUtil.parseBooleanParam(value, false)) {
                try {
                    // Run the Execution pool Job.
                    executionThreadPoolService.executeNextInQueueAsynchroneously(false);
                } catch (CerberusException ex) {
                    LOG.error("Exeption triggering the ThreadPool job.", ex);
                }
            }
        }
        return answer;
    }

    @Override
    public Answer create(Parameter object) {
        Answer answer = parameterDao.create(object);
        if (MessageEventEnum.DATA_OPERATION_OK.equals(answer.getResultMessage().getSource())) {
            purgeCacheEntry(object.getParam());
        }
        return answer;
    }

    @Override
    public Answer save(Parameter object, HttpServletRequest request) {
        Answer finalAnswer = new Answer();
        if (!hasPermissionsUpdate(object, request)) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNAUTHORISED);
            msg.setDescription(msg.getDescription()
                    .replace("%ITEM%", "Parameter")
                    .replace("%OPERATION%", "update")
                    .replace("%REASON%", "This parameter is protected and cannot be updated."));
            finalAnswer = new Answer(msg);
            LOG.warn("Attempt to modify Parameter '" + object.getParam() + "' to value '" + object.getValue() + "' refused !");
            return finalAnswer;
        }
        AnswerItem<Parameter> resp = readByKey(object.getSystem(), object.getParam());
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(resp.getResultMessage().getSource())) {
            /**
             * Object could not be found. We stop here and report the error.
             */
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

        } else if (resp.getItem() == null) {
            finalAnswer = create(object);
        } else if (!((object.getValue()).equals(resp.getItem().getValue())) && !StringUtil.SECRET_STRING.equals(object.getValue())) {
            // Parameter value is modified only if different from hiddem value (XXXXXXXXXX)
            finalAnswer = update(object);
        } else {
            /**
             * Nothing is done but everything went OK
             */
            finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED).resolveDescription("ITEM", "Parameter").resolveDescription("OPERATION", "Save").resolveDescription("REASON", "Parameter does not exist"));
        }
        return finalAnswer;
    }

    @Override
    public boolean hasPermissionsRead(Parameter testCase, HttpServletRequest request) {
        // Access right calculation.
        return true;
    }

    @Override
    public boolean hasPermissionsUpdate(Parameter parameter, HttpServletRequest request) {
        // Access right calculation.

        // master parameters cannot be changed.
        if (Parameter.VALUE_queueexecution_global_threadpoolsize_master.equalsIgnoreCase(parameter.getParam())) {
            return false;
        }
        // parameters that are protected in saas mode mode.
        if (Property.isSaaS()) {
            if (Parameter.VALUE_queueexecution_global_threadpoolsize.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_applicationobject_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_exeautomedia_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_exemanualmedia_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_ftpfile_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_testdatalibfile_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_gui_url.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_screenshot_max_size.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_smtp_host.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_smtp_isSetTls.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_smtp_password.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_smtp_port.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_smtp_username.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_manage_timeout.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_apikey_enable.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_url.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_webperf_thirdpartyfilepath.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_executeCerberusCommand_password.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_executeCerberusCommand_path.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_executeCerberusCommand_user.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_creditlimit_secondexeperday.equalsIgnoreCase(parameter.getParam())
                    || Parameter.VALUE_cerberus_creditlimit_nbexeperday.equalsIgnoreCase(parameter.getParam())) {

                return false;

            }
        }
        return request.isUserInRole("Administrator");
    }

    @Override
    public boolean hasPermissionsUpdate(String parameter, HttpServletRequest request) {
        Parameter paramObj = factoryParameter.create("", parameter, "", "");
        return this.hasPermissionsUpdate(paramObj, request);
    }

    @Override
    public boolean hasPermissionsCreate(Parameter parameter, HttpServletRequest request) {
        // Access right calculation.
        return false;
    }

    @Override
    public boolean hasPermissionsDelete(Parameter parameter, HttpServletRequest request) {
        // Access right calculation.
        return false;
    }

    @Override
    public Parameter secureParameter(Parameter parameter) {
        if (isToSecureParameter(parameter)) {
            parameter.setValue(StringUtil.SECRET_STRING);
            if (StringUtil.isNotEmptyOrNULLString(parameter.getSystem1value())) {
                parameter.setSystem1value(StringUtil.SECRET_STRING);
            }
        }
        return parameter;
    }

    @Override
    public boolean isToSecureParameter(Parameter parameter) {
        if (parameter.getParam().equals("cerberus_accountcreation_defaultpassword")
                || parameter.getParam().equals("cerberus_proxyauthentification_password")
                || parameter.getParam().equals("cerberus_jenkinsadmin_password")
                || parameter.getParam().equals("cerberus_smtp_password")
                || parameter.getParam().equals("cerberus_executeCerberusCommand_password")
                || parameter.getParam().equals(Parameter.VALUE_cerberus_xraycloud_clientsecret)
                || parameter.getParam().equals(Parameter.VALUE_cerberus_xraydc_token)
                || parameter.getParam().equals(Parameter.VALUE_cerberus_jiracloud_apiuser_apitoken)
                || parameter.getParam().equals(Parameter.VALUE_cerberus_github_apitoken)
                || parameter.getParam().equals(Parameter.VALUE_cerberus_gitlab_apitoken)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSystemManaged(Parameter parameter) {
        switch (parameter.getParam()) {
            // parameters that can be managed at system level.
            case "cerberus_actionexecutesqlstoredprocedure_timeout":
            case "cerberus_actionexecutesqlupdate_timeout":
            case "cerberus_action_wait_default":
            case "cerberus_appium_wait_element":
            case "cerberus_selenium_wait_element":
            case "cerberus_selenium_action_click_timeout":
            case "cerberus_selenium_implicitlyWait":
            case "cerberus_selenium_pageLoadTimeout":
            case "cerberus_selenium_setScriptTimeout":
            case "cerberus_callservice_enablehttpheadertoken":
            case "cerberus_callservice_timeoutms":
            case "cerberus_notinuse_timeout":
            case "cerberus_proxyauthentification_active":
            case "cerberus_proxyauthentification_password":
            case "cerberus_proxyauthentification_user":
            case "cerberus_proxy_active":
            case "cerberus_proxy_host":
            case "cerberus_proxy_nonproxyhosts":
            case "cerberus_proxy_port":
            case "cerberus_propertyexternalsql_timeout":
            case "cerberus_testdatalib_fetchmax":
            case "cerberus_notification_disableenvironment_body":
            case "cerberus_notification_disableenvironment_cc":
            case "cerberus_notification_disableenvironment_subject":
            case "cerberus_notification_disableenvironment_to":
            case "cerberus_notification_newbuildrevision_body":
            case "cerberus_notification_newbuildrevision_cc":
            case "cerberus_notification_newbuildrevision_subject":
            case "cerberus_notification_newbuildrevision_to":
            case "cerberus_notification_newchain_body":
            case "cerberus_notification_newchain_cc":
            case "cerberus_notification_newchain_subject":
            case "cerberus_notification_newchain_to":
            case "cerberus_loopstep_max":
            case "cerberus_url":
            case "cerberus_webperf_ignoredomainlist":
            case "cerberus_accept_unsigned_ssl_certificate":
            case Parameter.VALUE_cerberus_xraycloud_clientid:
            case Parameter.VALUE_cerberus_xraycloud_clientsecret:
            case Parameter.VALUE_cerberus_xraydc_token:
            case Parameter.VALUE_cerberus_xraydc_url:
                return true;
            // any other parameters are not managed at system level.
            default:
                return false;
        }
    }

}
