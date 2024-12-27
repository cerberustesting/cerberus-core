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
package org.cerberus.core.util.xml;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Auxiliary class to handle the parse of the Test Data Lib document used to
 * import libraries.
 *
 * @author FNogueira
 */
public class XMLTestDataLibHandler extends DefaultHandler {

    private final String TESTDATALIB = "testdatalib";

    private final String TESTDATALIBDATA = "testdatalibdata";
    private final String TESTDATALIBDATASET = "testdatalibdataset";

    private CharArrayWriter contents = new CharArrayWriter();
    private String text = "";
    HashMap<TestDataLib, List<TestDataLibData>> dataFromFile;

    public HashMap<TestDataLib, List<TestDataLibData>> getDataFromFile() {
        return dataFromFile;
    }

    //current list
    List<TestDataLibData> currentList;
    //curent testdatalib
    TestDataLib dataItem;
    //current testdatalibdata
    TestDataLibData subDataItem;
    String currentElement = "";

    @Override
    public void startDocument() throws SAXException {
        dataFromFile = new HashMap<>();
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(TESTDATALIB)) {
            dataFromFile.put(dataItem, currentList);
        } else if (qName.equalsIgnoreCase(TESTDATALIBDATA)) {
            currentList.add(subDataItem);
        }
        extractTestDataLib(qName);
        extractTestDataLibData(qName);

    }

    private void extractTestDataLib(String element) {
        if (element.equalsIgnoreCase("name")) {
            dataItem.setName(text.trim());
        } else if (element.equalsIgnoreCase("system")) {
            dataItem.setSystem(text.trim());
        } else if (element.equalsIgnoreCase("environment")) {
            dataItem.setEnvironment(text.trim());
        } else if (element.equalsIgnoreCase("country")) {
            dataItem.setCountry(text.trim());
        } else if (element.equalsIgnoreCase("group")) {
            dataItem.setGroup(text.trim());
        } else if (element.equalsIgnoreCase("type")) {
            dataItem.setType(text.trim());
        } else if (element.equalsIgnoreCase("database")) {
            dataItem.setDatabase(text.trim());
        } else if (element.equalsIgnoreCase("script")) {
            dataItem.setScript(text.trim());
        } else if (element.equalsIgnoreCase("servicepath")) {
            dataItem.setServicePath(text.trim());
        } else if (element.equalsIgnoreCase("method")) {
            dataItem.setMethod(text.trim());
        } else if (element.equalsIgnoreCase("envelope")) {
            dataItem.setEnvelope(text.trim());
        } else if (element.equalsIgnoreCase("description")) {
            dataItem.setDescription(text.trim());
        }
    }

    private void extractTestDataLibData(String element) {
        if (element.equalsIgnoreCase("subdata")) {
            subDataItem.setSubData(text.trim());
        } else if (element.equalsIgnoreCase("value")) {
            subDataItem.setValue(text.trim());
        } else if (element.equalsIgnoreCase("parswinganswer")) {
            subDataItem.setParsingAnswer(text.trim());
        } else if (element.equalsIgnoreCase("column")) {
            subDataItem.setColumn(text.trim());
        } else if (element.equalsIgnoreCase("descriptionsubdata")) {
            subDataItem.setDescription(text.trim());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase(TESTDATALIB)) {
            dataItem = new TestDataLib();
            dataItem.setTestDataLibID(dataFromFile.size()); //temporary ID

        } else if (qName.equalsIgnoreCase(TESTDATALIBDATASET)) {
            currentList = new ArrayList<>();
        } else if (qName.equalsIgnoreCase(TESTDATALIBDATA)) {
            subDataItem = new TestDataLibData();
            subDataItem.setTestDataLibID(dataFromFile.size());
        }
        currentElement = qName;

    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // accumulate the contents into a buffer.
        contents.write(ch, start, length);
        text = new String(ch, start, length);

    }
}
