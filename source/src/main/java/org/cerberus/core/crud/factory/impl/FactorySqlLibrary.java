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
package org.cerberus.core.crud.factory.impl;

import org.cerberus.core.crud.entity.SqlLibrary;
import org.cerberus.core.crud.factory.IFactorySqlLibrary;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactorySqlLibrary implements IFactorySqlLibrary {

    @Override
    public SqlLibrary create(String name, String type, String database, String script, String description) {
        SqlLibrary sqlLibrary = new SqlLibrary();
        sqlLibrary.setName(name);
        sqlLibrary.setType(type);
        sqlLibrary.setDatabase(database);
        sqlLibrary.setScript(script);
        sqlLibrary.setDescription(description);
        return sqlLibrary;
    }

}
