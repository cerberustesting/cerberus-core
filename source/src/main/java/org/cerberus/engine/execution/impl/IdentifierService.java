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
package org.cerberus.engine.execution.impl;

import java.util.Arrays;
import org.cerberus.engine.entity.Identifier;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.engine.execution.IIdentifierService;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class IdentifierService implements IIdentifierService {

    @Override
    public Identifier convertStringToIdentifier(String input) {
        return getIdentifier(input, "id");
    }

    @Override
    public Identifier convertStringToSelectIdentifier(String input) {
        return getIdentifier(input, "value");
    }

    private Identifier getIdentifier(String input, String defaultIdentifier) {
        Identifier result = new Identifier();
        String identifier;
        String locator;
        String[] strings = input.split("=", 2);
        if (strings.length == 1) {
            identifier = defaultIdentifier;
            locator = strings[0];
        } else {
            identifier = strings[0];
            locator = strings[1];
        }

        result.setIdentifier(identifier);
        result.setLocator(locator);
        return result;
    }

    @Override
    public void checkSelectOptionsIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {"label", "value", "index", "regexLabel", "regexValue", "regexIndex"};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SELENIUM_SELECT);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }

    }

    @Override
    public void checkWebElementIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {"id", "name", "class", "css", "xpath", "link", "data-cerberus", "coord", "picture"};

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
        String[] selectOptionAttributes = {"picture", "text"};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_UNKOWN_IDENTIFIER_SIKULI);
            message.setDescription(message.getDescription().replace("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }
    }
}
