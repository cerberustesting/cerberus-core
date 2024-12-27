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
package org.cerberus.core.session;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
@Component
public class SessionCounter {

    private Map<String, String> users;
    private Map<String, Integer> creditLimitNbExe;
    private Map<String, Integer> creditLimitSecondExe;

    @PostConstruct
    public void init() {
        users = new HashMap<>();
        creditLimitNbExe = new HashMap<>();
        creditLimitSecondExe = new HashMap<>();
    }

    public boolean isAuthentified(String sessionId) {
        return users.containsKey(sessionId);
    }

    public void identificateUser(String sessionId, String user) {
        if (!this.isAuthentified(sessionId)) {
            users.put(sessionId, user);
        }
    }

    public void destroyUser(String sessionId) {
        if (this.isAuthentified(sessionId)) {
            users.remove(sessionId);
        }
    }

    public int getTotalActiveSession() {
        return users.size();
    }

    public Collection<String> getActiveUsers() {
        return users.values();
    }

    public int getCreditLimitNbExe() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(date);
        if (creditLimitNbExe.containsKey(currentDate)) {
            return creditLimitNbExe.get(currentDate);
        } else {
            return 0;
        }
    }

    public void incrementCreditLimitNbExe() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(date);
        if (creditLimitNbExe.containsKey(currentDate)) {
            creditLimitNbExe.put(currentDate, creditLimitNbExe.get(currentDate) + 1);
        } else {
            creditLimitNbExe.put(currentDate, 1);
        }
    }

    public int getCreditLimitSecondExe() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(date);
        if (creditLimitSecondExe.containsKey(currentDate)) {
            return creditLimitSecondExe.get(currentDate);
        } else {
            return 0;
        }
    }

    public void incrementCreditLimitSecondExe(Integer durationToAdd) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(date);
        if (creditLimitSecondExe.containsKey(currentDate)) {
            creditLimitSecondExe.put(currentDate, creditLimitSecondExe.get(currentDate) + durationToAdd);
        } else {
            creditLimitSecondExe.put(currentDate, durationToAdd);
        }
    }

}
