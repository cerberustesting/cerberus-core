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
package org.cerberus.serviceEmail.impl;

import org.apache.commons.mail.HtmlEmail;

/**
 *
 * @author bcivel
 */
public class sendMail {

    public static void sendHtmlMail(String host, int port, String body, String subject, String from, String to, String cc) throws Exception {

        HtmlEmail email = new HtmlEmail();
        email.setSmtpPort(port);
        email.setDebug(false);
        email.setHostName(host);
        email.setFrom(from);
        email.setSubject(subject);
        email.setHtmlMsg(body);


        String[] destinataire = to.split(";");

        for (int i = 0; i < destinataire.length; i++) {
            String name;
            String emailaddress;
            if (destinataire[i].contains("<")){
            String[] destinatairedata = destinataire[i].split("<");
            name = destinatairedata[0].trim();
            emailaddress = destinatairedata[1].replace(">", "").trim();}
            else {
            name = "";
            emailaddress = destinataire[i];
            }
            email.addTo(emailaddress, name);
        }

        String[] copy = cc.split(";");

        for (int i = 0; i < copy.length; i++) {
            String namecc;
            String emailaddresscc;
            if (copy[i].contains("<")){
            String[] copydata = copy[i].split("<");
            namecc = copydata[0].trim();
            emailaddresscc = copydata[1].replace(">", "").trim();
            } else {
            namecc = "";
            emailaddresscc = copy[i];
            }
            email.addCc(emailaddresscc, namecc);
        }

        email.setTLS(true);

        email.send();

    }
}
