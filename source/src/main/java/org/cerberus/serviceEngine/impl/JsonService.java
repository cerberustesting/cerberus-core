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
package org.cerberus.serviceEngine.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.serviceEngine.IJsonService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class JsonService implements IJsonService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ExecutionRunService.class);

    /**
     * Get Json from URL and convert it into JSONObject format
     *
     * @param url Url location of the Json file to download.
     * @return JsonObject downloaded.
     */
    @Override
    public JSONObject callUrlAndGetJsonResponse(String url) {
        String str = "";
        JSONObject obj = new JSONObject();
        StringBuilder sb = new StringBuilder();
        try {
            URL urlToCall = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlToCall.openStream()));
            while (null != (str = br.readLine())) {
                sb.append(str);
            }
            obj = new JSONObject(sb.toString());

        } catch (IOException ex) {
            LOG.warn("Error Getting Json File " + ex);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return obj;
    }

    /**
     * Get element value from Json file
     *
     * @param url URL of the Json file to parse
     * @param attributeToFind
     * @return Value of the element from the Json File
     */
    @Override
    public String getFromJson(String url, String attributeToFind) {
        /**
         * Get the Json File and convert it in JSONObject format
         */
        JSONObject obj = this.callUrlAndGetJsonResponse(url);

        /**
         * Decode the attribute
         */
        ArrayList<ArrayList<String>> splittedAttribute = new ArrayList<ArrayList<String>>();
        List<String> items = Arrays.asList(attributeToFind.split("\\."));
        for (String unit : items){
            ArrayList<String> attributeList = new ArrayList<String>();
            String[] splitted = unit.split("\\[");
            attributeList.add(splitted[0]);
            attributeList.add(splitted.length>1?splitted[1].split("\\]")[0]:"0");
            splittedAttribute.add(attributeList);
            }
        ArrayList<String> attribute = splittedAttribute.get(0);
        splittedAttribute.remove(attribute);
       
        /**
         * Parse the JSONObject and get the value
         */
        String result = "";
        try {
            result = this.getFromJsonObject(obj, splittedAttribute, attribute);
        } catch (JSONException ex) {
            LOG.fatal(ex);
            return "";
        } catch (CerberusException ex) {
            Logger.getLogger(JsonService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String getFromJsonObject(JSONObject obj, ArrayList<ArrayList<String>> splittedAttribute, ArrayList<String> attribute) throws JSONException, CerberusException {
        String result = "";
        Iterator iter = obj.keys();
        while (iter.hasNext()) {
            String item = (String) iter.next();
            if (item.equals(attribute.get(0))) {
                return this.getValueFromJsonElement(obj.get(item), obj, splittedAttribute, attribute);
            }
        }
        //todo the case where we don't start from the base
        return result;
    }

    private String getValueFromJsonElement(Object element, JSONObject obj, ArrayList<ArrayList<String>> splittedAttribute, ArrayList<String> attribute) throws JSONException, CerberusException {
        String result = "";
        if (element instanceof JSONObject) {
            result = getFromJsonObject((JSONObject) element, splittedAttribute, attribute);
        }
        if (element instanceof JSONArray) {
            result = getFromJsonArray((JSONArray) element, splittedAttribute, attribute);
        }
        if (element instanceof String) {
            result = obj.getString(attribute.get(0));
        }
        if (element instanceof Boolean) {
            result = String.valueOf(obj.getBoolean(attribute.get(0)));
        }
        if (element instanceof Long) {
            result = String.valueOf(obj.getLong(attribute.get(0)));
        }
        if (element instanceof Double) {
            result = String.valueOf(obj.getDouble(attribute.get(0)));
        }
        if (element instanceof Integer) {
            result = String.valueOf(obj.getInt(attribute.get(0)));
        }
        return result;
    }

    private String getFromJsonArray(JSONArray array, ArrayList<ArrayList<String>> splittedAttribute, ArrayList<String> attribute) throws JSONException, CerberusException {
        //Get element in the array
        Object object = array.get(Integer.valueOf(attribute.get(1)));
        
        if (object instanceof JSONObject){
            if(!splittedAttribute.isEmpty()){
                attribute = splittedAttribute.get(0);
                splittedAttribute.remove(attribute);
                return getFromJsonObject((JSONObject) object , splittedAttribute, attribute);
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_NA));
            }
        } 
        if (object instanceof JSONArray) {
            if(!splittedAttribute.isEmpty()){
                attribute = splittedAttribute.get(0);
                splittedAttribute.remove(attribute);
                return getFromJsonArray((JSONArray) object, splittedAttribute, attribute);
            } else {
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_NA));
            }
        } 
        return object.toString();    
        
    }
}
