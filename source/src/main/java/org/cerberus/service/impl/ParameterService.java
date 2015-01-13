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
package org.cerberus.service.impl;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.dao.IParameterDAO;
import org.cerberus.entity.Parameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IParameterService;
import org.cerberus.version.Infos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ParameterService implements IParameterService {

    @Autowired
    private IParameterDAO parameterDao;

    @Override
    public Parameter findParameterByKey(String key, String system) throws CerberusException {
        Parameter myParameter;
        /**
         * We try to get the parameter using the system parameter but if it does
         * not exist or empty, we get it with system="" which correspond to the default
         * global Cerberus Parameter.
         */
        try {
            Logger.getLogger(ParameterService.class.getName()).log(Level.DEBUG, Infos.getInstance().getProjectNameAndVersion() + " - Trying to retrieve parameter : " + key + " - [" + system + "]");
            myParameter = parameterDao.findParameterByKey(system, key);
            if (myParameter.getValue().equalsIgnoreCase("")) {
                myParameter = parameterDao.findParameterByKey("", key);
            }
        } catch (CerberusException ex) {
            Logger.getLogger(ParameterService.class.getName()).log(Level.DEBUG, Infos.getInstance().getProjectNameAndVersion() + " - Trying to retrieve parameter : " + key + " - []");
            myParameter = parameterDao.findParameterByKey("", key);
            return myParameter;
        }
        return myParameter;
    }

    @Override
    public List<Parameter> findAllParameter() throws CerberusException {
        return parameterDao.findAllParameter();
    }

    @Override
    public void updateParameter(Parameter parameter) throws CerberusException {
        parameterDao.updateParameter(parameter);
    }

    @Override
    public void insertParameter(Parameter parameter) throws CerberusException {
        parameterDao.insertParameter(parameter);
    }

    @Override
    public void saveParameter(Parameter parameter) throws CerberusException {
        MyLogger.log(ParameterService.class.getName(), Level.DEBUG, "Saving Parameter");
        try {
            parameterDao.findParameterByKey(parameter.getSystem(), parameter.getParam());
            parameterDao.updateParameter(parameter);
            MyLogger.log(ParameterService.class.getName(), Level.DEBUG, "Parameter Updated");
        } catch (CerberusException ex) {
            parameterDao.insertParameter(parameter);
            MyLogger.log(ParameterService.class.getName(), Level.DEBUG, "Parameter Inserted");
        }
    }
}
