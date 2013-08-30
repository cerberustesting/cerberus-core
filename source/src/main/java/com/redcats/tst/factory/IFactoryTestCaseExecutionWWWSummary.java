/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.StatisticSummary;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCaseExecutionWWWSummary {
    
    StatisticSummary create(int totNbHits,int totTps,int totSize,int nbRc2xx,int nbRc3xx,int nbRc4xx,
            int nbRc5xx,int imgNb,int imgTps,int imgSizeTot,int imgSizeMax,String imgSizeMaxUrl,
            int jsNb,int jsTps,int jsSizeTot,int jsSizeMax,String jsSizeMaxUrl,int cssNb,int cssTps,
            int cssSizeTot,int cssSizeMax,String cssSizeMaxUrl);
}
