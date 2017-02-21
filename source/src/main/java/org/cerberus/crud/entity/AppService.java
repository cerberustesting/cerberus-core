/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
    private String parsingAnswer; // Should not be used.
    private String group; // Information in order to group the services in order to organise them
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;
    /**
     * From here are data outside database model.
     */
    private String attachmentUrl;
    private List<AppServiceContent> contentList;
    private List<AppServiceHeader> headerList;
    // Result from call.
    private String responseHTTPVersion;
    private int responseHTTPCode;
    private String responseHTTPBody;
    private String responseHTTPBodyContentType;
    private List<AppServiceHeader> responseHeaderList;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_SOAP = "SOAP";
    public static final String TYPE_REST = "REST";
    public static final String METHOD_HTTPPOST = "POST";
    public static final String METHOD_HTTPGET = "GET";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_XML = "XML";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_JSON = "JSON";
    public static final String RESPONSEHTTPBODYCONTENTTYPE_TXT = "TXT";

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

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setParsingAnswer(String parsingAnswer) {
        this.parsingAnswer = parsingAnswer;
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

    public String getParsingAnswer() {
        return parsingAnswer;
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

}
