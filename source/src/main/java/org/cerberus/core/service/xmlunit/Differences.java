/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.xmlunit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cerberus.core.util.XmlUtil;
import org.cerberus.core.util.XmlUtilException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Contains a {@link Difference} list by following the given format:
 * 
 * <pre>
 * {@code
 * <differences>
 *   <difference>difference #1</difference>
 *   <difference>difference #2</difference>
 * </differences>
 * }
 * </pre>
 * 
 * @author abourdon
 */
public class Differences implements Iterable<Difference> {

	/** {@link Differences} XML root */
	public static final String DIFFERENCES_NODE = "differences";

	/** {@link Differences} XML element */
	public static final String DIFFERENCE_NODE = "difference";

	/** {@link Difference} common XPath */
	public static final String DIFFERENCE_COMMON_XPATH = "/" + DIFFERENCES_NODE + "/" + DIFFERENCE_NODE;

	/** {@link String} representation of an empty {@link Differences} */
	public static final String EMPTY_DIFFERENCES_STRING = "";

	/** The associated list of {@link Difference} */
	private List<Difference> differences;

	/**
	 * Creates a {@link Differences} instance from the given {@link String} representation
	 * 
	 * @param stringDiff
	 *            the {@link String} representation of {@link Differences}
	 * @return a {@link Differences} instance containing the {@link String} representation
	 * @throws DifferencesException
	 *             if the given {@link String} representation does not have a correct {@link Differences} format
	 */
	public static Differences fromString(String stringDiff) throws DifferencesException {
		try {
			return EMPTY_DIFFERENCES_STRING.equals(stringDiff) ? new Differences() : fromDocument(XmlUtil.fromString(stringDiff));
		} catch (XmlUtilException e) {
			throw new DifferencesException(e);
		}
	}

	/**
	 * Creates a {@link Differences} instance from the given {@link Document}
	 * 
	 * @param documentDiff
	 *            the {@link Document} base to create the {@link Differences} instance
	 * @return a {@link Differences} instance from the given {@link Document}
	 * @throws DifferencesException
	 *             if the given {@link Document} does not have a correct {@link Differences} format
	 */
	public static Differences fromDocument(Document documentDiff) throws DifferencesException {
		NodeList list = null;
		try {
			XPath path = XPathFactory.newInstance().newXPath();
			list = (NodeList) path.compile(DIFFERENCE_COMMON_XPATH).evaluate(documentDiff, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new DifferencesException(e);
		}

		Differences diff = new Differences();
		for (int i = 0; i < list.getLength(); i++) {
			diff.addDifference(new Difference(list.item(i).getFirstChild().getNodeValue()));
		}
		return diff;
	}

	/**
	 * Creates a new {@link Differences} instance with no differences
	 */
	public Differences() {
		differences = new ArrayList<>();
	}

	/**
	 * Adds a new {@link Difference} to this {@link Differences}
	 * 
	 * @param diff
	 *            the new {@link Difference} to add
	 */
	public void addDifference(Difference diff) {
		differences.add(diff);
	}

	/**
	 * Removes the existing {@link Difference} from this {@link Differences}
	 * 
	 * @param diff
	 *            the existing {@link Difference} to remove
	 */
	public void removeDifference(Difference diff) {
		differences.remove(diff);
	}

	/**
	 * Gets the differences list contained by this {@link Differences}
	 * 
	 * @return
	 */
	public List<Difference> getDifferences() {
		return new ArrayList<>(differences);
	}
	
	/**
	 * Checks if there are no contained differences
	 * 
	 * @return <code>true</code> if no differences are contained, <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return differences.isEmpty();
	}
	
	/**
	 * Returns a {@link String} representation of this {@link Differences} by following the {@link Differences} format.
	 * 
	 * <p>
	 * If this {@link Differences} is empty then returned the {@link #EMPTY_DIFFERENCES_STRING} value
	 * </p>
	 * 
	 * <p>
	 * In case of error, then returned <code>null</code>
	 * </p>
	 */
	public String mkString() {
		try {
			Document doc = toDocument();
			XPath path = XPathFactory.newInstance().newXPath();
			NodeList list = (NodeList) path.compile(DIFFERENCE_COMMON_XPATH).evaluate(doc, XPathConstants.NODESET);
			return list.getLength() == 0 ? EMPTY_DIFFERENCES_STRING : XmlUtil.toString(doc);
		} catch (XmlUtilException e) {
			return null;
		} catch (DifferencesException e) {
			return null;
		} catch (XPathExpressionException e) {
			return null;
		}
	}

	/**
	 * Returns a {@link Document} representation of this {@link Differences} by following the {@link Differences} format.
	 * 
	 * @return a {@link Document} representation of this {@link Differences}
	 * @throws DifferencesException
	 *             if an error occurred
	 */
	public Document toDocument() throws DifferencesException {
		// Creates the result document which will contain difference list
		Document resultDoc = null;
		try {
			resultDoc = XmlUtil.newDocument();
		} catch (XmlUtilException e) {
			throw new DifferencesException(e);
		}

		// Creates the root node
		Element resultRoot = resultDoc.createElement(DIFFERENCES_NODE);
		resultDoc.appendChild(resultRoot);

		// Appends differences to the root node
		for (Difference diff : differences) {
			Element element = resultDoc.createElement(DIFFERENCE_NODE);
			element.appendChild(resultDoc.createTextNode(diff.getDiff()));
			resultRoot.appendChild(element);
		}

		// Returns the result document
		return resultDoc;
	}
	
	@Override
	public String toString() {
		String mkString = mkString();
		return mkString == null ? EMPTY_DIFFERENCES_STRING : mkString;
	}

	@Override
	public Iterator<Difference> iterator() {
		return new ArrayList<>(differences).iterator();
	}

}
