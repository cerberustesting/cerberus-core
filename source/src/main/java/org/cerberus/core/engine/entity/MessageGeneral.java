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
package org.cerberus.core.engine.entity;

import org.cerberus.core.enums.MessageGeneralEnum;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 19/Dez/2012
 * @since 0.9.0
 */
public class MessageGeneral {

    /**
     * Variable delimiter on the {@link MessageEvent#description} field
     */
    public static final char VARIABLE_DELIMITER = '%';

    /**
     * Message is a generic Message that is used to feedback the result of any Cerberus execution.
     * For every message, we have:
     * - a number
     * - a 2 digit code that report the status of the event.
     * - a clear message that will be reported to the user. describing what was done or the error that occured.
     */

    private final int code;
    private final String codeString;
    private String description;
    
    private MessageGeneralEnum source;

    public MessageGeneral(MessageGeneralEnum messageGeneralEnum) {
        this.code = messageGeneralEnum.getCode();
        this.codeString = messageGeneralEnum.getCodeString();
        this.description = messageGeneralEnum.getDescription();
        this.source = messageGeneralEnum;
    }

    public int getCode() {
        return this.code;
    }

    public String getCodeString() {
        return codeString;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MessageGeneralEnum getSource() {
        return source;
    }

    /**
     * Resolve description by injecting the given value for the given key
     * <p>
     * A key is a {@link MessageGeneral} variable that follows the given pattern:
     * {@link MessageGeneral#VARIABLE_DELIMITER}[variable name]{@link MessageGeneral#VARIABLE_DELIMITER}
     *
     * @param key   the variable name to replace on the {@link MessageGeneral} description
     * @param value the value to replace for the given variable name
     * @return this {@link MessageGeneral} instance
     */
    public MessageGeneral resolveDescription(String key, String value) {
        if (description != null) {
            description = description.replace(VARIABLE_DELIMITER + key + VARIABLE_DELIMITER, value);
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageGeneral msg = (MessageGeneral) obj;
        return this.code == msg.code;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "MessageGeneral{" + "code=" + code + ", codeString=" + codeString + ", description=" + description + '}';
    }
    
    
}
