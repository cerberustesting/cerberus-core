package com.redcats.tst.entity;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 19/Dez/2012
 * @since 2.0.0
 */
public class MessageGeneral {

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

    public MessageGeneral(MessageGeneralEnum messageGeneralEnum) {
        this.code = messageGeneralEnum.getCode();
        this.codeString = messageGeneralEnum.getCodeString();
        this.description = messageGeneralEnum.getDescription();
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

    public boolean equals(MessageGeneral msg) {
        return this.code == msg.code;
    }
}
