package com.redcats.tst.service;

import com.redcats.tst.entity.Test;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
public interface ITestService {


    /**
     * @return
     */
    List<String> getListOfTests();

    /**
     * @return
     */
    List<Test> getListOfTest();

}
