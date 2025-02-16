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
package org.cerberus.core.crud.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.execution.impl.RecorderService;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Map la table Service
 *
 * @author cte
 */
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppService {

    private String service; // Name and reference of the service
    private String application; // application that reference the service.
    private String type; // either SOAP/REST/KAFKA/FTP
    private String method; // Method used : POST/GET
    private String servicePath; // Path to access the service
    private boolean isFollowRedir; // Path to access the service
    private String fileName;
    private String operation; // Operation used for SOAP Requests
    private String attachementURL; // Attachement in cas of SOAP call with attachement.
    private String bodyType; // Body type used : none/raw/form-data/form-urlencoded
    private String serviceRequest; // Content of the request.
    private String kafkaTopic;
    private String kafkaKey;
    private String kafkaFilterPath;
    private String kafkaFilterValue;
    private String kafkaFilterHeaderPath;
    private String kafkaFilterHeaderValue;
    private boolean isAvroEnable;
    private String schemaRegistryURL;
    private boolean isAvroEnableKey;
    private String avroSchemaKey;
    private boolean isAvroEnableValue;
    private String avroSchemaValue;
    private String parentContentService;
    private String collection; // Information in order to group the services in order to organise them
    private String authType;
    private String authUser;
    private String authPassword;
    private String authAddTo;
    private JSONObject simulationParameters;
    private String description;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * From here are data outside database model.
     */
    @EqualsAndHashCode.Exclude
    private List<AppServiceContent> contentList;
    @EqualsAndHashCode.Exclude
    private List<AppServiceHeader> headerList;
    @EqualsAndHashCode.Exclude
    private String proxyHost;
    @EqualsAndHashCode.Exclude
    private int proxyPort;
    @EqualsAndHashCode.Exclude
    private boolean proxy;
    @EqualsAndHashCode.Exclude
    private boolean proxyWithCredential;
    @EqualsAndHashCode.Exclude
    private String proxyUser;
    // Result from call.
    @EqualsAndHashCode.Exclude
    private String responseHTTPVersion;
    @EqualsAndHashCode.Exclude
    private int responseHTTPCode;
    @EqualsAndHashCode.Exclude
    private String responseHTTPBody;
    @EqualsAndHashCode.Exclude
    private String responseHTTPBodyContentType;
    @EqualsAndHashCode.Exclude
    private List<AppServiceHeader> responseHeaderList;
    @EqualsAndHashCode.Exclude
    private int timeoutms; // Timeout used during service request
    @EqualsAndHashCode.Exclude
    private byte[] file;
    @EqualsAndHashCode.Exclude
    private long kafkaResponseOffset;
    @EqualsAndHashCode.Exclude
    private int kafkaResponsePartition;
    @EqualsAndHashCode.Exclude
    private int kafkaWaitNbEvent;
    @EqualsAndHashCode.Exclude
    private int kafkaWaitSecond;
    @EqualsAndHashCode.Exclude
    private boolean recordTraceFile;
    @EqualsAndHashCode.Exclude
    private int responseNb;
    @EqualsAndHashCode.Exclude
    private Timestamp start;
    @EqualsAndHashCode.Exclude
    private Timestamp end;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_SOAP = "SOAP";
    public static final String TYPE_REST = "REST";
    public static final String TYPE_FTP = "FTP";
    public static final String TYPE_KAFKA = "KAFKA";
    public static final String TYPE_MONGODB = "MONGODB";
    public static final String METHOD_HTTPPOST = "POST";
    public static final String METHOD_HTTPGET = "GET";
    public static final String METHOD_HTTPDELETE = "DELETE";
    public static final String METHOD_HTTPPUT = "PUT";
    public static final String METHOD_HTTPPATCH = "PATCH";
    public static final String METHOD_KAFKAPRODUCE = "PRODUCE";
    public static final String METHOD_KAFKASEARCH = "SEARCH";
    public static final String METHOD_MONGODBFIND = "FIND";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_XML = "XML";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_JSON = "JSON";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_TXT = "TXT";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_PDF = "PDF";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_HTML = "HTML";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_CSV = "CSV";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_UNKNOWN = "UNKNOWN";
    public static final String SRVBODYTYPE_NONE = "none";
    public static final String SRVBODYTYPE_RAW = "raw";
    public static final String SRVBODYTYPE_FORMDATA = "form-data";
    public static final String SRVBODYTYPE_FORMURLENCODED = "form-urlencoded";
    public static final String AUTHTYPE_NONE = "none";
    public static final String AUTHTYPE_APIKEY = "API Key";
    public static final String AUTHTYPE_BEARERTOKEN = "Bearer Token";
    public static final String AUTHTYPE_BASICAUTH = "Basic Auth";
    public static final String AUTHADDTO_QUERYSTRING = "Query String";
    public static final String AUTHADDTO_HEADERS = "Header";
    
    public static final String SERVICENAME_SIMULATIONCALL = "$TMP";
    

    public void addResponseHeaderList(AppServiceHeader object) {
        this.responseHeaderList.add(object);
    }

    public void addContentList(AppServiceContent object) {
        this.contentList.add(object);
    }

    public void addContentList(List<AppServiceContent> object) {
        this.contentList.addAll(object);
    }

    public JSONObject toJSONOnExecution() {
        switch (this.getType()) {
            case AppService.TYPE_FTP:
                return this.toJSONOnFTPExecution();
            case AppService.TYPE_KAFKA:
                return this.toJSONOnKAFKAExecution();
            case AppService.TYPE_MONGODB:
                return this.toJSONOnMONGODBExecution();
            default:
                return this.toJSONOnDefaultExecution();
        }
    }

    public JSONObject toJSONOnDefaultExecution() {

        JSONObject jsonMain = new JSONObject();
        JSONObject jsonTimings = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            if (!(this.getTimeoutms() == 0)) {
                jsonMyRequest.put("HTTP-TimeOutMs", this.getTimeoutms());
            }
            jsonMyRequest.put("CalledURL", this.getServicePath());
            if (!StringUtil.isEmptyOrNull(this.getMethod())) {
                jsonMyRequest.put("HTTP-Method", this.getMethod());
            }
            jsonMyRequest.put("ServiceType", this.getType());
            if (!(this.getHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : this.getHeaderList()) {
                    jsonHeaders.put(header.getKey(), header.getValue());
                }
                jsonMyRequest.put("HTTP-Header", jsonHeaders);
            }
            if (!(this.getContentList().isEmpty())) {
                JSONObject jsonContent = new JSONObject();
                for (AppServiceContent content : this.getContentList()) {
                    jsonContent.put(content.getKey(), content.getValue());
                }
                jsonMyRequest.put("Content", jsonContent);
            }
            jsonMyRequest.put("HTTP-Request", this.getServiceRequest());
            jsonMyRequest.put("HTTP-BodyRequestType", this.getBodyType());

            JSONObject jsonProxy = new JSONObject();
            jsonProxy.put("HTTP-Proxy", this.isProxy());
            if (this.isProxy()) {
                jsonProxy.put("HTTP-ProxyHost", this.getProxyHost());
                if (!(this.getProxyPort() == 0)) {
                    jsonProxy.put("HTTP-ProxyPort", this.getProxyPort());
                }
                jsonProxy.put("HTTP-ProxyAuthentification", this.isProxyWithCredential());
                if (this.isProxyWithCredential()) {
                    jsonProxy.put("HTTP-ProxyUser", this.getProxyUser());
                }
            }
            jsonMyRequest.put("HTTP-Proxy", jsonProxy);
            jsonMyRequest.put("isFollowRedir", this.isFollowRedir());

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            jsonMyResponse.put("HTTP-ReturnCode", this.getResponseHTTPCode());
            jsonMyResponse.put("HTTP-Version", this.getResponseHTTPVersion());
            
            // Timings
            jsonTimings.put("start", this.getStart());
            if ((this.getStart() != null) && (this.getEnd() != null) && (this.getEnd().getTime() > this.getStart().getTime())) {
                jsonTimings.put("end", this.getEnd());
                jsonTimings.put("durationMs", (this.getEnd().getTime() - this.getStart().getTime()));
            }
            jsonMyResponse.put("timings", jsonTimings);
            
            if (!StringUtil.isEmptyOrNull(this.getResponseHTTPBody())) {
                try {
                    JSONArray respBody = new JSONArray(this.getResponseHTTPBody());
                    jsonMyResponse.put("HTTP-ResponseBody", respBody);
                } catch (JSONException e1) {
                    try {
                        JSONObject respBody = new JSONObject(this.getResponseHTTPBody());
                        jsonMyResponse.put("HTTP-ResponseBody", respBody);
                    } catch (JSONException e2) {
                        jsonMyResponse.put("HTTP-ResponseBody", this.getResponseHTTPBody());
                    }
                }
            }
            jsonMyResponse.put("HTTP-ResponseContentType", this.getResponseHTTPBodyContentType());
            if (!(this.getResponseHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : this.getResponseHeaderList()) {
                    jsonHeaders.put(header.getKey(), header.getValue());
                }
                jsonMyResponse.put("Header", jsonHeaders);
            }
            jsonMain.put("Response", jsonMyResponse);

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(RecorderService.class);
            LOG.warn(ex);
        }
        return jsonMain;
    }

    public JSONObject toJSONOnMONGODBExecution() {

        JSONObject jsonMain = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            if (!(this.getTimeoutms() == 0)) {
                jsonMyRequest.put("TimeOutMs", this.getTimeoutms());
            }
            jsonMyRequest.put("ConnectionString", this.getServicePath());
            jsonMyRequest.put("DatabaseCollection", this.getOperation());
            if (!StringUtil.isEmptyOrNull(this.getMethod())) {
                jsonMyRequest.put("Method", this.getMethod());
            }
            jsonMyRequest.put("ServiceType", this.getType());

            jsonMyRequest.put("FindRequest", this.getServiceRequest());

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            jsonMyResponse.put("ResultNb", this.getResponseNb());
            if (!StringUtil.isEmptyOrNull(this.getResponseHTTPBody())) {
                try {
                    JSONArray respBody = new JSONArray(this.getResponseHTTPBody());
                    jsonMyResponse.put("ResponseArray", respBody);
                } catch (JSONException e1) {
                    try {
                        JSONObject respBody = new JSONObject(this.getResponseHTTPBody());
                        jsonMyResponse.put("ResponseArray", respBody);
                    } catch (JSONException e2) {
                        jsonMyResponse.put("ResponseArray", this.getResponseHTTPBody());
                    }
                }
            }
            jsonMyResponse.put("ResponseContentType", this.getResponseHTTPBodyContentType());
            jsonMain.put("Response", jsonMyResponse);
            
        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(RecorderService.class);
            LOG.warn(ex);
        }
        return jsonMain;
    }

    public JSONObject toJSONOnKAFKAExecution() {
        JSONObject jsonMain = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            if (!(this.getTimeoutms() == 0)) {
                jsonMyRequest.put("TimeOutMs", this.getTimeoutms());
            }
            jsonMyRequest.put("Servers", this.getServicePath());
            if (!StringUtil.isEmptyOrNull(this.getMethod())) {
                jsonMyRequest.put("KAFKA-Method", this.getMethod());
            }
            jsonMyRequest.put("ServiceType", this.getType());
            if (!(this.getContentList().isEmpty())) {
                JSONObject jsonProps = new JSONObject();
                for (AppServiceContent prop : this.getContentList()) {
                    if (prop.getKey().contains("passw")) {
                        jsonProps.put(prop.getKey(), StringUtil.SECRET_STRING);
                    } else {
                        jsonProps.put(prop.getKey(), prop.getValue());
                    }
                }
                jsonMyRequest.put("KAFKA-Props", jsonProps);
            }
            if (!(this.getHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : this.getHeaderList()) {
                    if (header.getKey().contains("passw")) {
                        jsonHeaders.put(header.getKey(), StringUtil.SECRET_STRING);
                    } else {
                        jsonHeaders.put(header.getKey(), header.getValue());
                    }
                }
                jsonMyRequest.put("KAFKA-Header", jsonHeaders);
            }
            if (!StringUtil.isEmptyOrNull(this.getServiceRequest())) {
                try {
                    JSONObject reqBody = new JSONObject(this.getServiceRequest());
                    jsonMyRequest.put("KAFKA-Value", reqBody);
                } catch (JSONException e) {
                    jsonMyRequest.put("KAFKA-Value", this.getServiceRequest());
                }
            }
            if (!StringUtil.isEmptyOrNull(this.getKafkaKey())) {
                try {
                    JSONObject keyBody = new JSONObject(this.getKafkaKey());
                    jsonMyRequest.put("KAFKA-Key", keyBody);
                } catch (JSONException e) {
                    jsonMyRequest.put("KAFKA-Key", this.getKafkaKey());
                }
            }
            if (!(this.getKafkaWaitNbEvent() == 0)) {
                jsonMyRequest.put("WaitNbEvents", this.getKafkaWaitNbEvent());
            }
            if (!(this.getKafkaWaitSecond() == 0)) {
                jsonMyRequest.put("WaitSeconds", this.getKafkaWaitSecond());
            }
            if (METHOD_KAFKASEARCH.equalsIgnoreCase(this.getMethod())) {
                JSONObject jsonFilters = new JSONObject();
                jsonFilters.put("Path", this.getKafkaFilterPath());
                jsonFilters.put("Value", this.getKafkaFilterValue());
                jsonMyRequest.put("KAFKA-SearchFilterValue", jsonFilters);
                JSONObject jsonFiltersHeader = new JSONObject();
                jsonFiltersHeader.put("Path", this.getKafkaFilterHeaderPath());
                jsonFiltersHeader.put("Value", this.getKafkaFilterHeaderValue());
                jsonMyRequest.put("KAFKA-SearchFilter", jsonFiltersHeader);
            }

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            if (this.getKafkaResponseOffset() >= 0) {
                jsonMyResponse.put("Offset", this.getKafkaResponseOffset());
            }
            if (this.getKafkaResponsePartition() >= 0) {
                jsonMyResponse.put("Partition", this.getKafkaResponsePartition());
            }
            if (!StringUtil.isEmptyOrNull(this.getResponseHTTPBody())) {
                try {
                    JSONArray respBody = new JSONArray(this.getResponseHTTPBody());
                    jsonMyResponse.put("Messages", respBody);
                } catch (JSONException e) {
                    jsonMyResponse.put("Messages", this.getResponseHTTPBody());
                }
            }
            jsonMain.put("Response", jsonMyResponse);

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(RecorderService.class);
            LOG.warn(ex);
        }
        return jsonMain;
    }

    public JSONObject toJSONOnFTPExecution() {
        JSONObject jsonMain = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            if (!(this.getTimeoutms() == 0)) {
                jsonMyRequest.put("FTP-TimeOutMs", this.getTimeoutms());
            }
            jsonMyRequest.put("CalledURL", this.getServicePath());
            if (!StringUtil.isEmptyOrNull(this.getMethod())) {
                jsonMyRequest.put("FTP-Method", this.getMethod());
            }
            jsonMyRequest.put("ServiceType", this.getType());
            if (!(this.getContentList().isEmpty())) {
                JSONObject jsonContent = new JSONObject();
                for (AppServiceContent content : this.getContentList()) {
                    jsonContent.put(content.getKey(), content.getValue());
                }
                jsonMyRequest.put("Content", jsonContent);
            }

            JSONObject jsonProxy = new JSONObject();
            jsonProxy.put("FTP-Proxy", this.isProxy());
            if (this.isProxy()) {
                jsonProxy.put("FTP-ProxyHost", this.getProxyHost());
                if (!(this.getProxyPort() == 0)) {
                    jsonProxy.put("FTP-ProxyPort", this.getProxyPort());
                }
                jsonProxy.put("FTP-ProxyAuthentification", this.isProxyWithCredential());
                if (this.isProxyWithCredential()) {
                    jsonProxy.put("FTP-ProxyUser", this.getProxyUser());
                }
            }
            jsonMyRequest.put("FTP-Proxy", jsonProxy);

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            jsonMyResponse.put("FTP-ReturnCode", this.getResponseHTTPCode());
            jsonMyResponse.put("FTP-Version", this.getResponseHTTPVersion());
            jsonMyResponse.put("FTP-ResponseBody", this.getResponseHTTPBody());
            jsonMyResponse.put("FTP-ResponseContentType", this.getResponseHTTPBodyContentType());
            if (!(this.getResponseHeaderList().isEmpty())) {
                JSONObject jsonHeaders = new JSONObject();
                for (AppServiceHeader header : this.getResponseHeaderList()) {
                    jsonHeaders.put(header.getKey(), header.getValue());
                }
                jsonMyResponse.put("Header", jsonHeaders);
            }
            jsonMain.put("Response", jsonMyResponse);

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(RecorderService.class);
            LOG.warn(ex);
        }
        return jsonMain;
    }

}
