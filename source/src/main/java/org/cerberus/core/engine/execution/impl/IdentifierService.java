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
package org.cerberus.core.engine.execution.impl;

import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IIdentifierService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author bcivel
 */
@Service
public class IdentifierService implements IIdentifierService {

    @Override
    public Identifier convertStringToIdentifier(String input) {
        return getIdentifier(input, Identifier.IDENTIFIER_ID);
    }

    @Override
    public Identifier convertStringToIdentifierStrict(String input) {
        return getIdentifier(input, "");
    }

    @Override
    public Identifier convertStringToSelectIdentifier(String input) {
        return getIdentifier(input, Identifier.IDENTIFIER_VALUE);
    }

    private Identifier getIdentifier(String input, String defaultIdentifier) {
        Identifier result = new Identifier();
        String identifier;
        String locator;

        if ((input.startsWith("//")) || (input.startsWith("(//"))) {
            identifier = Identifier.IDENTIFIER_XPATH;
            locator = input;
        } else {
            String[] strings = input.split("=", 2);
            if (strings.length == 1) {
                identifier = defaultIdentifier;
                locator = strings[0];
            } else {
                identifier = strings[0];
                locator = strings[1];

                String[] selectOptionAttributes = {
                    Identifier.IDENTIFIER_ID, Identifier.IDENTIFIER_XPATH, Identifier.IDENTIFIER_NAME,
                    Identifier.IDENTIFIER_CLASS, Identifier.IDENTIFIER_CSS, Identifier.IDENTIFIER_LINK,Identifier.IDENTIFIER_DATACERBERUS, 
                    Identifier.IDENTIFIER_QUERYSELECTOR, Identifier.IDENTIFIER_ERRATUM,
                    Identifier.IDENTIFIER_PICTURE, Identifier.IDENTIFIER_TEXT,
                    Identifier.IDENTIFIER_COORD, Identifier.IDENTIFIER_OFFSET,
                    Identifier.IDENTIFIER_TITLE, Identifier.IDENTIFIER_REGEXTITLE, Identifier.IDENTIFIER_URL, Identifier.IDENTIFIER_REGEXURL,
                    Identifier.IDENTIFIER_VALUE, Identifier.IDENTIFIER_REGEXVALUE,
                    Identifier.IDENTIFIER_INDEX, Identifier.IDENTIFIER_REGEXINDEX,
                    Identifier.IDENTIFIER_LABEL, Identifier.IDENTIFIER_REGEXLABEL
                };

                if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
                    identifier = defaultIdentifier;
                    locator = input;
                }
            }
        }

        result.setIdentifier(identifier);
        result.setLocator(locator);
        return result;
    }

    @Override
    public void checkSelectOptionsIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {Identifier.IDENTIFIER_LABEL, Identifier.IDENTIFIER_VALUE, Identifier.IDENTIFIER_INDEX, Identifier.IDENTIFIER_REGEXLABEL,
            Identifier.IDENTIFIER_REGEXVALUE, Identifier.IDENTIFIER_REGEXINDEX};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SELENIUM_SELECT);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }

    }

    @Override
    public void checkWebElementIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {Identifier.IDENTIFIER_ID, Identifier.IDENTIFIER_NAME, Identifier.IDENTIFIER_CLASS, Identifier.IDENTIFIER_CSS,
            Identifier.IDENTIFIER_XPATH, Identifier.IDENTIFIER_LINK, Identifier.IDENTIFIER_DATACERBERUS, Identifier.IDENTIFIER_COORD, Identifier.IDENTIFIER_PICTURE,
            Identifier.IDENTIFIER_QUERYSELECTOR, Identifier.IDENTIFIER_ERRATUM, Identifier.IDENTIFIER_OFFSET,};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SELENIUM);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }
    }

    @Override
    public void checkSQLIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {"script", "procedure"};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SQL);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }
    }

    @Override
    public void checkSikuliIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {Identifier.IDENTIFIER_PICTURE, Identifier.IDENTIFIER_TEXT};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SIKULI);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }
    }
}
