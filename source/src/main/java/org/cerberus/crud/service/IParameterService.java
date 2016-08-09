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
package org.cerberus.crud.service;

import java.util.List;

import org.cerberus.crud.entity.Parameter;
import org.cerberus.exception.CerberusException;

/**
 * @author bcivel
 */
public interface IParameterService {

    /**
     * Be aware about a changing {@link Parameter}
     *
     * @author abourdon
     */
    interface ParameterAware {

        /**
         * If this {@link ParameterAware} is registered, then this method will trigger it to alert of a {@link Parameter} change
         *
         * @param parameter the changing {@link Parameter}
         * @see IParameterService#register(String, ParameterAware)
         */
        void parameterChanged(Parameter parameter);
    }

    Parameter findParameterByKey(String key, String system) throws CerberusException;
    
    Integer getParameterByKey(String key, String system, Integer defaultValue);

    List<Parameter> findAllParameter() throws CerberusException;

    void updateParameter(Parameter parameter) throws CerberusException;

    void insertParameter(Parameter parameter) throws CerberusException;

    void saveParameter(Parameter parameter) throws CerberusException;

    /**
     * Register the given {@link ParameterAware} to given {@link Parameter}'s key related changes
     *
     * @param key            the {@link Parameter}'s key from which the given {@link ParameterAware} will be registered
     * @param parameterAware the {@link ParameterAware} to register to the given {@link Parameter}'s key related changes
     */
    void register(String key, ParameterAware parameterAware);

    /**
     * Unregister the given {@link ParameterAware} from given {@link Parameter}'s key related changes
     *
     * @param key            the {@link Parameter}'s key from which the given {@link ParameterAware} will be unregistered
     * @param parameterAware the {@link ParameterAware} to unregister from the given {@link Parameter}'s key related changes
     */
    void unregister(String key, ParameterAware parameterAware);

}
