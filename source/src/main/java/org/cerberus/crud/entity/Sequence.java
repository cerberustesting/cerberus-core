/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.crud.entity;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Sequence {
    private int step;
    private int sequence;
    private String action;
    private String object;
    private String property;
    private MessageEvent messageResult;
    private long start;
    private long end;

    public int getStep() {
        return this.step;
    }

    public void setStep(int tempStep) {
        this.step = tempStep;
    }

    public int getSequence() {
        return this.sequence;
    }

    public void setSequence(int tempSequence) {
        this.sequence = tempSequence;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String tempAction) {
        this.action = tempAction;
    }

    public String getObject() {
        return this.object;
    }

    public void setObject(String tempObject) {
        this.object = tempObject;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String tempProperty) {
        this.property = tempProperty;
    }

    public MessageEvent getMessageResult() {
        return this.messageResult;
    }

    public void setMessageResult(MessageEvent tempMessageResult) {
        this.messageResult = tempMessageResult;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long tempStart) {
        this.start = tempStart;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long tempEnd) {
        this.end = tempEnd;
    }

}
