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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionHttpStat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecutionHttpStat implements IFactoryTestCaseExecutionHttpStat {

    /**
     *
     * @param id
     * @param start
     * @param controlStatus
     * @param system
     * @param application
     * @param test
     * @param testCase
     * @param country
     * @param environment
     * @param robotDecli
     * @param total_hits
     * @param total_size
     * @param total_time
     * @param internal_hits
     * @param internal_size
     * @param internal_time
     * @param img_size
     * @param img_size_max
     * @param img_hits
     * @param js_size
     * @param js_size_max
     * @param js_hits
     * @param css_size
     * @param css_size_max
     * @param css_hits
     * @param html_size
     * @param html_size_max
     * @param html_hits
     * @param media_size
     * @param media_size_max
     * @param media_hits
     * @param nb_thirdparty
     * @param crbVersion
     * @param statDetail
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return
     */
    @Override
    public TestCaseExecutionHttpStat create(long id, Timestamp start, String controlStatus, String system, String application, String test, String testCase, String country, String environment, String robotDecli,
            int total_hits, int total_size, int total_time,
            int internal_hits, int internal_size, int internal_time,
            int img_size, int img_size_max, int img_hits,
            int js_size, int js_size_max, int js_hits,
            int css_size, int css_size_max, int css_hits,
            int html_size, int html_size_max, int html_hits,
            int media_size, int media_size_max, int media_hits,
            int nb_thirdparty,
            String crbVersion, JSONObject statDetail,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseExecutionHttpStat obj = new TestCaseExecutionHttpStat();
        obj.setId(id);
        obj.setStart(start);
        obj.setControlStatus(controlStatus);
        obj.setSystem(system);
        obj.setApplication(application);
        obj.setTest(test);
        obj.setTestcase(testCase);
        obj.setCountry(country);
        obj.setEnvironment(environment);
        obj.setRobotDecli(robotDecli);
        obj.setTotal_hits(total_hits);
        obj.setTotal_size(total_size);
        obj.setTotal_time(total_time);
        obj.setInternal_hits(internal_hits);
        obj.setInternal_size(internal_size);
        obj.setInternal_time(internal_time);
        obj.setImg_hits(img_hits);
        obj.setImg_size(img_size);
        obj.setImg_size_max(img_size_max);
        obj.setJs_hits(js_hits);
        obj.setJs_size(js_size);
        obj.setJs_size_max(js_size_max);
        obj.setCss_hits(css_hits);
        obj.setCss_size(css_size);
        obj.setCss_size_max(css_size_max);
        obj.setHtml_hits(html_hits);
        obj.setHtml_size(html_size);
        obj.setHtml_size_max(html_size_max);
        obj.setMedia_hits(media_hits);
        obj.setMedia_size(media_size);
        obj.setMedia_size_max(media_size_max);
        obj.setNb_thirdparty(nb_thirdparty);
        obj.setStatDetail(statDetail);
        obj.setCrbVersion(crbVersion);
        obj.setUsrCreated(usrCreated);
        obj.setDateCreated(dateCreated);
        obj.setUsrModif(usrModif);
        obj.setDateModif(dateModif);

        return obj;

    }
}
