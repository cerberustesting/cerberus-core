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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

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
	public void testCreateNewDocument() throws ParserConfigurationException {
		Assert.assertTrue(XmlUtil.createNewDocument() instanceof Document);
	}

	@Test
	public void convertToString() throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		Document doc = XmlUtil.createNewDocument();
		Element expected = doc.createElement("root");
		doc.appendChild(expected);
		Element child = doc.createElement("child");
		expected.appendChild(child);

		String actual = XmlUtil.convertToString(expected);
		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML("<root><child/></root>", actual));

		Assert.assertTrue(diff.similar());
	}

}
