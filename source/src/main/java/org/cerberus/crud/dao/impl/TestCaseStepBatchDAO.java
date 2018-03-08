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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseStepBatchDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TestCaseStepBatch;
import org.cerberus.crud.factory.IFactoryTestCaseStepBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseStepBatchDAO implements ITestCaseStepBatchDAO {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepBatchDAO.class);
    
    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseStepBatch factoryTestCaseStepBatch;

    /**
     * Short one line description.
     * <p/>
     * Longer description. If there were any, it would be here. <p> And even
     * more explanations to follow in consecutive paragraphs separated by HTML
     * paragraph breaks.
     *
     * @param variable Description text text text.
     * @return Description text text text.
     */
    @Override
    public List<TestCaseStepBatch> findTestCaseStepBatchByTestCaseStep(String test, String testcase, int stepNumber) {
        List<TestCaseStepBatch> list = null;
        final String query = "SELECT * FROM testcasestepbatch WHERE test = ? AND testcase = ? AND step = ?";

        
        try(Connection connection = this.databaseSpring.connect();
        		PreparedStatement preStat = connection.prepareStatement(query);) {
            
        	preStat.setString(1, test);
            preStat.setString(2, testcase);
            preStat.setInt(3, stepNumber);
            
            list = new ArrayList<TestCaseStepBatch>();
            try(ResultSet resultSet = preStat.executeQuery();) {
                while (resultSet.next()) {
                    String batch = resultSet.getString("Batch");
                    list.add(factoryTestCaseStepBatch.create(test, testcase, stepNumber, batch));
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to execute query : "+exception.toString());
            } 
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : "+exception.toString());
        } 
        return list;
    }
}
