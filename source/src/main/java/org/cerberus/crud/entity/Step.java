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

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Step {

    private int number;
    private String name;
    private boolean dailyChain;
    private boolean fastChain;
    private boolean morningChain;
    private List<Sequence> sequences;
    private long start;
    private long end;
    private MessageEvent messageResult;

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int tempNumber) {
        this.number = tempNumber;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String tempName) {
        this.name = tempName;
    }

    public boolean isDailyChain() {
        return this.dailyChain;
    }

    public void setDailyChain(boolean tempDailyChain) {
        this.dailyChain = tempDailyChain;
    }

    public boolean isFastChain() {
        return this.fastChain;
    }

    public void setFastChain(boolean tempFastChain) {
        this.fastChain = tempFastChain;
    }

    public boolean isMorningChain() {
        return this.morningChain;
    }

    public void setMorningChain(boolean tempMorningChain) {
        this.morningChain = tempMorningChain;
    }

    public List<Sequence> getSequences() {
        return this.sequences;
    }

    public void setSequences(List<Sequence> tempSequences) {
        this.sequences = tempSequences;
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

    public MessageEvent getMessageResult() {
        return this.messageResult;
    }

    public void setMessageResult(MessageEvent tempMessageResult) {
        this.messageResult = tempMessageResult;
    }

    public boolean isBatch() {
        return this.dailyChain || this.fastChain || this.morningChain;
    }
}
