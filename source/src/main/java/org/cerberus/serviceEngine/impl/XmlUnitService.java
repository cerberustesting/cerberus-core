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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.log.MyLogger;
import org.cerberus.serviceEngine.IXmlUnitService;
import org.cerberus.util.xmlUnitUtil;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
@Service
public class XmlUnitService implements IXmlUnitService{

    @Autowired
    ExecutionSOAPResponse executionSOAPResponse;

    @Override
    public boolean isElementPresent(TestCaseExecution tCExecution, String element) {

        try {
            String xml = executionSOAPResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID());
            InputSource source = new InputSource(new StringReader(xml));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            
            xpath.evaluate(element, document);
            Node node = (Node) xpath.evaluate(element, document, XPathConstants.NODE);
            if (node!=null){
            return true;}

        } catch (XPathExpressionException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.INFO, null, ex);
        }

        return false;
    }

    @Override
    public boolean isTextInElement(TestCaseExecution tCExecution, String element, String text) {
        try {
            String xml = executionSOAPResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID());
            InputSource source = new InputSource(new StringReader(xml));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            
            XPathExpression expr = xpath.compile(element+"/text()");
            Object result = expr.evaluate(document, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            String res = "";
            for (int i = 0; i < nodes.getLength(); i++) {
            res = nodes.item(i).getNodeValue();
            MyLogger.log(XmlUnitService.class.getName(), org.apache.log4j.Level.INFO, nodes.item(i).getNodeValue());
            MyLogger.log(XmlUnitService.class.getName(), org.apache.log4j.Level.INFO, ""+nodes.getLength());
            }
        
            
            MyLogger.log(XmlUnitService.class.getName(), org.apache.log4j.Level.INFO, res+element+text);
            if (res.equals(text)){
            return true;}

        } catch (XPathExpressionException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.INFO, null, ex);
        }

        return false;
    }

    @Override
    public boolean isSimilarTree(TestCaseExecution tCExecution, String element, String text) {
        try {
            String xml = executionSOAPResponse.getExecutionSOAPResponse(tCExecution.getExecutionUUID());
            InputSource source = new InputSource(new StringReader(xml));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
  
            XPathExpression expr = xpath.compile(element);
            Object result = expr.evaluate(document, XPathConstants.NODE);
            Node nodes = (Node) result;
            
            NodeList nl = nodes.getChildNodes();
            
            
            int length = nl.getLength();
            String[] copy = new String[length+1];
      
            if (nodes.hasChildNodes()){
            for (int n = 0; n < length; ++n){
            copy[n] = nl.item(n).getNodeName();
            }
            }
            copy[length] = nodes.getNodeName();
            
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(nodes), xmlOutput);
            String nodeAsAString = xmlOutput.getWriter().toString(); 
            
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
           
            //System.out.println("Compare "+nodeAsAString+" with "+text);
            
            Diff diff = new Diff(nodeAsAString, text);
            
            DetailedDiff detDiff = new DetailedDiff(diff);
            List differences = detDiff.getAllDifferences();
            List d = new ArrayList();
            xmlUnitUtil xuu = new xmlUnitUtil(copy);
            for (Object object : differences) {
                Difference difference = (Difference)object;
//                System.out.println("***********************");
//                System.out.println(difference);
//                System.out.println("***********************");
                xuu.differenceFound(difference);
            }
              
            diff.overrideDifferenceListener(xuu);
            
                return diff.similar();
                
        } catch (SAXException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }  catch (Exception ex) {
            Logger.getLogger(XmlUnitService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    
    
    
    
}
