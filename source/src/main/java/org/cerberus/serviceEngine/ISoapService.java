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
import org.cerberus.entity.SoapLibrary;
import org.cerberus.exception.CerberusException;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
public interface ISoapService {

    SOAPMessage createSoapRequest(final String pBody, final String method) throws SOAPException, IOException, SAXException, ParserConfigurationException;
    
    String getSOAPResponse(final String envelope, final String servicePath, final String method);
    
    String calculatePropertyFromSOAPResponse(final SoapLibrary pSoapLibrary, org.cerberus.entity.TestCaseCountryProperties pTestCaseCountry, org.cerberus.entity.TestCaseExecution pTestCaseExecution) throws CerberusException;
}
