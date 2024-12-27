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
package org.cerberus.core.service.har.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

/**
 * @author vertigo17
 */
public class HarStat {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(HarStat.class);

    public enum Types {
        TOTAL, JS, CSS, HTML, IMG, CONTENT, FONT, MEDIA, OTHER
    };

    public enum Parties {
        TOTAL, INTERNAL
    };

    public enum Units {
        REQUEST, TOTALSIZE, SIZEMAX, TOTALTIME, TIMEMAX, NBTHIRDPARTY
    };

    private HashMap<String, String> hosts;
    private List<JSONObject> urlList;

    private int nbRequests;
    private HashMap<Integer, Integer> httpStatusCode;
    private int nbError;
    private List<String> urlError;

    // size requests.
    private int sizeSum;
    private int sizeMax;
    private String urlSizeMax;

    // timing requests.
    private Date firstStart;
    private String firstStartS;
    private Date firstEnd;
    private int firstDuration;
    private String firstURL;

    private Date lastStart;
    private String lastStartS;
    private Date lastEnd;
    private int lastDuration;
    private String lastURL;

    private int timeSum;
    private int timeAvg;
    private int timeMax;
    private int timeTotalDuration;
    private String urlTimeMax;

    // per type.
    private int jsSizeSum;
    private int jsSizeMax;
    private int jsRequests;
    private String urlJsSizeMax;
    private List<String> jsList;

    private int cssSizeSum;
    private int cssSizeMax;
    private int cssRequests;
    private String urlCssSizeMax;
    private List<String> cssList;

    private int htmlSizeSum;
    private int htmlSizeMax;
    private int htmlRequests;
    private String urlHtmlSizeMax;
    private List<String> htmlList;

    private int imgSizeSum;
    private int imgSizeMax;
    private int imgRequests;
    private String urlImgSizeMax;
    private List<String> imgList;

    private int contentSizeSum;
    private int contentSizeMax;
    private int contentRequests;
    private String urlContentSizeMax;
    private List<String> contentList;

    private int fontSizeSum;
    private int fontSizeMax;
    private int fontRequests;
    private String urlFontSizeMax;
    private List<String> fontList;

    private int mediaSizeSum;
    private int mediaSizeMax;
    private int mediaRequests;
    private String urlMediaSizeMax;
    private List<String> mediaList;

    private int otherSizeSum;
    private int otherSizeMax;
    private int otherRequests;
    private String urlOtherSizeMax;
    private List<String> otherList;

    public HarStat() {
        LOG.debug("Init HarStat Object.");

        hosts = new HashMap<>();
        urlList = new ArrayList<>();

        jsList = new ArrayList<>();
        cssList = new ArrayList<>();
        htmlList = new ArrayList<>();
        imgList = new ArrayList<>();
        contentList = new ArrayList<>();
        fontList = new ArrayList<>();
        otherList = new ArrayList<>();

        List<Integer> httpRetList = Arrays.asList(200, 300, 301, 302, 303, 304, 400, 401, 402, 403, 404, 500);

        httpStatusCode = new HashMap<>();
        for (Integer tmpInteger : httpRetList) {
            httpStatusCode.put(tmpInteger, 0);
        }
        nbRequests = 0;
        nbError = 0;
        urlError = new ArrayList<>();

        sizeSum = 0;
        sizeMax = 0;
        urlSizeMax = null;

        timeSum = 0;
        timeAvg = 0;
        timeMax = 0;
        timeTotalDuration = 0;
        urlTimeMax = null;

        firstStartS = null;
        firstDuration = 0;
        firstURL = null;

        lastStartS = null;
        lastDuration = 0;
        lastURL = null;

        jsSizeSum = 0;
        jsSizeMax = 0;
        jsRequests = 0;
        urlJsSizeMax = null;

        cssSizeSum = 0;
        cssSizeMax = 0;
        cssRequests = 0;
        urlCssSizeMax = null;

        htmlSizeSum = 0;
        htmlSizeMax = 0;
        htmlRequests = 0;
        urlHtmlSizeMax = null;

        imgSizeSum = 0;
        imgSizeMax = 0;
        imgRequests = 0;
        urlImgSizeMax = null;

        contentSizeSum = 0;
        contentSizeMax = 0;
        contentRequests = 0;
        urlContentSizeMax = null;

        fontSizeSum = 0;
        fontSizeMax = 0;
        fontRequests = 0;
        urlFontSizeMax = null;

        mediaSizeSum = 0;
        mediaSizeMax = 0;
        mediaRequests = 0;
        urlMediaSizeMax = null;

        otherSizeSum = 0;
        otherSizeMax = 0;
        otherRequests = 0;
        urlOtherSizeMax = null;

    }

