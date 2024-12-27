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

import com.google.gson.Gson;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IDocumentationDAO;
import org.cerberus.core.crud.entity.Documentation;
import org.cerberus.core.crud.service.IDocumentationService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class DocumentationService implements IDocumentationService {

    private static final Logger LOG = LogManager.getLogger(DocumentationService.class);
    
    @Autowired
    private IDocumentationDAO documentationDAO;

    @Override
    public String findLabel(String docTable, String docField, String defaultLabel, String lang) {
        String result = null;
        StringBuilder label = new StringBuilder();

        String labelFromDB = "";
        Documentation myDoc = this.documentationDAO.findDocumentationByKey(docTable, docField, "", lang);

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
    public String findLabelHTML(String docTable, String docField, String defaultLabel, String lang) {
        String result = null;
        StringBuilder label = new StringBuilder();

        String labelFromDB = "";
        Documentation myDoc = this.documentationDAO.findDocumentationByKey(docTable, docField, "", lang);

        if (myDoc == null) {
            label.append("!!NoDoc!! ");
            label.append(docTable);
            label.append("|");
            label.append(docField);
        } else {
            labelFromDB = myDoc.getDocLabel();
            label.append(labelFromDB);
            if (!(StringUtil.isEmptyOrNull(myDoc.getDocDesc().trim()))) {
                label.append(" <a class=\"docOnline\" href=\'javascript:popup(\"Documentation.jsp?DocTable=");
                label.append(docTable);
                label.append("&DocField=");
                label.append(docField);
                label.append("&Lang=");
                label.append(lang);
                // need for a function in the header of dataTables so the table doesn't sort when we click on '?' anchor
                if (defaultLabel.equals("table")) {
                    label.append("\")\' onclick=\"stopPropagation(event)\">?</a>");
                } else {
                    label.append("\")\'>?</a>");
                }
            }
        }
        result = label.toString();

        return result;
    }

    @Override
    public List<Documentation> findDocumentationsWithNotEmptyValueAndDescription(String docTable, String docField, String lang) {
        return this.documentationDAO.findDocumentationsWithNotEmptyValueAndDescription(docTable, docField, lang);
    }

    @Override
    public List<Documentation> findDocumentationsWithEmptyValueAndNotEmptyDescription(String docTable, String docField, String lang) {
        return this.documentationDAO.findDocumentationsWithEmptyValueAndNotEmptyDescription(docTable, docField, lang);
    }

    @Override
    public String findLabelFromTableAndField(String docTable, String docField, String lang) {
        return this.documentationDAO.findLabelFromTableAndField(docTable, docField, lang);
    }

    @Override
    public String findDescriptionFromTableFieldAndValue(String docTable, String docField, String docValue, String lang) {
        return this.documentationDAO.findDescriptionFromTableFieldAndValue(docTable, docField, docValue, lang);
    }

    @Override
    public List<Documentation> findAll(String lang) {
        return this.documentationDAO.findAll(lang);
    }

    @Override
    public JSONObject formatGroupByDocTable(List<Documentation> docList) {
        JSONObject result = new JSONObject();

        for (Documentation doc : docList) {
            String docTable = doc.getDocTable();
            if (result.has(docTable)) {
                try {
                    result.getJSONObject(docTable).put(doc.getDocField(), convertDocToJSONObject(doc));
                } catch (JSONException ex) {
                    LOG.warn(ex);
                }
            } else {
                try {
                    result.put(docTable, new JSONObject());
                    result.getJSONObject(docTable).put(doc.getDocField(), convertDocToJSONObject(doc));
                } catch (JSONException ex) {
                    LOG.warn(ex);
                }
            }
        }
        return result;
    }

    private JSONObject convertDocToJSONObject(Documentation doc) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(doc));
        return result;
    }

    @Override
    public List<Documentation> findAllWithEmptyDocLabel(String lang) {
        return this.documentationDAO.findAllWithEmptyDocValue(lang);
    }
}
