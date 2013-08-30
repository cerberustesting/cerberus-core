package com.redcats.tst.entity;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 05/03/2013
 * @since 2.0.0
 */
public class StatisticDetail {
    private long start;
    private long end;
    private String url;
    private String ext;
    private int status;
    private String method;
    private long bytes;
    private long time;
    private String hostReq;
    private String pageRes;
    private String contentType;

    public long getStart() {
        return this.start;
    }

    public void setStart(long tempStart) {
        this.start = tempStart;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long tempEnd) {
        this.end = tempEnd;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String tempUrl) {
        this.url = tempUrl;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String tempExt) {
        this.ext = tempExt;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int tempStatus) {
        this.status = tempStatus;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String tempMethod) {
        this.method = tempMethod;
    }

    public long getBytes() {
        return this.bytes;
    }

    public void setBytes(long tempBytes) {
        this.bytes = tempBytes;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long tempTime) {
        this.time = tempTime;
    }

    public String getHostReq() {
        return this.hostReq;
    }

    public void setHostReq(String host) {
        this.hostReq = host;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String tempContentType) {
        this.contentType = tempContentType;
    }

    public void setContentType() {
        if (this.isImage()) {
            this.contentType = "image";
        } else if (this.isPage()) {
            this.contentType = "page";
        } else if (this.isStyle()) {
            this.contentType = "style";
        } else if (this.isScript()) {
            this.contentType = "script";
        }
    }

    public String getPageRes() {
        return this.pageRes;
    }

    public void setPageRes(String tempPageReq) {
        this.pageRes = tempPageReq;
    }

    public boolean isImage() {
        return this.ext.equalsIgnoreCase("jpg") || this.ext.equalsIgnoreCase("png") || this.ext.equalsIgnoreCase("gif");
    }

    public boolean isPage() {
        return this.ext.equalsIgnoreCase("html") || this.ext.equalsIgnoreCase("php") || this.ext.equalsIgnoreCase("aspx") || this.ext.equalsIgnoreCase("jsp");
    }

    public boolean isStyle() {
        return this.ext.equalsIgnoreCase("css");
    }

    public boolean isScript() {
        return this.ext.equalsIgnoreCase("javascript") || this.ext.equalsIgnoreCase("js");
    }
}
