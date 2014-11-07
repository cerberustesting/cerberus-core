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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class to handle XML files
 * 
 * @author abourdon
 */
public final class XmlUtil {

	/**
	 * Returns a new {@link Document} instance from the default {@link DocumentBuilder}
	 * 
	 * @return a new {@link Document} instance from the default {@link DocumentBuilder}
	 * @throws XmlUtilException
	 *             if an error occurred
	 */
	public static Document newDocument() throws XmlUtilException {
		try {
			DocumentBuilderFactory resultDocFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder resultDocBuilder = resultDocFactory.newDocumentBuilder();
			return resultDocBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new XmlUtilException(e);
		}
	}

	/**
	 * Returns a {@link String} representation of the {@link Node} given in argument
	 * 
	 * @param node
	 *            the {@link Node} from which create the {@link String} representation
	 * @return the {@link String} representation of the {@link Node} given in argument
	 * @throws XmlUtilException
	 *             if {@link Node} cannot be represented as a {@link String}
	 */
	public static String toString(Node node) throws XmlUtilException {
		if (node == null) {
			throw new XmlUtilException("Cannot parse a null node");
		}
		
		try {
			StreamResult xmlOutput = new StreamResult(new StringWriter());
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (TransformerConfigurationException e) {
			throw new XmlUtilException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new XmlUtilException(e);
		} catch (TransformerException e) {
			throw new XmlUtilException(e);
		}
	}

	/**
	 * Returns a {@link Document} representation of the {@link String} given in argument
	 * 
	 * @param xml
	 *            the {@link String} from which create the {@link Document} representation
	 * @return the {@link Document} representation of the {@link String} given in argument
	 * @throws XmlUtilException
	 *             if {@link String} cannot be represented as a {@link Document}
	 */
	public static Document fromString(String xml) throws XmlUtilException {
		if (xml == null) {
			throw new XmlUtilException("Cannot parse a null XML file");
		}
		
		try {
			InputSource sourceInput = new InputSource(new StringReader(xml));
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return dBuilder.parse(sourceInput);
		} catch (ParserConfigurationException e) {
			throw new XmlUtilException(e);
		} catch (SAXException e) {
			throw new XmlUtilException(e);
		} catch (IOException e) {
			throw new XmlUtilException(e);
		}
	}

	/**
	 * Returns a {@link Document} representation of the {@link URL} given in argument
	 * 
	 * @param url
	 *            the {@link URL} from which create the {@link Document} representation
	 * @return the {@link Document} representation of the {@link URL} given in argument
	 * @throws XmlUtilException
	 *             if {@link URL} cannot be represented as a {@link Document}
	 */
	public static Document fromURL(URL url) throws XmlUtilException {
		if (url == null) {
			throw new XmlUtilException("Cannot parse a null URL");
		}
		
		try {
			BufferedInputStream streamInput = new BufferedInputStream(url.openStream());
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return dBuilder.parse(streamInput);
		} catch (ParserConfigurationException e) {
			throw new XmlUtilException(e);
		} catch (SAXException e) {
			throw new XmlUtilException(e);
		} catch (IOException e) {
			throw new XmlUtilException(e);
		}
	}
	
	/**
	 * Evaluates the given xpath against the given document and produces new
	 * document which satisfy the xpath expression.
	 * 
	 * @param document
	 *            the document to search against the given xpath
	 * @param xpath
	 *            the xpath expression
	 * @return a list of new document which gather all results which satisfy the
	 *         xpath expression against the given document.
	 * @throws XmlUtilException
	 *             if an error occurred
	 */
	public static List<Document> evaluate(Document document, String xpath) throws XmlUtilException {
		if (document == null || xpath == null) {
			throw new XmlUtilException("Unable to evaluate null document or xpath");
		}
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpathObject = xpathFactory.newXPath();

		NodeList nodeList = null;
		try {
			XPathExpression expr = xpathObject.compile(xpath);
			nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException xpee) {
			throw new XmlUtilException(xpee);
		}

		if (nodeList == null) {
			throw new XmlUtilException("Evaluation caused a null result");
		}
		
		List<Document> result = new ArrayList<Document>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Document resultItem = XmlUtil.newDocument();
			resultItem.appendChild(resultItem.adoptNode(nodeList.item(i).cloneNode(true)));
			result.add(resultItem);
		}
		return result;
	}
	
	/**
	 * {@link String} version of the {@link #evaluate(Document, String)} method
	 * 
	 * @see #evaluate(Document, String)
	 */
	public static List<String> evaluate(String xml, String xpath) throws XmlUtilException {
		List<Document> resultDocuments = evaluate(XmlUtil.fromString(xml), xpath);
		List<String> resultStrings = new ArrayList<String>();
		for (Document resultDocument : resultDocuments) {
			resultStrings.add(XmlUtil.toString(resultDocument));
		}
		return resultStrings;
	}

	/**
	 * Utility class then private constructor
	 */
	private XmlUtil() {
	}

}
