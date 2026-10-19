/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IBuildRevisionParametersService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.notifications.email.IEmailBuildContentService;
import org.cerberus.core.service.notifications.email.entity.BuildContentRow;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailBuildContentService implements IEmailBuildContentService {

    private static final Logger LOG = LogManager.getLogger(EmailBuildContentService.class);

    @Autowired
    private IBuildRevisionParametersService buildRevisionParametersService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private IUserService userService;

    @Override
    public List<BuildContentRow> read(String system, String build, String revision, String lastRevision) {
        List<BuildContentRow> rows = new ArrayList<>();
        try {
            AnswerList<BuildRevisionParameters> answer = buildRevisionParametersService.readByBuildBetweenRevisions(system, build, revision, lastRevision);
            if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED.getCode())) {
                LOG.warn("Could not read the build content of '" + system + "' " + build + "/" + revision + " : " + answer.getResultMessage().getDescription());
                return rows;
            }

            // Bug tracker url of each application and display name of each owner : read once.
            Map<String, String> bugTrackerUrls = new HashMap<>();
            for (Application application : applicationService.convert(applicationService.readBySystem(Arrays.asList(system)))) {
                bugTrackerUrls.put(application.getApplication(), application.getBugTrackerUrl());
            }
            Map<String, String> ownerNames = new HashMap<>();

            for (BuildRevisionParameters brp : answer.getDataList()) {
                String bugUrl = "";
                String bugTrackerUrl = bugTrackerUrls.get(brp.getApplication());
                if (isHttpUrl(bugTrackerUrl) && !StringUtil.isEmptyOrNull(brp.getBugIdFixed())) {
                    bugUrl = bugTrackerUrl.replace("%BUGID%", brp.getBugIdFixed());
                }
                rows.add(new BuildContentRow(
                        nullToEmpty(brp.getBuild()),
                        nullToEmpty(brp.getRevision()),
                        nullToEmpty(brp.getApplication()),
                        nullToEmpty(brp.getProject()),
                        nullToEmpty(brp.getBugIdFixed()),
                        bugUrl,
                        nullToEmpty(brp.getTicketIdFixed()),
                        ownerName(ownerNames, brp.getReleaseOwner()),
                        nullToEmpty(brp.getRelease()),
                        isHttpUrl(brp.getLink()) ? brp.getLink() : "",
                        nullToEmpty(brp.getSubject())));
            }
        } catch (Exception e) {
            LOG.warn("Exception when reading the build content of '" + system + "' " + build + "/" + revision, e);
            return new ArrayList<>();
        }
        return rows;
    }

    // Name of the owner when the login is a known user, the login itself otherwise.
    private String ownerName(Map<String, String> cache, String login) {
        if (StringUtil.isEmptyOrNull(login)) {
            return "";
        }
        return cache.computeIfAbsent(login, key -> {
            try {
                User user = userService.findUserByKey(key);
                return user != null && !StringUtil.isEmptyOrNull(user.getName()) ? user.getName() : key;
            } catch (Exception e) {
                return key;
            }
        });
    }

    // Links come from the database : only http(s) ones are kept.
    private static boolean isHttpUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
