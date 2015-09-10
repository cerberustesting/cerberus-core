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

import org.cerberus.enums.TestDataLibTypeEnum;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.cerberus.util.answer.AnswerItem;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author FNogueira
 */
public class TestDataLibResultSOAP extends TestDataLibResult {
    public Document rawData;
    private String soapResponseKey;

    public String getSoapResponseKey() {
        return soapResponseKey;
    }

    public void setSoapResponseKey(String soapResponseKey) {
        this.soapResponseKey = soapResponseKey;
    }

    
    
    public TestDataLibResultSOAP(){
        this.type = TestDataLibTypeEnum.SOAP.getCode();
    }
    public Document getData() {
        return rawData;
    }

    public void setData(Document data) {
        this.rawData = data;
    }

    @Override
    public AnswerItem<String> getValue(TestDataLibData entry) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIBDATA);
        AnswerItem ansGetValue = new AnswerItem(msg);
        
        if(!values.containsKey(entry.getSubData())){
            try {
                
                NodeList candidates = XmlUtil.evaluate(rawData, entry.getParsingAnswer());
                if(candidates.getLength() > 0){
                    //if the map don't contain the entry that we want, we will get it
                   String value = candidates.item(0).getNodeValue();        
                    
                    if(value == null){
                        if(candidates.item(0) != null){
                            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_CHECK_XPATH);
                            msg.setDescription(msg.getDescription().replace("%XPATH%", entry.getParsingAnswer()).replace("%SUBDATA%", entry.getSubData()).
                            replace("%ENTRY%", entry.getTestDataLibID().toString()));   
                        }
                    }else {
                        //associates the subdata with the xpath expression data retrieved by the query
                        values.put(entry.getSubData(), value);
                    }


                }else{
                    //no elements were returned by the XPATH expression
                    msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_XML_NOTFOUND);                
                    msg.setDescription(msg.getDescription().replace("%XPATH%", entry.getParsingAnswer()).replace("%SUBDATA%", entry.getSubData()).
                        replace("%ENTRY%", entry.getTestDataLibID().toString()));    
                }      
            } catch (XmlUtilException ex) {
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_XMLEXCEPTION);            
                msg.setDescription(msg.getDescription().replace("%XPATH%", entry.getParsingAnswer()).replace("%SUBDATA%", entry.getSubData()).
                        replace("%ENTRY%", entry.getTestDataLibID().toString()).replace("%REASON%", ex.toString()));

            }
        }
        
        
        
    
        ansGetValue.setResultMessage(msg);    
        ansGetValue.setItem(values.get(entry.getSubData()));
        
        return ansGetValue;
    }
    
    
}
