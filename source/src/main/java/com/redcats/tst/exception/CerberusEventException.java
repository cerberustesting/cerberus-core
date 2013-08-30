package com.redcats.tst.exception;

import com.redcats.tst.entity.MessageEvent;

/**
 * {Insert class description here}
 *
 * @author Benoit DUMONT
 * @version 1.0, 24/06/2013
 * @since 2.0.0
 */
public class CerberusEventException extends Exception {

    private MessageEvent MessageError;

    public CerberusEventException(MessageEvent message) {
        this.MessageError = message;
    }

    public MessageEvent getMessageError() {
        return MessageError;
    }

    public void setMessageError(MessageEvent MessageError) {
        this.MessageError = MessageError;
    }

}
