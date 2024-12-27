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

import org.cerberus.core.crud.entity.Documentation;
import org.cerberus.core.crud.factory.IFactoryDocumentation;
import org.cerberus.core.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryDocumentation implements IFactoryDocumentation {

    @Override
    public Documentation create(String docTable, String docField, String docValue, String docLabel, String docDesc, String docAnchor) {
        Documentation documentation = new Documentation();
        documentation.setDocTable(docTable);
        documentation.setDocField(docField);
        documentation.setDocValue(docValue);
        documentation.setDocLabel(docLabel);
        documentation.setDocDesc(docDesc);
        documentation.setHavedocDesc(!(StringUtil.isEmptyOrNull(docDesc)));
        documentation.setDocAnchor(docAnchor);
        documentation.setHaveDocAnchor(!(StringUtil.isEmptyOrNull(docAnchor)));
        return documentation;
    }

}
