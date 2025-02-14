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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.ConnectException;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.Identifier;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.robotextension.ISikuliService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class SikuliService implements ISikuliService {

    @Autowired
    private IParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(SikuliService.class);

    /**
     * Invariant SIKULI ACTION String.
     */
    public static final String SIKULI_KEYPRESS = "type";
    public static final String SIKULI_CLICK = "click";
    public static final String SIKULI_DRAGANDDROP = "dragAndDrop";
    public static final String SIKULI_DOUBLECLICK = "doubleClick";
    public static final String SIKULI_RIGHTCLICK = "rightClick";
    public static final String SIKULI_LEFTCLICKPRESS = "mouseDown";
    public static final String SIKULI_LEFTCLICKRELEASE = "mouseUp";
    public static final String SIKULI_MOUSEMOVE = "mouseMove";
    public static final String SIKULI_SWITCHTOWINDOW = "switchToWindow";
    public static final String SIKULI_OPENAPP = "openApp";
    public static final String SIKULI_CLOSEAPP = "closeApp";
    public static final String SIKULI_SWITCHAPP = "switchApp";
    public static final String SIKULI_TYPE = "paste";
    public static final String SIKULI_WAIT = "wait";
    public static final String SIKULI_WAITVANISH = "waitVanish";
    public static final String SIKULI_MOUSEOVER = "mouseOver";
    public static final String SIKULI_VERIFYELEMENTPRESENT = "exists";
    public static final String SIKULI_VERIFYELEMENTNOTPRESENT = "notExists";
    public static final String SIKULI_VERIFYTEXTINPAGE = "findText";
    public static final String SIKULI_CAPTURE = "capture";
    public static final String SIKULI_ENDEXECUTION = "endExecution";

    public static final String SIKULI_IDENTIFIER_PICTURE = "picture";
    public static final String SIKULI_IDENTIFIER_TEXT = "text";

    private static final String SIKULI_EXECUTEACTION_PATH = "/extra/ExecuteSikuliAction";

    private JSONObject generatePostParameters(String action, String locator, String locator2, String text, String text2,
            long defaultWait, String minSimilarity, Integer highlightElement, String typeDelay) throws JSONException, IOException, MalformedURLException, MimeTypeException {
        JSONObject result = new JSONObject();
        String picture = "";
        String extension = "";
        int xOffset = 0;
        int yOffset = 0;
        String picture2 = "";
        String extension2 = "";
        int xOffset2 = 0;
        int yOffset2 = 0;
        /**
         * Get Picture from URL and convert to Base64
         */
        JSONObject pic = getContentBase64FromLocator(locator);
        JSONObject pic2 = getContentBase64FromLocator(locator2);

        if (pic != null) {
            picture = pic.getString("content");
            extension = pic.getString("extension");
            xOffset = pic.getInt("xOffset");
            yOffset = pic.getInt("yOffset");
        }
        if (pic2 != null) {
            picture2 = pic2.getString("content");
            extension2 = pic2.getString("extension");
            xOffset2 = pic2.getInt("xOffset");
            yOffset2 = pic2.getInt("yOffset");
        }

        /**
         * Build JSONObject with parameters action : Action expected to be done
         * by Sikuli picture : Picture in Base64 format text : Text to type
         * defaultWait : Timeout for the action pictureExtension : Extension for
         * Base64 decoding
         */
        result.put("action", action);
        result.put("picture", picture);
        result.put("text", text);
        result.put("xOffset", xOffset);
        result.put("yOffset", yOffset);
        result.put("picture2", picture2);
        result.put("text2", text2);
        result.put("xOffset2", xOffset2);
        result.put("yOffset2", yOffset2);
        result.put("defaultWait", defaultWait);
        result.put("pictureExtension", extension);
        result.put("picture2Extension", extension2);
        result.put("minSimilarity", minSimilarity);
        result.put("highlightElement", highlightElement);
        result.put("typeDelay", typeDelay);
        return result;
    }

    private JSONObject getContentBase64FromLocator(String locator) {
        String extension = "";
        String picture = "";
        int xOffset = 0;
        int yOffset = 0;
        String xOffsetS = null;
        String yOffsetS = null;

        JSONObject result = new JSONObject();
        if (locator != null && !"".equals(locator)) {
            URL url;
            try {
                url = new URL(locator);

                // Get the x and y Offsets from URL.
                String[] offsets = locator.split("#");
                if (offsets.length > 1) {
                    String offsetsR = offsets[offsets.length - 1];
                    String[] offsetsRs = offsetsR.split("\\|");
                    for (String offsetsR1 : offsetsRs) {
                        if (offsetsR1.contains("xoffset=")) {
                            xOffsetS = offsetsR1.replace("xoffset=", "");
                        }
                        if (offsetsR1.contains("yoffset=")) {
                            yOffsetS = offsetsR1.replace("yoffset=", "");
                        }
                    }
                    if (!StringUtil.isEmptyOrNull(xOffsetS)) {
                        try {
                            xOffset = Integer.valueOf(xOffsetS);
                        } catch (NumberFormatException e) {
                            LOG.warn("Failed to convert xOffset : {}", xOffsetS, e);
                        }
                    }
                    if (!StringUtil.isEmptyOrNull(yOffsetS)) {
                        try {
                            yOffset = Integer.valueOf(yOffsetS);
                        } catch (NumberFormatException e) {
                            LOG.warn("Failed to convert xOffset : {}", yOffsetS, e);
                        }
                    }
                }

                URLConnection connection = url.openConnection();

                InputStream istream = new BufferedInputStream(connection.getInputStream());

                /**
                 * Get the MimeType and the extension
                 */
                String mimeType = URLConnection.guessContentTypeFromStream(istream);
                MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
                MimeType mt = allTypes.forName(mimeType);
                extension = mt.getExtension();

                /**
                 * Encode in Base64
                 */
                byte[] bytes = IOUtils.toByteArray(istream);
                picture = Base64.encodeBase64URLSafeString(bytes);

            } catch (MalformedURLException ex) {
                picture = "";
            } catch (IOException ex) {
                LOG.error(ex, ex);
            } catch (MimeTypeException ex) {
                LOG.error(ex, ex);
            } finally {
                try {
                    result.put("content", picture);
                    result.put("extension", extension);
                    result.put("xOffset", xOffset);
                    result.put("yOffset", yOffset);
                    return result;
                } catch (JSONException ex) {
                    LOG.error(ex, ex);
                }

            }
        }
        return null;

    }

    @Override
    public boolean isSikuliServerReachableOnRobot(Session session) {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        URL url;
        String urlToConnect = generateSikuliUrlOnRobot(session, SIKULI_EXECUTEACTION_PATH);
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            connection = (HttpURLConnection) url.openConnection();
            LOG.debug("Trying to connect to: {}.", urlToConnect);

            if (connection != null) {
                LOG.debug("Answer from Server: {}", connection.getResponseCode());
            }

            if (connection == null || connection.getResponseCode() != 200) {
                return false;
            }
        } catch (ConnectException exception) { //Handle Sikuli not reachable with Selenium 4
            LOG.info("Robot extension not reachable at '{}'.", urlToConnect);
            return false;
        } catch (IOException ex) {
            LOG.warn(ex);
            return false;
        } finally {
            if (os != null) {
                os.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }

    @Override
    public boolean isSikuliServerReachableOnNode(Session session) {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        URL url;
        String urlToConnect = generateSikuliUrlOnNode(session, SIKULI_EXECUTEACTION_PATH);
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            LOG.debug("Trying to connect to: {}", urlToConnect);

            if (connection != null) {
                LOG.debug("Answer from Server: {}", connection.getResponseCode());
            }

            if (connection == null || connection.getResponseCode() != 200) {
                LOG.warn("Response code different from 200 when calling '{}'", urlToConnect);
                return false;
            }
        } catch (ConnectException exception) { //Handle Sikuli not reachable with Selenium 4
            LOG.info("Robot extension not reachable at '{}'.", urlToConnect);
            return false;
        } catch (IOException ex) {
            LOG.warn("Exception catch when calling '{}' {}", urlToConnect, ex, ex);
            return false;
        } finally {
            if (os != null) {
                os.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }

    @Override
    public AnswerItem<JSONObject> doSikuliAction(Session session, String action, String locator, String locator2, String text, String text2) {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        StringBuilder response = new StringBuilder();
        URL url;
        String urlToConnect = generateSikuliUrlOnNode(session, SIKULI_EXECUTEACTION_PATH);
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            if (session.getExecutorExtensionProxyPort() > 0) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(session.getHost(), session.getExecutorExtensionProxyPort()));

                LOG.info("Open Connection to Robot Node Sikuli (using proxy : {}:{}) : {}", session.getHost(), session.getExecutorExtensionProxyPort(), urlToConnect);
                connection = (HttpURLConnection) url.openConnection(proxy);

            } else {
                LOG.info("Open Connection to Robot Node Sikuli : {}", urlToConnect);
                connection = (HttpURLConnection) url.openConnection();
            }
            // We let Sikuli extension the sikuli timeout + 60 s to perform the action/control.
            connection.setReadTimeout(session.getCerberus_sikuli_wait_element() + 60000);
            connection.setConnectTimeout(session.getCerberus_sikuli_wait_element() + 60000);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            JSONObject postParameters = generatePostParameters(action, locator, locator2, text, text2,
                    session.getCerberus_sikuli_wait_element(),
                    session.getCerberus_sikuli_minSimilarity(),
                    session.getCerberus_selenium_highlightElement(),
                    session.getCerberus_sikuli_typeDelay()
            );
            connection.setDoOutput(true);

            // Send post request
            os = new PrintStream(connection.getOutputStream());
            LOG.debug("Sending JSON : {}", postParameters.toString(1));
            os.println(postParameters.toString());
//            os.println("|ENDS|");

            if (connection == null) {
                LOG.warn("No response from Robot Node Sikuli !!");
            } else {
                LOG.debug("Robot Node Sikuli http response status code : {}", connection.getResponseCode());
            }

            if (connection == null || connection.getResponseCode() != 200) {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_ROBOTEXTENSION_SERVER_NOT_REACHABLE);
            }

            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine = "";

            /**
             * Wait here until all data received
             */
            while (inputLine != null) {
                inputLine = in.readLine();
                if (inputLine != null) {
                    response.append(inputLine);
                } else {
                    break;
                }
            }

            LOG.debug("Robot Node Sikuli Answer: {}", response.toString());

            if (response.toString() != null && response.length() > 0) {
                /**
                 * Convert received string into JSONObject
                 */
                JSONObject objReceived = new JSONObject(response.toString());
                answer.setItem(objReceived);
                if (objReceived.has("status")) {
                    if ("OK".equals(objReceived.getString("status"))) {
                        msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                    } else if ("KO".equals(objReceived.getString("status"))) {
                        msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_BUTRETURNEDKO);
                        msg.resolveDescription("DETAIL", "");
                    } else {
                        if (objReceived.has("message") && !StringUtil.isEmptyOrNull(objReceived.getString("message"))) {
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL);
                            msg.resolveDescription("DETAIL", objReceived.getString("message"));
                        } else {
                            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
                        }
                    }
                } else {
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL).resolveDescription("DETAIL", "Sikuli Extention returned an invalid answer !! (Missing status information)");
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_WITHDETAIL).resolveDescription("DETAIL", "Sikuli Extention returned an invalid answer !! (empty answer)");

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
            LOG.warn("Exception when converting response to JSON : {}", response.toString(), ex);
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
    public MessageEvent doSikuliActionOpenApp(Session session, String appName) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_OPENAPP, null, null, appName, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_OPENAPP);
            message.setDescription(message.getDescription().replace("%APP%", appName));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_OPENAPP);
            mes.setDescription(mes.getDescription().replace("%STRING1%", appName) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionCloseApp(Session session, String appName) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_CLOSEAPP, null, null, appName, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLOSEAPP);
            message.setDescription(message.getDescription().replace("%APP%", appName));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLOSEAPP);
            mes.setDescription(mes.getDescription().replace("%STRING1%", appName) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionClick(Session session, String locator, String text) {

        AnswerItem<JSONObject> actionResult = null;
        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_CLICK, locator, null, "", "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_CLICK, null, null, text, "");
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replace("%ELEMENTFOUND%", "At least 1 element found"));
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionDragAndDrop(Session session, Identifier identifierDrag, Identifier identifierDrop) {

        AnswerItem<JSONObject> actionResult = null;

        if (SIKULI_IDENTIFIER_PICTURE.equals(identifierDrag.getIdentifier())
                && SIKULI_IDENTIFIER_PICTURE.equals(identifierDrop.getIdentifier())) {
            actionResult = doSikuliAction(session, this.SIKULI_DRAGANDDROP, identifierDrag.getLocator(), identifierDrop.getLocator(), "", "");
        } else if (SIKULI_IDENTIFIER_PICTURE.equals(identifierDrag.getIdentifier())
                && SIKULI_IDENTIFIER_TEXT.equals(identifierDrop.getIdentifier())) {
            actionResult = doSikuliAction(session, this.SIKULI_DRAGANDDROP, identifierDrag.getLocator(), null, "", identifierDrop.getLocator());
        } else if (SIKULI_IDENTIFIER_TEXT.equals(identifierDrag.getIdentifier())
                && SIKULI_IDENTIFIER_PICTURE.equals(identifierDrop.getIdentifier())) {
            actionResult = doSikuliAction(session, this.SIKULI_DRAGANDDROP, null, identifierDrop.getLocator(), identifierDrag.getLocator(), "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_DRAGANDDROP, null, null, identifierDrag.getLocator(), identifierDrop.getLocator());
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DRAGANDDROP);
            message.setDescription(message.getDescription().replace("%SOURCE%", identifierDrag.getLocator()));
            message.setDescription(message.getDescription().replace("%TARGET%", identifierDrop.getLocator()));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_DRAGANDDROPSIKULI_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription() + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionRightClick(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_RIGHTCLICK, locator, null, "", "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_RIGHTCLICK, null, null, text, "");
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_RIGHTCLICK);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_RIGHTCLICK_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionLeftButtonPress(Session session) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_LEFTCLICKPRESS, null, null, "", "");
        return actionResult.getResultMessage();

    }

    @Override
    public MessageEvent doSikuliActionLeftButtonRelease(Session session) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_LEFTCLICKRELEASE, null, null, "", "");
        return actionResult.getResultMessage();

    }

    @Override
    public MessageEvent doSikuliActionMouseMove(Session session, String xyoffset) {
        AnswerItem<JSONObject> actionResult = null;

        if (StringUtil.isNotEmptyOrNull(xyoffset)) {
            actionResult = doSikuliAction(session, this.SIKULI_MOUSEMOVE, null, null, xyoffset, "");
        } else {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEMOVE);
            message.resolveDescription("%COORD%", "0,0");
            return message;
        }

        if (actionResult == null || actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEMOVE);
            message.resolveDescription("COORD", xyoffset);
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEMOVE);
            mes.resolveDescription("ERROR", actionResult.getMessageDescription());
            mes.resolveDescription("COORD", xyoffset);
            return mes;
        }

        return actionResult.getResultMessage();

    }

    @Override
    public MessageEvent doSikuliActionSwitchApp(Session session, String locator) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_SWITCHAPP, locator, null, "", "");
        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionDoubleClick(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_DOUBLECLICK, locator, null, "", "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_DOUBLECLICK, null, null, text, "");
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_DOUBLECLICK);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_DOUBLECLICK_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionType(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_TYPE, locator, null, text, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
            message.setDescription(message.getDescription().replace("%ELEMENTFOUND%", "At least 1 element found"));
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            message.setDescription(message.getDescription().replace("%DATA%", text));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionMouseOver(Session session, String locator, String text, String offset) {
        AnswerItem<JSONObject> actionResult = null;

        // We check here that offxet format is correct and report an error if invalid.
        if (StringUtil.isNotEmptyOrNull(offset)) {
            try {
                Integer[] offsetInt = StringUtil.getxFromOffset(offset);
            } catch (Exception e) {
                MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_OFFSETFORMAT);
                mes.setDescription(mes.getDescription().replace("%OFFSET%", offset));
                return mes;
            }
        }

        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_MOUSEOVER, locator, null, "", "");
            if (StringUtil.isNotEmptyOrNull(offset)) {
                actionResult = doSikuliAction(session, this.SIKULI_MOUSEMOVE, null, null, offset, "");
            }
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_MOUSEOVER, null, null, text, "");
            if (StringUtil.isNotEmptyOrNull(offset)) {
                actionResult = doSikuliAction(session, this.SIKULI_MOUSEMOVE, null, null, offset, "");
            }
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            message.setDescription(message.getDescription().replace("%OFFSET%", offset));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            mes.setDescription(mes.getDescription().replace("%OFFSET%", offset));
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionWait(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_WAIT, locator, null, "", "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_WAIT, null, null, text, "");
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAIT_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_WAIT_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionWaitVanish(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.isEmpty()) {
            actionResult = doSikuliAction(session, this.SIKULI_WAITVANISH, locator, null, "", "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_WAITVANISH, null, null, text, "");
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_WAITVANISH_ELEMENT);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_RIGHTCLICK_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionKeyPress(Session session, String locator, String textToKey, String modifier) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_KEYPRESS, locator, null, textToKey, modifier);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS)
                    .resolveDescription("ELEMENT", locator)
                    .resolveDescription("KEY", textToKey)
                    .resolveDescription("MODIFIER", modifier);
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER)
                    .resolveDescription("ELEMENT", locator)
                    .resolveDescription("KEY", textToKey)
                    .resolveDescription("MODIFIER", modifier)
                    .resolveDescription("REASON", actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyElementPresent(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, SikuliService.SIKULI_VERIFYELEMENTPRESENT, locator, null, text, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
            message.setDescription(message.getDescription().replace("%STRING1%", locator == null ? text : locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS_BUTRETURNEDKO).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
            mes.setDescription(mes.getDescription().replace("%STRING1%", locator == null ? text : locator));
            return mes;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
            mes.setDescription(mes.getDescription().replace("%ERROR%", locator == null ? text : locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyElementNotPresent(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_VERIFYELEMENTNOTPRESENT, locator, null, text, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
            message.setDescription(message.getDescription().replace("%STRING1%", locator == null ? text : locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS_BUTRETURNEDKO).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
            message.setDescription(message.getDescription().replace("%STRING1%", locator == null ? text : locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_GENERIC);
            mes.setDescription(mes.getDescription().replace("%ERROR%", locator == null ? text : locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyTextInPage(Session session, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_VERIFYTEXTINPAGE, null, null, text, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_TEXTINPAGE);
            message.setDescription(message.getDescription().replace("%STRING1%", text));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_TEXTINPAGE);
            mes.setDescription(mes.getDescription().replace("%STRING1%", text) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliEndExecution(Session session) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, SikuliService.SIKULI_ENDEXECUTION, null, null, "", "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_ENDEXECUTION);
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_ENDEXECUTION);
            mes.resolveDescription("DETAIL", actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public File takeScreenShotFile(Session session) {
        File image = null;
        long timeout = System.currentTimeMillis() + (session.getCerberus_selenium_wait_element());

        try {
            AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_CAPTURE, null, null, "", "");
            String screenshotInBase64 = actionResult.getItem().getString("screenshot");
            byte[] data = Base64.decodeBase64(screenshotInBase64);

            image = new File(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_exeautomedia_path, "", File.separator + "tmp") + File.separator + "tmp" + File.separator
                    + "screenshotsikuli-" + UUID.randomUUID().toString().subSequence(0, 13) + ".png");
            FileUtils.writeByteArrayToFile(image, data);

            if (image != null) {
                //logs for debug purposes
                LOG.info("Screenshot taken with success: {} (size : {} b)", image.getName(), image.length());
            } else {
                LOG.warn("Screenshot returned null: ");
            }

        } catch (JSONException ex) {
            LOG.warn(ex, ex);
        } catch (IOException ex) {
            LOG.warn(ex, ex);
        }
        return image;
    }

    private String generateSikuliUrlOnRobot(Session session, String path) {
        int port = session.getExecutorExtensionPort() != 0 ? session.getExecutorExtensionPort() : Integer.parseInt(session.getPort());
        return String.format("%s:%d%s", StringUtil.cleanHostURL(session.getHost()), port, path);
    }

    private String generateSikuliUrlOnNode(Session session, String path) {
        int port = session.getExecutorExtensionPort() != 0 ? session.getExecutorExtensionPort() : Integer.parseInt(session.getNodePort());
        return String.format("%s:%d%s", StringUtil.cleanHostURL(session.getNodeHost()), port, path);
    }

}
