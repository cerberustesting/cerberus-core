/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCaseLabel;
import org.cerberus.core.crud.factory.IFactoryTestCaseLabel;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseLabel implements IFactoryTestCaseLabel {

    
    @Override
    public TestCaseLabel create(Integer id, String test, String testCase, Integer labelId, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif, Label label) {
        TestCaseLabel testCaseLabel = new TestCaseLabel();
        testCaseLabel.setDateCreated(dateCreated);
        testCaseLabel.setDateModif(dateModif);
        testCaseLabel.setId(id);
        testCaseLabel.setLabelId(labelId);
        testCaseLabel.setTest(test);
        testCaseLabel.setTestcase(testCase);
        testCaseLabel.setUsrCreated(usrCreated);
        testCaseLabel.setUsrModif(usrModif);
        testCaseLabel.setLabel(label);
        return testCaseLabel;
    }


}
