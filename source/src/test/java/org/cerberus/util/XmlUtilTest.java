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
package org.cerberus.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.cerberus.service.xmlunit.impl.XmlUnitService;
import org.cerberus.service.xmlunit.Differences;
import org.cerberus.service.xmlunit.DifferencesException;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * {@link XmlUtil} unit tests
 * 
 * @author abourdon
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ XmlUnitService.class })
@ContextConfiguration(locations = { "/applicationContextTest.xml" })
public class XmlUtilTest {

	@InjectMocks
	private XmlUnitService xmlUnitService;

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setCompareUnmatched(false);
	}

	public XmlUtilTest() {
	}

	@Before
	public void before() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method init = xmlUnitService.getClass().getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(xmlUnitService);
	}

	@Test
	public void testNewDocument() throws XmlUtilException {
		Assert.assertTrue(XmlUtil.newDocument() instanceof Document);
	}

	@Test
	public void testToString() throws XmlUtilException, SAXException, IOException {
		Document doc = XmlUtil.newDocument();
		Element expected = doc.createElement("root");
		doc.appendChild(expected);
		Element child = doc.createElement("child");
		expected.appendChild(child);

		String actual = XmlUtil.toString(expected);
		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML("<root><child/></root>", actual));

		Assert.assertTrue(diff.toString(), diff.similar());
	}

	@Test(expected = XmlUtilException.class)
	public void testToStringWithNullArguments() throws XmlUtilException {
		XmlUtil.toString(null);
	}

	@Test
	public void testFromString() throws XmlUtilException {
		Document expected = XmlUtil.newDocument();
		Element element = expected.createElement("root");
		expected.appendChild(element);
		Element child = expected.createElement("child");
		element.appendChild(child);

		Document actual = XmlUtil.fromString("<root><child/></root>");

		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue(diff.toString(), diff.similar());
	}

	@Test(expected = XmlUtilException.class)
	public void testFromStringWithNullArguments() throws XmlUtilException {
		XmlUtil.fromString(null);
	}

	@Test
	public void testFromURL() throws XmlUtilException {
		Document expected = XmlUtil.newDocument();
		Element element = expected.createElement("root");
		expected.appendChild(element);
		Element child = expected.createElement("child");
		child.appendChild(expected.createTextNode("a"));
		element.appendChild(child);

		Document actual = XmlUtil.fromURL(getClass().getResource("input.xml"));

		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue(diff.toString(), diff.similar());
	}

	@Test(expected = XmlUtilException.class)
	public void testFromURLWithNullArguments() throws XmlUtilException {
		XmlUtil.fromURL(null);
	}

	@Test
	public void testEvaluateDocument() throws XmlUtilException, DifferencesException {
		List<Document> expected = Arrays.asList(XmlUtil.fromString("<child><item1/></child>"), XmlUtil.fromString("<child><item2/></child>"));
		List<Document> actual = XmlUtil.fromNodeList(XmlUtil.evaluate(XmlUtil.fromString("<root><child><item1/></child><child><item2/></child></root>"), "/root/child"));

		Assert.assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			String differences = xmlUnitService.getDifferencesFromXml(XmlUtil.toString(expected.get(i)), XmlUtil.toString(actual.get(i)));
			Assert.assertTrue(Differences.fromString(differences).isEmpty());
		}
	}
	
	@Test
	public void testEvaluateDocumentWithNamespaces() throws XmlUtilException, DifferencesException {
		List<Document> expected = Arrays.asList(XmlUtil.fromURL(getClass().getResource("part.xml"), true));
		List<Document> actual = XmlUtil.fromNodeList(XmlUtil.evaluate(XmlUtil.fromURL(getClass().getResource("all.xml"), true), "//ns0:Response_1.0"));
		
		Assert.assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			String differences = xmlUnitService.getDifferencesFromXml(XmlUtil.toString(expected.get(i)), XmlUtil.toString(actual.get(i)));
			Assert.assertTrue(Differences.fromString(differences).isEmpty());
		}
	}

	@Test(expected = XmlUtilException.class)
	public void testEvaluateDocumentWithNullDocumentArgument() throws XmlUtilException {
		XmlUtil.evaluate((Document) null, "/foo");
	}

	@Test(expected = XmlUtilException.class)
	public void testEvaluateDocumentWithNullXPathArgument() throws XmlUtilException {
		XmlUtil.evaluate(XmlUtil.newDocument(), null);
	}

	@Test
	public void testEvaluateString() throws XmlUtilException, DifferencesException {
		List<String> expected = Arrays.asList("<child><item1/></child>", "<child><item2/></child>");
		List<String> actual = new ArrayList<String>();
		List<Document> actualDocuments = XmlUtil.fromNodeList(XmlUtil.evaluate("<root><child><item1/></child><child><item2/></child></root>", "/root/child"));
		for (Document actualDocument : actualDocuments) {
			actual.add(XmlUtil.toString(actualDocument));
		}

		Assert.assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			String differences = xmlUnitService.getDifferencesFromXml(expected.get(i), actual.get(i));
			Assert.assertTrue(Differences.fromString(differences).isEmpty());
		}
	}

	@Test(expected = XmlUtilException.class)
	public void testEvaluateStringWithNullDocumentArgument() throws XmlUtilException {
		XmlUtil.evaluate((String) null, "/foo");
	}

	@Test(expected = XmlUtilException.class)
	public void testEvaluateStringtWithNullXPathArgument() throws XmlUtilException {
		XmlUtil.evaluate("<foo/>", null);
	}
}
