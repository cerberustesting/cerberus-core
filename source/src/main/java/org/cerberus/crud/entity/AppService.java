/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

import java.sql.Timestamp;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.engine.execution.impl.RecorderService;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Map la table Service
 *
 * @author cte
 */
public class AppService {

    private String service; // Name and reference of the service
    private String application; // application that reference the service.
    private String type; // either SOAP/REST
    private String method; // Method used : POST/GET
    private String servicePath; // Path to access the service
    private String operation; // Operation used for SOAP Requests
    private String serviceRequest; // Content of the request.
    private String attachementURL; // Attachement in cas of SOAP call with attachement.
    private String group; // Information in order to group the services in order to organise them
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;
    private String fileName;
    private String kafkaTopic;
    private String kafkaKey;
    private String kafkaFilterPath;
    private String kafkaFilterValue;

    /**
     * From here are data outside database model.
     */
    private List<AppServiceContent> contentList;
    private List<AppServiceHeader> headerList;
    private String proxyHost;
    private int proxyPort;
    private boolean proxy;
    private boolean proxyWithCredential;
    private String proxyUser;
    // Result from call.
    private String responseHTTPVersion;
    private int responseHTTPCode;
    private String responseHTTPBody;
    private String responseHTTPBodyContentType;
    private List<AppServiceHeader> responseHeaderList;
    private int timeoutms; // Timeout used during service request
    private byte[] file;
    private long kafkaResponseOffset;
    private int kafkaResponsePartition;
    private int kafkaWaitNbEvent;
    private int kafkaWaitSecond;
    private boolean recordTraceFile;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_SOAP = "SOAP";
    public static final String TYPE_REST = "REST";
    public static final String TYPE_FTP = "FTP";
    public static final String TYPE_KAFKA = "KAFKA";
    public static final String METHOD_HTTPPOST = "POST";
    public static final String METHOD_HTTPGET = "GET";
    public static final String METHOD_HTTPDELETE = "DELETE";
    public static final String METHOD_HTTPPUT = "PUT";
    public static final String METHOD_HTTPPATCH = "PATCH";
    public static final String METHOD_KAFKAPRODUCE = "PRODUCE";
    public static final String METHOD_KAFKASEARCH = "SEARCH";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_XML = "XML";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_JSON = "JSON";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_TXT = "TXT";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_UNKNOWN = "UNKNOWN";

