/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;


import com.redcats.tst.entity.TestCase;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface ITCEwwwDetService {
    
    List<TestcaseExecutionwwwDet> getListOfDetail(int id);
    
    BufferedImage getHistoricOfParameter(TestCase testcase, String parameter);
    
}