    public List<JSONObject> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<JSONObject> urlList) {
        this.urlList = urlList;
    }

    public void appendUrlList(JSONObject urlEntry) {
        this.urlList.add(urlEntry);
    }

    public HashMap<String, String> getHosts() {
        return hosts;
    }

    public void setHosts(HashMap<String, String> domains) {
        this.hosts = domains;
    }

    public int getJsRequests() {
        return jsRequests;
    }

    public void setJsRequests(int jssRequests) {
        this.jsRequests = jssRequests;
    }

    public int getCssRequests() {
        return cssRequests;
    }

    public void setCssRequests(int cssRequests) {
        this.cssRequests = cssRequests;
    }

    public int getHtmlRequests() {
        return htmlRequests;
    }

    public void setHtmlRequests(int htmlRequests) {
        this.htmlRequests = htmlRequests;
    }

    public int getImgRequests() {
        return imgRequests;
    }

    public void setImgRequests(int imgRequests) {
        this.imgRequests = imgRequests;
    }

    public int getContentRequests() {
        return contentRequests;
    }

    public void setContentRequests(int contentRequests) {
        this.contentRequests = contentRequests;
    }

    public int getFontRequests() {
        return fontRequests;
    }

    public void setFontRequests(int fontRequests) {
        this.fontRequests = fontRequests;
    }

    public int getOtherRequests() {
        return otherRequests;
    }

    public void setOtherRequests(int otherRequests) {
        this.otherRequests = otherRequests;
    }

    public HashMap<Integer, Integer> getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(HashMap<Integer, Integer> httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public int getNbRequests() {
        return nbRequests;
    }

    public void setNbRequests(int nbRequests) {
        this.nbRequests = nbRequests;
    }

    public int getNbError() {
        return nbError;
    }

    public void setNbError(int nbError) {
        this.nbError = nbError;
    }

    public List<String> getUrlError() {
        return urlError;
    }

    public void setUrlError(List<String> urlError) {
        this.urlError = urlError;
    }

    public int getSizeSum() {
        return sizeSum;
    }

    public void setSizeSum(int sizeSum) {
        this.sizeSum = sizeSum;
    }

    public int getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(int sizeMax) {
        this.sizeMax = sizeMax;
    }

    public String getUrlSizeMax() {
        return urlSizeMax;
    }

    public void setUrlSizeMax(String urlSizeMax) {
        this.urlSizeMax = urlSizeMax;
    }

    public Date getFirstStart() {
        return firstStart;
    }

    public void setFirstStart(Date firstStart) {
        this.firstStart = firstStart;
    }

    public String getFirstStartS() {
        return firstStartS;
    }

    public void setFirstStartS(String firstStartS) {
        this.firstStartS = firstStartS;
    }

    public int getFirstDuration() {
        return firstDuration;
    }

    public void setFirstDuration(int firstDuration) {
        this.firstDuration = firstDuration;
    }

    public String getFirstURL() {
        return firstURL;
    }

    public void setFirstURL(String firstURL) {
        this.firstURL = firstURL;
    }

    public int getLastDuration() {
        return lastDuration;
    }

    public void setLastDuration(int lastDuration) {
        this.lastDuration = lastDuration;
    }

    public String getLastURL() {
        return lastURL;
    }

    public void setLastURL(String lastURL) {
        this.lastURL = lastURL;
    }

    public Date getLastStart() {
        return lastStart;
    }

    public void setLastStart(Date lastStart) {
        this.lastStart = lastStart;
    }

    public String getLastStartS() {
        return lastStartS;
    }

    public void setLastStartS(String lastStartS) {
        this.lastStartS = lastStartS;
    }

    public int getTimeSum() {
        return timeSum;
    }

    public void setTimeSum(int timeSum) {
        this.timeSum = timeSum;
    }

    public int getTimeAvg() {
        return timeAvg;
    }

    public void setTimeAvg(int timeAvg) {
        this.timeAvg = timeAvg;
    }

    public int getTimeTotalDuration() {
        return timeTotalDuration;
    }

    public void setTimeTotalDuration(int timeTotalDuration) {
        this.timeTotalDuration = timeTotalDuration;
    }

    public int getTimeMax() {
        return timeMax;
    }

    public void setTimeMax(int timeMax) {
        this.timeMax = timeMax;
    }

    public String getUrlTimeMax() {
        return urlTimeMax;
    }

    public void setUrlTimeMax(String urlTimeMax) {
        this.urlTimeMax = urlTimeMax;
    }

    public Date getFirstEnd() {
        return firstEnd;
    }

    public void setFirstEnd(Date firstEnd) {
        this.firstEnd = firstEnd;
    }

    public Date getLastEnd() {
        return lastEnd;
    }

    public void setLastEnd(Date lastEnd) {
        this.lastEnd = lastEnd;
    }

    public int getJsSizeSum() {
        return jsSizeSum;
    }

    public void setJsSizeSum(int jsSizeSum) {
        this.jsSizeSum = jsSizeSum;
    }

    public int getJsSizeMax() {
        return jsSizeMax;
    }

    public void setJsSizeMax(int jsSizeMax) {
        this.jsSizeMax = jsSizeMax;
    }

    public String getUrlJsSizeMax() {
        return urlJsSizeMax;
    }

    public void setUrlJsSizeMax(String urlJsSizeMax) {
        this.urlJsSizeMax = urlJsSizeMax;
    }

    public List<String> getJsList() {
        return jsList;
    }

    public void setJsList(List<String> jsList) {
        this.jsList = jsList;
    }

    public int getCssSizeSum() {
        return cssSizeSum;
    }

    public void setCssSizeSum(int cssSizeSum) {
        this.cssSizeSum = cssSizeSum;
    }

    public int getCssSizeMax() {
        return cssSizeMax;
    }

    public void setCssSizeMax(int cssSizeMax) {
        this.cssSizeMax = cssSizeMax;
    }

    public String getUrlCssSizeMax() {
        return urlCssSizeMax;
    }

    public void setUrlCssSizeMax(String urlCssSizeMax) {
        this.urlCssSizeMax = urlCssSizeMax;
    }

    public List<String> getCssList() {
        return cssList;
    }

    public void setCssList(List<String> cssList) {
        this.cssList = cssList;
    }

    public int getHtmlSizeSum() {
        return htmlSizeSum;
    }

    public void setHtmlSizeSum(int htmlSizeSum) {
        this.htmlSizeSum = htmlSizeSum;
    }

    public int getHtmlSizeMax() {
        return htmlSizeMax;
    }

    public void setHtmlSizeMax(int htmlSizeMax) {
        this.htmlSizeMax = htmlSizeMax;
    }

    public String getUrlHtmlSizeMax() {
        return urlHtmlSizeMax;
    }

    public void setUrlHtmlSizeMax(String urlHtmlSizeMax) {
        this.urlHtmlSizeMax = urlHtmlSizeMax;
    }

    public List<String> getHtmlList() {
        return htmlList;
    }

    public void setHtmlList(List<String> htmlList) {
        this.htmlList = htmlList;
    }

    public int getImgSizeSum() {
        return imgSizeSum;
    }

    public void setImgSizeSum(int imgSizeSum) {
        this.imgSizeSum = imgSizeSum;
    }

    public int getImgSizeMax() {
        return imgSizeMax;
    }

    public void setImgSizeMax(int imgSizeMax) {
        this.imgSizeMax = imgSizeMax;
    }

    public String getUrlImgSizeMax() {
        return urlImgSizeMax;
    }

    public void setUrlImgSizeMax(String urlImgSizeMax) {
        this.urlImgSizeMax = urlImgSizeMax;
    }

    public List<String> getImgList() {
        return imgList;
    }

    public void setImgList(List<String> imgList) {
        this.imgList = imgList;
    }

    public int getContentSizeSum() {
        return contentSizeSum;
    }

    public void setContentSizeSum(int contentSizeSum) {
        this.contentSizeSum = contentSizeSum;
    }

    public int getContentSizeMax() {
        return contentSizeMax;
    }

    public void setContentSizeMax(int contentSizeMax) {
        this.contentSizeMax = contentSizeMax;
    }

    public String getUrlContentSizeMax() {
        return urlContentSizeMax;
    }

    public void setUrlContentSizeMax(String urlContentSizeMax) {
        this.urlContentSizeMax = urlContentSizeMax;
    }

    public List<String> getContentList() {
        return contentList;
    }

    public void setContentList(List<String> contentList) {
        this.contentList = contentList;
    }

    public int getFontSizeSum() {
        return fontSizeSum;
    }

    public void setFontSizeSum(int fontSizeSum) {
        this.fontSizeSum = fontSizeSum;
    }

    public int getFontSizeMax() {
        return fontSizeMax;
    }

    public void setFontSizeMax(int fontSizeMax) {
        this.fontSizeMax = fontSizeMax;
    }

    public String getUrlFontSizeMax() {
        return urlFontSizeMax;
    }

    public void setUrlFontSizeMax(String urlFontSizeMax) {
        this.urlFontSizeMax = urlFontSizeMax;
    }

    public List<String> getFontList() {
        return fontList;
    }

    public void setFontList(List<String> fontList) {
        this.fontList = fontList;
    }

    public int getOtherSizeSum() {
        return otherSizeSum;
    }

    public void setOtherSizeSum(int otherSizeSum) {
        this.otherSizeSum = otherSizeSum;
    }

    public int getOtherSizeMax() {
        return otherSizeMax;
    }

    public void setOtherSizeMax(int otherSizeMax) {
        this.otherSizeMax = otherSizeMax;
    }

    public String getUrlOtherSizeMax() {
        return urlOtherSizeMax;
    }

    public void setUrlOtherSizeMax(String urlOtherSizeMax) {
        this.urlOtherSizeMax = urlOtherSizeMax;
    }

    public List<String> getOtherList() {
        return otherList;
    }

    public void setOtherList(List<String> otherList) {
        this.otherList = otherList;
    }

    public int getMediaSizeSum() {
        return mediaSizeSum;
    }

    public void setMediaSizeSum(int mediaSizeSum) {
        this.mediaSizeSum = mediaSizeSum;
    }

    public int getMediaSizeMax() {
        return mediaSizeMax;
    }

    public void setMediaSizeMax(int mediaSizeMax) {
        this.mediaSizeMax = mediaSizeMax;
    }

    public int getMediaRequests() {
        return mediaRequests;
    }

    public void setMediaRequests(int mediaRequests) {
        this.mediaRequests = mediaRequests;
    }

    public String getUrlMediaSizeMax() {
        return urlMediaSizeMax;
    }

    public void setUrlMediaSizeMax(String urlMediaSizeMax) {
        this.urlMediaSizeMax = urlMediaSizeMax;
    }

    public List<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<String> mediaList) {
        this.mediaList = mediaList;
    }

}