    public byte[] getFile() {
        return this.file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public boolean isRecordTraceFile() {
        return recordTraceFile;
    }

    public void setRecordTraceFile(boolean recordTraceFile) {
        this.recordTraceFile = recordTraceFile;
    }

    public int getTimeoutms() {
        return timeoutms;
    }

    public void setTimeoutms(int timeoutms) {
        this.timeoutms = timeoutms;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isProxyWithCredential() {
        return proxyWithCredential;
    }

    public void setProxyWithCredential(boolean proxyWithCredential) {
        this.proxyWithCredential = proxyWithCredential;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getResponseHTTPBodyContentType() {
        return responseHTTPBodyContentType;
    }

    public void setResponseHTTPBodyContentType(String responseHTTPBodyContentType) {
        this.responseHTTPBodyContentType = responseHTTPBodyContentType;
    }

    public String getResponseHTTPVersion() {
        return responseHTTPVersion;
    }

    public void setResponseHTTPVersion(String responseHTTPVersion) {
        this.responseHTTPVersion = responseHTTPVersion;
    }

    public List<AppServiceHeader> getResponseHeaderList() {
        return responseHeaderList;
    }

    public void setResponseHeaderList(List<AppServiceHeader> responseHeaderList) {
        this.responseHeaderList = responseHeaderList;
    }

    public void addResponseHeaderList(AppServiceHeader object) {
        this.responseHeaderList.add(object);
    }

    public String getResponseHTTPBody() {
        return responseHTTPBody;
    }

    public void setResponseHTTPBody(String responseHTTPBody) {
        this.responseHTTPBody = responseHTTPBody;
    }

    public int getResponseHTTPCode() {
        return responseHTTPCode;
    }

    public void setResponseHTTPCode(int responseHTTPCode) {
        this.responseHTTPCode = responseHTTPCode;
    }

    public List<AppServiceContent> getContentList() {
        return contentList;
    }

    public void setContentList(List<AppServiceContent> contentList) {
        this.contentList = contentList;
    }

    public List<AppServiceHeader> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<AppServiceHeader> headerList) {
        this.headerList = headerList;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setAttachementURL(String attachementURL) {
        this.attachementURL = attachementURL;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setServiceRequest(String serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperation() {
        return operation;
    }

    public String getAttachementURL() {
        return attachementURL;
    }

    public String getServicePath() {
        return servicePath;
    }

    public String getGroup() {
        return group;
    }

    public String getService() {
        return service;
    }

    public String getServiceRequest() {
        return serviceRequest;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public String getKafkaKey() {
        return kafkaKey;
    }

    public void setKafkaKey(String kafkaKey) {
        this.kafkaKey = kafkaKey;
    }

    public long getKafkaResponseOffset() {
        return kafkaResponseOffset;
    }

    public void setKafkaResponseOffset(long kafkaResponseOffset) {
        this.kafkaResponseOffset = kafkaResponseOffset;
    }

    public int getKafkaResponsePartition() {
        return kafkaResponsePartition;
    }

    public void setKafkaResponsePartition(int kafkaResponsePartition) {
        this.kafkaResponsePartition = kafkaResponsePartition;
    }

    public String getKafkaFilterPath() {
        return kafkaFilterPath;
    }

    public void setKafkaFilterPath(String kafkaFilter) {
        this.kafkaFilterPath = kafkaFilter;
    }

    public int getKafkaWaitNbEvent() {
        return kafkaWaitNbEvent;
    }

    public void setKafkaWaitNbEvent(int kafkaWaitNbEvent) {
        this.kafkaWaitNbEvent = kafkaWaitNbEvent;
    }

    public int getKafkaWaitSecond() {
        return kafkaWaitSecond;
    }

    public void setKafkaWaitSecond(int kafkaWaitSecond) {
        this.kafkaWaitSecond = kafkaWaitSecond;
    }

    public String getKafkaFilterValue() {
        return kafkaFilterValue;
    }

    public void setKafkaFilterValue(String kafkaFilterValue) {
        this.kafkaFilterValue = kafkaFilterValue;
    }

    public JSONObject toJSONOnExecution() {
        switch (this.getType()) {
            case AppService.TYPE_FTP:
                return this.toJSONOnFTPExecution();
            case AppService.TYPE_KAFKA:
                return this.toJSONOnKAFKAExecution();
            default:
                return this.toJSONOnDefaultExecution();
        }

    }

    public JSONObject toJSONOnDefaultExecution() {

        JSONObject jsonMain = new JSONObject();
        JSONObject jsonMyRequest = new JSONObject();
        JSONObject jsonMyResponse = new JSONObject();
        try {
            // Request Information.
            if (!(this.getTimeoutms() == 0)) {
                jsonMyRequest.put("HTTP-TimeOutMs", this.getTimeoutms());
            }
            jsonMyRequest.put("CalledURL", this.getServicePath());
            if (!StringUtil.isNullOrEmpty(this.getMethod())) {
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

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            jsonMyResponse.put("HTTP-ReturnCode", this.getResponseHTTPCode());
            jsonMyResponse.put("HTTP-Version", this.getResponseHTTPVersion());
            if (!StringUtil.isNullOrEmpty(this.getResponseHTTPBody())) {
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
            if (!StringUtil.isNullOrEmpty(this.getMethod())) {
                jsonMyRequest.put("KAFKA-Method", this.getMethod());
            }
            jsonMyRequest.put("ServiceType", this.getType());
            if (!(this.getContentList().isEmpty())) {
                JSONObject jsonProps = new JSONObject();
                for (AppServiceContent prop : this.getContentList()) {
                    if (prop.getKey().contains("passw")) {
                        jsonProps.put(prop.getKey(), "XXXXXXXX");
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
                        jsonHeaders.put(header.getKey(), "XXXXXXXX");
                    } else {
                        jsonHeaders.put(header.getKey(), header.getValue());
                    }
                }
                jsonMyRequest.put("KAFKA-Header", jsonHeaders);
            }
            if (!StringUtil.isNullOrEmpty(this.getServiceRequest())) {
                try {
                    JSONObject reqBody = new JSONObject(this.getServiceRequest());
                    jsonMyRequest.put("KAFKA-Request", reqBody);
                } catch (JSONException e) {
                    jsonMyRequest.put("KAFKA-Request", this.getServiceRequest());
                }
            }
            jsonMyRequest.put("KAFKA-Key", this.getKafkaKey());
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
                jsonMyRequest.put("KAFKA-SearchFilter", jsonFilters);
            }

            jsonMain.put("Request", jsonMyRequest);

            // Response Information.
            if (this.getKafkaResponseOffset() >= 0) {
                jsonMyResponse.put("Offset", this.getKafkaResponseOffset());
            }
            if (this.getKafkaResponsePartition() >= 0) {
                jsonMyResponse.put("Partition", this.getKafkaResponsePartition());
            }
            if (!StringUtil.isNullOrEmpty(this.getResponseHTTPBody())) {
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
            if (!StringUtil.isNullOrEmpty(this.getMethod())) {
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
