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
package org.cerberus.service.soap.impl;

import java.util.Date;
import java.util.UUID;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author bcivel
 */
@WebService
@SOAPBinding(style = Style.RPC)
public class DummySoap {

    public DummyObject checkSoapResponse(
            @WebParam(name = "string1") String string1,
            @WebParam(name = "integer1") Integer integer1,
            @WebParam(name = "listVal1") String listVal1,
            @WebParam(name = "listVal2") String listVal2,
            @WebParam(name = "listVal3") String listVal3) {

        DummyObject dummyObject = new DummyObject();
        dummyObject.setString1(string1);
        dummyObject.setInteger1(integer1);
        dummyObject.setList1(listVal1, listVal2, listVal3);
        return dummyObject;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    private static class DummyObject {

        @XmlElement
        private final String randomString = getRandomString();
        @XmlElement
        private final Date now = getNow();
        @XmlElement(required = true, type = String.class)
        private String string1;
        @XmlElement(nillable = false, type = Integer.class)
        private Integer integer1;
        @XmlElement
        private DummySubObject list1;

        public String getRandomString(){
            return UUID.randomUUID().toString();
        }
        
        public Date getNow(){
            return new Date();
        }
        
        public String getString1() {
            return string1;
        }

        public Integer getInteger1() {
            return integer1;
        }

        public DummySubObject getList1() {
            return list1;
        }

        public void setString1(String string1) {
            this.string1 = string1;
        }

        public void setInteger1(Integer integer1) {
            this.integer1 = integer1;
        }

        public void setList1(String listVal1, String listVal2, String listVal3) {
            list1 = new DummySubObject();
            list1.setAttribute1(listVal1);
            list1.setAttribute2(listVal2);
            list1.setAttribute3(listVal3);
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    private static class DummySubObject {

        @XmlElement(name = "subElem1", type = String.class)
        private String attribute1;
        @XmlElement(name = "subElem1", type = String.class)
        private String attribute2;
        @XmlElement(name = "subElem1", type = String.class)
        private String attribute3;

        public String getAttribute1() {
            return attribute1;
        }

        public String getAttribute2() {
            return attribute2;
        }

        public String getAttribute3() {
            return attribute3;
        }

        public void setAttribute1(String attribute1) {
            this.attribute1 = attribute1;
        }

        public void setAttribute2(String attribute2) {
            this.attribute2 = attribute2;
        }

        public void setAttribute3(String attribute3) {
            this.attribute3 = attribute3;
        }
    }
}
