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
package org.cerberus.core.mcpdelta.doc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A testcase as a document: what the AI reads and writes. It carries meaning only — no internal ids, no
 * timestamps — so the same text always describes the same testcase.
 */
public final class TcDoc {

    public String test;
    public String testcase;
    public String title = "";
    /** Header attributes in document order (application, status, priority, countries, labels...). */
    public final Map<String, String> attrs = new LinkedHashMap<>();
    public Cond condition;
    public final List<Prop> props = new ArrayList<>();
    public final List<Step> steps = new ArrayList<>();
    public int line;

    public String ref() {
        return test + "/" + testcase;
    }

    /** A condition: operator plus up to three values, plus its options (timeout...). */
    public static final class Cond {
        public String operator;
        public final List<String> values = new ArrayList<>();
        public final Map<String, String> options = new LinkedHashMap<>();

        public boolean isAlways() {
            return operator == null || operator.isBlank() || operator.equals("always");
        }
    }

    public static final class Prop {
        public String name;
        public String type;
        public final List<String> values = new ArrayList<>();
        public final Map<String, String> flags = new LinkedHashMap<>();
        public String description = "";
        /** Null means "every country of the testcase". */
        public List<String> countries;
        public int line;
    }

    public static final class Step {
        public String title = "";
        public final Map<String, String> flags = new LinkedHashMap<>();
        public Cond condition;
        public final List<Item> actions = new ArrayList<>();
        public int line;
    }

    /** An action, or a control when it hangs under an action. */
    public static final class Item {
        public String name;
        public final List<String> values = new ArrayList<>();
        public final Map<String, String> flags = new LinkedHashMap<>();
        public Cond condition;
        public String description = "";
        public final List<Item> controls = new ArrayList<>();
        public int line;
    }
}
