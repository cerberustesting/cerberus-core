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

import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.serviceEngine.impl.diff.Difference;
import org.cerberus.serviceEngine.impl.diff.Differences;
import org.cerberus.util.XmlUtilException;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXException;

/**
 * {@link XmlUnitService} unit tests
 * 
 * @author abourdon
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ XmlUnitService.class })
@ContextConfiguration(locations = { "/applicationContextTest.xml" })
public class XmlUnitServiceTest {

	@InjectMocks
	private XmlUnitService xmlUnitService;

	@Mock
	private ExecutionSOAPResponse executionSOAPResponse;

	private Differences differences;

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
	public void before() throws XmlUtilException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		differences = new Differences();

		Method init = xmlUnitService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(xmlUnitService);
	}

	@Test
	public void testGetDifferencesFromXmlWithNoDifference() throws XmlUtilException {
		String expected = differences.mkString();
		String actual = xmlUnitService.getDifferencesFromXml("<root><a>1</a></root>", "<root><a>1</a></root>");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDifferencesFromXmlWithValueDifference() throws XmlUtilException {
		differences.addDifference(new Difference("/root[1]/a[1]/text()[1]"));

		String expected = differences.mkString();
		String actual = xmlUnitService.getDifferencesFromXml("<root><a>1</a></root>", "<root><a>2</a></root>");
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testGetDifferencesFromXmlWithStructureDifference() throws XmlUtilException {
		differences.addDifference(new Difference("/root[1]/a[1]"));
		differences.addDifference(new Difference("/root[1]/b[1]"));

		String expected = differences.mkString();
		String diff = xmlUnitService.getDifferencesFromXml("<root><a>1</a></root>", "<root><b>1</b></root>");
		Assert.assertEquals(expected, diff);
	}

	@Test
	public void testGetDifferencesFromXmlByUsingURL() throws XmlUtilException {
		differences.addDifference(new Difference("/root[1]/a[1]"));
		differences.addDifference(new Difference("/root[1]/b[1]"));

		String expected = differences.mkString();

		URL left = getClass().getResource("/org/cerberus/serviceEngine/impl/left.xml");
		URL right = getClass().getResource("/org/cerberus/serviceEngine/impl/right.xml");
		String actual = xmlUnitService.getDifferencesFromXml("url=" + left, "url=" + right);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testRemoveDifferenceWhenDifferenceMatch() throws XmlUtilException, SAXException, IOException {
		differences.addDifference(new Difference("/root[1]/a[1]"));
		differences.addDifference(new Difference("/root[1]/a[2]"));
		differences.addDifference(new Difference("/root[1]/b[1]"));

		String actual = xmlUnitService.removeDifference("/root\\[1\\]/a\\[[1-2]\\]", differences.mkString());
		String expected = "<differences><difference>/root[1]/b[1]</difference></differences>";

		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue(diff.toString(), diff.similar());
	}

	@Test
	public void testRemoveDifferenceWhenNoDifferenceMatch() throws XmlUtilException, SAXException, IOException {
		differences.addDifference(new Difference("/root[1]/a[1]"));
		differences.addDifference(new Difference("/root[1]/b[1]"));

		String actual = xmlUnitService.removeDifference("toto", differences.mkString());
		String expected = "<differences><difference>/root[1]/a[1]</difference><difference>/root[1]/b[1]</difference></differences>";

		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue(diff.toString(), diff.similar());
	}

	@Test
	public void testRemoveDifferenceFromEmptyDifferences() throws XmlUtilException {
		String expected = Differences.EMPTY_DIFFERENCES_STRING;
		String actual = xmlUnitService.removeDifference("foo", Differences.EMPTY_DIFFERENCES_STRING);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testIsElementInElementWithExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isElementInElement(tce, "/root/a", "<a>2</a>"));
	}

	@Test
	public void testIsElementInElementWithNotFormatedExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isElementInElement(tce, "/root/a", "               <a>2</a>   "));
	}

	@Test
	public void testIsElementInElementWithNotExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isElementInElement(tce, "/root/a", "<a>3</a>"));
	}

}
