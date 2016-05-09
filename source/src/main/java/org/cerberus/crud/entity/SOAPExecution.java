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
package org.cerberus.crud.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author bcivel
 */
public class SOAPExecution {

    private SOAPMessage SOAPRequest;
    private SOAPMessage SOAPResponse;

    public SOAPMessage getSOAPRequest() {
        return SOAPRequest;
    }

    public void setSOAPRequest(SOAPMessage SOAPRequest) {
        this.SOAPRequest = SOAPRequest;
    }

    public SOAPMessage getSOAPResponse() {
        return SOAPResponse;
    }

    public void setSOAPResponse(SOAPMessage SOAPResponse) {
        this.SOAPResponse = SOAPResponse;
    }
   
}
