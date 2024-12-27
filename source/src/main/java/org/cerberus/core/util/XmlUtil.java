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
package org.cerberus.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class to handle XML files
 *
 * @author abourdon
 */
public final class XmlUtil {

    private static final Logger LOG = LogManager.getLogger(XmlUtil.class);
    /**
     * If xml parsing has to be aware of namespaces
     */
    public static final boolean DEFAULT_NAMESPACE_AWARENESS = true;

    /**
     * An {@link Iterable} {@link NodeList}
     *
     * @author abourdon
     */
    public static final class IterableNodeList implements NodeList, Iterable<Node> {

        /**
         * The delegate {@link NodeList} to iterate on it
         */
        private NodeList delegate;

        public IterableNodeList(NodeList delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getLength() {
            return delegate.getLength();
        }

        @Override
        public Node item(int index) {
            return delegate.item(index);
        }

        @Override
        public Iterator<Node> iterator() {
            return new Iterator<Node>() {
                /**
                 * The current index for this iterator
                 */
                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex < getLength();
                }

                @Override
                public Node next() {
                    return item(currentIndex++);
                }

                @Override
                public void remove() {
                    throw new IllegalStateException("Unable to remove a node from the nested list");
                }
            };
        }

    }

    /**
     * A universal namespace cache to use when parsing XML files against XPath
     *
     * @author abourdon
     * @see
     * <a href="https://www.ibm.com/developerworks/library/x-nmspccontext/#N10158">...</a>
     */
    @SuppressWarnings("unchecked")
    public static final class UniversalNamespaceCache implements NamespaceContext {

        /**
         * The associated {@link Logger} to this class
         */
        private static final Logger LOG = LogManager.getLogger(UniversalNamespaceCache.class);

        public static final boolean DEFAULT_TOP_LEVEL_ONLY = false;

        /**
         * The associated maps between prefixes and URIs
         */
        private Map<String, String> prefix2Uri = new HashMap<>();
        private Map<String, String> uri2Prefix = new HashMap<>();

        public UniversalNamespaceCache(Document document) {
            this(document, DEFAULT_TOP_LEVEL_ONLY);
        }

        /**
         * This constructor parses the document and stores all namespaces it can
         * find. If toplevelOnly is <code>true</code>, only namespaces in the
         * root are used.
         *
         * @param document source document
         * @param toplevelOnly restriction of the search to enhance performance
         */
        public UniversalNamespaceCache(Document document, boolean toplevelOnly) {
            examineNode(document.getFirstChild(), toplevelOnly);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Hereunder list of found namespaces (prefix, uri):");
                for (Map.Entry<String, String> items : prefix2Uri.entrySet()) {
                    LOG.debug("{}, {}", items.getKey(), items.getValue());
                }
            }
        }

        /**
         * A single node is read, the namespace attributes are extracted and
         * stored.
         *
         * @param node to examine
         * @param attributesOnly if <code>true</code> no recursion happens
         */
        private void examineNode(Node node, boolean attributesOnly) {
            if (node == null) {
                LOG.warn("Unable to examine null node");
                return;
            }

            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                storeAttribute((Attr) attribute);
            }

