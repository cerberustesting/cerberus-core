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

import org.cerberus.dao.IDocumentationDAO;
import org.cerberus.entity.Documentation;
import org.cerberus.service.IDocumentationService;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class DocumentationService implements IDocumentationService {

    @Autowired
    private IDocumentationDAO documentationDAO;

    @Override
    public String findLabel(String docTable, String docField, String defaultLabel) {
        String result = null;
        StringBuilder label = new StringBuilder();

        String labelFromDB = "";
        Documentation myDoc = this.documentationDAO.findDocumentationByKey(docTable, docField, "");

        if (myDoc == null) {
            label.append("!!NoDoc!! ");
            label.append(docTable);
            label.append("|");
            label.append(docField);
        } else {
            labelFromDB = myDoc.getDocLabel();
            label.append(labelFromDB);
        }
        result = label.toString();

        return result;
    }

    @Override
    public String findLabelHTML(String docTable, String docField, String defaultLabel) {
        String result = null;
        StringBuilder label = new StringBuilder();

        String labelFromDB = "";
        Documentation myDoc = this.documentationDAO.findDocumentationByKey(docTable, docField, "");

        if (myDoc == null) {
            label.append("!!NoDoc!! ");
            label.append(docTable);
            label.append("|");
            label.append(docField);
        } else {
            labelFromDB = myDoc.getDocLabel();
            label.append(labelFromDB);
            label.append(" <a href=\'javascript:popup(\"Documentation.jsp?DocTable=");
            label.append(docTable);
            label.append("&DocField=");
            label.append(docField);
            label.append("\")\'>?</a>");
        }
        result = label.toString();

        return result;
    }
}
