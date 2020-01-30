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
package org.cerberus.service.ftp.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.ftp.IFtpService;
import org.cerberus.service.proxy.IProxyService;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.Authenticator;
import org.cerberus.util.StringUtil;

/**
 *
 * @author ryltar
 */
@Service
public class FtpService implements IFtpService {

    private static final Logger LOG = LogManager.getLogger(FtpService.class);

    @Autowired
    private IFactoryAppService factoryAppService;
    @Autowired
    private IRecorderService recorderService;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;
    @Autowired
    IParameterService parameterService;
    @Autowired
    IProxyService proxyService;

    /**
     * Proxy default config. (Should never be used as default config is inserted
     * into database)
     */
    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    @Override
    public HashMap<String, String> fromFtpStringToHashMap(String ftpChain) {
        HashMap<String, String> map = new HashMap<String, String>();
        String tmp;
        Matcher accountMatcher = Pattern.compile("(\\/\\/|\\\\\\\\)(.*)@").matcher(ftpChain);
        Matcher hostMatcher = Pattern.compile("\\@([^\\/|\\\\]*)").matcher(ftpChain);
        Matcher pathMatcher = Pattern.compile("(\\/|\\\\)([^:]+)$").matcher(ftpChain);

        if (accountMatcher.find() && hostMatcher.find() && pathMatcher.find()) {
            try {
                tmp = accountMatcher.group(2);
                String[] account = tmp.split(":", 2);
                map.put("pseudo", account[0]);
                map.put("password", account[1]);
                tmp = hostMatcher.group(1);
                String[] fullHost = tmp.split(":", 2);
                map.put("host", fullHost[0]);
                map.put("port", fullHost[1]);
                map.put("path", pathMatcher.group(0));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error("Exception when parsing ftp url.", e);
            }

        }
        return map;
    }

