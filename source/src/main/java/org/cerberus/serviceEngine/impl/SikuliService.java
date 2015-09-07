/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.serviceEngine.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import org.cerberus.entity.Identifier;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.entity.Session;
import org.cerberus.serviceEngine.ISikuliService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SikuliService implements ISikuliService{

    @Override
    public MessageEvent doSikuliActionClick(Session session, String url) {
        MessageEvent message;
        try {
            Socket clientSocket = new Socket("localhost", 9001);
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("action", "click");
            obj.put("picture", url);

            os.println(obj.toString());
            os.println("|ENDS|");

            String responseLine;
            while (!(responseLine = is.readLine()).equals("|ENDR|")) {
                System.out.println(responseLine);
            }

            os.close();
            is.close();
            clientSocket.close();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", url));
            return message;

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (JSONException ex) {
            Logger.getLogger(SikuliService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED);
    }

    @Override
    public MessageEvent doSikuliActionWait(Session session, String url) {
        MessageEvent message;
        try {
            Socket clientSocket = new Socket("localhost", 9001);
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("action", "wait");
            obj.put("picture", url);

            os.println(obj.toString());
            os.println("|ENDS|");

            String responseLine;
            while (!(responseLine = is.readLine()).equals("|ENDR|")) {
                System.out.println(responseLine);
            }

            os.close();
            is.close();
            clientSocket.close();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", url));
            return message;

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (JSONException ex) {
            Logger.getLogger(SikuliService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED);
    }

    @Override
    public MessageEvent doSikuliActionType(Session session, String url) {
        MessageEvent message;
        try {
            Socket clientSocket = new Socket("localhost", 9001);
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("action", "paste");
            obj.put("picture", url);

            os.println(obj.toString());
            os.println("|ENDS|");

            String responseLine;
            while (!(responseLine = is.readLine()).equals("|ENDR|")) {
                System.out.println(responseLine);
            }

            os.close();
            is.close();
            clientSocket.close();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", url));
            return message;

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (JSONException ex) {
            Logger.getLogger(SikuliService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED);
    }

    @Override
    public MessageEvent doSikuliActionKeyPress(Session session, String url) {
        MessageEvent message;
        try {
            Socket clientSocket = new Socket("localhost", 9001);
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            PrintStream os = new PrintStream(clientSocket.getOutputStream());

            JSONObject obj = new JSONObject();
            obj.put("action", "type");
            obj.put("picture", url);

            os.println(obj.toString());
            os.println("|ENDS|");

            String responseLine;
            while (!(responseLine = is.readLine()).equals("|ENDR|")) {
                System.out.println(responseLine);
            }

            os.close();
            is.close();
            clientSocket.close();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", url));
            return message;

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch (IOException e) {
            System.out.println("No I/O");
        } catch (JSONException ex) {
            Logger.getLogger(SikuliService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new MessageEvent(MessageEventEnum.ACTION_FAILED);
    }

    @Override
    public MessageEvent doSikuliActionMouseDown(Session session, String locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MessageEvent doSikuliActionMouseUp(Session session, String locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MessageEvent doSikuliActionMouseOver(Session session, String locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MessageEvent doSikuliActionMouseDownMouseUp(Session session, String locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
