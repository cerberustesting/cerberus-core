/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.StatisticDetail;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionWWWDetail {
    
    StatisticDetail create(long start,long end,String url,String ext,int status,String method,long bytes,
            long time,String hostReq,String pageRes,String contentType);
}
