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

import org.cerberus.entity.MessageEvent;

/**
 * //TODO:FN comment
 * @author FNogueira
 */
public class AnswerItem <T extends Object> extends Answer{
    
    private T item;
    
    public AnswerItem() {
    
    }
    public AnswerItem(T item) {
         this.item = item;
         //TODO:FN faz sentido sem a mensagem?
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
