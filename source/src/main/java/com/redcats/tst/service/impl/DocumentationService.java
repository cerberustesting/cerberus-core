/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IDocumentationDAO;
import com.redcats.tst.service.IDocumentationService;
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

        String labelFromDB = this.documentationDAO.findDocumentationByKey(docTable, docField).getDocLabel();

        if (!labelFromDB.equals("")) {
            label.append(labelFromDB);
            label.append(" <a href=\'javascript:popup(\"Documentation.jsp?DocTable=");
            label.append(docTable);
            label.append("&DocField=");
            label.append(docField);
            label.append("\")\'>?</a>");

        } else {
            label.append(defaultLabel);
        }
        result = label.toString();

        return result;
    }

}
