/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.engine.gwt;


import java.util.List;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.exception.CerberusEventException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface IPropertyService {
	
    String decodeValueWithExistingProperties(String stringToDecode, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException;
        
    List<TestCaseCountryProperties> getListOfPropertiesLinkedToProperty(String test, String testCase, String country, String property, String usedTest, String usedTestCase, List<String> crossedProperties, List<TestCaseCountryProperties> propertieOfTestcase) ;
    
        
}
