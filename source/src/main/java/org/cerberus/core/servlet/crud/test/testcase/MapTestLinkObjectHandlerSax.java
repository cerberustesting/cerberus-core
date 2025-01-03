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
package org.cerberus.core.servlet.crud.test.testcase;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author vertigo
 */
public class MapTestLinkObjectHandlerSax extends DefaultHandler {

    private static final Logger LOG = LogManager.getLogger(MapTestLinkObjectHandlerSax.class);

    private StringBuilder currentValue = new StringBuilder();
    List<TestCase> result;
    TestCase currentTestCase;

    public List<TestCase> getResult() {
        return result;
    }

    @Override
    public void startDocument() {
        LOG.debug("Start Document.");

        result = new ArrayList<>();
    }

    @Override
    public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes) {

        // reset the tag value
        currentValue.setLength(0);

        LOG.debug("Start Element. '{}' '{}' '{}' '{}'", uri, localName, qName, attributes);

        // start of loop
        if (qName.equalsIgnoreCase("staff")) {

            // new staff
            currentTestCase = new TestCase();

            // staff id
            String id = attributes.getValue("id");
            currentTestCase.setTestcase(String.valueOf(id));
        }

        if (qName.equalsIgnoreCase("salary")) {
            // salary currency
            String currency = attributes.getValue("currency");
            currentTestCase.setDescription(currency);
        }

    }

    @Override
    public void endElement(String uri,
            String localName,
            String qName) {
        LOG.debug("End Element.");

        if (qName.equalsIgnoreCase("name")) {
            currentTestCase.setTestcase(currentValue.toString());
        }

        if (qName.equalsIgnoreCase("role")) {
            currentTestCase.setTestcase(currentValue.toString());
        }

//        if (qName.equalsIgnoreCase("salary")) {
//            currentTestCase.setPriority(currentValue.toString());
//        }
//
//        if (qName.equalsIgnoreCase("bio")) {
//            currentTestCase.setBio(currentValue.toString());
//        }
        // end of loop
        if (qName.equalsIgnoreCase("staff")) {
            result.add(currentTestCase);
        }

    }

    @Override
    public void characters(char ch[], int start, int length) {
        currentValue.append(ch, start, length);

    }

}
