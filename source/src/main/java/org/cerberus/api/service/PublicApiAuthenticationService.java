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
package org.cerberus.api.service;

import java.security.Principal;
import lombok.AllArgsConstructor;
import org.cerberus.service.authentification.IAPIKeyService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 *
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class PublicApiAuthenticationService {

    private final IAPIKeyService apiKeyService;

    public void authenticate(String apiKey) {
        if (!this.apiKeyService.authenticate(apiKey)) {
            throw new BadCredentialsException("authentication failed");
        }
    }

    public void authenticate(Principal principal, String apiKey) {
        if (!this.apiKeyService.authenticate(principal, apiKey)) {
            throw new BadCredentialsException("authentication failed");
        }
    }
}