            if (!attributesOnly) {
                for (Node child : new IterableNodeList(node.getChildNodes())) {
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        examineNode(child, false);
                    }
                }
            }
        }

        /**
         * This method looks at an attribute and stores it, if it is a namespace
         * attribute.
         *
         * @param attribute to examine
         */
        private void storeAttribute(Attr attribute) {
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())) {
                putInCache(XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getNodeName()) ? XMLConstants.DEFAULT_NS_PREFIX : attribute.getLocalName(), attribute.getNodeValue());
            }
        }

        /**
         * Put the given prefix and URI in cache
         *
         * @param prefix to put in cache
         * @param uri to put in cache
         */
        private void putInCache(String prefix, String uri) {
            prefix2Uri.put(prefix, uri);
            uri2Prefix.put(uri, prefix);
        }

        /**
         * This method is called by XPath. It returns the namespace URI
         * associated to the given prefix.
         *
         * @param prefix to search for
         * @return uri
         */
        @Override
        public String getNamespaceURI(String prefix) {
            return prefix2Uri.get(prefix);
        }

        /**
         * This method is not needed in this context, but can be implemented in
         * a similar way.
         */
        @Override
        public String getPrefix(String namespaceURI) {
            return uri2Prefix.get(namespaceURI);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }

    /**
     * Returns a new {@link Document} instance from the default
     * {@link DocumentBuilder}
     *
     * @return a new {@link Document} instance from the default
     * {@link DocumentBuilder}
     * @throws XmlUtilException if an error occurred
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
     * Returns a {@link String} representation of the {@link Node} given in
     * argument
     *
     * @param node the {@link Node} from which create the {@link String}
     * representation
     * @return the {@link String} representation of the {@link Node} given in
     * argument
     * @throws XmlUtilException if {@link Node} cannot be represented as a
     * {@link String}
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
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new XmlUtilException(e);
        }
    }

    /**
     * Returns a {@link Document} representation of the {@link String} given in
     * argument
     *
     * @param xml the {@link String} from which create the {@link Document}
     * representation
     * @param namespaceAwareness if namespaces have to be taking into account
     * during parsing
     * @return the {@link Document} representation of the {@link String} given
     * in argument
     * @throws XmlUtilException if {@link String} cannot be represented as a
     * {@link Document}
     */
    public static Document fromString(String xml, boolean namespaceAwareness) throws XmlUtilException {
        if (xml == null) {
            throw new XmlUtilException("Cannot parse a null XML file");
        }

        try {
            return newDocumentBuilder(namespaceAwareness, true).parse(new InputSource(new StringReader(xml)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new XmlUtilException(e);
        }
    }

    /**
     * The {@link #fromString(String, boolean)} version by using the
     * {@link #DEFAULT_NAMESPACE_AWARENESS} value
     */
    public static Document fromString(String xml) throws XmlUtilException {
        return fromString(xml, DEFAULT_NAMESPACE_AWARENESS);
    }

    /**
     * Returns a {@link Document} representation of the {@link URL} given in
     * argument
     *
     * @param url the {@link URL} from which create the {@link Document}
     * representation
     * @param namespaceAwareness if namespaces have to be taking into account
     * during parsing
     * @return the {@link Document} representation of the {@link URL} given in
     * argument
     * @throws XmlUtilException if {@link URL} cannot be represented as a
     * {@link Document}
     */
    public static Document fromURL(URL url, boolean namespaceAwareness) throws XmlUtilException {
        if (url == null) {
            throw new XmlUtilException("Cannot parse a null URL");
        }

        try {
            return newDocumentBuilder(namespaceAwareness, true).parse(new BufferedInputStream(url.openStream()));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new XmlUtilException(e);
        }
    }

    /**
     * The {@link #fromURL(URL, boolean)} version by using the
     * {@link #DEFAULT_NAMESPACE_AWARENESS} value
     *
     * @see #fromURL(URL, boolean)
     */
    public static Document fromURL(URL url) throws XmlUtilException {
        return fromURL(url, DEFAULT_NAMESPACE_AWARENESS);
    }

    /**
     * Evaluates the given xpath against the given document and produces new
     * document which satisfy the xpath expression.
     *
     * @param document the document to search against the given xpath
     * @param xpath the xpath expression
     * @return a list of new document which gather all results which satisfy the
     * xpath expression against the given document.
     * @throws XmlUtilException if an error occurred
     */
    public static NodeList evaluate(Document document, String xpath) throws XmlUtilException {
        if (document == null || xpath == null) {
            throw new XmlUtilException("Unable to evaluate null document or xpath");
        }

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpathObject = xpathFactory.newXPath();
        xpathObject.setNamespaceContext(new UniversalNamespaceCache(document));

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

        return nodeList;
    }

    /**
     * Evaluates the given xpath against the given document and produces string
     * which satisfy the xpath expression.
     *
     * @param document the document to search against the given xpath
     * @param xpath the xpath expression
     * @return a string which satisfy the xpath expression against the given
     * document.
     * @throws XmlUtilException if an error occurred
     */
    public static String evaluateString(Document document, String xpath) throws XmlUtilException {
        if (document == null || xpath == null) {
            throw new XmlUtilException("Unable to evaluate null document or xpath");
        }

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpathObject = xpathFactory.newXPath();
        xpathObject.setNamespaceContext(new UniversalNamespaceCache(document));

        String result = null;
        try {
            XPathExpression expr = xpathObject.compile(xpath);
            result = (String) expr.evaluate(document, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            throw new XmlUtilException(xpee);
        }

        if (result == null) {
            throw new XmlUtilException("Evaluation caused a null result");
        }

        return result;
    }

    public static Node evaluateNode(Document doc, String xpath) throws XmlUtilException {
        if (doc == null || xpath == null) {
            throw new XmlUtilException("Unable to evaluate null document or xpath");
        }

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpathObject = xpathFactory.newXPath();
        xpathObject.setNamespaceContext(new UniversalNamespaceCache(doc));
        Node node = null;
        try {
            XPathExpression expr = xpathObject.compile(xpath);
            node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException xpee) {
            throw new XmlUtilException(xpee);
        }

        if (node == null) {
            throw new XmlUtilException("Evaluation caused a null result");
        }

        return node;
    }

    /**
     * {@link String} version of the {@link #evaluate(Document, String)} method
     *
     * @see #evaluate(Document, String)
     */
    public static NodeList evaluate(String xml, String xpath) throws XmlUtilException {
        return evaluate(XmlUtil.fromString(xml), xpath);
    }

    /**
     * Returns a {@link Document} from the given {@link Node}
     *
     * @param node to transform to {@link Document}
     * @return a {@link Document} from the given {@link Node}
     * @throws XmlUtilException if an error occurs
     */
    public static Document fromNode(Node node) throws XmlUtilException {
        try {
            Document document = XmlUtil.newDocument();
            document.appendChild(document.adoptNode(node.cloneNode(true)));
            return document;
        } catch (DOMException e) {
            LOG.warn("Unable to create document from node " + node, e);
            return null;
        }
    }

    /**
     * Returns a {@link Document} list from the given {@link NodeList}
     *
     * @param nodeList to parse
     * @return a {@link Document} list from the given {@link NodeList}
     * @throws XmlUtilException if an error occurs. For instance if
     * {@link NodeList} cannot be transforms as a {@link Document} list
     */
    public static List<Document> fromNodeList(NodeList nodeList) throws XmlUtilException {
        List<Document> result = new ArrayList<>();
        for (Node node : new IterableNodeList(nodeList)) {
            if (node == null) {
                throw new XmlUtilException("Unable to add null node");
            }
            result.add(fromNode(node));
        }
        return result;
    }

    public static boolean isXmlWellFormed(String xmlString) {
        DocumentBuilder dBuilder;
        try {
            dBuilder = newDocumentBuilder(true, true);
            dBuilder.parse(new ByteArrayInputStream(xmlString.getBytes()));
        } catch (ParserConfigurationException | SAXException e) {
            return false;
        } catch (IOException e) {
            LOG.error("Unable to evaluate the document", e);
            return false;
        }
        return true;
    }

    /**
     * Create a new {@link DocumentBuilder} according to the given configuration
     * parameters
     *
     * @param namespaceAwareness if the created {@link DocumentBuilder} has to
     * be aware of namespaces
     * @param ignoringComment if the created {@link DocumentBuilder} has to
     * ignore comments
     * @return a new {@link DocumentBuilder} configured by the given
     * configuration parameters
     */
    private static DocumentBuilder newDocumentBuilder(final boolean namespaceAwareness, final boolean ignoringComment) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaceAwareness);
        factory.setIgnoringComments(ignoringComment);
        return factory.newDocumentBuilder();
    }

    /**
     * Utility class then private constructor
     */
    private XmlUtil() {
    }

}
