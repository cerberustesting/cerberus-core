/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class FactoryTestTest {

    @Autowired
    private IFactoryTest factory;

    @Test
    public void testCanCreateTest() {

        com.redcats.tst.entity.Test test;
        String tst = "test";
        String description = "description";
        String active = "Y";
        String automated = "N";
        String tDateCrea = "tDateCrea";
         
        test = factory.create(tst, description, active, automated, tDateCrea);

        assertEquals(test.getTest(),          tst);
        assertEquals(test.getDescription(),   description);
        assertEquals(test.getActive(),        active);
        assertEquals(test.getAutomated(),     automated);
        assertEquals(test.gettDateCrea(),     tDateCrea);
    }
}
