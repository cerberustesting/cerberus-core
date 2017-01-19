/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.service.impl;

import org.apache.log4j.Logger;
import org.cerberus.crud.dao.IParameterDAO;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.observe.ObservableEngine;
import org.cerberus.util.observe.Observer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author bcivel
 */
@Service
public class ParameterService implements IParameterService {

    @Autowired
    private IParameterDAO parameterDao;

    @Autowired
    private ObservableEngine<String, Parameter> observableEngine;

    private static final Logger LOG = Logger.getLogger(ParameterService.class);

    @Override
    public Parameter findParameterByKey(String key, String system) throws CerberusException {
        Parameter myParameter;
        /**
         * We try to get the parameter using the system parameter but if it does
         * not exist or empty, we get it with system="" which correspond to the
         * default global Cerberus Parameter.
         */
        try {
            LOG.debug("Trying to retrieve parameter : " + key + " - [" + system + "]");
            myParameter = parameterDao.findParameterByKey(system, key);
            if (myParameter != null && myParameter.getValue().equalsIgnoreCase("")) {
                myParameter = parameterDao.findParameterByKey("", key);
            }
        } catch (CerberusException ex) {
            LOG.debug("Trying to retrieve parameter (default value) : " + key + " - []");
            myParameter = parameterDao.findParameterByKey("", key);
            return myParameter;
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
            LOG.error("Error when trying to retreive parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex);
        }
        return outPutResult;
    }

    @Override
    public Integer getParameterIntegerByKey(String key, String system, Integer defaultValue) {
        Parameter myParameter;
        Integer outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = Integer.valueOf(myParameter.getValue());
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error("Error when trying to retreive parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex);
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
            LOG.error("Error when trying to retreive parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex);
        }
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
            LOG.error("Error when trying to retreive parameter : '" + key + "' for system : '" + system + "'. Default value returned : '" + defaultValue + "'. Trace : " + ex);
        }
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
            LOG.error(ex);
        }
        return outPutResult;
    }

    @Override
    public List<Parameter> findAllParameter() throws CerberusException {
        return parameterDao.findAllParameter();
    }

    @Override
    public void updateParameter(Parameter parameter) throws CerberusException {
        parameterDao.updateParameter(parameter);
        fireUpdate(parameter.getParam(), parameter);
    }

    @Override
    public void insertParameter(Parameter parameter) throws CerberusException {
        parameterDao.insertParameter(parameter);
        fireCreate(parameter.getParam(), parameter);
    }

    @Override
    public void saveParameter(Parameter parameter) throws CerberusException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving Parameter");
        }
        try {
            parameterDao.findParameterByKey(parameter.getSystem(), parameter.getParam());
            updateParameter(parameter);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Parameter Updated");
            }

        } catch (CerberusException ex) {
            insertParameter(parameter);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Parameter Inserted");
            }
        }
    }

    @Override
    public List<Parameter> findAllParameterWithSystem1(String system, String system1) throws CerberusException {
        return parameterDao.findAllParameterWithSystem1(system, system1);
    }

    @Override
    public AnswerList readWithSystem1BySystemByCriteria(String system, String system1, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return parameterDao.readWithSystem1BySystemByCriteria(system, system1, startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readWithSystem1ByKey(String system, String key, String system1) {
        return parameterDao.readWithSystem1ByKey(system, key, system1);
    }

    @Override
    public AnswerList<String> readDistinctValuesWithSystem1ByCriteria(String system, String system1, String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return parameterDao.readDistinctValuesWithSystem1ByCriteria(system, system1, searchParameter, individualSearch, columnName);
    }

    @Override
    public AnswerItem readByKey(String system, String param) {
        return parameterDao.readByKey(system, param);
    }

    @Override
    public Answer create(Parameter object) {
        Answer answer = parameterDao.create(object);
        if (MessageEventEnum.DATA_OPERATION_OK.getCode() == answer.getResultMessage().getCode()) {
            fireCreate(object.getParam(), object);
        }
        return answer;
    }

    @Override
    public Answer update(Parameter object) {
        Answer answer = parameterDao.update(object);
        if (MessageEventEnum.DATA_OPERATION_OK.getCode() == answer.getResultMessage().getCode()) {
            fireUpdate(object.getParam(), object);
        }
        return answer;
    }

    @Override
    public Answer delete(Parameter object) {
        Answer answer = parameterDao.delete(object);
        if (MessageEventEnum.DATA_OPERATION_OK.getCode() == answer.getResultMessage().getCode()) {
            fireDelete(object.getParam(), object);
        }
        return answer;
    }

    @Override
    public Answer save(Parameter object) {
        Answer finalAnswer = new Answer();
        AnswerItem resp = readByKey(object.getSystem(), object.getParam());
        if (!resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            /**
             * Object could not be found. We stop here and report the error.
             */
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) resp);
        } else if (resp.getItem() == null) {
            finalAnswer = create(object);
        } else if (!((object.getValue()).equals(((Parameter) resp.getItem()).getValue()))) {
            finalAnswer = update(object);
        } else {
            /**
             * Nothing is done but everything went OK
             */
            finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED));
        }
        return finalAnswer;
    }

    @Override
    public boolean register(Observer<String, Parameter> observer) {
        return observableEngine.register(observer);
    }

    @Override
    public boolean register(String topic, Observer<String, Parameter> observer) {
        return observableEngine.register(topic, observer);
    }

    @Override
    public boolean unregister(String topic, Observer<String, Parameter> observer) {
        return observableEngine.unregister(topic, observer);
    }

    @Override
    public boolean unregister(Observer<String, Parameter> observer) {
        return observableEngine.unregister(observer);
    }

    @Override
    public void fireCreate(String topic, Parameter parameter) {
        observableEngine.fireCreate(topic, parameter);
    }

    @Override
    public void fireUpdate(String topic, Parameter parameter) {
        observableEngine.fireUpdate(topic, parameter);
    }

    @Override
    public void fireDelete(String topic, Parameter parameter) {
        observableEngine.fireDelete(topic, parameter);
    }

}
