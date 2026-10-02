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

import org.cerberus.core.mcpdelta.entity.Spec.Audit;
import org.cerberus.core.mcpdelta.entity.Spec.Entity;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.cerberus.core.mcpdelta.entity.Spec.bool;
import static org.cerberus.core.mcpdelta.entity.Spec.child;
import static org.cerberus.core.mcpdelta.entity.Spec.entity;
import static org.cerberus.core.mcpdelta.entity.Spec.label;
import static org.cerberus.core.mcpdelta.entity.Spec.num;
import static org.cerberus.core.mcpdelta.entity.Spec.secret;
import static org.cerberus.core.mcpdelta.entity.Spec.text;
import static org.cerberus.core.mcpdelta.entity.Spec.yn;

/**
 * Every Cerberus object MCP Delta handles besides testcases, as documents. Each entry replaces a family of
 * tools of the classic MCP (create, get, list, update, delete, and the same again for each child table).
 */
public final class Specs {

    private static final Map<String, Entity> BY_KIND = new LinkedHashMap<>();
    private static final Map<String, Entity> BY_PLURAL = new LinkedHashMap<>();

    static {
        // The per-application environment settings, seen from the application and from the environment.
        Spec.Child appEnv = child("env", "countryenvironmentparameters")
                .link("Application", "Application")
                .keys(text("system", "system"), text("country", "Country"), text("environment", "Environment"))
                .fields(text("ip", "IP"), text("url", "URL"), text("urlLogin", "URLLOGIN"), text("domain", "domain"),
                        bool("active", "IsActive", "1"), text("var1", "Var1"), text("var2", "Var2"), text("var3", "Var3"),
                        text("var4", "Var4"), secret("secret1", "Secret1"), secret("secret2", "Secret2"),
                        num("poolSize", "poolSize", "0"), text("mobileActivity", "mobileActivity"), text("mobilePackage", "mobilePackage"))
                .order("`system`, Country, Environment");

        add(entity("application", "applications", "application")
                .summary("an application under test, its objects (named selectors) and its URL per environment")
                .keys(text("application", "Application"))
                .title("description")
                .fields(text("type", "type", "GUI").always().values("APPLITYPE"),
                        text("system", "System", "DEFAULT").always().values("SYSTEM"),
                        text("subsystem", "SubSystem"), num("sort", "sort", "10"), text("repoUrl", "RepoUrl"),
                        text("bugTracker", "BugTrackerConnector", "NONE").values("BUGTRACKERCONNECTOR"),
                        text("bugTrackerParam1", "BugTrackerParam1"), text("bugTrackerParam2", "BugTrackerParam2"),
                        text("bugTrackerParam3", "BugTrackerParam3"), text("bugTrackerUrl", "BugTrackerUrl"),
                        text("bugTrackerNewUrl", "BugTrackerNewUrl"), num("poolSize", "poolSize", null),
                        text("deployType", "deploytype", null), text("mavenGroupId", "mavengroupid"))
                .children(child("object", "applicationobject")
                                .link("Application", "Application")
                                .keys(text("object", "Object"))
                                .fields(text("value", "Value"), text("screenshot", "ScreenshotFileName"),
                                        text("x", "XOffset"), text("y", "YOffset"))
                                .id("ID").order("Object"),
                        appEnv)
                .order("Application").list("type", "system").filter("System")
                .guard("SELECT COUNT(*) FROM testcase WHERE Application=?", "testcase(s) still belong to it; deleting it would delete them too")
                .guard("SELECT COUNT(*) FROM buildrevisionparameters WHERE Application=?", "build/revision record(s) depend on it"));

        add(entity("service", "services", "appservice")
                .summary("a service (REST, SOAP, Kafka, MongoDB, FTP) called by tests, with its body contents and headers")
                .keys(text("service", "Service"))
                .title("Description")
                .fields(text("application", "Application", null).always(),
                        text("type", "Type", "REST").always().values("SRVTYPE"),
                        text("method", "Method", "GET").always().values("SRVMETHOD"),
                        text("path", "ServicePath").always(), text("bodyType", "BodyType", "none").values("SRVBODYTYPE"),
                        text("request", "ServiceRequest"), text("requestExtra", "ServiceRequestExtra1"),
                        bool("followRedirect", "isFollowRedir", "1"), text("operation", "Operation"),
                        text("attachmentUrl", "AttachementURL"), text("fileName", "FileName"),
                        text("authType", "AuthType", "none").values("AUTHTYPE"), text("authUser", "AuthUser"),
                        secret("authPassword", "AuthPassword"), text("authAddTo", "AuthAddTo", "Header"),
                        text("kafkaTopic", "KafkaTopic"), text("kafkaKey", "KafkaKey"), text("kafkaFilterPath", "KafkaFilterPath"),
                        text("kafkaFilterValue", "KafkaFilterValue"), text("kafkaFilterHeaderPath", "KafkaFilterHeaderPath"),
                        text("kafkaFilterHeaderValue", "KafkaFilterHeaderValue"), bool("avro", "IsAvroEnable", "0"),
                        text("schemaRegistryUrl", "SchemaRegistryUrl"), bool("avroKey", "IsAvroEnableKey", "0"),
                        text("avroSchemaKey", "AvroSchemaKey"), bool("avroValue", "IsAvroEnableValue", "0"),
                        text("avroSchemaValue", "AvroSchemaValue"), text("parentService", "ParentContentService", null),
                        text("collection", "Collection"), text("simulation", "SimulationParameters"))
                .children(child("content", "appservicecontent").link("Service", "Service").keys(text("key", "Key"))
                                .fields(text("value", "Value"), num("sort", "Sort", "10"), bool("active", "IsActive", "1"))
                                .description("Description").order("Sort, `Key`"),
                        child("header", "appserviceheader").link("Service", "Service").keys(text("key", "Key"))
                                .fields(text("value", "Value"), num("sort", "Sort", "10"), bool("active", "IsActive", "1"))
                                .description("Description").order("Sort, `Key`"))
                .order("Service").list("type", "method", "application").filter("Application"));

        add(entity("campaign", "campaigns", "campaign")
                .summary("a campaign: what it runs (labels, criteria), where (country/environment/robot parameters), when (schedules) and who is told (hooks)")
                .keys(text("campaign", "campaign"))
                .title("Description").id("campaignID")
                .fields(text("longDescription", "LongDescription"), text("tag", "Tag"),
                        text("screenshot", "Screenshot").values("SCREENSHOT"), text("video", "Video").values("VIDEO"),
                        text("verbose", "Verbose").values("VERBOSE"), text("pageSource", "PageSource").values("PAGESOURCE"),
                        text("robotLog", "RobotLog").values("ROBOTLOG"), text("consoleLog", "ConsoleLog").values("CONSOLELOG"),
                        text("timeout", "Timeout"), text("retries", "Retries").values("RETRIES"), text("priority", "Priority"),
                        text("manualExecution", "ManualExecution").values("MANUALEXECUTION"),
                        text("ciScoreThreshold", "CIScoreThreshold"), text("group1", "Group1"), text("group2", "Group2"),
                        text("group3", "Group3"))
                .children(child("param", "campaignparameter").link("campaign", "campaign")
                                .keys(text("parameter", "Parameter"), text("value", "Value"))
                                .id("campaignparameterID").audit(Audit.NONE).order("Parameter, Value"),
                        child("label", "campaignlabel").link("campaign", "campaign").keys(label("label", "labelId"))
                                .id("campaignlabelID").order("labelId"),
                        child("schedule", "scheduleentry").link("name", "campaign").link("type", "=CAMPAIGN")
                                .keys(text("cron", "cronDefinition")).fields(yn("active", "active", "Y"))
                                .description("description").id("ID").order("ID"),
                        child("hook", "eventhook").link("ObjectKey1", "campaign")
                                .keys(text("event", "EventReference").values("EVENTHOOK"),
                                        text("connector", "HookConnector").values("EVENTCONNECTOR"), text("recipient", "HookRecipient"))
                                .fields(text("channel", "HookChannel"), bool("active", "IsActive", "1"), text("key2", "ObjectKey2"))
                                .description("Description").id("ID").order("ID"))
                .order("campaign").list("group1", "tag").filter("Group1"));

        add(entity("robot", "robots", "robot")
                .summary("a robot (browser or device profile) with its capabilities and the executors (Selenium/Appium hosts) it runs on")
                .keys(text("robot", "robot"))
                .title("description").id("robotID")
                .fields(text("type", "type", "GUI").always().values("APPLITYPE"),
                        text("platform", "platform").always().values("PLATFORM"),
                        text("browser", "browser").always().values("BROWSER"), text("version", "version"),
                        bool("active", "IsActive", "1"), text("userAgent", "useragent"), text("screenSize", "screensize"),
                        text("profileFolder", "ProfileFolder"), bool("acceptNotifications", "AcceptNotifications", "0"),
                        text("extraParam", "ExtraParam"), bool("acceptInsecureCerts", "IsAcceptInsecureCerts", "1"),
                        text("preloadScript", "preloadScript", null), text("robotDecli", "robotdecli"),
                        text("lbMethod", "lbexemethod", "BYRANKING").values("ROBOTLBMETHOD"))
                .children(child("capability", "robotcapability").link("robot", "robot").keys(text("capability", "capability"))
                                .fields(text("value", "value")).id("id").audit(Audit.NONE).order("capability"),
                        child("executor", "robotexecutor").link("robot", "robot").keys(text("executor", "executor"))
                                .fields(text("host", "host"), text("port", "Port"), bool("active", "IsActive", "1"), num("rank", "rank", "10"),
                                        text("hostUser", "HostUser"), secret("hostPassword", "HostPassword"),
                                        text("extensionHost", "ExecutorExtensionHost", null), num("extensionPort", "ExecutorExtensionPort", "0"),
                                        num("extensionProxyPort", "ExecutorExtensionProxyPort", "0"), text("deviceUdid", "deviceUdid"),
                                        text("deviceName", "deviceName"), num("devicePort", "devicePort", null),
                                        bool("deviceLockUnlock", "IsDeviceLockUnlock", "0"),
                                        text("proxyType", "executorProxyType", "NONE").values("PROXYTYPE"),
                                        text("proxyServiceHost", "ExecutorProxyServiceHost"), num("proxyServicePort", "ExecutorProxyServicePort", "0"),
                                        text("browserProxyHost", "ExecutorBrowserProxyHost"), num("browserProxyPort", "ExecutorBrowserProxyPort", "0"))
                                .description("description").id("id").order("`rank`, executor"))
                .order("robot").list("browser", "platform", "type").filter("browser"));

        add(entity("folder", "folders", "test")
                .summary("a test folder")
                .keys(text("folder", "Test"))
                .title("Description")
                .fields(bool("active", "isActive", "1"), text("parent", "ParentTest", null))
                .order("Test").list("active")
                .guard("SELECT COUNT(*) FROM testcase WHERE Test=?", "testcase(s) are still in it; deleting it would delete them too"));

        add(entity("environment", "environments", "countryenvparam")
                .summary("an environment of a system for a country (build, revision, activation) and the URL of each application in it")
                .keys(text("system", "system"), text("country", "Country"), text("environment", "Environment"))
                .title("Description").audit(Audit.NONE)
                .fields(yn("active", "active", "Y").always(), text("type", "Type", "STD").values("ENVTYPE"),
                        text("build", "Build"), text("revision", "Revision"), text("chain", "Chain"),
                        text("distribList", "DistribList"), yn("maintenance", "maintenanceact", "N"),
                        text("maintenanceStart", "maintenancestr", "00:00:00"), text("maintenanceEnd", "maintenanceend", "00:00:00"),
                        text("emailRevision", "EMailBodyRevision"), text("emailChain", "EMailBodyChain"),
                        text("emailDisable", "EMailBodyDisableEnvironment"))
                .children(child("app", "countryenvironmentparameters")
                        .link("system", "system").link("Country", "Country").link("Environment", "Environment")
                        .keys(text("application", "Application"))
                        .fields(text("ip", "IP"), text("url", "URL"), text("urlLogin", "URLLOGIN"), text("domain", "domain"),
                                bool("active", "IsActive", "1"), text("var1", "Var1"), text("var2", "Var2"), text("var3", "Var3"),
                                text("var4", "Var4"), secret("secret1", "Secret1"), secret("secret2", "Secret2"),
                                num("poolSize", "poolSize", "0"), text("mobileActivity", "mobileActivity"), text("mobilePackage", "mobilePackage"))
                        .order("Application"))
                .order("`system`, Country, Environment").list("active", "build", "revision").filter("system")
                .guard("SELECT (SELECT COUNT(*) FROM buildrevisionbatch WHERE `system`=? AND Country=? AND Environment=?)"
                        + "+(SELECT COUNT(*) FROM countryenvdeploytype WHERE `system`=? AND Country=? AND Environment=?)"
                        + "+(SELECT COUNT(*) FROM countryenvironmentdatabase WHERE `system`=? AND Country=? AND Environment=?)"
                        + "+(SELECT COUNT(*) FROM countryenvlink WHERE `system`=? AND Country=? AND Environment=?)"
                        + "+(SELECT COUNT(*) FROM countryenvparam_log WHERE `system`=? AND Country=? AND Environment=?)"
                        + "+(SELECT COUNT(*) FROM host WHERE `system`=? AND Country=? AND Environment=?)",
                        "build, deploy, database, link, log or host record(s) depend on it and could not be restored"));

        add(entity("invariant", "invariants", null)
                .summary("an invariant family (the allowed values of a list: COUNTRY, ENVIRONMENT, TCSTATUS...)")
                .keys(text("idname", "idname"))
                .children(child("value", "invariant").link("idname", "idname").keys(text("value", "value"))
                        .fields(num("sort", "sort", "0"), text("short", "VeryShortDesc"), text("gp1", "gp1"), text("gp2", "gp2"),
                                text("gp3", "gp3"), text("gp4", "gp4"), text("gp5", "gp5"), text("gp6", "gp6"), text("gp7", "gp7"),
                                text("gp8", "gp8"), text("gp9", "gp9"))
                        .description("description").order("sort, value")));

        add(entity("labels", "labels", null)
                .summary("the labels of a system (stickers, batteries, requirements), used on testcases and campaigns")
                .keys(text("system", "System"))
                .children(child("label", "label").link("System", "System").keys(text("label", "Label"))
                        .fields(text("type", "Type", "STICKER").values("LABELTYPE"), text("color", "Color", "#000000"),
                                label("parent", "ParentLabelID"), text("requirementType", "RequirementType"),
                                text("requirementStatus", "RequirementStatus"), text("requirementCriticity", "RequirementCriticity"),
                                text("longDescription", "LongDescription"))
                        .description("Description").id("Id").order("Label")));

        add(entity("datalib", "datalibs", "testdatalib")
                .summary("a test data library entry and its sub-data (the key entry has an empty name)")
                .keys(text("name", "Name"), text("system", "system"), text("environment", "Environment"), text("country", "Country"))
                .title("Description").id("TestDataLibID").audit(Audit.DATALIB)
                .fields(text("type", "Type", "INTERNAL").always().values("TESTDATATYPE"), text("group", "Group"),
                        yn("private", "PrivateData", "N"), text("database", "Database"), text("script", "Script"),
                        text("databaseUrl", "DatabaseUrl"), text("service", "Service", null), text("servicePath", "ServicePath"),
                        text("method", "Method"), text("envelope", "Envelope", null), text("databaseCsv", "DatabaseCsv"),
                        text("csvUrl", "CsvUrl"), text("separator", "Separator"), bool("ignoreFirstLine", "IgnoreFirstLine", "0"))
                .children(child("sub", "testdatalibdata").link("TestDataLibID", "TestDataLibID").keys(text("subdata", "SubData"))
                        .fields(text("value", "Value"), text("column", "Column"), text("parsing", "ParsingAnswer"),
                                text("position", "ColumnPosition"), yn("encrypt", "Encrypt", "N"))
                        .description("Description").id("TestDataLibDataID").audit(Audit.NONE).order("SubData"))
                .order("Name, `system`, Environment, Country").list("type", "group").filter("Type"));
    }

    static {
        add(entity("context", "contexts", "user")
                .summary("a Cerberus user's active systems (the scope the MCP works in); allowed systems are set by an administrator")
                .keys(text("login", "Login"))
                .fields(Spec.list("systems", "DefaultSystem").always())
                .order("Login").fixed().columns("Login", "DefaultSystem", "UsrModif", "DateModif"));
    }

    private Specs() {
    }

    private static void add(Entity e) {
        BY_KIND.put(e.kind, e);
        BY_PLURAL.put(e.plural, e);
    }

    public static Entity byKind(String kind) {
        return BY_KIND.get(kind);
    }

    public static Entity byPlural(String plural) {
        return BY_PLURAL.get(plural);
    }

    public static Map<String, Entity> all() {
        return BY_KIND;
    }
}
