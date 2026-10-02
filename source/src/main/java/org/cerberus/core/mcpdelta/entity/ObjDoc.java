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
package org.cerberus.core.mcpdelta.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A Cerberus object as a document: a header line, key=value attributes, then one line per child. */
public final class ObjDoc {

    public String kind;
    public final List<String> key = new ArrayList<>();
    public String title;
    public final Map<String, String> attrs = new LinkedHashMap<>();
    public final List<Line> lines = new ArrayList<>();
    public int line;

    public String ref() {
        return kind + ":" + String.join("/", key);
    }

    /** One child: its kind word, its key values, its attributes and its description. */
    public static final class Line {
        public String kind;
        public final List<String> keys = new ArrayList<>();
        public final Map<String, String> attrs = new LinkedHashMap<>();
        public String description = "";
        public int line;
    }
}