    @Override
    public void setProxy(FTPClient client, String system, AppService myResponse) {
        String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", "",
                DEFAULT_PROXY_HOST);
        int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", "",
                DEFAULT_PROXY_PORT);

        myResponse.setProxy(true);
        myResponse.setProxyHost(proxyHost);
        myResponse.setProxyPort(proxyPort);

        SocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
        if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system,
                DEFAULT_PROXYAUTHENT_ACTIVATE)) {
            Authenticator.setDefault(
                    new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", "", DEFAULT_PROXYAUTHENT_USER);
                    String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", "", DEFAULT_PROXYAUTHENT_PASSWORD);
                    myResponse.setProxyWithCredential(true);
                    myResponse.setProxyUser(proxyUser);
                    return new PasswordAuthentication(
                            proxyUser, proxyPassword.toCharArray()
                    );
                }
            }
            );
        }
        client.setProxy(proxy);
    }

    public AnswerItem<AppService> callFTP(String chain, String system, String content, String method, String filePath, String service) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        HashMap<String, String> informations = this.fromFtpStringToHashMap(chain);

        if (informations.size() <= 4) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", chain));
            message.setDescription(
                    message.getDescription().replace("%DESCRIPTION%", "Ftp url bad format! you missed something. please modify ftp url with correct syntax"));
            result.setResultMessage(message);
            return result;
        }

        FTPClient ftp = new FTPClient();
        AppService myResponse = factoryAppService.create(service, AppService.TYPE_FTP,
                method, "", "", content, "", "", "", "", "", informations.get("path"), "", "", "", null, "", null, filePath);

        try {
            if (proxyService.useProxy(StringUtil.getURLFromString(informations.get("host"), "", "", "ftp://"), system)) {
                this.setProxy(ftp, system, myResponse);
            }
            ftp.connect(informations.get("host"), Integer.valueOf(informations.get("port")));
            boolean logged = ftp.login(informations.get("pseudo"), informations.get("password"));
            if (!logged) {
                LOG.error("Exception when logging to ftp server.");
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
                message.setDescription(
                        message.getDescription().replace("%DESCRIPTION%", "Error on logging to FTP Server"));
                result.setResultMessage(message);
                return result;
            } else {
                LOG.info("Successfully logged to the ftp server");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            if (method.equals("GET")) {
                result = this.getFTP(informations, ftp, myResponse);
            } else {
                result = this.postFTP(informations, ftp, myResponse);
            }

        } catch (Exception e) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
            message.setDescription(
                    message.getDescription().replace("%DESCRIPTION%", "Error on CallFTP : " + e.toString()));
            result.setResultMessage(message);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException e) {
                    LOG.warn(e.toString());
                }
            }
        }
        return result;
    }

    @Override
    public AnswerItem<AppService> getFTP(HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        LOG.info("Start retrieving ftp file");
        FTPFile[] ftpFile = ftp.listFiles(informations.get("path"));
        if (ftpFile.length != 0) {
            InputStream done = ftp.retrieveFileStream(informations.get("path"));
            boolean success = ftp.completePendingCommand();
            myResponse.setResponseHTTPCode(ftp.getReplyCode());
            if (success && FTPReply.isPositiveCompletion(myResponse.getResponseHTTPCode())) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while ((n = done.read(buf)) >= 0) {
                    baos.write(buf, 0, n);
                }
                byte[] content = baos.toByteArray();
                myResponse.setFile(content);
                LOG.info("ftp file successfully retrieve");
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
                message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", "GET"));
                message.setDescription(message.getDescription().replace("%SERVICEPATH%", informations.get("path")));
                result.setResultMessage(message);
                String expectedContent = IOUtils.toString(new ByteArrayInputStream(content), "UTF-8");
                String extension = testCaseExecutionFileService.checkExtension(informations.get("path"), "");
                if ("JSON".equals(extension) || "XML".equals(extension) || "TXT".equals(extension)) {
                    myResponse.setResponseHTTPBody(expectedContent);
                }
                myResponse.setResponseHTTPBodyContentType(extension);
                result.setItem(myResponse);
                baos.close();
            } else {
                LOG.error("Error when downloading the file. Something went wrong");
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
                message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                        "Error when downloading the file. Something went wrong"));
                result.setResultMessage(message);
            }
            done.close();
        } else {
            LOG.error("The file is not present on FTP server. Please check the FTP path");
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                    "Impossible to retrieve the file. Please check the FTP path"));
            result.setResultMessage(message);
        }
        return result;
    }

    @Override
    public AnswerItem<AppService> postFTP(HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        InputStream inputStream = null;
        byte[] byteContent = null;
        LOG.info("Start retrieving ftp file");
        if (!myResponse.getServiceRequest().isEmpty()) {
            inputStream = new ByteArrayInputStream(myResponse.getServiceRequest().getBytes("UTF-8"));
            byteContent = IOUtils.toByteArray(inputStream);
            inputStream.close();
        } else if (!myResponse.getFileName().isEmpty()) {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    "cerberus_ftpfile_path Parameter not found");
            AnswerItem<Parameter> a = parameterService.readByKey("", "cerberus_ftpfile_path");
            if (a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                Parameter p = (Parameter) a.getItem();
                String uploadPath = p.getValue();
                Path path = Paths.get(uploadPath + File.separator + myResponse.getService() + File.separator + myResponse.getFileName());
                byteContent = Files.readAllBytes(path);
            } else {
                result.setResultMessage(msg);
                return result;
            }
        } else {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    "file path and service request are null ! you need to specify one of this field");
            result.setResultMessage(msg);
            return result;
        }
        boolean done = ftp.storeFile(informations.get("path"), new ByteArrayInputStream(byteContent));
        myResponse.setResponseHTTPCode(ftp.getReplyCode());
        if (done) {
            myResponse.setFile(byteContent);
            LOG.info("ftp file successfully sending");
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", "POST"));
            message.setDescription(message.getDescription().replace("%SERVICEPATH%", informations.get("path")));
            result.setResultMessage(message);
            String expectedContent = IOUtils.toString(byteContent, "UTF-8");
            String extension = testCaseExecutionFileService.checkExtension(informations.get("path"), "");
            if (extension == "JSON" || extension == "XML" || extension == "TXT") {
                myResponse.setResponseHTTPBody(expectedContent);
            }
            myResponse.setResponseHTTPBodyContentType(extension);
            result.setItem(myResponse);
        } else {
            LOG.error("Error when uploading the file. Something went wrong");
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
            message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                    "Error when uploading the file. Something went wrong"));
            result.setResultMessage(message);
        }
        return result;
    }
}
