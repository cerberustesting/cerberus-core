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
package org.cerberus.service.file.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.file.IFileService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class FileService implements IFileService {

    private static final Logger LOG = LogManager.getLogger(FileService.class);

    @Override
    public AnswerList<HashMap<String, String>> parseCSVFile(String urlToCSVFile, String separator, HashMap<String, String> columnsToGet) {
        String str = "";
        AnswerList<HashMap<String, String>> result = new AnswerList<>();
        List<HashMap<String, String>> csv = new ArrayList<>();
        /**
         * Init message with generic failed message
         */
        result.setResultMessage(new MessageEvent(MessageEventEnum.PROPERTY_FAILED_GETFROMDATALIB_CSV_GENERIC)
                .resolveDescription("URL", urlToCSVFile));

        BufferedReader br = null;

        try {
            /**
             * Get CSV File and parse it line by line
             */
            if (StringUtil.isURL(urlToCSVFile)) {
                URL urlToCall = new URL(urlToCSVFile);
                br = new BufferedReader(new InputStreamReader(urlToCall.openStream()));
            } else {
                br = new BufferedReader(new FileReader(urlToCSVFile));
                br.readLine();
            }

            if ("".equals(separator)) {
                separator = ",";
            }
            boolean noDataMapped = true;
            while (null != (str = br.readLine())) {
                HashMap<String, String> line = new HashMap<>();
                Integer columnPosition = 1;
                /**
                 * For each line, split result by separator, and put it in
                 * result object if it has been defined in subdata
                 */
                for (String element : str.split(separator)) {

                    // Looping against all subdata to get any column that match the current element position.
                    for (Map.Entry<String, String> entry : columnsToGet.entrySet()) {
                        String columnPos = entry.getValue();
                        String subDataName = entry.getKey();
                        if (columnPos.equals(String.valueOf(columnPosition))) { // If columns defined from subdata match the column number, we add the value here.
                            line.put(subDataName, element);
                            noDataMapped = false;
                        }
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
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.warn(e.toString());
                }
            }
        }
        return result;
    }
}
