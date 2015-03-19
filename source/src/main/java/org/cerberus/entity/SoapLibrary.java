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
package org.cerberus.entity;

/**
 * Map la table SOAPLIBRARY
 *
 * @author cte
 */
public class SoapLibrary {

    private String type;
    private String name;
    private String envelope;
    private String description;
    private String servicePath;
    private String parsingAnswer;
    private String method;
    private String attachmentUrl;

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParsingAnswer(String parsingAnswer) {
        this.parsingAnswer = parsingAnswer;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public String getParsingAnswer() {
        return parsingAnswer;
    }

    public String getServicePath() {
        return servicePath;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getEnvelope() {
        return envelope;
    }

    public String getDescription() {
        return description;
    }

    
}
