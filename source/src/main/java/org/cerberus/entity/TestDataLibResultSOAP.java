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
package org.cerberus.entity;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author FNogueira
 */
public class TestDataLibResultSOAP extends TestDataLibResult {
    public Document rawData;
    private String soapResponseKey;

   
    @Override
    public String getValue(TestDataLibData entry) {
        //gets the data from the xml document
        String value = null;
        try {
            NodeList candidates = XmlUtil.evaluate(rawData, entry.getParsingAnswer());
            if(candidates.getLength() > 0){
                value = candidates.item(0).getNodeValue();//TODO:FN check if we are trying to get the information from an attribute?
            }            
        } catch (XmlUtilException ex) {
            Logger.getLogger(TestDataLibResultSOAP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }
    
    public String getSoapResponseKey() {
        return soapResponseKey;
    }

    public void setSoapResponseKey(String soapResponseKey) {
        this.soapResponseKey = soapResponseKey;
    }

    
    
    public TestDataLibResultSOAP(){
        this.type = TestDataLibTypeEnum.SOAP.getCode();;
    }
    public Document getData() {
        return rawData;
    }

    public void setData(Document data) {
        this.rawData = data;
    }
    
    
}
