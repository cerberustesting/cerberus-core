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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.springframework.stereotype.Service;
import org.cerberus.crud.factory.IFactoryAppService;

/**
 *
 * @author cte
 */
@Service
public class FactoryAppService implements IFactoryAppService {

    @Override
    public AppService create(String service, String type, String method, String application, String group, String serviceRequest, String kafkaTopic, String kafkaKey, String kafkaFilterPath, String kafkaFilterValue, 
            String description,
            String servicePath, String attachementURL, String operation, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif, String fileName) {
        AppService s = new AppService();
        s.setService(service);
        s.setServiceRequest(serviceRequest);
        s.setGroup(group);
        s.setDescription(description);
        s.setServicePath(servicePath);
        s.setAttachementURL(attachementURL);
        s.setOperation(operation);
        s.setMethod(method);
        s.setApplication(application);
        s.setType(type);
        s.setUsrCreated(usrCreated);
        s.setUsrModif(usrModif);
        s.setDateCreated(dateCreated);
        s.setDateModif(dateModif);
        s.setFileName(fileName);
        List<AppServiceContent> objectContentList = new ArrayList<>();
        s.setContentList(objectContentList);
        List<AppServiceHeader> objectHeaderList = new ArrayList<>();
        s.setHeaderList(objectHeaderList);
        List<AppServiceHeader> objectResponseHeaderList = new ArrayList<>();
        s.setResponseHeaderList(objectResponseHeaderList);
        s.setKafkaTopic(kafkaTopic);
        s.setKafkaKey(kafkaKey);
        s.setKafkaFilterPath(kafkaFilterPath);
        s.setKafkaFilterValue(kafkaFilterValue);
        s.setRecordTraceFile(true);
        return s;
    }
}
