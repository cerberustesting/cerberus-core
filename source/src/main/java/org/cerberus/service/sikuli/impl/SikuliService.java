/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.sikuli.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.sikuli.ISikuliService;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
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
    public static final String SIKULI_DOUBLECLICK = "doubleClick";
    public static final String SIKULI_RIGHTCLICK = "rightClick";
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

    public static final String SIKULI_IDENTIFIER_PICTURE = "picture";
    public static final String SIKULI_IDENTIFIER_TEXT = "text";

    private JSONObject generatePostParameters(String action, String locator, String text, long defaultWait) throws JSONException, IOException, MalformedURLException, MimeTypeException {
        JSONObject result = new JSONObject();
        String picture = "";
        String extension = "";
        /**
         * Get Picture from URL and convert to Base64
         */
        if (locator != null && !"".equals(locator)) {
            URL url = new URL(locator);
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
        result.put("defaultWait", defaultWait);
        result.put("pictureExtension", extension);
//        result.put("minSimilarity", parameterService.getParameterStringByKey("cerberus_sikuli_minSimilarity", "", null));
        return result;
    }

    @Override
    public boolean isSikuliServerReachable(Session session) {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        URL url;
        String urlToConnect = "http://" + session.getHost() + ":" + session.getPort() + "/extra/ExecuteSikuliAction";
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            connection = (HttpURLConnection) url.openConnection();

            if (connection == null || connection.getResponseCode() != 200) {
                return false;
            }

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
    public AnswerItem<JSONObject> doSikuliAction(Session session, String action, String locator, String text) {
        AnswerItem<JSONObject> answer = new AnswerItem<JSONObject>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream os = null;

        URL url;
        String urlToConnect = "http://" + session.getHost() + ":" + session.getPort() + "/extra/ExecuteSikuliAction";
        try {
            /**
             * Connect to ExecuteSikuliAction Servlet Through SeleniumServer
             */
            url = new URL(urlToConnect);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            JSONObject postParameters = generatePostParameters(action, locator, text, session.getCerberus_selenium_wait_element());
            connection.setDoOutput(true);

            // Send post request
            os = new PrintStream(connection.getOutputStream());
            os.println(postParameters.toString());
            os.println("|ENDS|");

            if (connection == null || connection.getResponseCode() != 200) {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_SIKULI_SERVER_NOT_REACHABLE);
            }

            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            /**
             * Wait here until receiving |ENDR| String
             */
            while (!(inputLine = in.readLine()).equals("|ENDR|")) {
                response.append(inputLine);
            }

            /**
             * Convert received string into JSONObject
             *
             */
            JSONObject objReceived = new JSONObject(response.toString());
            answer.setItem(objReceived);

            if (objReceived.has("status")) {
                if ("Failed".equals(objReceived.getString("status"))) {
                    msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
                } else {
                    msg = new MessageEvent(MessageEventEnum.ACTION_SUCCESS);
                }
            } else {
                msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
            }
            in.close();
        } catch (MalformedURLException ex) {
            LOG.warn(ex);
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_SIKULI_SERVER_BADURL);
            mes.setDescription(mes.getDescription().replace("%URL%", urlToConnect));
            msg = mes;
        } catch (FileNotFoundException ex) {
            LOG.warn(ex);
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_SIKULI_FILE_NOT_FOUND);
            mes.setDescription(mes.getDescription().replace("%FILE%", locator));
            msg = mes;
        } catch (IOException ex) {
            LOG.warn(ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
        } catch (JSONException ex) {
            LOG.warn(ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
        } catch (MimeTypeException ex) {
            LOG.warn(ex);
            msg = new MessageEvent(MessageEventEnum.ACTION_FAILED);
        } catch (Exception ex) {
            LOG.warn(ex);
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_SIKULI_SERVER_NOT_REACHABLE);
            mes.setDescription(mes.getDescription().replace("%URL%", urlToConnect));
            msg = mes;
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
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_OPENAPP, null, appName);

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
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_CLOSEAPP, null, appName);

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
        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_CLICK, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_CLICK, null, text);
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
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
    public MessageEvent doSikuliActionRightClick(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_RIGHTCLICK, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_RIGHTCLICK, null, text);
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
    public MessageEvent doSikuliActionSwitchApp(Session session, String locator) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_SWITCHAPP, locator, "");
        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionDoubleClick(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_DOUBLECLICK, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_DOUBLECLICK, null, text);
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
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_TYPE, locator, text);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
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
    public MessageEvent doSikuliActionMouseOver(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_MOUSEOVER, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_MOUSEOVER, null, text);
        }

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_MOUSEOVER);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_MOUSEOVER_NO_SUCH_ELEMENT);
            mes.setDescription(mes.getDescription().replace("%ELEMENT%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliActionWait(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = null;

        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_WAIT, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_WAIT, null, text);
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

        if (!locator.equals("")) {
            actionResult = doSikuliAction(session, this.SIKULI_WAITVANISH, locator, "");
        } else {
            actionResult = doSikuliAction(session, this.SIKULI_WAITVANISH, null, text);
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
    public MessageEvent doSikuliActionKeyPress(Session session, String locator, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_KEYPRESS, locator, text);

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_KEYPRESS);
            message.setDescription(message.getDescription().replace("%ELEMENT%", locator));
            message.setDescription(message.getDescription().replace("%DATA%", text));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.ACTION_FAILED_KEYPRESS_OTHER);
            mes.setDescription(mes.getDescription().replace("%KEY%", text).replace("%REASON%", actionResult.getMessageDescription()));
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyElementPresent(Session session, String locator) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_VERIFYELEMENTPRESENT, locator, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_PRESENT);
            message.setDescription(message.getDescription().replace("%STRING1%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_PRESENT);
            mes.setDescription(mes.getDescription().replace("%STRING1%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyElementNotPresent(Session session, String locator) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_VERIFYELEMENTNOTPRESENT, locator, "");

        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_SUCCESS).getCodeString())) {
            MessageEvent message = new MessageEvent(MessageEventEnum.CONTROL_SUCCESS_NOTPRESENT);
            message.setDescription(message.getDescription().replace("%STRING1%", locator));
            return message;
        }
        if (actionResult.getResultMessage().getCodeString().equals(new MessageEvent(MessageEventEnum.ACTION_FAILED).getCodeString())) {
            MessageEvent mes = new MessageEvent(MessageEventEnum.CONTROL_FAILED_NOTPRESENT);
            mes.setDescription(mes.getDescription().replace("%STRING1%", locator) + " - " + actionResult.getMessageDescription());
            return mes;
        }

        return actionResult.getResultMessage();
    }

    @Override
    public MessageEvent doSikuliVerifyTextInPage(Session session, String text) {
        AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_VERIFYTEXTINPAGE, null, text);

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
    public File takeScreenShotFile(Session session) {
        File image = null;
        long timeout = System.currentTimeMillis() + (session.getCerberus_selenium_wait_element());

        try {
            AnswerItem<JSONObject> actionResult = doSikuliAction(session, this.SIKULI_CAPTURE, null, "");
            String screenshotInBase64 = ((JSONObject) actionResult.getItem()).getString("screenshot");
            byte[] data = Base64.decodeBase64(screenshotInBase64);

            image = new File("temp.png");
            FileUtils.writeByteArrayToFile(image, data);

            if (image != null) {
                //logs for debug purposes
                LOG.info("screen-shot taken with succes: " + image.getName() + "(size" + image.length() + ")");
            } else {
                LOG.warn("screen-shot returned null: ");
            }

        } catch (JSONException ex) {
            LOG.warn(ex);
        } catch (IOException ex) {
            LOG.warn(ex);
        }
        return image;
    }

}
