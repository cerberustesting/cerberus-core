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
package org.cerberus.service.har.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author vertigo17
 */
public class HarStat {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(HarStat.class);

    private int nbRequests;
    private int nb200;
    private int nb300;
    private int nb301;
    private int nb302;
    private int nb307;
    private int nb400;
    private int nb403;
    private int nb404;
    private int nb500;
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
    private String urlJsSizeMax;
    private List<String> jsList;

    private int cssSizeSum;
    private int cssSizeMax;
    private String urlCssSizeMax;
    private List<String> cssList;

    private int htmlSizeSum;
    private int htmlSizeMax;
    private String urlHtmlSizeMax;
    private List<String> htmlList;

    private int imgSizeSum;
    private int imgSizeMax;
    private String urlImgSizeMax;
    private List<String> imgList;

    private int contentSizeSum;
    private int contentSizeMax;
    private String urlContentSizeMax;
    private List<String> contentList;

    private int fontSizeSum;
    private int fontSizeMax;
    private String urlFontSizeMax;
    private List<String> fontList;

    private int otherSizeSum;
    private int otherSizeMax;
    private String urlOtherSizeMax;
    private List<String> otherList;

    public HarStat() {
        LOG.debug("Init HarStat Object.");
        urlError = new ArrayList<>();
        jsList = new ArrayList<>();
        cssList = new ArrayList<>();
        htmlList = new ArrayList<>();
        imgList = new ArrayList<>();
        contentList = new ArrayList<>();
        fontList = new ArrayList<>();
        otherList = new ArrayList<>();
        nbRequests = 0;
        nb200 = 0;
        nb300 = 0;
        nb301 = 0;
        nb302 = 0;
        nb307 = 0;
        nb400 = 0;
        nb403 = 0;
        nb404 = 0;
        nb500 = 0;
        nbError = 0;
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
        urlJsSizeMax = null;
        cssSizeSum = 0;
        cssSizeMax = 0;
        urlCssSizeMax = null;
        htmlSizeSum = 0;
        htmlSizeMax = 0;
        urlHtmlSizeMax = null;
        imgSizeSum = 0;
        imgSizeMax = 0;
        urlImgSizeMax = null;
        contentSizeSum = 0;
        contentSizeMax = 0;
        urlContentSizeMax = null;
        fontSizeSum = 0;
        fontSizeMax = 0;
        urlFontSizeMax = null;
        otherSizeSum = 0;
        otherSizeMax = 0;
        urlOtherSizeMax = null;

    }

    public int getNbRequests() {
        return nbRequests;
    }

    public void setNbRequests(int nbRequests) {
        this.nbRequests = nbRequests;
    }

    public int getNb200() {
        return nb200;
    }

    public void setNb200(int nb200) {
        this.nb200 = nb200;
    }

    public int getNb300() {
        return nb300;
    }

    public int getNb307() {
        return nb307;
    }

    public void setNb307(int nb307) {
        this.nb307 = nb307;
    }

    public int getNb403() {
        return nb403;
    }

    public void setNb403(int nb403) {
        this.nb403 = nb403;
    }

    public void setNb300(int nb300) {
        this.nb300 = nb300;
    }

    public int getNb301() {
        return nb301;
    }

    public void setNb301(int nb301) {
        this.nb301 = nb301;
    }

    public int getNb302() {
        return nb302;
    }

    public void setNb302(int nb302) {
        this.nb302 = nb302;
    }

    public int getNb400() {
        return nb400;
    }

    public void setNb400(int nb400) {
        this.nb400 = nb400;
    }

    public int getNb404() {
        return nb404;
    }

    public void setNb404(int nb404) {
        this.nb404 = nb404;
    }

    public int getNb500() {
        return nb500;
    }

    public void setNb500(int nb500) {
        this.nb500 = nb500;
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

}
