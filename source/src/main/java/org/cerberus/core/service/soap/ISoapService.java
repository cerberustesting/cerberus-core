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
package org.cerberus.core.service.soap;

import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
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
     * @param header List of header values to add in request.
     * @param token will be added to header in 'cerberus-token' entry if not null.
     * @return SOAPMessage generated
     * @throws SOAPException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException 
     */
    SOAPMessage createSoapRequest(final String pBody, final String method, List<AppServiceHeader> header, String token) throws SOAPException, IOException, SAXException, ParserConfigurationException;
    
    /**
     * 
     * @param message
     * @param url 
     * @throws org.cerberus.core.exception.CerberusException 
     */
    void addAttachmentPart(SOAPMessage message, String url) throws CerberusException;
    
    /**
     * Call Soap Message 
     * @param envelope
     * @param servicePath
     * @param operation
     * @param attachmentUrl
     * @param header
     * @param token
     * @param timeOutMs
     * @param system
     * @return 
     */
    AnswerItem<AppService> callSOAP(String envelope, String servicePath, String operation, String attachmentUrl, List<AppServiceHeader> header, String token, int timeOutMs, String system);
    
}
