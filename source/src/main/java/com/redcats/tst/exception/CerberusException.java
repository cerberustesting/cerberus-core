package com.redcats.tst.exception;

import com.redcats.tst.entity.MessageEventEnum;
import com.redcats.tst.entity.MessageGeneral;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 24/06/2013
 * @since 2.0.0
 */
public class CerberusException extends Exception {

    private MessageGeneral MessageError;

    public CerberusException(MessageGeneral message) {
        this.MessageError = message;
    }

    public MessageGeneral getMessageError() {
        return MessageError;
    }

    public void setMessageError(MessageGeneral MessageError) {
        this.MessageError = MessageError;
    }

}
