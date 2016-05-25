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
package org.cerberus.engine.entity;

import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.TestDataLibTypeEnum;
import java.util.HashMap;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestDataLibData;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author FNogueira
 */
public class TestDataLibResultSQL extends TestDataLibResult {

    private HashMap<String, String> rawData; //only saves a row each time - TODO:FN save a set of rows

    public TestDataLibResultSQL() {
        this.type = TestDataLibTypeEnum.SQL.getCode();
    }

    @Override
    public AnswerItem<String> getValue(TestDataLibData entry) {
        MessageEvent msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_GETFROMDATALIBDATA);
        AnswerItem ansGetValue = new AnswerItem();

        //checks if the column was already retrieved
        if (!rawData.containsKey(entry.getSubData())) {
            //if the map don't contain the entry that we want, we will get it
            String value = rawData.get(entry.getColumn());
            //associates the subdata with the column data retrieved by the query

            if (value == null) {
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIBDATA_INVALID_COLUMN);
                msg.setDescription(msg.getDescription().replace("%COLUMNNAME%", entry.getColumn()).replace("%SUBDATA%", entry.getSubData()).
                        replace("%ENTRYID%", entry.getTestDataLibID().toString()));
            } else {
                values.put(entry.getSubData(), value);
            }
        }

        ansGetValue.setResultMessage(msg);
        ansGetValue.setItem(rawData.get(entry.getSubData()));

        return ansGetValue;
    }

    public HashMap<String, String> getRawData() {
        return rawData;
    }

    public void setRawData(HashMap<String, String> data) {
        this.rawData = data;
    }
}
