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

package org.cerberus.service.ftp;


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
	 * this method is used to retrieve a file from FTP server
	 * @param chain
	 * @param system
	 * @return
	 */
	public AnswerItem<AppService> getFTP(String chain, String system);
	
	/**
	 * this auxiliary method allow to set a PROXY to a FTPClient
	 * @param ftpClient
	 * @param system
	 */
	public void setProxy(FTPClient ftpClient, String system);
	

}
