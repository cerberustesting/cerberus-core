/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.xmlunit.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.service.xmlunit.AInputTranslator;
import org.cerberus.service.xmlunit.Differences;
import org.cerberus.service.xmlunit.DifferencesException;
import org.cerberus.service.xmlunit.IXmlUnitService;
import org.cerberus.service.xmlunit.InputTranslator;
import org.cerberus.service.xmlunit.InputTranslatorException;
import org.cerberus.service.xmlunit.InputTranslatorManager;
import org.cerberus.service.xmlunit.InputTranslatorUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.XmlUtil;
import org.cerberus.util.XmlUtilException;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author bcivel
 */
@Service
public class XmlUnitService implements IXmlUnitService {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOG = LogManager.getLogger(XmlUnitService.class);

    /**
     * Difference value for null XPath
     */
    public static final String NULL_XPATH = "null";

    /**
     * The default value for the getFromXML action
     */
    public static final String DEFAULT_GET_FROM_XML_VALUE = null;

    /**
     * Prefixed input handling
     */
    private InputTranslatorManager<Document> inputTranslator;

    @PostConstruct
    private void init() {
        initInputTranslator();
        initXMLUnitProperties();
    }

    /**
     * Initializes {@link #inputTranslator} by two {@link InputTranslator}
     * <ul>
     * <li>One for handle the <code>url</code> prefix</li>
     * <li>One for handle without prefix</li>
     * </ul>
     */
    private void initInputTranslator() {
        inputTranslator = new InputTranslatorManager<Document>();
        // Add handling on the "url" prefix, to get URL input
        inputTranslator.addTranslator(new AInputTranslator<Document>("url") {
            @Override
            public Document translate(String input) throws InputTranslatorException {
                try {
                    URL urlInput = new URL(InputTranslatorUtil.getValue(input));
                    return XmlUtil.fromURL(urlInput);
                } catch (MalformedURLException e) {
                    throw new InputTranslatorException(e);
                } catch (XmlUtilException e) {
                    throw new InputTranslatorException(e);
                }
            }
        });
        // Add handling for raw XML input
        inputTranslator.addTranslator(new AInputTranslator<Document>(null) {
            @Override
            public Document translate(String input) throws InputTranslatorException {
                try {
                    return XmlUtil.fromString(input);
                } catch (XmlUtilException e) {
                    throw new InputTranslatorException(e);
                }
            }
        });
    }

    /**
     * Initializes {@link XMLUnit} properties
     */
    private void initXMLUnitProperties() {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setCompareUnmatched(false);
    }

    @Override
    public boolean isElementPresent(String lastSOAPResponse, String xpath) {
        if (xpath == null) {
            LOG.warn("Null argument");
            return false;
        }

        try {
            return XmlUtil.evaluate(lastSOAPResponse, xpath).getLength() != 0;
        } catch (XmlUtilException e) {
            LOG.warn("Unable to check if element is present", e);
        }

        return false;
    }

    @Override
    public boolean isSimilarTree(String lastSOAPResponse, String xpath, String tree) {
        if (xpath == null || tree == null) {
            LOG.warn("Null argument");
            return false;
        }

        try {
            NodeList candidates = XmlUtil.evaluate(lastSOAPResponse, xpath);
            for (Node candidate : new XmlUtil.IterableNodeList(candidates)) {
                boolean found = true;
                for (org.cerberus.service.xmlunit.Difference difference : Differences.fromString(getDifferencesFromXml(XmlUtil.toString(candidate), tree))) {
                    if (!difference.getDiff().endsWith("/text()[1]")) {
                        found = false;
                    }
                }

                if (found) {
                    return true;
                }
            }
        } catch (XmlUtilException e) {
            LOG.warn("Unable to check similar tree", e);
        } catch (DifferencesException e) {
            LOG.warn("Unable to check similar tree", e);
        }

        return false;
    }

    @Override
    public String getFromXml(final String xmlToParse, final String xpath) {
        if (xpath == null) {
            LOG.warn("Null argument");
            return DEFAULT_GET_FROM_XML_VALUE;
        }

        try {
            final Document document = StringUtil.isURL(xmlToParse) ? XmlUtil.fromURL(new URL(xmlToParse)) : XmlUtil.fromString(xmlToParse);
            final String result = XmlUtil.evaluateString(document, xpath);
            // Not that in case of multiple values then send the first one
            return result != null && result.length() > 0 ? result : DEFAULT_GET_FROM_XML_VALUE;
        } catch (XmlUtilException e) {
            LOG.warn("Unable to get from xml", e);
        } catch (MalformedURLException e) {
            LOG.warn("Unable to get from xml", e);
        }

        return DEFAULT_GET_FROM_XML_VALUE;
    }

