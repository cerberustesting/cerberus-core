/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.StatisticSummary;
import com.redcats.tst.factory.IFactoryTestCaseExecutionWWWSummary;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecutionWWWSummary implements IFactoryTestCaseExecutionWWWSummary {

    @Override
    public StatisticSummary create(int totNbHits, int totTps, int totSize, int nbRc2xx, int nbRc3xx, int nbRc4xx, int nbRc5xx, int imgNb, int imgTps, int imgSizeTot, int imgSizeMax, String imgSizeMaxUrl, int jsNb, int jsTps, int jsSizeTot, int jsSizeMax, String jsSizeMaxUrl, int cssNb, int cssTps, int cssSizeTot, int cssSizeMax, String cssSizeMaxUrl) {
        return new StatisticSummary();
    }

}
