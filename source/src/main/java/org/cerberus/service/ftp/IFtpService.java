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

package org.cerberus.service.ftp;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.cerberus.crud.entity.AppService;
import org.cerberus.util.answer.AnswerItem;

/**
*
* @author ryltar
*/
public interface IFtpService {
	
	/**
	 * this method is used to transform a ftpString to a simple HashMap
	 * @param ftpChain
	 * @return
	 */
	public Map<String, String> fromFtpStringToHashMap(String ftpChain);
	
	/**
	 * this method is used to interact with the ftp server
	 * @param chain
	 * @param system
	 * @param content
	 * @param method
	 * @param ftpPath
	 * @param service
	 * @return AppService
	 */
	public AnswerItem<AppService> callFTP(String chain, String system, String content, String method, String ftpPath, String service);
	
	
	/**
	 * this method is used to retrieve a file from FTP server
	 * @param chain
	 * @param system
	 * @return AppService
	 */
	public AnswerItem<AppService> getFTP( HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException;
	
	/**
	 * this method is used to post a file from FTP server
	 * @param informations
	 * @param ftp
	 * @param myResponse
	 * @return AppService
	 */
	public AnswerItem<AppService> postFTP(HashMap<String, String> informations, FTPClient ftp, AppService myResponse) throws IOException;

	/**
	 * this auxiliary method allow to set a PROXY to a FTPClient
	 * @param ftpClient
	 * @param system
	 * @param appService
	 */
	public void setProxy(FTPClient ftpClient, String system, AppService myResponse);
	

}
