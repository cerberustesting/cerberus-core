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
package org.cerberus.serviceEngine;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.SoapLibrary;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
public interface ISoapService {

    /**
     * 
     * @param pBody Body of the SOAP message
     * @param method Method/Action of the WSDL which will be used
     * @return SOAPMessage generated
     * @throws SOAPException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException 
     */
    SOAPMessage createSoapRequest(final String pBody, final String method) throws SOAPException, IOException, SAXException, ParserConfigurationException;
    
    /**
     * 
     * @param uuid uuid of the execution or of the request
     * @param envelope The envelope of the the SOAP
     * @param servicePath The servicePath (WSDL) of the SOAP
     * @param method The name of the method of the SOAP
     * @param attachmentUrl
     * @return MessageEvent with the status of the call
     */
    MessageEvent callSOAPAndStoreResponseInMemory(String uuid, final String envelope, final String servicePath, final String method, final String attachmentUrl);
    
    /**
     * 
     * @param message
     * @param url 
     * @throws org.cerberus.exception.CerberusException 
     */
    void addAttachmentPart(SOAPMessage message, String url) throws CerberusException;
    
}
