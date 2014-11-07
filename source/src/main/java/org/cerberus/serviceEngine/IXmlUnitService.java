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
package org.cerberus.serviceEngine;

import org.cerberus.entity.TestCaseExecution;

/**
 *
 * @author bcivel
 */
public interface IXmlUnitService {

    boolean isElementPresent(TestCaseExecution tCExecution, String element);
    
    boolean isTextInElement(TestCaseExecution tCExecution, String element, String text);
    
    boolean isSimilarTree(TestCaseExecution tCExecution, String element, String text);
    
    String getFromXml(String uuid, String url, String element);
    
	/**
	 * Gets differences from XML representations given in argument.
	 * 
	 * <p>
	 * XML representation can be:
	 * <ul>
	 * <li>a raw XML from a {@link String}</li>
	 * <li>an URL to a XML file. In this case, XML representation must be prefixed by <code>url=</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Differences are computed by using left as base. So, results are left relative. However, in case of non-existing path from the left part, then the right one is given, instead
	 * of getting a null XPath.
	 * </p>
	 * 
	 * <p>
	 * Differences are represented by a list of XPath contained into the following XML structure:
	 * 
	 * <pre>
	 * {@code
	 * 	<differences>
	 * 		<difference>/xpath/to/the/first/difference</difference>
	 * 		<difference>/xpath/to/the/second/difference</difference>
	 * 	</differences>
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param left
	 *            the base XML representation to compare
	 * @param right
	 *            the XML representation to compare from the <code>left</code>
	 * @return a list of XPath
	 */
	String getDifferencesFromXml(String left, String right);
	
	/**
	 * Removes differences found by applying the given pattern.
	 * 
	 * @param pattern
	 *            the pattern used to find differences to remove
	 * @param differences
	 *            the differences variable to filter
	 * @return a new filtered differences variable
	 */
	String removeDifference(String pattern, String differences);
	
	/**
	 * Checks if the given element is contained into the given xpath from the
	 * last SOAP call
	 * 
	 * @param tCExecution
	 *            the associated {@link TestCaseExecution}
	 * @param element
	 *            the element to test if it is contained into the given xpath
	 *            from the last SOAP call
	 * @param element
	 *            the element to test if it is contained into the given xpath
	 *            from the last SOAP call
	 * @return <code>true</code> if the given element is contained into the
	 *         xpath from the last SOAP call, <code>false</code> otherwise
	 */
	boolean isElementInElement(TestCaseExecution tCExecution, String xpath, String element);
}
