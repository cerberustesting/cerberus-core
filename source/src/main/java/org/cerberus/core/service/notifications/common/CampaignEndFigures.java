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
package org.cerberus.core.service.notifications.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.cerberus.core.crud.entity.Tag;

/**
 * Figures of the "Report by status" of a campaign execution (tag), shared by the end of campaign email and Slack message.
 */
public final class CampaignEndFigures {

    private final Map<String, Integer> counts = new LinkedHashMap<>();
    private final int total;
    private final int okPct;
    private final int notOk;
    private final int pending;

    public CampaignEndFigures(Tag tag) {
        counts.put("OK", tag.getNbOK());
        counts.put("KO", tag.getNbKO());
        counts.put("FA", tag.getNbFA());
        counts.put("NA", tag.getNbNA());
        counts.put("NE", tag.getNbNE());
        counts.put("WE", tag.getNbWE());
        counts.put("PE", tag.getNbPE());
        counts.put("QU", tag.getNbQU());
        counts.put("QE", tag.getNbQE());
        counts.put("PA", tag.getNbPA());
        counts.put("CA", tag.getNbCA());
        total = tag.getNbExeUsefull();
        okPct = percent(counts.get("OK"), total);
        notOk = counts.get("KO") + counts.get("FA") + counts.get("NA") + counts.get("CA");
        pending = counts.get("QU") + counts.get("QE") + counts.get("PA") + counts.get("PE") + counts.get("WE") + counts.get("NE");
    }

    public int count(String status) {
        return counts.get(status);
    }

    public int total() {
        return total;
    }

    public int okPercent() {
        return okPct;
    }

    public int notOk() {
        return notOk;
    }

    public int pending() {
        return pending;
    }

    public static int percent(int part, int total) {
        return total > 0 ? Math.round(part * 100f / total) : 0;
    }

    /**
     * Color of the OK rate at the center of the donut, like on the campaign report page : green when everything is OK,
     * orange from 50%, red below. Medium tones (tailwind -600) : the image is readable on a light and on a dark background.
     */
    public static String rateColor(int okPct) {
        return okPct >= 100 ? "#16a34a" : okPct >= 50 ? "#d97706" : "#dc2626";
    }
}
