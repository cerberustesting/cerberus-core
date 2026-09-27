/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.api.mappers;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapstruct.Mapper;

import static org.cerberus.core.util.StringUtil.parseBoolean;

/**
 * @author lucashimpens
 */
@Mapper(componentModel = "spring")
public interface BooleanMapper {

    static final Logger LOG = LogManager.getLogger(BooleanMapper.class);

    public default boolean toBoolean(String text) {
        return parseBoolean(text);
    }

    default boolean toBoolean(Optional<Boolean> value) {
        boolean products = true;
        LOG.debug("mapping from Optional " + value + " to boolean");

        //add your custom mapping implementation
        return products;
    }

    default Optional<Boolean> toOptionalBoolean(boolean value) {
        Optional<Boolean> products = null;
        LOG.debug("mapping from boolean : " + value + " to Optional ");
        //add your custom mapping implementation
        return products;
    }
}
