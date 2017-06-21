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
package org.cerberus.servlet.api.zzpublic;

import org.cerberus.crud.service.ILogEventService;
import org.cerberus.servlet.api.GetableHttpServlet;
import org.cerberus.util.servlet.ServletUtil;
import org.cerberus.util.validity.Validable;

import javax.servlet.http.HttpServletRequest;

/**
 * A {@link GetableHttpServlet} that can be publicly published
 *
 * @author Aurelien Bourdon
 */
public abstract class PublicGetableHttpServlet<REQUEST extends Validable, RESPONSE> extends GetableHttpServlet<REQUEST, RESPONSE> {

    private static final String CALL_PUBLIC_CALL_ACTION = "CALL";

    @Override
    protected final void preRequestParsing(final HttpServletRequest req) throws PreRequestParsingException {
        corePreRequestParsing(req);
        additionalPreRequestParsing(req);
    }

    protected void additionalPreRequestParsing(final HttpServletRequest req) throws PreRequestParsingException {
        // Nothing to do
    }

    private void corePreRequestParsing(final HttpServletRequest req) throws PreRequestParsingException {
        registerServlet(req);
        publicCallLog(req);
    }

    private void registerServlet(final HttpServletRequest req) {
        ServletUtil.servletStart(req);
    }

    private void publicCallLog(final HttpServletRequest req) {
        getApplicationContext().getBean(ILogEventService.class).createForPublicCalls(
                String.format("/%s", getClass().getSimpleName()),
                CALL_PUBLIC_CALL_ACTION,
                String.format("%s called: %s", getClass().getSimpleName(), req.getRequestURL()),
                req
        );
    }

}
