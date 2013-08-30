/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Test;

/**
 *
 * @author bcivel
 */
public interface IFactoryTest {

    /**
     * 
     * @param test Name of the Test
     * @param description Description of the Test
     * @param active Boolean active : Y=Active / N=Inactive
     * @param automated Boolean Automated : Y=Automated / N=Manual
     * @param tDateCrea String Date of Creation of the test
     * @return Test Object Created
     */
    Test create(String test, String description,
            String active, String automated, String tDateCrea);
}
