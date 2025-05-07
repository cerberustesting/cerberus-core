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
package org.cerberus.core.service.robotextension.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.tika.mime.MimeTypeException;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.robotextension.IFilemanagementService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FilemanagementService implements IFilemanagementService {

    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(FilemanagementService.class);

    /**
     * Invariant SIKULI ACTION String.
     */
    public static final String FILEMANAGEMENT_CLEANROBOTFILE = "cleanFolder";
    public static final String FILEMANAGEMENT_UPLOADROBOTFILE = "upload";
    public static final String FILEMANAGEMENT_GETROBOTFILE = "download";

    private static final String SIKULI_FILEMANAGEMENT_PATH = "/extra/ExecuteFilemanagementAction";

    private JSONObject generatePostParameters(String action, int nbFiles, String filename, String option, String contentBase64) throws JSONException, IOException, MalformedURLException, MimeTypeException {
        JSONObject result = new JSONObject();

        if (nbFiles == 0) {
            nbFiles = 1;
        }

        /**
         * Build JSONObject with parameters action : Action expected to be done
         * by Sikuli picture : Picture in Base64 format text : Text to type
         * defaultWait : Timeout for the action pictureExtension : Extension for
         * Base64 decoding
         */
        result.put("action", action);
        result.put("nbFiles", nbFiles);
        result.put("filename", filename);
        result.put("option", option);
        result.put("contentBase64", contentBase64);
        return result;
    }

    @Override
    public AnswerItem<JSONObject> doFilemanagementAction(Session session, String action, int nbFiles, String filename, String option, String contentBase64) {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        StringBuilder response = new StringBuilder();
        URL url;
        String urlToConnect = generateSikuliUrlOnNode(session, SIKULI_FILEMANAGEMENT_PATH);
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            if (session.getExecutorExtensionProxyPort() > 0) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(session.getHost(), session.getExecutorExtensionProxyPort()));

                LOG.info("Open Connection to Robot Node Filemanagement (using proxy : " + session.getHost() + ":" + session.getExecutorExtensionProxyPort() + ") : " + urlToConnect);
                connection = (HttpURLConnection) url.openConnection(proxy);

            } else {
                LOG.info("Open Connection to Robot Node Filemanagement : " + urlToConnect);
                connection = (HttpURLConnection) url.openConnection();
            }
            // We let Robot extension the sikuli timeout + 60 s to perform the action/control.
            connection.setReadTimeout(session.getCerberus_sikuli_wait_element() + 60000);
            connection.setConnectTimeout(session.getCerberus_sikuli_wait_element() + 60000);

            connection.setRequestMethod("POST");

            JSONObject postParameters = generatePostParameters(action, nbFiles, filename, option, contentBase64);
            connection.setDoOutput(true);

            // Send post request
            os = new PrintStream(connection.getOutputStream());
            LOG.debug("Sending JSON : " + postParameters.toString());
            os.println(postParameters.toString());
