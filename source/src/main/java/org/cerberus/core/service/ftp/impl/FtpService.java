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
package org.cerberus.core.service.ftp.impl;

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
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.ftp.IFtpService;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.Authenticator;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.sshd.sftp.client.fs.SftpFileSystemProvider;
import org.cerberus.core.util.StringUtil;

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
    public AnswerItem<AppService> callFTP(String chain, String system, String content, String method, String filePath, String service, int timeOutMs) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        HashMap<String, String> informations = this.fromFtpStringToHashMap(chain);

        if (informations.size() <= 4) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("SERVICENAME", service)
                    .resolveDescription("DESCRIPTION", "FTP url bad format! you missed something. please modify ftp url with correct syntax");
            result.setResultMessage(message);
            return result;
        }

        if (chain.startsWith("sftp://")) {

            return call_SFTP(informations, chain, system, content, method, filePath, service, timeOutMs);

        } else {

            return call_FTP_FTPS(informations, chain, system, content, method, filePath, service, timeOutMs);

        }

    }

    private AnswerItem<AppService> call_SFTP(HashMap<String, String> informations, String chain, String system, String content, String method, String filePath, String service, int timeOutMs) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        LOG.debug("starting SFTP.");

        AppService myResponse = factoryAppService.create(service, AppService.TYPE_FTP,
                method, "", "", "", content, "", "", "", "", "", "", "", informations.get("path"), true, "", "", false, "", false, "", false, "", "", "", null, "", null, filePath);

        URI uri = null;
        try {
            uri = new URI("sftp://" + informations.get("pseudo") + ":" + informations.get("password") + "@" + informations.get("host") + ":" + informations.get("port") + "/");

            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap(), new SftpFileSystemProvider().getClass().getClassLoader())) {

                LOG.info("Successfully logged to the sftp server");

                if (method.equals("GET")) {
                    result = this.getSFTP(informations, fs, myResponse);
                } else {
                    result = this.postSFTP(informations, fs, myResponse);
                }

            } catch (Exception e) {
                LOG.error("Exception when logging to sftp server.", e);
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICENAME", service)
                        .resolveDescription("DESCRIPTION", "Error on CallFTP '" + informations.get("host") + ":" + informations.get("port") + "' : " + e.toString());
                result.setResultMessage(message);
            }

        } catch (URISyntaxException ex) {
            LOG.error(ex, ex);
        }
        result.getResultMessage()
                .resolveDescription("SERVICENAME", service);
        return result;
    }

    private AnswerItem<AppService> call_FTP_FTPS(HashMap<String, String> informations, String chain, String system, String content, String method, String filePath, String service, int timeOutMs) {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();

        FTPClient ftp;
        if (chain.startsWith("ftps://")) {
            ftp = new FTPSClient();
        } else {
            ftp = new FTPClient();
        }

        AppService myResponse = factoryAppService.create(service, AppService.TYPE_FTP,
                method, "", "", "", content, "", "", "", "", "", "", "", informations.get("path"), true, "", "", false, "", false, "", false, "", "", "", null, "", null, filePath);

        try {
            if (proxyService.useProxy(StringUtil.getURLFromString(informations.get("host"), "", "", "ftp://"), system)) {
                this.setProxy(ftp, system, myResponse);
            }
            LOG.debug("Timeout : " + timeOutMs);
            ftp.setDefaultTimeout(timeOutMs);
            ftp.setConnectTimeout(timeOutMs);
            ftp.connect(informations.get("host"), Integer.valueOf(informations.get("port")));
            boolean logged = ftp.login(informations.get("pseudo"), informations.get("password"));
            if (!logged) {
                LOG.error("Exception when logging to ftp server.");
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICENAME", service)
                        .resolveDescription("DESCRIPTION", "Error on logging to FTP Server using login : '" + informations.get("pseudo") + "'");
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
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("SERVICENAME", service)
                    .resolveDescription("DESCRIPTION", "Error on CallFTP '" + informations.get("host") + ":" + informations.get("port") + "' : " + e.toString());
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
        result.getResultMessage()
                .resolveDescription("SERVICENAME", service);
        return result;
    }

    private AnswerItem<AppService> getFTP(HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        LOG.info("Start retrieving ftp file");
        FTPFile[] ftpFile = ftp.listFiles(informations.get("path"));
        String fileList = "";
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
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE)
                        .resolveDescription("SERVICEMETHOD", "GET")
                        .resolveDescription("SERVICEPATH", informations.get("path"));
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
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                        .resolveDescription("SERVICE", informations.get("path"))
                        .resolveDescription("DESCRIPTION", "Error when downloading the file. Something went wrong");
                result.setResultMessage(message);
            }
            done.close();
        } else {
            LOG.error("The file '" + informations.get("path") + "' is not present on FTP server. Please check the FTP path");
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("SERVICE", informations.get("path"))
                    .resolveDescription("DESCRIPTION", "Impossible to retrieve the file. Please check the FTP path : '" + informations.get("path") + "'");
            result.setResultMessage(message);
        }
        return result;
    }

    private AnswerItem<AppService> postFTP(HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        InputStream inputStream = null;
        byte[] byteContent = null;
        String targetPathFilename = informations.get("path");
        LOG.info("Start posting new ftp file");
        if (!myResponse.getServiceRequest().isEmpty()) {
            // Posting from service content.
            inputStream = new ByteArrayInputStream(myResponse.getServiceRequest().getBytes("UTF-8"));
            byteContent = IOUtils.toByteArray(inputStream);
            inputStream.close();
        } else if (!myResponse.getFileName().isEmpty()) {
            // Posting from file.
            String uploadPath = parameterService.getParameterStringByKey("cerberus_ftpfile_path", "", null);
            Path path = Paths.get(uploadPath + File.separator + myResponse.getService() + File.separator + myResponse.getFileName());
            byteContent = Files.readAllBytes(path);
            if (targetPathFilename.endsWith("/")) {
                targetPathFilename += myResponse.getFileName();
            }
        } else {
            LOG.info("no file to upload");
            MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "No data to upload. You need to specify either a file content on service request or file to upload");
            result.setResultMessage(msg);
            return result;
        }
        boolean done = ftp.storeFile(targetPathFilename, new ByteArrayInputStream(byteContent));
        myResponse.setResponseHTTPCode(ftp.getReplyCode());
        if (done) {
            myResponse.setFile(byteContent);
            LOG.info("ftp file successfully posted to " + targetPathFilename);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE)
                    .resolveDescription("SERVICEMETHOD", "POST")
                    .resolveDescription("SERVICEPATH", targetPathFilename);
            result.setResultMessage(message);
            String expectedContent = IOUtils.toString(byteContent, "UTF-8");
            String extension = testCaseExecutionFileService.checkExtension(targetPathFilename, "");
            if ("JSON".equals(extension) || "XML".equals(extension) || "TXT".equals(extension)) {
                myResponse.setResponseHTTPBody(expectedContent);
            }
            myResponse.setResponseHTTPBodyContentType(extension);
            result.setItem(myResponse);
        } else {
            LOG.error("Error when uploading the ftp file to " + targetPathFilename);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "Error when uploading to '" + targetPathFilename + "'. Something went wrong that prevent the file to be uploaded (maybe target does not exist or is a folder)");
            result.setResultMessage(message);
        }
        return result;
    }

    private AnswerItem<AppService> getSFTP(HashMap<String, String> informations, FileSystem fs, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        LOG.info("Start retrieving sftp file ");
        String remoteFilePath = informations.get("path");
        if (remoteFilePath.startsWith("/")) {
            remoteFilePath = remoteFilePath.substring(1);
        }
        Path remotePath = fs.getPath(remoteFilePath);
        try {

            byte[] byteArray = Files.readAllBytes(remotePath);
            LOG.info("sftp file successfully retrieve");
            myResponse.setFile(byteArray);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE)
                    .resolveDescription("SERVICEMETHOD", "GET")
                    .resolveDescription("SERVICEPATH", informations.get("path"));
            result.setResultMessage(message);
            String expectedContent = IOUtils.toString(new ByteArrayInputStream(byteArray), "UTF-8");
            String extension = testCaseExecutionFileService.checkExtension(informations.get("path"), "");
            if ("JSON".equals(extension) || "XML".equals(extension) || "TXT".equals(extension)) {
                myResponse.setResponseHTTPBody(expectedContent);
            }
            myResponse.setResponseHTTPBodyContentType(extension);
            result.setItem(myResponse);

        } catch (Exception e) {
            LOG.error("Error when downloading the sftp file. Something went wrong", e);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "Impossible to retrieve the file. Please check the SFTP path : '" + informations.get("path") + "' " + e.toString());
            result.setResultMessage(message);
        }
        return result;
    }

    private AnswerItem<AppService> postSFTP(HashMap<String, String> informations, FileSystem fs, AppService myResponse) throws IOException {
        MessageEvent message = null;
        AnswerItem<AppService> result = new AnswerItem<>();
        InputStream inputStream = null;
        byte[] byteContent = null;
        String targetPathFilename = informations.get("path");
        LOG.info("Start posting new sftp file");
        if (!myResponse.getServiceRequest().isEmpty()) {
            // Posting from service content.
            inputStream = new ByteArrayInputStream(myResponse.getServiceRequest().getBytes("UTF-8"));
            byteContent = IOUtils.toByteArray(inputStream);
            inputStream.close();
        } else if (!myResponse.getFileName().isEmpty()) {
            // Posting from file.
            String uploadPath = parameterService.getParameterStringByKey("cerberus_ftpfile_path", "", null);
            Path path = Paths.get(uploadPath + File.separator + myResponse.getService() + File.separator + myResponse.getFileName());
            byteContent = Files.readAllBytes(path);
            if (targetPathFilename.endsWith("/")) {
                targetPathFilename += myResponse.getFileName();
            }
        } else {
            LOG.info("no sftp file to upload");
            MessageEvent msg = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "No data to upload. You need to specify either a file content on service request or file to upload");
            result.setResultMessage(msg);
            return result;
        }

        if (targetPathFilename.startsWith("/")) {
            targetPathFilename = targetPathFilename.substring(1);
        }

        Path remotePath = fs.getPath(targetPathFilename);

        try {

            Files.write(remotePath, byteContent, StandardOpenOption.CREATE);
            myResponse.setFile(byteContent);
            LOG.info("sftp file successfully posted to " + targetPathFilename);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE)
                    .resolveDescription("SERVICEMETHOD", "POST")
                    .resolveDescription("SERVICEPATH", targetPathFilename);
            result.setResultMessage(message);
            String expectedContent = IOUtils.toString(byteContent, "UTF-8");
            String extension = testCaseExecutionFileService.checkExtension(targetPathFilename, "");
            if ("JSON".equals(extension) || "XML".equals(extension) || "TXT".equals(extension)) {
                myResponse.setResponseHTTPBody(expectedContent);
            }
            myResponse.setResponseHTTPBodyContentType(extension);
            result.setItem(myResponse);

        } catch (Exception e) {
            LOG.error("Error when uploading the sftp file to " + targetPathFilename, e);
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE)
                    .resolveDescription("DESCRIPTION", "Error when uploading to '" + targetPathFilename + "'. Something went wrong that prevent the file to be uploaded (maybe target does not exist or is a folder)");
            result.setResultMessage(message);
        }

        return result;
    }

    private HashMap<String, String> fromFtpStringToHashMap(String ftpChain) {
        HashMap<String, String> map = new HashMap<>();
        String tmp;
        Matcher accountMatcher = Pattern.compile("(\\/\\/|\\\\\\\\)(.*)@").matcher(ftpChain);
        Matcher hostMatcher = Pattern.compile("\\@([^\\/|\\\\]*)").matcher(ftpChain);
        Matcher pathMatcher = Pattern.compile("(\\/|\\\\)([^:]+)$").matcher(ftpChain);
        LOG.debug("FTP info :");
        LOG.debug(ftpChain);

        if (accountMatcher.find()) {
            try {
                tmp = accountMatcher.group(2);
                String[] account = tmp.split(":", 2);
                map.put("pseudo", account[0]);
                map.put("password", account[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error("Exception when parsing ftp url.", e);
            }

        }
        if (hostMatcher.find()) {
            try {
                tmp = hostMatcher.group(1);
                if (tmp.contains(":")) {
                    String[] fullHost = tmp.split(":", 2);
                    map.put("host", fullHost[0]);
                    map.put("port", fullHost[1]);
                } else {
                    map.put("host", tmp);
                    if (ftpChain.startsWith("sftp://")) {
                        map.put("port", "22"); // Default Port when not defined and sftp.
                    } else {
                        map.put("port", "21"); // Default Port when not defined.
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error("Exception when parsing ftp url.", e);
            }

        }
        if (pathMatcher.find()) {
            try {
                map.put("path", pathMatcher.group(0));
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error("Exception when parsing ftp url.", e);
            }

        } else {
            map.put("path", "/");
        }
        LOG.debug("Result of FTP Parsing : " + map);
        return map;
    }

    private void setProxy(FTPClient client, String system, AppService myResponse) {
        String proxyHost = parameterService.getParameterStringByKey("cerberus_proxy_host", "", DEFAULT_PROXY_HOST);
        int proxyPort = parameterService.getParameterIntegerByKey("cerberus_proxy_port", "", DEFAULT_PROXY_PORT);

        myResponse.setProxy(true);
        myResponse.setProxyHost(proxyHost);
        myResponse.setProxyPort(proxyPort);

        SocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
        if (parameterService.getParameterBooleanByKey("cerberus_proxyauthentification_active", system,
                DEFAULT_PROXYAUTHENT_ACTIVATE)) {
            Authenticator.setDefault(
                    new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    String proxyUser = parameterService.getParameterStringByKey("cerberus_proxyauthentification_user", "", DEFAULT_PROXYAUTHENT_USER);
                    String proxyPassword = parameterService.getParameterStringByKey("cerberus_proxyauthentification_password", "", DEFAULT_PROXYAUTHENT_PASSWORD);
                    myResponse.setProxyWithCredential(true);
                    myResponse.setProxyUser(proxyUser);
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            }
            );
        }
        client.setProxy(proxy);
    }

}
