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
package org.cerberus.core.mcpdelta.run;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What a session has already verified: for every testcase it queued, the content fingerprint the testcase
 * had at that moment. A testcase whose last run on a target passed, was queued by the same session with
 * exactly the content it has now, and not long ago, has nothing new to say; running it again only costs a
 * browser. Scoped to the session on purpose: a new session (another day, another person) always runs for
 * real, because there the question is what changed in the application, not in the test.
 */
final class RunLedger {

    static final long VALID_MS = 60 * 60_000L;

    record Entry(String session, String fingerprint, long at) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    static String key(String tag, String test, String testcase, String country, String env, String robot) {
        return String.join("\u0001", tag, test, testcase, country, env, robot);
    }

    void record(String key, String session, String fingerprint) {
        long now = System.currentTimeMillis();
        entries.values().removeIf(e -> now - e.at() > VALID_MS);
        entries.put(key, new Entry(session, fingerprint, now));
    }

    /** The entry of one queued run, or null. */
    Entry get(String key) {
        return entries.get(key);
    }

    /** Tags of the other runs of this testcase on this target that this session queued with this exact content. */
    java.util.List<String> sameContent(String tag, String test, String testcase, String country, String env, String robot, String session,
                                       String fingerprint) {
        java.util.List<String> out = new java.util.ArrayList<>();
        String suffix = "\u0001" + String.join("\u0001", test, testcase, country, env, robot);
        for (Map.Entry<String, Entry> e : entries.entrySet()) {
            String k = e.getKey();
            if (k.endsWith(suffix) && !k.startsWith(tag + "\u0001") && e.getValue().session().equals(session)
                    && e.getValue().fingerprint().equals(fingerprint)) {
                out.add(k.substring(0, k.length() - suffix.length()));
            }
        }
        return out;
    }

    /** True when this session queued that run with this exact content, recently. */
    boolean vouches(String key, String session, String fingerprint) {
        Entry e = entries.get(key);
        return e != null && e.session().equals(session) && e.fingerprint().equals(fingerprint)
                && System.currentTimeMillis() - e.at() <= VALID_MS;
    }
}
