/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
