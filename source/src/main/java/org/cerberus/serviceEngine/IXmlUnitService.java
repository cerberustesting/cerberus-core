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

	/**
	 * If and element is present or not from the last SOAP call
	 * 
	 * @param tCExecution
	 *            the associated {@link TestCaseExecution}
	 * @param xpath
	 *            to the element to find from the last SOAP call
	 * @return <code>true</code> if element is present from the last SOAP call,
	 *         <code>false</code> otherwise
	 */
	boolean isElementPresent(TestCaseExecution tCExecution, String xpath);

	/**
	 * If the given tree is similar to the element which is located to the xpath
	 * from the last SOAP call.
	 * 
	 * <p>
	 * Similar trees have the same structure but can have not same values
	 * </p>
	 * 
	 * @param tCExecution
	 *            the associated {@link TestCaseExecution}
	 * @param xpath
	 *            to the element to find from the last SOAP call
	 * @param tree
	 *            to test against what is located from the xpath from the last
	 *            SOAP call
	 * @return <code>true</code> if trees are similars, <code>false</code>
	 *         otherwise
	 */
	boolean isSimilarTree(TestCaseExecution tCExecution, String xpath, String tree);

	/**
	 * Gets a text value contained from the given URL or last SOAP call
	 * 
	 * @param uuid
	 *            the associated execution UUID
	 * @param url
	 *            if not <code>null</code> then use this URL to get the value.
	 *            If <code>null</code> then use the last SOAP call
	 * @param xpath
	 *            to the element to get text value
	 * @return the text value from the given URL or last SOAP call which is
	 *         located to the given xpath
	 */
	String getFromXml(String uuid, String url, String xpath);

	/**
	 * Gets differences from XML representations given in argument.
	 * 
	 * <p>
	 * XML representation can be:
	 * <ul>
	 * <li>a raw XML from a {@link String}</li>
	 * <li>an URL to a XML file. In this case, XML representation must be
	 * prefixed by <code>url=</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Differences are computed by using left as base. So, results are left
	 * relative. However, in case of non-existing path from the left part, then
	 * the right one is given, instead of getting a null XPath.
	 * </p>
	 * 
	 * <p>
	 * Differences are represented by a list of XPath contained into the
	 * following XML structure:
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
	 * Checks if the element contained into the given xpath from the last SOAP
	 * call is equal to the expected given one.
	 * 
	 * @param tCExecution
	 *            the associated {@link TestCaseExecution}
	 * @param xpath
	 *            the xpath to the element to test from the last SOAP call
	 * @param expectedElement
	 *            the expected element to test against what have been reached
	 *            from the last SOAP call in the given xpath
	 * @return <code>true</code> if the given xpath from the last SOAP call is
	 *         equal to the expected one, <code>false</code> otherwise
	 */
	boolean isElementEquals(TestCaseExecution tCExecution, String xpath, String expectedElement);
}
