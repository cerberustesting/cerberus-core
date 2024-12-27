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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author bcivel
 */
public final class SoapUtil {
    
    /**
     * 
     * @param message SOAPMessage to convert into String
     * @return String format of a SOAPMessage
     */
    public static String convertSoapMessageToString(SOAPMessage message) {
        String result = null;

        if (message != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                result = baos.toString();
            } catch (SOAPException e) {
                
            } catch (IOException e) {
                
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return result;
    }

    private SoapUtil() {
    }
}
