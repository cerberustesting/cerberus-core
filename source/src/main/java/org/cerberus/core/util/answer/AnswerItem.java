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

import org.cerberus.core.engine.entity.MessageEvent;

/**
 * Auxiliary class that is used to store an answer that contains a message and
 * an item with the desired results.
 *
 * @author FNogueira
 * @param <T> / object that is sent in the answer
 */
public class AnswerItem<T extends Object> extends Answer {

    private T item;

    public AnswerItem() {

    }

    public AnswerItem(T item) {
        this.item = item;
    }

    public AnswerItem(MessageEvent resultMessage) {
        this.item = null;
        this.resultMessage = resultMessage;
    }

    public AnswerItem(T item, MessageEvent resultMessage) {
        this.item = item;
        this.resultMessage = resultMessage;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

}
