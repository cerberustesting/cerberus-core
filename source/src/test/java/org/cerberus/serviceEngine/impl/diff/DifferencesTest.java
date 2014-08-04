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
package org.cerberus.serviceEngine.impl.diff;

import java.io.IOException;

import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * {@link Differences} unit tests
 * 
 * @author abourdon
 */
public class DifferencesTest {

	private Differences differences;

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setCompareUnmatched(false);
	}

	public DifferencesTest() {
	}

	@Before
	public void before() {
		differences = new Differences();
	}

	@Test
	public void testAddDifference() {
		Difference expected = new Difference("diff");
		differences.addDifference(expected);

		Assert.assertEquals("Add a difference increase the difference list to 1", 1, differences.getDifferences().size());
		Assert.assertEquals("Add a difference correctly add the given difference", expected, differences.getDifferences().get(0));
	}

	@Test
	public void testRemoveExistingDifference() {
		Difference diff = new Difference("diff");
		differences.addDifference(diff);
		differences.removeDifference(diff);

		Assert.assertTrue("Remove an existing difference cause remove it from the differences list", differences.getDifferences().isEmpty());
	}

	@Test
	public void testRemoveNotExistingDifference() {
		Difference diff1 = new Difference("diff1");
		differences.addDifference(diff1);

		Difference diff2 = new Difference("diff2");
		differences.removeDifference(diff2);

		Assert.assertEquals("Remove a not existing difference cause make the differences list unchanged", 1, differences.getDifferences().size());
		Assert.assertEquals("Remove a not existing difference cause make the differences list unchanged", diff1, differences.getDifferences().get(0));
	}

	@Test
	public void testToStringWhenExistingDifference() throws XmlUtilException, SAXException, IOException {
		differences.addDifference(new Difference("diff1"));
		differences.addDifference(new Difference("diff2"));
		String actual = differences.toString();

		Document doc = XmlUtil.newDocument();
		Element root = doc.createElement(Differences.DIFFERENCES_NODE);
		doc.appendChild(root);

		Element diff1 = doc.createElement(Differences.DIFFERENCE_NODE);
		diff1.appendChild(doc.createTextNode("diff1"));
		root.appendChild(diff1);
		Element diff2 = doc.createElement(Differences.DIFFERENCE_NODE);
		diff2.appendChild(doc.createTextNode("diff2"));
		root.appendChild(diff2);

		String expected = XmlUtil.toString(doc);

		DetailedDiff result = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue("Differences can be correctly transforms as String", result.similar());
	}

	@Test
	public void testToStringWhenNotExistingDifference() throws XmlUtilException, SAXException, IOException {
		String actual = differences.toString();
		String expected = Differences.EMPTY_DIFFERENCES_STRING;
		Assert.assertEquals("Differences can be correctly transforms as Document when there is no differences", expected, actual);
	}

	@Test
	public void testToDocumentWhenExistingDifference() throws DifferencesException, XmlUtilException {
		Document actual = differences.toDocument();

		Document expected = XmlUtil.newDocument();
		Element root = expected.createElement(Differences.DIFFERENCES_NODE);
		expected.appendChild(root);

		DetailedDiff result = new DetailedDiff(XMLUnit.compareXML(expected, actual));
		Assert.assertTrue("Differences can be correctly transforms as Document when there is no differences", result.similar());
	}
}
