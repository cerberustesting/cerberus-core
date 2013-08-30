/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.StatisticDetail;
import com.redcats.tst.factory.IFactoryTestCaseExecutionWWWDetail;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecutionWWWDetail implements IFactoryTestCaseExecutionWWWDetail {

    @Override
    public StatisticDetail create(long start, long end, String url, String ext, int status, String method, long bytes, long time, String hostReq, String pageRes, String contentType) {
        StatisticDetail statisticDetail = new StatisticDetail();
        statisticDetail.setBytes(bytes);
        statisticDetail.setContentType(contentType);
        statisticDetail.setEnd(end);
        statisticDetail.setExt(ext);
        statisticDetail.setHostReq(hostReq);
        statisticDetail.setMethod(method);
        statisticDetail.setPageRes(pageRes);
        statisticDetail.setStart(start);
        statisticDetail.setStatus(status);
        statisticDetail.setTime(time);
        statisticDetail.setUrl(url);
        return statisticDetail;
    }

}
