/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

/**
 *
 * @author bcivel
 */
public class TestCaseExecutionData {

    private long id;
    private String property;
    private String value;
    private String type;
    private String object;
    private String RC;
    private String rMessage;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    /**
     *
     */
    private TCExecution tCExecution;
    private MessageEvent propertyResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;

    public String getrMessage() {
        return rMessage;
    }

    public void setrMessage(String rMessage) {
        this.rMessage = rMessage;
    }

    public MessageEvent getPropertyResultMessage() {
        return propertyResultMessage;
    }

    public void setPropertyResultMessage(MessageEvent propertyResultMessage) {
        this.propertyResultMessage = propertyResultMessage;
        if (propertyResultMessage != null) {
            this.setRC(propertyResultMessage.getCodeString());
            this.setrMessage(propertyResultMessage.getDescription());
            this.executionResultMessage = new MessageGeneral(propertyResultMessage.getMessage());
            this.stopExecution = propertyResultMessage.isStopTest();
        }
    }

    public MessageGeneral getExecutionResultMessage() {
        return executionResultMessage;
    }

    public void setExecutionResultMessage(MessageGeneral executionResultMessage) {
        this.executionResultMessage = executionResultMessage;
    }

    public boolean isStopExecution() {
        return stopExecution;
    }

    public void setStopExecution(boolean stopExecution) {
        this.stopExecution = stopExecution;
    }

    public TCExecution gettCExecution() {
        return tCExecution;
    }

    public void settCExecution(TCExecution tCExecution) {
        this.tCExecution = tCExecution;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getEndLong() {
        return endLong;
    }

    public void setEndLong(long endLong) {
        this.endLong = endLong;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getRC() {
        return RC;
    }

    public void setRC(String rc) {
        this.RC = rc;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStartLong() {
        return startLong;
    }

    public void setStartLong(long startLong) {
        this.startLong = startLong;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
