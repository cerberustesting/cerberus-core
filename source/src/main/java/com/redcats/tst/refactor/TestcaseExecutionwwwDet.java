/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

/**
 *
 * @author bcivel
 */
public class TestcaseExecutionwwwDet {

   int id;
   
   int execID;
   
   String start;
   
   String url;
   
   String end;
   
   String ext;
   
   int statusCode;
   
   String method;
   
   int bytes;
   
   int timeInMillis;
   
   String reqHeader_Host;
   
   String resHeader_ContentType;

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getExecID() {
        return execID;
    }

    public void setExecID(int execID) {
        this.execID = execID;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReqHeader_Host() {
        return reqHeader_Host;
    }

    public void setReqHeader_Host(String reqHeader_Host) {
        this.reqHeader_Host = reqHeader_Host;
    }

    public String getResHeader_ContentType() {
        return resHeader_ContentType;
    }

    public void setResHeader_ContentType(String resHeader_ContentType) {
        this.resHeader_ContentType = resHeader_ContentType;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(int timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
   
   
    
}
