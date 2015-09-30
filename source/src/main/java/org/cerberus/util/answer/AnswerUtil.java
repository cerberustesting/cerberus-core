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
package org.cerberus.util.answer;

import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;

/**
 * Auxiliary class that provides methods related to error messages
 * @author FNogueira
 */
public class AnswerUtil {
 
    public static String createGenericErrorAnswer(){
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("{'messageType':'").append(msg.getCode()).append("', ");
        errorMessage.append(" 'message': '");
        errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature " +
        "<a href=\"https://github.com/vertigo17/Cerberus/issues/\" target=\"_blank\">here</a>"));
        errorMessage.append("'}");
        return errorMessage.toString();
    }
}
