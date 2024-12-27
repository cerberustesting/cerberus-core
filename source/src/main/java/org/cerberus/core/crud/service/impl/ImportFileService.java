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
package org.cerberus.core.crud.service.impl;

import java.io.IOException; 
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.crud.service.IImportFileService;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.xml.XMLTestDataLibHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException; 

/**
 * Concrete implementation of the services that handle requests for importing external files.
 * @author FNogueira
 */
@Service
public class ImportFileService implements IImportFileService{

    private static final Logger LOG = LogManager.getLogger(ImportFileService.class);
    
    @Override
    public AnswerItem<Object> importAndValidateXMLFromInputStream(InputStream filecontent, InputStream schemaContent, XMLHandlerEnumType handlerType) {
        AnswerItem<Object> answer = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library").replace("%OPERATION%", "Import"));
        if(schemaContent != null){
            try {
                
                //InputStream data = new BufferedInputStream(filecontent);
                Charset charset = StandardCharsets.UTF_8;
                
                String textContent = IOUtils.toString(filecontent, charset);

                Source source = new StreamSource(IOUtils.toInputStream(textContent, charset));
                SchemaFactory factory=SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                 
                
                Source sourceschema = new StreamSource(schemaContent);
                
                Schema schema=factory.newSchema(sourceschema);
                Validator validator = schema.newValidator();
                //is valid
                validator.validate(source);
                //document is valid, then proceed to load the data
                 
                answer.setItem(parseXMLFile(IOUtils.toInputStream(textContent, charset), handlerType));                           
                            
            } catch (SAXException ex) {
                LOG.warn("Unable to parse XML: " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_IMPORT_ERROR_FORMAT);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library").replace("%FORMAT%", "XML"));
                
            }catch (ParserConfigurationException ex) {
                LOG.warn("Unable to parse XML: " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to parse the XML document. Please try again later."));                                               
            } catch (IOException ex) {
                LOG.warn("Unable to parse XML: " + ex.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to verify if the XML document is valid. Please try again later."));                               
            }
            
        }
        answer.setResultMessage(msg);
        return answer;
    }
    
    /**
     * Auxiliary method that parses an XML. Depending on the type, a different handler can be used, and different information is retrieved to the user
     * @param filecontent content to be parse
     * @param handlerType handler type that defines the type of information that should be retrieved
     * @return the content of the XML file that was parsed
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException 
     */
    private Object parseXMLFile(InputStream xmlContent, XMLHandlerEnumType handlerType) throws SAXException, ParserConfigurationException, IOException{
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();        
        SAXParser saxParser = saxParserFactory.newSAXParser();
        
        if(handlerType.ordinal() == XMLHandlerEnumType.TESTDATALIB_HANDLER.ordinal()){
            XMLTestDataLibHandler handler = new XMLTestDataLibHandler();            
            saxParser.parse(xmlContent, handler);              
            return handler.getDataFromFile(); // returns tha map that contains the data that we want to parse
        }
        return null;
    }
    
    
}
