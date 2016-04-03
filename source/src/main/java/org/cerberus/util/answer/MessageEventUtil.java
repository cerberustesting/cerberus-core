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

import java.util.HashMap;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.json.JSONObject;

/**
 * Auxiliary class that provides common methods to handle messages.
 *
 * @author FNogueira
 */
public class MessageEventUtil {

    private static final String CREATE_OPERATION = "Insert";
    private static final String UPDATE_OPERATION = "Update";
    private static final String DELETE_OPERATION = "Delete";
    private static final String SELECT_OPERATION = "Select";

    private MessageEventUtil() {
    }

    private static MessageEvent createUnexpectedErrorMessageDAO(String operation) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to execute " + operation + " operation(s)!"));
        return msg;
    }

    private static MessageEvent createSuccessMessageDAO(String item, String operation) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", item));
        msg.setDescription(msg.getDescription().replace("%OPERATION%", operation));
        return msg;
    }

    private static MessageEvent createExpectedErrorMessageDAO(String item, String operation, String description) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
        msg.setDescription(msg.getDescription().replace("%ITEM%", item));
        msg.setDescription(msg.getDescription().replace("%OPERATION%", operation));
        msg.setDescription(msg.getDescription().replace("%REASON%", description));
        return msg;
    }

    public static MessageEvent createDeleteExpectedErrorMessageDAO(String item, String description) {
        return createExpectedErrorMessageDAO(item, DELETE_OPERATION, description);
    }

    public static MessageEvent createInsertExpectedErrorMessageDAO(String item, String description) {
        return createExpectedErrorMessageDAO(item, CREATE_OPERATION, description);
    }

    public static MessageEvent createUpdateExpectedErrorMessageDAO(String item, String description) {
        return createExpectedErrorMessageDAO(item, UPDATE_OPERATION, description);
    }

    public static MessageEvent createSelectExpectedErrorMessageDAO(String item, String description) {
        return createExpectedErrorMessageDAO(item, SELECT_OPERATION, description);
    }

    public static MessageEvent createInsertSuccessMessageDAO(String item) {
        return createSuccessMessageDAO(item, CREATE_OPERATION);
    }

    public static MessageEvent createUpdateSuccessMessageDAO(String item) {
        return createSuccessMessageDAO(item, UPDATE_OPERATION);
    }

    public static MessageEvent createDeleteSuccessMessageDAO(String item) {
        return createSuccessMessageDAO(item, DELETE_OPERATION);
    }

    public static MessageEvent createSelectSuccessMessageDAO(String item) {
        return createSuccessMessageDAO(item, SELECT_OPERATION);
    }

    public static MessageEvent createInsertUnexpectedErrorMessageDAO() {
        return createUnexpectedErrorMessageDAO(CREATE_OPERATION);
    }

    public static MessageEvent createUpdateUnexpectedErrorMessageDAO() {
        return createUnexpectedErrorMessageDAO(UPDATE_OPERATION);
    }

    public static MessageEvent createDeleteUnexpectedErrorMessageDAO() {
        return createUnexpectedErrorMessageDAO(DELETE_OPERATION);
    }

    public static MessageEvent createSelectUnexpectedErrorMessageDAO() {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve data!"));
        return msg;
    }

    public static MessageEvent createMessageDescriptionJSONFormat(MessageEventEnum messageType,
            HashMap<String, String> data) {
        MessageEvent message = new MessageEvent(messageType);

        JSONObject obj = new JSONObject(data);
        message.setDescription(obj.toString());
        return message;
    }
}