    public String getRawFromXml(final String xmlToParse, final String xpath) {
        if (xpath == null) {
            return DEFAULT_GET_FROM_XML_VALUE;
        }

        try {
            final Document document = StringUtil.isURL(xmlToParse) ? XmlUtil.fromURL(new URL(xmlToParse)) : XmlUtil.fromString(xmlToParse);
            Node node = XmlUtil.evaluateNode(document, xpath);
            String result = XmlUtil.toString(node);
            // Not that in case of multiple values then send the first one
            return result != null && result.length() > 0 ? result : DEFAULT_GET_FROM_XML_VALUE;
        } catch (XmlUtilException e) {
            LOG.warn("Unable to get from xml", e);
        } catch (MalformedURLException e) {
            LOG.warn("Unable to get from xml URL malformé", e);
        }

        return DEFAULT_GET_FROM_XML_VALUE;
    }

    @Override
    public String getDifferencesFromXml(String left, String right) {
        try {
            // Gets the detailed diff between left and right argument
            Document leftDocument = inputTranslator.translate(left);
            Document rightDocument = inputTranslator.translate(right);
            DetailedDiff diffs = new DetailedDiff(XMLUnit.compareXML(leftDocument, rightDocument));

            // Creates the result structure which will contain difference list
            Differences resultDiff = new Differences();

            // Add each difference to our result structure
            for (Object diff : diffs.getAllDifferences()) {
                if (!(diff instanceof Difference)) {
                    LOG.warn("Unable to handle no XMLUnit Difference " + diff);
                    continue;
                }
                Difference wellTypedDiff = (Difference) diff;
                String xPathLocation = wellTypedDiff.getControlNodeDetail().getXpathLocation();
                // Null XPath location means additional data from the right
                // structure.
                // Then we retrieve XPath from the right structure.
                if (xPathLocation == null) {
                    xPathLocation = wellTypedDiff.getTestNodeDetail().getXpathLocation();
                }
                // If location is still null, then both of left and right
                // differences have been marked as null
                // This case should never happen
                if (xPathLocation == null) {
                    LOG.warn("Null left and right differences found");
                    xPathLocation = NULL_XPATH;
                }
                resultDiff.addDifference(new org.cerberus.service.xmlunit.Difference(xPathLocation));
            }

            // Finally returns the String representation of our result structure
            return resultDiff.mkString();
        } catch (InputTranslatorException e) {
            LOG.warn("Unable to get differences from XML", e);
        }

        return null;
    }

    @Override
    public String removeDifference(String pattern, String differences) {
        if (pattern == null || differences == null) {
            LOG.warn("Null argument");
            return null;
        }

        try {
            // Gets the difference list from the differences
            Differences current = Differences.fromString(differences);
            Differences returned = new Differences();

            // Compiles the given pattern
            Pattern compiledPattern = Pattern.compile(pattern);
            for (org.cerberus.service.xmlunit.Difference currentDiff : current.getDifferences()) {
                if (compiledPattern.matcher(currentDiff.getDiff()).matches()) {
                    continue;
                }
                returned.addDifference(currentDiff);
            }

            // Returns the empty String if there is no difference left, or the
            // String XML representation
            return returned.mkString();
        } catch (DifferencesException e) {
            LOG.warn("Unable to remove differences", e);
        }

        return null;
    }

    @Override
    public boolean isElementEquals(String lastSOAPResponse, String xpath, String expectedElement) {
        if (lastSOAPResponse == null || xpath == null || expectedElement == null) {
            LOG.warn("Null argument");
            return false;
        }

        try {
            NodeList candidates = XmlUtil.evaluate(lastSOAPResponse, xpath);
            LOG.debug(candidates.toString());
            for (Document candidate : XmlUtil.fromNodeList(candidates)) {
                if (Differences.fromString(getDifferencesFromXml(XmlUtil.toString(candidate), expectedElement)).isEmpty()) {
                    return true;
                }
            }
        } catch (XmlUtilException xue) {
            LOG.warn("Unable to check if element equality", xue);
        } catch (DifferencesException de) {
            LOG.warn("Unable to check if element equality", de);
        }

        return false;
    }

    @Override
    public Document getXmlDocument(String lastSOAPResponse) {
        Document document = null;
        try {
            document = XmlUtil.fromString(lastSOAPResponse);
            return document;
        } catch (XmlUtilException ex) {
            LOG.warn(ex);
        }
        return document;
    }
}
