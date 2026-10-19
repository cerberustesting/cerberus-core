/*
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

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IBuildRevisionParametersService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.notifications.email.entity.BuildContentRow;
import org.cerberus.core.util.answer.AnswerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailBuildContentServiceTest {

    private EmailBuildContentService service;
    private List<BuildRevisionParameters> brps;
    private MessageEvent brpMessage;
    private final List<String> requestedLastRevisions = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        service = new EmailBuildContentService();
        brps = new ArrayList<>();
        brpMessage = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);

        inject("buildRevisionParametersService", Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IBuildRevisionParametersService.class},
                (proxy, method, args) -> {
                    requestedLastRevisions.add((String) args[3]);
                    return new AnswerList<>(brps, brps.size(), brpMessage);
                }));
        inject("applicationService", Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IApplicationService.class},
                (proxy, method, args) -> {
                    if ("convert".equals(method.getName())) {
                        return ((AnswerList<?>) args[0]).getDataList();
                    }
                    Application app = new Application();
                    app.setApplication("App");
                    app.setBugTrackerUrl("https://bugs.test/browse/%BUGID%");
                    Application noTracker = new Application();
                    noTracker.setApplication("Other");
                    noTracker.setBugTrackerUrl("javascript:alert(1)");
                    return new AnswerList<>(List.of(app, noTracker), 2);
                }));
        inject("userService", Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IUserService.class},
                (proxy, method, args) -> {
                    if ("jo".equals(args[0])) {
                        User user = new User();
                        user.setName("Jo Doe");
                        return user;
                    }
                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                }));
    }

    private void inject(String field, Object value) throws Exception {
        Field f = EmailBuildContentService.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(service, value);
    }

    private static BuildRevisionParameters brp(String application, String owner, String bug, String link) {
        BuildRevisionParameters brp = new BuildRevisionParameters();
        brp.setBuild("B1");
        brp.setRevision("R2");
        brp.setApplication(application);
        brp.setReleaseOwner(owner);
        brp.setBugIdFixed(bug);
        brp.setLink(link);
        brp.setRelease("1.2");
        brp.setSubject("Fix");
        return brp;
    }

    @Test
    void buildsTheBugLinkAndTheOwnerName() {
        brps.add(brp("App", "jo", "BUG-1", "https://release.test/1"));
        brps.add(brp("App", "unknown.login", "", "javascript:alert(1)"));

        List<BuildContentRow> rows = service.read("SYS", "B1", "R2", "R1");

        assertEquals(2, rows.size());
        assertEquals("https://bugs.test/browse/BUG-1", rows.get(0).bugUrl());
        assertEquals("Jo Doe", rows.get(0).owner());
        assertEquals("https://release.test/1", rows.get(0).releaseUrl());
        // Unknown user : the login is displayed. No bug : no link. Not an http link : dropped.
        assertEquals("unknown.login", rows.get(1).owner());
        assertEquals("", rows.get(1).bugUrl());
        assertEquals("", rows.get(1).releaseUrl());
        assertEquals("R1", requestedLastRevisions.get(0));
    }

    @Test
    void ignoresANonHttpBugTracker() {
        brps.add(brp("Other", "jo", "BUG-2", ""));
        assertEquals("", service.read("SYS", "B1", "R2", "").get(0).bugUrl());
    }

    @Test
    void returnsNothingWhenTheReadFails() {
        brps.add(brp("App", "jo", "BUG-1", ""));
        brpMessage = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        assertTrue(service.read("SYS", "B1", "R2", "R1").isEmpty());
    }
}
