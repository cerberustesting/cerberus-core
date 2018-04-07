/*
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.StringWriter;
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
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.ftp.IFtpService;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.stereotype.Service;

/**
*
* @author ryltar
*/
@Service
public class FtpService implements IFtpService{
	
	private static final Logger LOG = LogManager.getLogger(FtpService.class);
	
	@Autowired
    private IFactoryAppService factoryAppService;
	@Autowired
    private IRecorderService recorderService;
	@Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;
	
	@Override
	public HashMap<String, String> fromFtpStringToHashMap(String ftpChain) {
		HashMap<String, String> map = new HashMap<String, String>();
		String tmp;
		Matcher accountMatcher = Pattern.compile("(\\/\\/|\\\\\\\\)(.*)@").matcher(ftpChain);
		Matcher hostMatcher = Pattern.compile("@(.*)[\\/|\\\\]").matcher(ftpChain);
		Matcher pathMatcher = Pattern.compile("(\\/|\\\\)([^:]+)$").matcher(ftpChain);
	
 		if(accountMatcher.find() && hostMatcher.find() && pathMatcher.find()) {
 			try {
 			    tmp = accountMatcher.group(2);
 				String[] account = tmp.split(":",2);
 				map.put("pseudo", account[0]);
 				map.put("password", account[1]);
 				tmp = hostMatcher.group(1);
 				String[] fullHost = tmp.split(":", 2);
 				map.put("host", fullHost[0]);
 				map.put("port", fullHost[1]);
 				map.put("path", pathMatcher.group(0));
 			}catch(ArrayIndexOutOfBoundsException e) {
 				LOG.error("Exception when parsing ftp url.");
 			}
			
		}
		return map;
	}
	
	@Override
	public AnswerItem<AppService> getFTP(String chain) {
		MessageEvent message = null;
		AnswerItem result = new AnswerItem();
		HashMap<String, String> informations = this.fromFtpStringToHashMap(chain);
		
		if(informations.size() <= 4) {
		    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", chain));
            message.setDescription(
                    message.getDescription().replace("%DESCRIPTION%", "Ftp url bad format! you missed something. please modify ftp url with correct syntax"));
            result.setResultMessage(message);
            return result;
		}
		
		FTPClient ftp = new FTPClient();
		AppService myResponse = factoryAppService.create("", AppService.TYPE_FTP,
                AppService.METHOD_HTTPGET, "", "", informations.get("path"), "", "", "", "", "", null, "", null);
		 
		try{
			ftp.connect(informations.get("host"),Integer.valueOf(informations.get("port")));
			boolean logged = ftp.login(informations.get("pseudo"),informations.get("password"));
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
            String remoteFile = informations.get("path");
            LOG.info("Start retrieving ftp file");
            FTPFile[] ftpFile = ftp.listFiles(informations.get("path"));
            if(ftpFile.length != 0 && ftpFile[0].getSize() != 0) {
            	InputStream done = ftp.retrieveFileStream(remoteFile);
                boolean success = ftp.completePendingCommand();
                myResponse.setResponseHTTPCode(ftp.getReplyCode());
                if(success && FTPReply.isPositiveCompletion(myResponse.getResponseHTTPCode())) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
             		byte[] buf = new byte[1024];
             		int n = 0;
             		while ((n = done.read(buf)) >= 0)
             		    baos.write(buf, 0, n);
             		byte[] content = baos.toByteArray();
             		myResponse.setFile(content);
                	LOG.info("ftp file successfully retrieve");
                    message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE);
                    message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", "GET"));
                    message.setDescription(message.getDescription().replace("%SERVICEPATH%", remoteFile));
                    result.setResultMessage(message);
                    String expectedContent =  IOUtils.toString(new ByteArrayInputStream(content), "UTF-8");
                    String extension = testCaseExecutionFileService.checkExtension(informations.get("path"), "");
                    if(extension == "JSON" || extension == "XML" || extension == "TXT") {
                        myResponse.setResponseHTTPBody(expectedContent);
                    }
                    myResponse.setResponseHTTPBodyContentType(extension);
                    result.setItem(myResponse);
                    baos.close();
                }else {
                	LOG.error("Error when downloading the file. Something went wrong");
                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                    message.setDescription(message.getDescription().replace("%SERVICE%", remoteFile));
                    message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                            "Error when downloading the file. Something went wrong"));
                    result.setResultMessage(message);
                }
                done.close();
            }else {
            	LOG.error("Impossible to retrieve the file, or the file is empty. Please check the FTP path");
                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
                message.setDescription(message.getDescription().replace("%SERVICE%", remoteFile));
                message.setDescription(message.getDescription().replace("%DESCRIPTION%",
                        "Impossible to retrieve the file, or the file is empty. Please check the FTP path"));
                result.setResultMessage(message);
            }
		}catch(Exception e) {
			message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE);
            message.setDescription(message.getDescription().replace("%SERVICE%", informations.get("path")));
            message.setDescription(
                    message.getDescription().replace("%DESCRIPTION%", "Error on CallFTP : " + e.toString()));
            result.setResultMessage(message);
		}finally {
			if(ftp.isConnected()) {
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
}