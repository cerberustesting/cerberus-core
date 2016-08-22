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
package org.cerberus.service.file.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.file.IFileService;
import org.cerberus.util.answer.AnswerList;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FileService implements IFileService {

    private static final Logger LOG = Logger.getLogger(FileService.class);

    @Override
    public AnswerList<List<HashMap<String, String>>> parseCSVFile(String urlToCSVFile, String separator, HashMap<String, String> columnsToGet) {
        String str = "";
        AnswerList result = new AnswerList();
        List<HashMap<String, String>> csv = new ArrayList();
        /**
         * Init message with generic failed message
         */
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_GENERIC)
                .resolveDescription("URL", urlToCSVFile));

        try {
            /**
             * Get CSV File and parse it line by line
             */
            URL urlToCall = new URL(urlToCSVFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlToCall.openStream()));

            if ("".equals(separator)) {
                separator = ",";
            }
            boolean noDataMapped = true;
            while (null != (str = br.readLine())) {
                HashMap<String, String> line = new HashMap();
                Integer columnPosition = 1;
                /**
                 * For each line, split result by separator, and put it in
                 * result object if it has been defined in subdata
                 */
                for (String element : str.split(separator)) {
                    if (columnsToGet.containsKey(String.valueOf(columnPosition))) {
                        line.put(columnsToGet.get(String.valueOf(columnPosition)), element);
                        noDataMapped = false;
                    }
                    columnPosition++;
                }
                csv.add(line);
            }
            if (noDataMapped) { // No columns at all could be mapped on the full file.
                result.setDataList(null);
                result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_NOCOLUMEDMAPPED).resolveDescription("SEPARATOR", separator));
                result.setTotalRows(0);
                return result;
            }
            /**
             * Set result with datalist and resultMeassage.
             */
            result.setDataList(csv);
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_CSV).resolveDescription("URL", urlToCSVFile));
            result.setTotalRows(csv.size());
        } catch (Exception exception) {
            LOG.warn("Error Getting CSV File " + exception);
            result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_FILENOTFOUND)
                    .resolveDescription("URL", urlToCSVFile).resolveDescription("EX", exception.toString()));
        }
        return result;
    }
}
