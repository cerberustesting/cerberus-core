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

import org.cerberus.service.engine.impl.XmlUnitService;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.service.engine.impl.diff.Difference;
import org.cerberus.service.engine.impl.diff.Differences;
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
	public void testIsElementPresentWithElementPresent() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isElementPresent(tce, "/root/a"));
	}

	@Test
	public void testIsElementPresentWithElementPresentWithNamespace() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID()))
				.thenReturn("<root xmlns:prefix=\"http://prefix\"><prefix:a>1</prefix:a><prefix:a>2</prefix:a></root>");

		Assert.assertTrue(xmlUnitService.isElementPresent(tce, "/root/prefix:a"));
	}

	@Test
	public void testIsElementPresentWithElementAbsent() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isElementPresent(tce, "/root/b"));
	}

	@Test
	public void testIsSimilarTreeWithExistingElementAndSimilarTree() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isSimilarTree(tce, "/root", "<root><a>foo</a><a>bar</a></root>"));
	}

	@Test
	public void testIsSimilarTreeWithExistingElementAndIdenticalTree() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isSimilarTree(tce, "/root", "<root><a>1</a><a>2</a></root>"));
	}

	@Test
	public void testIsSimilarTreeWithExistingElementAndSimilarTreeWithNamespace() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID()))
				.thenReturn("<root xmlns:prefix=\"http://prefix\"><prefix:a>1</prefix:a><prefix:a>2</prefix:a></root>");

		Assert.assertTrue(xmlUnitService.isSimilarTree(tce, "/root/prefix:a", "<prefix:a xmlns:prefix=\"http://prefix\">1</prefix:a>"));
	}

	@Test
	public void testIsSimilarTreeWithExistingElementAndNotSimilarTree() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isSimilarTree(tce, "/root", "<root><wrong>foo</wrong><a>bar</a></root>"));
	}

	@Test
	public void testIsSimilarTreeWithNotExistingElementAndSimilarTree() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isSimilarTree(tce, "/plop", "<root><a>foo</a><a>bar</a></root>"));
	}

	@Test
	public void testIsSimilarTreeWithNotExistingElementAndNotSimilarTree() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isSimilarTree(tce, "/plop", "<root><wrong>foo</wrong><a>bar</a></root>"));
	}

	@Test
	public void testGetFromXmlWithValidURLAndExistingElement() {
		Assert.assertEquals("2", xmlUnitService.getFromXml("1234", getClass().getResource("data.xml").toString(), "/root/a[2]"));
	}

	@Test
	public void testGetFromXmlWithValidURLAndExistingElementJustTheFirstOne() {
		Assert.assertEquals("1", xmlUnitService.getFromXml("1234", getClass().getResource("data.xml").toString(), "/root/a"));
	}

	@Test
	public void testGetFromXmlWithValidURLAndExistingElementWithNamespace() {
		Assert.assertEquals("2", xmlUnitService.getFromXml("1234", getClass().getResource("data-namespaces.xml").toString(), "/:root/prefix:a[2]"));
	}

	@Test
	public void testGetFromXmlWithValidURLAndNotExistingElement() {
		Assert.assertEquals(XmlUnitService.DEFAULT_GET_FROM_XML_VALUE, xmlUnitService.getFromXml("1234", getClass().getResource("data.xml").toString(), "/root/b"));
	}

	@Test
	public void testGetFromXmlWithNullURLAndExistingElement() {
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse("1234")).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertEquals("2", xmlUnitService.getFromXml("1234", null, "/root/a[2]"));
	}

	@Test
	public void testGetFromXmlWithNullURLAndExistingElementJustTheFirstOne() {
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse("1234")).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertEquals("1", xmlUnitService.getFromXml("1234", null, "/root/a"));
	}

	@Test
	public void testGetFromXmlWithNullURLAndExistingElementWithNamespace() {
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse("1234")).thenReturn("<root xmlns:prefix=\"http://prefix\"><prefix:a>1</prefix:a><prefix:a>2</prefix:a></root>");

		Assert.assertEquals("2", xmlUnitService.getFromXml("1234", null, "/root/prefix:a[2]"));
	}

	@Test
	public void testGetFromXmlWithNullURLAndNotExistingElement() {
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse("1234")).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertEquals(XmlUnitService.DEFAULT_GET_FROM_XML_VALUE, xmlUnitService.getFromXml("1234", null, "/root/b"));
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
	public void testRemoveDifferenceWhenDifferenceMatchAll() throws XmlUtilException, SAXException, IOException {
		differences.addDifference(new Difference("/root[1]/a[1]"));
		differences.addDifference(new Difference("/root[1]/a[2]"));
		differences.addDifference(new Difference("/root[1]/b[1]"));

		String actual = xmlUnitService.removeDifference(".*root.*", differences.mkString());

		Assert.assertEquals(Differences.EMPTY_DIFFERENCES_STRING, actual);
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
	public void testIsElementEqualsWithExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isElementEquals(tce, "/root/a", "<a>2</a>"));
	}

	@Test
	public void testIsElementEqualsWithNotFormatedExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertTrue(xmlUnitService.isElementEquals(tce, "/root/a", "               <a>2</a>   "));
	}

	@Test
	public void testIsElementEqualsWithExistingElementWithNamespace() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID()))
				.thenReturn("<root xmlns:prefix=\"http://prefix\"><prefix:a>1</prefix:a><prefix:a>2</prefix:a></root>");

		Assert.assertTrue(xmlUnitService.isElementEquals(tce, "/root/prefix:a", "<prefix:a xmlns:prefix=\"http://prefix\">2</prefix:a>"));
	}

	@Test
	public void testIsElementEqualsWithNotExistingElement() {
		TestCaseExecution tce = new TestCaseExecution();
		tce.setExecutionUUID("1234");
		Mockito.when(executionSOAPResponse.getExecutionSOAPResponse(tce.getExecutionUUID())).thenReturn("<root><a>1</a><a>2</a></root>");

		Assert.assertFalse(xmlUnitService.isElementEquals(tce, "/root/a", "<a>3</a>"));
	}

	@Test
	public void testIsElementEqualsWithNullTCE() {
		Assert.assertFalse(xmlUnitService.isElementEquals(null, "/foo", "<bar/>"));
	}

	@Test
	public void testIsElementEqualsWithNullXPath() {
		Assert.assertFalse(xmlUnitService.isElementEquals(new TestCaseExecution(), null, "<bar/>"));
	}

	@Test
	public void testIsElementEqualsWithNullElement() {
		Assert.assertFalse(xmlUnitService.isElementEquals(new TestCaseExecution(), "/foo", null));
	}

}
