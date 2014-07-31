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
package org.cerberus.serviceEngine.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.cerberus.entity.TestCaseExecution;
import org.cerberus.util.XmlUtil;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * {@link XmlUnitService} unit tests
 * 
 * @author abourdon
 */
@PrepareForTest({ XmlUnitService.class })
@ContextConfiguration(locations = { "/applicationContextTest.xml" })
public class XmlUnitServiceTest {

	@InjectMocks
	private XmlUnitService xmlUnitService;

	private Document resultDoc;

	private Element resultRoot;

	private TestCaseExecution tCExecution;

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setCompareUnmatched(false);
	}

	public XmlUnitServiceTest() {
	}

	@Before
	public void before() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ParserConfigurationException {
		resultDoc = XmlUtil.createNewDocument();
		resultRoot = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCES);
		tCExecution = new TestCaseExecution();

		MockitoAnnotations.initMocks(this);
		Method init = xmlUnitService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(xmlUnitService);
	}

	@Test
	public void testGetDifferencesFromXmlWithNoDifference() throws SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		String expected = XmlUtil.convertToString(resultRoot);
		String actual = xmlUnitService.getDifferencesFromXml(tCExecution, "<root><a>1</a></root>", "<root><a>1</a></root>");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDifferencesFromXmlWithValueDifference() throws TransformerFactoryConfigurationError, TransformerException {
		Element diff = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCE);
		diff.appendChild(resultDoc.createTextNode("/root[1]/a[1]/text()[1]"));
		resultRoot.appendChild(diff);

		String expected = XmlUtil.convertToString(resultRoot);
		String actual = xmlUnitService.getDifferencesFromXml(tCExecution, "<root><a>1</a></root>", "<root><a>2</a></root>");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDifferencesFromXmlWithStructureDifference() throws TransformerFactoryConfigurationError, TransformerException {
		Element firstDiff = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCE);
		firstDiff.appendChild(resultDoc.createTextNode("/root[1]/a[1]"));
		resultRoot.appendChild(firstDiff);

		Element secondDiff = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCE);
		secondDiff.appendChild(resultDoc.createTextNode("null"));
		resultRoot.appendChild(secondDiff);

		String expected = XmlUtil.convertToString(resultRoot);
		String diff = xmlUnitService.getDifferencesFromXml(tCExecution, "<root><a>1</a></root>", "<root><b>1</b></root>");
		Assert.assertEquals(expected, diff);
	}

	@Test
	public void testGetDifferencesFromXmlByUsingURL() throws TransformerFactoryConfigurationError, TransformerException {
		Element firstDiff = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCE);
		firstDiff.appendChild(resultDoc.createTextNode("/root[1]/a[1]"));
		resultRoot.appendChild(firstDiff);

		Element secondDiff = resultDoc.createElement(XmlUnitService.RESULT_NODE_NAME_DIFFERENCE);
		secondDiff.appendChild(resultDoc.createTextNode("null"));
		resultRoot.appendChild(secondDiff);

		String expected = XmlUtil.convertToString(resultRoot);

		URL left = getClass().getResource("/org/cerberus/serviceEngine/impl/left.xml");
		URL right = getClass().getResource("/org/cerberus/serviceEngine/impl/right.xml");
		String diff = xmlUnitService.getDifferencesFromXml(tCExecution, "url=" + left, "url=" + right);
		Assert.assertEquals(expected, diff);
	}

}
