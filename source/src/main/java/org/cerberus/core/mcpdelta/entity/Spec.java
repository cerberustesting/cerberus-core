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
import java.util.Arrays;
import java.util.List;

/**
 * Declarative description of a Cerberus object as a Delta document: its table, key, header attributes, the
 * kinds of child lines it holds, and the rules that keep a write safe. One generic engine reads, writes,
 * journals and undoes every object described this way.
 */
public final class Spec {

    private Spec() {
    }

    /** How a column reads in a document. */
    public enum Type {
        /** Free text. */
        TEXT,
        /** Whole number. */
        INT,
        /** 0/1 column written yes/no. */
        BOOL,
        /** Y/N column written yes/no. */
        YN,
        /** Password or secret: shown as *** when set, *** written back keeps it. */
        SECRET,
        /** A label id, written as the label's name. */
        LABEL,
        /** A JSON array of strings, written as a comma-separated list. */
        LIST
    }

    /** Which audit columns a table has. */
    public record Audit(String usrCreated, String dateCreated, String usrModif, String dateModif) {
        public static final Audit NONE = new Audit(null, null, null, null);
        public static final Audit STD = new Audit("UsrCreated", "DateCreated", "UsrModif", "DateModif");
        public static final Audit DATALIB = new Audit("Creator", "Created", "LastModifier", "LastModified");

        public boolean has(String column) {
            return column != null && Arrays.asList(usrCreated, dateCreated, usrModif, dateModif).stream()
                    .anyMatch(c -> c != null && c.equalsIgnoreCase(column));
        }
    }

    /** One attribute: its name in the document, its column, its type, its default and its allowed values. */
    public static final class Field {
        public final String attr;
        public final String column;
        public final Type type;
        public final String def;
        /** Shown even when equal to the default (the attributes that identify what the object is). */
        public boolean always;
        /** Invariant family listing the allowed values, checked on write. */
        public String values;

        Field(String attr, String column, Type type, String def) {
            this.attr = attr;
            this.column = column;
            this.type = type;
            this.def = def;
        }

        public Field always() {
            this.always = true;
            return this;
        }

        public Field values(String invariant) {
            this.values = invariant;
            return this;
        }
    }

    public static Field text(String attr, String column, String def) {
        return new Field(attr, column, Type.TEXT, def);
    }

    public static Field text(String attr, String column) {
        return new Field(attr, column, Type.TEXT, "");
    }

    public static Field num(String attr, String column, String def) {
        return new Field(attr, column, Type.INT, def);
    }

    public static Field bool(String attr, String column, String def) {
        return new Field(attr, column, Type.BOOL, def);
    }

    public static Field yn(String attr, String column, String def) {
        return new Field(attr, column, Type.YN, def);
    }

    public static Field secret(String attr, String column) {
        return new Field(attr, column, Type.SECRET, "");
    }

    public static Field label(String attr, String column) {
        return new Field(attr, column, Type.LABEL, null);
    }

    public static Field list(String attr, String column) {
        return new Field(attr, column, Type.LIST, null);
    }

    /** A delete that must not happen while other rows depend on the object. */
    public record Guard(String countSql, String message) {
    }

    /** A kind of child line ("object", "header", "executor"...) stored in its own table. */
    public static final class Child {
        public final String kind;
        public final String table;
        /** Pairs {child column, parent column} or {child column, "=constant"}. */
        public final List<String[]> link = new ArrayList<>();
        /** Positional values after the kind word: the child's key inside its parent. */
        public final List<Field> keys = new ArrayList<>();
        public final List<Field> fields = new ArrayList<>();
        public String description;
        public String id;
        public String order;
        public Audit audit = Audit.STD;

        Child(String kind, String table) {
            this.kind = kind;
            this.table = table;
        }

        public Child link(String childColumn, String parentColumnOrConstant) {
            link.add(new String[]{childColumn, parentColumnOrConstant});
            return this;
        }

        public Child keys(Field... f) {
            keys.addAll(List.of(f));
            return this;
        }

        public Child fields(Field... f) {
            fields.addAll(List.of(f));
            return this;
        }

        public Child description(String column) {
            this.description = column;
            return this;
        }

        public Child id(String column) {
            this.id = column;
            return this;
        }

        public Child order(String sql) {
            this.order = sql;
            return this;
        }

        public Child audit(Audit a) {
            this.audit = a;
            return this;
        }
    }

    public static Child child(String kind, String table) {
        return new Child(kind, table);
    }

    /** A kind of document ("application", "robot", "campaign"...). */
    public static final class Entity {
        public final String kind;
        public final String plural;
        /** Null for a collection document, which only groups child lines (an invariant family, a system's labels). */
        public final String table;
        public final List<Field> keys = new ArrayList<>();
        public final List<Field> fields = new ArrayList<>();
        public final List<Child> children = new ArrayList<>();
        public final List<Guard> guards = new ArrayList<>();
        public String title;
        public String id;
        public Audit audit = Audit.STD;
        public String order;
        /** Columns shown on a listing line, after the key and the title. */
        public final List<String> listAttrs = new ArrayList<>();
        /** Column a listing filter ("robots:chrome") is matched against, besides the key and title. */
        public String filterColumn;
        public String summary;
        /** Objects that exist only through Cerberus itself (users): documents can change them, not create or delete them. */
        public boolean noCreate;
        public boolean noDelete;
        /** When set, only these columns are ever read: a table holding secrets (user) never reaches a document or the journal. */
        public List<String> columns;

        Entity(String kind, String plural, String table) {
            this.kind = kind;
            this.plural = plural;
            this.table = table;
        }

        public Entity keys(Field... f) {
            keys.addAll(List.of(f));
            return this;
        }

        public Entity fields(Field... f) {
            fields.addAll(List.of(f));
            return this;
        }

        public Entity children(Child... c) {
            children.addAll(List.of(c));
            return this;
        }

        public Entity title(String column) {
            this.title = column;
            return this;
        }

        public Entity id(String column) {
            this.id = column;
            return this;
        }

        public Entity audit(Audit a) {
            this.audit = a;
            return this;
        }

        public Entity order(String sql) {
            this.order = sql;
            return this;
        }

        public Entity list(String... attrs) {
            listAttrs.addAll(List.of(attrs));
            return this;
        }

        public Entity filter(String column) {
            this.filterColumn = column;
            return this;
        }

        public Entity guard(String countSql, String message) {
            guards.add(new Guard(countSql, message));
            return this;
        }

        public Entity summary(String s) {
            this.summary = s;
            return this;
        }

        public Entity columns(String... cols) {
            this.columns = List.of(cols);
            return this;
        }

        public Entity fixed() {
            this.noCreate = true;
            this.noDelete = true;
            return this;
        }

        public boolean isCollection() {
            return table == null;
        }

        public Child child(String kind) {
            for (Child c : children) {
                if (c.kind.equals(kind)) {
                    return c;
                }
            }
            return null;
        }

        public Field field(String attr) {
            for (Field f : fields) {
                if (f.attr.equalsIgnoreCase(attr) || f.column.equalsIgnoreCase(attr)) {
                    return f;
                }
            }
            return null;
        }
    }

    public static Entity entity(String kind, String plural, String table) {
        return new Entity(kind, plural, table);
    }
}
