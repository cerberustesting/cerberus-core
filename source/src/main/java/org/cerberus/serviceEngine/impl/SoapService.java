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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Level;
import org.cerberus.entity.ExecutionSOAPResponse;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.Property;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.entity.TestCaseCountryProperties;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.refactor.GetConnectionPoolName;
import org.cerberus.service.ICountryEnvironmentDatabaseService;
import org.cerberus.serviceEngine.ISoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author bcivel
 */
@Service
public class SoapService implements ISoapService{
    
    @Autowired
    private ICountryEnvironmentDatabaseService countryEnvironmentDatabaseService;
    @Autowired
    ExecutionSOAPResponse executionSOAPResponse;
    
    /**
     * On chercher le premier chiffre entre crochet le ? dans le premier groupe
     * permet de s'arrêter à la première regexp trouvée
     */
    private final static Pattern patCount = Pattern.compile("(.*?)(\\[\\d*\\]+)(.*)");

    private final static Pattern patReplace = Pattern.compile("(\\[\\d*\\]+)");

    /**
     * Pattern pour détecter le début d'une requête SOAP REDOUTE qui lance une
     * requête sur le MF
     */
    private final static Pattern patExec = Pattern.compile("(?s)<(.*)(ExecuteSQLRequestRequest_1.0)(.*)ExecuteSQLRequestRequest_1.0>");

    /**
     * Pattern pour détecter le header d'une requête SOAP REDOUTE qui lance une
     * requête sur le MF
     */
    private final static Pattern patHead = Pattern.compile("(?s)<(.*)(Header)(.*)</Header>");

    /**
     * Pattern pour détecter la balise Environment à l'intérieur d'une requête
     * SOAP REDOUTE qui lance une requête sur le MF
     */
    private final static Pattern patEnvi = Pattern.compile("<Environment>(.*)</Environment>");

    @Override
    public SOAPMessage createSoapRequest(String pBody, String method) throws SOAPException, IOException, SAXException, ParserConfigurationException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        
        SOAPMessage soapMessage = messageFactory.createMessage();

        MimeHeaders headers = soapMessage.getMimeHeaders();

        // Précise la méthode du WSDL à interroger
        headers.addHeader("SOAPAction", method);
        // Encodage UTF-8
        headers.addHeader("Content-Type", "text/xml;charset=UTF-8");

        final SOAPBody soapBody = soapMessage.getSOAPBody();
        
        // convert String into InputStream - traitement des caracères escapés > < ... (contraintes de l'affichage IHM)
        String unescaped = HtmlUtils.htmlUnescape(pBody);

        
        InputStream is;

           is = new ByteArrayInputStream(unescaped.getBytes());
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;

        
        // Important à laisser sinon KO
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();

            Document document = builder.parse(is);

