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
package org.cerberus.core.service.cerberuscommand.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.gwt.impl.ActionService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.service.cerberuscommand.ICerberusCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author mlombard
 */
@Service("CerberusCommand")
public class CerberusCommand implements ICerberusCommand {

    /**
     * Associated {@link Logger} to this class
     */
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ActionService.class);

    private MessageEvent message;
    private String scriptPath;
    private String scriptUser;
    private String scriptPassword;
    private String newMessageDescription;
    private String messageDescriptionToReplace;
    private String[] commandToRun;
    private String command;

    @Autowired
    private IParameterService parameterService;

    /**
     * This method can be used in order to execute a script that is located on
     * the application server.
     *
     * @param command script file name (with extention)
     *
     * @return
     * @throws org.cerberus.core.exception.CerberusEventException
     */
    @Override
    public MessageEvent executeCerberusCommand(String command) throws CerberusEventException {
        this.command = command;

        try {
            checkCommandContent();
            checkOS();
            initializeParameters();
            checkPathParameterNotEmpty();
            checkPasswordParameterNotEmpty();
            checkUserParameterNotEmpty();
            checkCommandFirstCharacter();
            concatenateCommandToRun();
            executeProcessBuilder();
        } catch (CerberusEventException ex) {
            this.message = ex.getMessageError();
            checkNewMessageDescription();
            throw new CerberusEventException(this.message);
        }
        return this.message;
    }

    private void checkCommandContent() throws CerberusEventException {
        if (this.command.isEmpty()) {
            this.messageDescriptionToReplace = "%FIELD%";
            this.newMessageDescription = "Command";
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_MISSINGCOMMAND));
        }
    }

    private void checkOS() throws CerberusEventException {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            this.messageDescriptionToReplace = "%OS%";
            this.newMessageDescription = System.getProperty("os.name");
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_NOTSUPPORTED_FOR_OS));
        }
    }

    private void initializeParameters() {
        this.scriptPath = parameterService.getParameterStringByKey("cerberus_executeCerberusCommand_path", "", "");
        this.scriptUser = parameterService.getParameterStringByKey("cerberus_executeCerberusCommand_user", "", "");
        this.scriptPassword = parameterService.getParameterStringByKey("cerberus_executeCerberusCommand_password", "", "");
    }

    private void checkPathParameterNotEmpty() throws CerberusEventException {
        if (this.scriptPath.isEmpty()) {
            this.messageDescriptionToReplace = "%PARAM%";
            this.newMessageDescription = "cerberus_executeCerberusCommand_path";
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_MISSINGPARAMETER));
        }
    }

    private void checkPasswordParameterNotEmpty() throws CerberusEventException {
        if (this.scriptPassword.isEmpty()) {
            this.messageDescriptionToReplace = "%PARAM%";
            this.newMessageDescription = "cerberus_executeCerberusCommand_password";
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_MISSINGPARAMETER));
        }
    }

    private void checkUserParameterNotEmpty() throws CerberusEventException {
        if (this.scriptUser.isEmpty()) {
            this.messageDescriptionToReplace = "%PARAM%";
            this.newMessageDescription = "cerberus_executeCerberusCommand_user";
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_MISSINGPARAMETER));
        }
    }

    private void checkCommandFirstCharacter() throws CerberusEventException {

        String firstChar = Character.toString(this.command.charAt(0));

        if (firstChar.equalsIgnoreCase("/")) {
            this.messageDescriptionToReplace = "%FIRST_CHAR%";
            this.newMessageDescription = firstChar;
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECOMMAND_ILLEGALSTART));
        }
    }

    private void concatenateCommandToRun() {

        this.commandToRun = new String[]{"bash", "-c", "echo -n \""
            + this.scriptPassword
            + "\" | su - "
            + this.scriptUser
            + " -c \"bash /"
            + this.scriptPath + "/"
            + this.command
            + "\""};
    }

    private MessageEvent executeProcessBuilder() {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(this.commandToRun);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            checkExitVal(exitVal);

            this.message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_EXECUTECOMMAND);
            this.messageDescriptionToReplace = "%LOG%";
            this.newMessageDescription = this.command + " Output : " + output;
            checkNewMessageDescription();
            return this.message;

        } catch (IOException | InterruptedException e) {
            this.message = new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECERBERUSCOMMAND);
            this.messageDescriptionToReplace = "%EXCEPTION%";
            this.newMessageDescription = e.toString();
            checkNewMessageDescription();
            return this.message;

        } catch (CerberusEventException ex) {
            this.message = ex.getMessageError();
            checkNewMessageDescription();
            return this.message;
        }
    }

    private void checkExitVal(int exitVal) throws CerberusEventException {

        if (exitVal != 0) {
            this.messageDescriptionToReplace = "%EXCEPTION%";
            this.newMessageDescription = this.command;
            throw new CerberusEventException(new MessageEvent(MessageEventEnum.ACTION_FAILED_EXECUTECERBERUSCOMMAND));
        }
    }

    private void checkNewMessageDescription() {

        if (!this.newMessageDescription.isEmpty()) {
            message.setDescription(message.getDescription().replace(this.messageDescriptionToReplace, this.newMessageDescription));
        }
    }
}
