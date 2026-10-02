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
package org.cerberus.core.mcpdelta.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.db.Db.Row;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything the compiler checks a document against: the action, control and condition catalogs shipped
 * with Cerberus, the invariants of this instance, its applications, folders and labels. Refreshed every
 * 30 seconds, so a value added in Cerberus is accepted almost at once.
 */
public final class Catalog {

    /** What one action, control, condition or property type expects in its three values. */
    public record Spec(String name, String p1, String p2, String p3, List<String> appTypes, String description) {
        public boolean requires(int index) {
            String p = index == 1 ? p1 : index == 2 ? p2 : p3;
            return p != null && !p.isBlank() && !p.toLowerCase().startsWith("[opt") && !p.toLowerCase().contains("optional")
                    && !p.toLowerCase().startsWith("[deprecated");
        }

        public String params() {
            List<String> out = new ArrayList<>();
            for (String p : new String[]{p1, p2, p3}) {
                if (p != null && !p.isBlank() && !p.equalsIgnoreCase("null")) {
                    out.add(p);
                }
            }
            return String.join(" | ", out);
        }
    }

    public record Application(String name, String type, String system) {
    }

    public record Label(String id, String system, String label, String type) {
    }

    private static final long TTL_MS = 30_000;

    private final Db db;
    private final Map<String, Spec> actionSpecs;
    private final Map<String, Spec> controlSpecs;
    private final Map<String, Spec> conditionSpecs;
    private final Map<String, Spec> propertySpecs;

    private volatile long loadedAt;
    private volatile Map<String, List<String>> invariants = Map.of();
    private volatile Map<String, Application> applications = Map.of();
    private volatile Set<String> tests = Set.of();
    private volatile List<Label> labels = List.of();
    private volatile Map<String, List<String>> appUrls = Map.of();

    public Catalog(Db db) {
        this.db = db;
        this.actionSpecs = loadSpecs("mcpdelta/catalog/actions.json", "action");
        this.controlSpecs = loadSpecs("mcpdelta/catalog/controls.json", "control");
        this.conditionSpecs = loadSpecs("mcpdelta/catalog/conditions.json", "condition");
        this.propertySpecs = loadSpecs("mcpdelta/catalog/properties.json", "control");
    }

    private static Map<String, Spec> loadSpecs(String resource, String nameKey) {
        Map<String, Spec> out = new LinkedHashMap<>();
        try (InputStream in = Catalog.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                // Without it every action and control name would go unchecked: refuse to start rather than degrade.
                throw new IllegalStateException("MCP Delta resource " + resource + " is missing from the classpath");
            }
            List<Map<String, Object>> list = new ObjectMapper().readValue(in, new TypeReference<>() {
            });
            for (Map<String, Object> m : list) {
                String name = String.valueOf(m.get(nameKey));
                @SuppressWarnings("unchecked")
                List<String> types = m.get("applicationType") instanceof List<?> l ? (List<String>) l : List.of("ALL");
                out.put(name, new Spec(name, str(m.get("param1")), str(m.get("param2")), str(m.get("param3")), types,
                        str(m.get("description"))));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read " + resource, e);
        }
        return out;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        if (now - loadedAt < TTL_MS) {
            return;
        }
        synchronized (this) {
            if (now - loadedAt < TTL_MS) {
                return;
            }
            db.read(c -> {
                Map<String, List<String>> inv = new LinkedHashMap<>();
                // Every family: the object documents check their enumerations (APPLITYPE, PLATFORM, SRVMETHOD...) too.
                for (Row r : Db.query(c, "SELECT idname, value FROM invariant ORDER BY idname, sort, value")) {
                    inv.computeIfAbsent(r.s("idname"), k -> new ArrayList<>()).add(r.s("value"));
                }
                Map<String, Application> apps = new LinkedHashMap<>();
                for (Row r : Db.query(c, "SELECT Application, type, `System` FROM application ORDER BY Application")) {
                    apps.put(r.s("Application"), new Application(r.s("Application"), r.s("type"), r.s("System")));
                }
                Set<String> folders = new LinkedHashSet<>();
                for (Row r : Db.query(c, "SELECT Test FROM test ORDER BY Test")) {
                    folders.add(r.s("Test"));
                }
                List<Label> lbl = new ArrayList<>();
                for (Row r : Db.query(c, "SELECT Id, `System`, Label, Type FROM label ORDER BY Label")) {
                    lbl.add(new Label(r.s("Id"), r.s("System"), r.s("Label"), r.s("Type")));
                }
                Map<String, List<String>> urls = new LinkedHashMap<>();
                for (Row r : Db.query(c, "SELECT Application, IP, URL FROM countryenvironmentparameters WHERE IsActive=1")) {
                    urls.computeIfAbsent(r.s("Application"), k -> new ArrayList<>()).add(r.s("IP") + "|" + r.s("URL"));
                }
                appUrls = urls;
                invariants = inv;
                applications = apps;
                tests = folders;
                labels = lbl;
                return null;
            });
            loadedAt = System.currentTimeMillis();
        }
    }

    /** "ip|url" of each active environment of an application: what openUrlWithBase prefixes. */
    public List<String> baseUrls(String application) {
        refresh();
        return appUrls.getOrDefault(application, List.of());
    }

    public void invalidate() {
        loadedAt = 0;
    }

    public List<String> invariant(String idName) {
        refresh();
        return invariants.getOrDefault(idName, List.of());
    }

    /**
     * Actions the engine implements. The instance's invariant lists only drive its editor's drop-downs and
     * drift from the engine: this one still offers verifyTitle (no longer run) and lacks verifyTitleEqual,
     * verifyUrlContains, returnPreviousPage (run). What runs is the reference; the lists are a fallback.
     */
    public Set<String> actions() {
        return usable(actionSpecs.keySet(), invariant("ACTION"));
    }

    public Set<String> controls() {
        return usable(controlSpecs.keySet(), invariant("CONTROL"));
    }

    private static Set<String> usable(Set<String> implemented, List<String> offered) {
        Set<String> s = new LinkedHashSet<>(implemented.isEmpty() ? offered : implemented);
        s.remove("Unknown");
        return s;
    }

    public Set<String> conditions(String scope) {
        Set<String> s = new LinkedHashSet<>(invariant(scope + "CONDITIONOPERATOR"));
        if (s.isEmpty()) {
            s.addAll(invariant("ACTIONCONDITIONOPERATOR"));
        }
        if (s.isEmpty()) {
            s.addAll(conditionSpecs.keySet());
        }
        return s;
    }

    public Spec actionSpec(String name) {
        return actionSpecs.get(name);
    }

    public Spec controlSpec(String name) {
        return controlSpecs.get(name);
    }

    public Spec conditionSpec(String name) {
        return conditionSpecs.get(name);
    }

    public Spec propertySpec(String name) {
        return propertySpecs.get(name);
    }

    public Map<String, Spec> actionSpecs() {
        return actionSpecs;
    }

    public Map<String, Spec> controlSpecs() {
        return controlSpecs;
    }

    public Map<String, Spec> conditionSpecs() {
        return conditionSpecs;
    }

    public Map<String, Spec> propertySpecs() {
        return propertySpecs;
    }

    public Map<String, Application> applications() {
        refresh();
        return applications;
    }

    public Set<String> tests() {
        refresh();
        return tests;
    }

    public List<Label> labels() {
        refresh();
        return labels;
    }

    /** Country order as Cerberus sorts them, so a testcase always lists its countries the same way. */
    public List<String> countryOrder() {
        return invariant("COUNTRY");
    }
}
