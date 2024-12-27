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
package org.cerberus.core.util.answer;

import java.util.List;
import org.cerberus.core.engine.entity.MessageEvent;

/**
 * Auxiliary class that is used to store an answer that contains a message and a
 * list containing the expected results.
 *
 * @author FNogueira
 * @param <E>
 */
public class AnswerList<E extends Object> extends Answer {

    /**
     * Content retrieved from the database that match some criteria
     */
    private List<E> dataList;

    /**
     * Total number of rows available in the database table
     */
    private int totalRows;

    public AnswerList() {
        this.totalRows = 0;
    }

    /**
     * Public constructor that returns an answer that contains a list
     *
     * @param dataList
     * @param totalRows
     */
    public AnswerList(List<E> dataList, int totalRows) {
        this.totalRows = totalRows;
        this.dataList = dataList;
    }

    public AnswerList(List<E> dataList, int totalRows, MessageEvent resultMessage) {
        this.totalRows = totalRows;
        this.dataList = dataList;
        this.resultMessage = resultMessage;
    }

    public AnswerList(MessageEvent resultMessage) {
        this.totalRows = 0;
        this.dataList = null;
        this.resultMessage = resultMessage;
    }

    public List<E> getDataList() {
        return dataList;
    }

    public void setDataList(List<E> dataList) {
        this.dataList = dataList;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
}
