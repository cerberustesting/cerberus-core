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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.tool.Extension;
import org.cerberus.crud.dao.IParameterDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.version.Infos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author bcivel
 */
@Service
public class ParameterService implements IParameterService {

    @Autowired
    private IParameterDAO parameterDao;

    private Map<String, Set<ParameterAware>> propertyRegistration;

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
    public Integer getParameterIntegerByKey(String key, String system, Integer defaultValue) {
        Parameter myParameter;
        Integer outPutResult = defaultValue;
        try {
            myParameter = this.findParameterByKey(key, system);
            outPutResult = Integer.valueOf(myParameter.getValue());
        } catch (CerberusException | NumberFormatException ex) {
            LOG.error(ex);
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
            LOG.error(ex);
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
        firePropertyChange(parameter);
    }

    @Override
    public void insertParameter(Parameter parameter) throws CerberusException {
        parameterDao.insertParameter(parameter);
        firePropertyChange(parameter);
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
    public void register(String key, ParameterAware parameterAware) {
        synchronized (propertyRegistration) {
            Set<ParameterAware> existingRegistration = propertyRegistration.get(key);
            if (existingRegistration == null) {
                existingRegistration = new HashSet<>();
            }
            existingRegistration.add(parameterAware);
            propertyRegistration.put(key, existingRegistration);
        }
    }

    @Override
    public void unregister(String key, ParameterAware parameterAware) {
        synchronized (propertyRegistration) {
            Set<ParameterAware> existingRegistration = propertyRegistration.get(key);
            if (existingRegistration != null) {
                existingRegistration.remove(parameterAware);
            }
        }
    }

    @PostConstruct
    private void init() {
        propertyRegistration = new HashMap<>();
    }

    private void firePropertyChange(Parameter parameter) {
        Set<ParameterAware> existingRegistration;
        synchronized (propertyRegistration) {
            existingRegistration = propertyRegistration.get(parameter.getParam());
        }
        if (existingRegistration != null) {
            for (ParameterAware parameterAware : existingRegistration) {
                parameterAware.parameterChanged(parameter);
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
        return parameterDao.create(object);
    }

    @Override
    public Answer update(Parameter object) {
        return parameterDao.update(object);
    }

    @Override
    public Answer delete(Parameter object) {
        return parameterDao.delete(object);
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
        } else {
            if(!((object.getValue()).equals(((Parameter)resp.getItem()).getValue()))) {
                finalAnswer = update(object);
            }else{
                /**
                 * Nothing is done but everything went OK
                 */
                finalAnswer = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED));
            }
        }
        return finalAnswer;
    }
}