//            os.println("|ENDS|");

            if (connection == null) {
                LOG.warn("No response from Robot Node Filemanagement !!");
            } else {
                LOG.debug("Robot Node Filemanagement http response status code : " + connection.getResponseCode());
            }

            if (connection == null || connection.getResponseCode() >= 500) {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_NOT_REACHABLE);
            }

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = "";

            /**
             * Wait here until receiving |ENDR| String
             */
            while (inputLine != null) {
                inputLine = in.readLine();
                if (inputLine != null && !"|ENDR|".equals(inputLine)) {
                    response.append(inputLine);
                } else {
                    break;
                }
            }

            LOG.debug("Robot Node Filemanagement Answer: " + response.toString());

            if (response.toString() != null && response.length() > 0) {
                /**
                 * Convert received string into JSONObject
                 */
                JSONObject objReceived = new JSONObject(response.toString());
                answer.setItem(objReceived);
                LOG.debug("Robot Node Filemanagement Answer (json): " + objReceived.toString(1));

                if (objReceived.has("status")) {
                    if (null == objReceived.getString("status")) {
                        msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL);
                    } else {
                        switch (objReceived.getString("status")) {
                            case "OK":
                                msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WITHDETAIL);
                                break;
                            case "KO":
                                msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_BUTRETURNEDKO);
                                break;
                            default:
                                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL);
                                break;
                        }
                    }
                    if (objReceived.has("message") && !StringUtil.isEmptyOrNull(objReceived.getString("message"))) {
                        msg.resolveDescription("DETAIL", objReceived.getString("message"));
                    } else {
                        msg.resolveDescription("DETAIL", "");
                    }
                } else {
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL).resolveDescription("DETAIL", "Robot Extention returned an invalid answer !! (Missing status information)");
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL).resolveDescription("DETAIL", "Robot Extention returned an invalid answer !! (empty answer)");

            }
            in.close();
        } catch (MalformedURLException ex) {
            LOG.warn(ex, ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_BADURL);
            msg.resolveDescription("URL", urlToConnect);
        } catch (FileNotFoundException ex) {
            LOG.warn(ex, ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_BADURL);
            msg.resolveDescription("URL", urlToConnect);
        } catch (IOException ex) {
            LOG.warn(ex, ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_BADURL);
            msg.resolveDescription("URL", urlToConnect);
        } catch (JSONException ex) {
            LOG.warn("Exception when converting response to JSON : " + response.toString(), ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
        } catch (MimeTypeException ex) {
            LOG.warn(ex, ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_NOT_REACHABLE);
            msg.resolveDescription("URL", urlToConnect);
        } finally {
            if (os != null) {
                os.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public MessageEvent doFilemanagementActionCleanRobotFile(Session session, String filename) {
        AnswerItem<JSONObject> actionResult = doFilemanagementAction(session, this.FILEMANAGEMENT_CLEANROBOTFILE, 0, filename, null, null);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLEANROBOTFILE);
            message.setDescription(message.getDescription() + " - " + actionResult.getMessageDescription());
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLEANROBOTFILE);
            mes.setDescription(mes.getDescription() + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doFilemanagementActionUploadRobotFile(Session session, String filename, String contentBase64, String option) {
        // First we check if content is already in base64 format. If not, we convert it to base64.
        if (!Base64.isBase64(contentBase64)) {
            contentBase64 = Base64.encodeBase64String(contentBase64.getBytes(StandardCharsets.UTF_8));
        }

        AnswerItem<JSONObject> actionResult = doFilemanagementAction(session, this.FILEMANAGEMENT_UPLOADROBOTFILE, 0, filename, option, contentBase64);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_UPLOADROBOTFILE);
            message.setDescription(message.getDescription() + " - " + actionResult.getMessageDescription());
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_UPLOADROBOTFILE);
            mes.setDescription(mes.getDescription() + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public AnswerItem<JSONObject> doFilemanagementActionGetRobotFile(Session session, String filename, int nbFiles, String option) {
        AnswerItem<JSONObject> ans = new AnswerItem<>();

        AnswerItem<JSONObject> actionResult = doFilemanagementAction(session, this.FILEMANAGEMENT_GETROBOTFILE, nbFiles, filename, option, null);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_GETROBOTFILE);
            message.setDescription(message.getDescription() + " - " + actionResult.getMessageDescription());
            ans.setResultMessage(message);
            ans.setItem(actionResult.getItem());
            return ans;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_GETROBOTFILE);
            mes.setDescription(mes.getDescription() + " - " + actionResult.getMessageDescription());
            ans.setResultMessage(mes);
            return ans;
        }

        MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_GETROBOTFILE);
        ans.setResultMessage(mes);
        return ans;
    }

    private String generateSikuliUrlOnNode(Session session, String path) {
        int port = session.getExecutorExtensionPort() != 0 ? session.getExecutorExtensionPort() : Integer.parseInt(session.getNodePort());
        return String.format("%s:%d%s", StringUtil.cleanHostURL(session.getNodeHost()), port, path);
    }

}
