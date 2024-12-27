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

import org.apache.logging.log4j.*;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;

import java.util.*;

/**
 * Auxiliary class that provides methods related to error messages
 *
 * @author FNogueira
 */
public class AnswerUtil {

    private static final Logger LOG = LogManager.getLogger(AnswerUtil.class);

    private AnswerUtil() {
    }

    public static String createGenericErrorAnswer() {
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("{\"messageType\":\"").append(msg.getCode()).append("\", ");
        errorMessage.append("\"message\": \"");
        errorMessage.append(msg.getDescription().replace("%DESCRIPTION%", "Unable to check the status of your request! Try later or - Open a bug or ask for any new feature"));
        errorMessage.append("\"}");
        return errorMessage.toString();
    }

    public static Answer agregateAnswer(Answer existingAnswer, Answer newAnswer) {
        Answer ans = new Answer();
        if (newAnswer == null) {// When new is null, nothing happen to the old (existing) Answer
            return existingAnswer;
        }
        if (newAnswer.isCodeStringEquals(MessageEventEnum.GENERIC_OK.getCodeString())) { // When new is OK, nothing happen to the old (existing) Answer

            return existingAnswer;

        } else if (newAnswer.isCodeStringEquals(MessageEventEnum.GENERIC_WARNING.getCodeString())) { // When new is Warning, 

            if (existingAnswer.isCodeStringEquals(MessageEventEnum.GENERIC_OK.getCodeString())) { // and exsting is OK, we replace the message to the answer and move the code to Warning.
                // Move existing to WARNING and add description
                MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_WARNING);
                msg.setDescription(newAnswer.getMessageDescription());
                ans.setResultMessage(msg);
                return ans;
            } else {
                // Leave the code and just add the description
                MessageEvent msg = existingAnswer.resultMessage;
                msg.setDescription(msg.getDescription().concat(" -- " + newAnswer.getMessageDescription()));
                ans.setResultMessage(msg);
                return ans;
            }

        } else if (newAnswer.isCodeStringEquals(MessageEventEnum.GENERIC_ERROR.getCodeString())) {  // When new is ERROR,
            // Keep the ERROR Error code.
            MessageEvent msg = newAnswer.resultMessage;
            if (existingAnswer.isCodeStringEquals(MessageEventEnum.GENERIC_OK.getCodeString())) {
                msg.setDescription(newAnswer.getMessageDescription()); // If old is OK we replace the error message
            } else {
                msg.setDescription(msg.getDescription().concat(" -- " + newAnswer.getMessageDescription())); // If old is not OK we add the error message
            }
            ans.setResultMessage(msg);
            return ans;

        }
        return null; // That should never happen.
    }

    @FunctionalInterface
    public interface AnswerItemFunction<R> {

        R apply() throws CerberusException;
    }

    @FunctionalInterface
    public interface AnswerListFunction<R> {

        List<R> apply() throws CerberusException;
    }

    public static <R> AnswerItem<R> convertToAnswerItem(AnswerItemFunction<R> answerFunction) {
        AnswerItem<R> answer = new AnswerItem<>();
        MessageEvent msg = null;
        R result = null;
        try {
            result = answerFunction.apply();
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK_GENERIC);
        } catch (CerberusException exception) {
            LOG.error("A CerberusException occured : " + exception.toString(), exception);
            msg = new MessageEvent(exception.getMessageError().getCodeString(), exception.getMessageError().getDescription());
        }

        answer.setItem(result);
        answer.setResultMessage(msg);

        return answer;

    }

    public static <R> AnswerList<R> convertToAnswerList(AnswerListFunction<R> answerFunction) {
        AnswerList<R> answer = new AnswerList<>();
        MessageEvent msg = null;
        List<R> result = null;

        try {
            result = answerFunction.apply();
            answer.setTotalRows(result.size());
            answer.setDataList(result);

        } catch (CerberusException exception) {
            LOG.error("A CerberusException occured : " + exception.toString(), exception);
            msg = new MessageEvent(exception.getMessageError().getCodeString(), exception.getMessageError().getDescription());
        }

        answer.setResultMessage(msg);

        return answer;

    }
}
