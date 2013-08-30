/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class TestDAOTest {

    @Autowired
    private ITestDAO testDAO;

    @Test
    public void testCanFindAllTest() {
        List<com.redcats.tst.entity.Test> listOfTest;

        listOfTest = testDAO.findAllTest();

        assertTrue(listOfTest.size() > 0);

    }
}
