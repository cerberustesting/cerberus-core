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

import junit.framework.Assert;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * {@link XmlUtil} unit tests
 * 
 * @author abourdon
 */
public class XmlUtilTest {

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setCompareUnmatched(false);
	}

	public XmlUtilTest() {
	}

	@Test
	public void testNewDocument() throws XmlUtilException {
		Assert.assertTrue(XmlUtil.newDocument() instanceof Document);
	}

	@Test
	public void testToString() throws XmlUtilException, SAXException, IOException  {
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
}