            soapBody.addDocument(document);
        } catch (ParserConfigurationException e) {
            MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
        } finally{
            is.close();
        if (builder != null) {
            builder.reset();
        }
        }
        soapMessage.saveChanges();

        return soapMessage;
    }

    @Override
    public  MessageEvent callSOAPAndStoreResponseInMemory(TestCaseExecution tCExecution, String envelope, String servicePath, String method) {
        String result = null;
        ByteArrayOutputStream out = null;
        MessageEvent message = null;
        // Test des inputs nécessaires.
        if (envelope != null && servicePath != null && method != null) {
            
            SOAPConnectionFactory soapConnectionFactory;
            SOAPConnection soapConnection = null;
            try {
                soapConnectionFactory = SOAPConnectionFactory.newInstance();
                soapConnection = soapConnectionFactory.createConnection();
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Connection opened");
                
                // Création de la requete SOAP
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Create request");
                SOAPMessage input = createSoapRequest(envelope, method);

                // Appel du WS
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Calling WS");
                MyLogger.log(SoapService.class.getName(), Level.INFO, "Input :"+input);
                SOAPMessage soapResponse = soapConnection.call(input, servicePath);
                
                
                out = new ByteArrayOutputStream();

                soapResponse.writeTo(out);
                MyLogger.log(SoapService.class.getName(), Level.INFO, "WS response received");
                MyLogger.log(SoapService.class.getName(), Level.DEBUG, "WS response : "+out.toString());
                result = out.toString();
                executionSOAPResponse.setExecutionSOAPResponse(tCExecution.getId(), result);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
                return message;
                
            } catch (SOAPException e){  
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (IOException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (ParserConfigurationException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            } catch (SAXException e) {
                MyLogger.log(SoapService.class.getName(), Level.ERROR, e.toString());
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSOAP);
                message.setDescription(message.getDescription().replaceAll("%SOAPNAME%", method));
            }
            finally{
                try {
                    if (soapConnection != null) {
                    soapConnection.close();
                    }
                    if (out != null) {
                    out.close();
                    }
                    MyLogger.log(SoapService.class.getName(), Level.INFO, "Connection and ByteArray closed");
                } catch (SOAPException ex) {
                    Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SoapService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
        }
        }
        
        return message;
    }
    
    /**
     * Calcule d'une propriété depuis une requête SOAP.
     *
     * @param pSoapLibrary La librarie SOAP à exécuter
     * @param pNature Nature de la propriété à calculé STATIC/RANDOM
     * @thorw CerberusException
     * @return String
     */
    @Override
    public String calculatePropertyFromSOAPResponse(final SoapLibrary pSoapLibrary, TestCaseCountryProperties pTestCaseCountry, TestCaseExecution pTestCaseExecution) throws CerberusException {
        String result = null;
        // Test des inputs nécessaires.
        if (pSoapLibrary != null && pSoapLibrary.getEnvelope() != null && pSoapLibrary.getServicePath() != null && pSoapLibrary.getParsingAnswer() != null && pSoapLibrary.getMethod() != null) {

            SOAPConnectionFactory soapConnectionFactory;

            SOAPConnection soapConnection;
            try {
                soapConnectionFactory = SOAPConnectionFactory
                        .newInstance();
                soapConnection = soapConnectionFactory.createConnection();

                // Création de la requete SOAP
                SOAPMessage input = createSOAPRequest(pSoapLibrary, pTestCaseCountry, pTestCaseExecution);

                // Appel du WS
                SOAPMessage soapResponse = soapConnection.call(input, pSoapLibrary.getServicePath());

                // Traitement de la réponse SOAP à l'aide d'une expression Xpath stockée en BDD
                result = parseSOAPResponse(soapResponse, pSoapLibrary.getParsingAnswer(), pTestCaseCountry.getNature());

                soapConnection.close();

            } catch (SOAPException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            } catch (IOException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            } catch (SAXException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            } catch (ParserConfigurationException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
                throw new CerberusException(new MessageGeneral(MessageGeneralEnum.EXECUTION_FA));
            }
        }
        return result;
    }

    /**
     * Méthode qui parse la réponse d'une requête soap.
     * Retourne la première bonne réponse ou la nième (en fonction de la nature).
     * @param pSoapResponse réponse de la requête SOAP
     * @param pRule règle de parsing de la réponse
     * @param pNature STATIC ou RANDOM
     * @return String
     */
    private String parseSOAPResponse(final SOAPMessage pSoapResponse, final String pRule, String pNature) {
        String result = null;
        if (pSoapResponse != null) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                pSoapResponse.writeTo(out);

                InputStream is = new ByteArrayInputStream(out.toByteArray());

                // Parse la réponse SOAP
                Document xmlDocument = builder.parse(is);

                XPath xPath = XPathFactory.newInstance().newXPath();

                // Pour le cas où la règle de parsing ne change pas
                String newRule = pRule;

                // La nature demande de changer la règle de parsing
                if (pNature != null && Property.NATURE_RANDOM.equals(pNature)) {
                    Double count = 0.0;

                    Matcher mat = patCount.matcher(pRule);

                    String ruleCount = "";

                    while (mat.find()) {
                        // On prend le premier groupe pour compter le nombre de résultat dans la réponse SOAP
                        ruleCount = mat.group(1);

                        // Détermine le nombre de résultat retourné par la requete SOAP
                        count = (Double) xPath.compile("count(" + ruleCount + ")").evaluate(xmlDocument, XPathConstants.NUMBER);
                    
                        // Détermine un nombre entre 1 et index qui est le nombre total de résultat de la requête SOAP
                        int randomNum = new Random().nextInt(count.intValue()) + 1;
                        // Détermine la nouvelle règle de parsing de la réponse
                        Matcher mat2 = patReplace.matcher(pRule);
                        while (mat2.find()) {
                            newRule = mat2.replaceFirst("[" + randomNum + "]");
                            
                            break;
                        }

                        break;
                    }
                }
                NodeList nodeList2 = (NodeList) xPath.compile(newRule)
                        .evaluate(xmlDocument, XPathConstants.NODESET);

                StringBuilder s = new StringBuilder();
                for (int i = 0; i < nodeList2.getLength(); i++) {
                    // On retourne le premier noeud non null trouvé 
                    if (nodeList2.item(i).getFirstChild().getNodeValue() != null) {
                        s.append(nodeList2.item(i).getFirstChild().getNodeValue());
                    }
                }

                result = s.toString();
                out.close();
                is.close();
            } catch (SOAPException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            } catch (SAXParseException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            } catch (SAXException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            } catch (IOException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            } catch (XPathExpressionException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            } catch (ParserConfigurationException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            }
        }
        return result;
    }

    /**
     * Contruction dynamique de la requête SOAP
     *
     * @param pBody
     * @param method
     * @return SOAPMessage
     * @throws SOAPException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private SOAPMessage createSOAPRequest(final SoapLibrary pSoapLibrary, TestCaseCountryProperties pTestCaseCountry, TestCaseExecution pTestCaseExecution) throws SOAPException, IOException, SAXException, ParserConfigurationException {

        // Précise la version du protocole SOAP à utiliser (nécessaire pour les appels de WS Externe)
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

        SOAPMessage soapMessage = messageFactory.createMessage();

        MimeHeaders headers = soapMessage.getMimeHeaders();

        // Précise la méthode du WSDL à interroger
        headers.addHeader("SOAPAction", pSoapLibrary.getMethod());
        // Encodage UTF-8
        headers.addHeader("Content-Type", "text/xml;charset=UTF-8");

        final SOAPBody soapBody = soapMessage.getSOAPBody();

        // convert String into InputStream - traitement des caracères escapés > < ... (contraintes de l'affichage IHM)
        String unescaped = HtmlUtils.htmlUnescape(pSoapLibrary.getEnvelope());

        String unescapedEnv = checkEnvironment(unescaped, pTestCaseCountry, pTestCaseExecution);

        InputStream is;

        if (unescapedEnv != null) {
            is = new ByteArrayInputStream(unescapedEnv.getBytes());
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            // Important à laisser sinon KO
            builderFactory.setNamespaceAware(true);
            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();

                Document document = builder.parse(is);

                soapBody.addDocument(document);
            } catch (ParserConfigurationException e) {
                MyLogger.log(PropertyService.class.getName(), Level.ERROR, e.toString());
            }
            soapMessage.saveChanges();
        }
        return soapMessage;
    }

    private String checkEnvironment(final String pBody, TestCaseCountryProperties pTestCaseCountry, TestCaseExecution pTestCaseExecution) {
        String result = null;
        if (pBody != null) {
            result = pBody;
            Matcher matExec = patExec.matcher(pBody);
            while (matExec.find()) {

                Matcher matHead = patHead.matcher(pBody);
                while (matHead.find()) {

                    Matcher matEnvi = patEnvi.matcher(pBody);
                    while (matEnvi.find()) {

                        try {
                            String s = countryEnvironmentDatabaseService.findCountryEnvironmentDatabaseByKey(pTestCaseExecution.getApplication().getSystem(), pTestCaseCountry.getCountry(), pTestCaseExecution.getEnvironment(), pTestCaseCountry.getDatabase()).getConnectionPoolName();
                            String newString = matEnvi.replaceAll("<Environment>" + s + "</Environment>");
                            result = newString;
                        } catch (CerberusException ex) {
                            Logger.getLogger(GetConnectionPoolName.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                            result = null;
                        }
                        break;
                    }
                    break;
                }
                break;
            }
        }
        return result;
    }
    
}
