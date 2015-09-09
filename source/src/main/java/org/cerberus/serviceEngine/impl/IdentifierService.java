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
package org.cerberus.serviceEngine.impl;

import java.util.Arrays;
import org.cerberus.entity.Identifier;
import org.cerberus.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.serviceEngine.IIdentifierService;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class IdentifierService implements IIdentifierService {

    @Override
    public Identifier convertStringToIdentifier(String input) {
        Identifier result = new Identifier();
        String identifier;
        String locator;
        String[] strings = input.split("=", 2);
        if (strings.length == 1) {
            identifier = "id";
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
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELECT_NO_IDENTIFIER);
            message.setDescription(message.getDescription().replaceAll("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }

    }

    @Override
    public void checkWebElementIdentifier(String identifier) throws CerberusEventException {
        String[] selectOptionAttributes = {"id", "name", "class", "css", "xpath", "link", "data-cerberus", "picture"};

        if (!Arrays.asList(selectOptionAttributes).contains(identifier)) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replaceAll("%IDENTIFIER%", identifier));
            throw new CerberusEventException(message);
        }
    }
}
