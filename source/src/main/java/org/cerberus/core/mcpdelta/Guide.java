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
package org.cerberus.core.mcpdelta;

/**
 * What the model is told once, at connection: the document format and the workflow. Everything else is
 * read on demand ("read guide", "read catalog:click"), so the fixed cost of the server stays tiny.
 */
public final class Guide {

    private Guide() {
    }

    public static final String INSTRUCTIONS = """
            Cerberus through MCP Delta. A testcase is a short text document: read it, write it back changed; the \
            server checks it, computes the minimal change and applies it in one undoable transaction.

            testcase DemoShop/DEMO-001: Login with valid credentials
              application=DemoShop status=WORKING priority=1 countries=FR,BE
            prop email = text "alice@demo.test"
            step 1: Open login page
              openUrlWithBase "login.html"
                verifyElementPresent "id=email"
            step 2 use="DemoShop/LIB-001#2": Login as Alice

            - step at the margin; action indented 2 under its step; control indented 4 under its action.
            - Values are quoted, up to 3 ("id=x", "%property.email%"). Then options: fatal=no timeout=5000 \
            waitBefore=500 shot=after, a condition (if ifElementPresent "id=x"), a description (// text).
            - Header attributes you leave out are kept; steps, actions, controls and props are exactly what you write.

            Every other Cerberus object is a document too, read and written the same way: application:<name>, \
            service:<name>, campaign:<name>, robot:<name>, environment:<system>/<country>/<env>, folder:<name>, \
            invariant:<IDNAME>, labels:<system>, datalib:<name>[/<system>/<env>/<country>], context:<login>. The plural \
            lists them (applications, robots, campaigns...). Read one to see its exact shape before writing.

            Do as much as possible per call:
            - read "live:<url>" (several at once) to see a page as the robot's browser renders it — visible elements by region, \
            closed menus, the cookie banner, ready selectors — before writing tests for it.
            - read: refs like "Folder/Testcase", "Folder", "app:X", "run:<id>", "tag:<tag>", "page:<id>" (what the robot saw), \
            "executions status=FA env=QA", "file:<id>", "library", "targets", "catalog", "catalog:<name>", "catalog:variables", \
            "journal", "guide". Several refs at once.
            - find: where a value is used, across a scope.
            - write: docs (whole testcases, new or replaced), edits [{ref, old, new}] on the document as read \
            (like a file editor), replace [{find, with, in}] for rules over many testcases, delete — all in one call, one atomic \
            transaction; the answer is the diff and a delta id. dryRun:true simulates. Add run:{env:"QA"} to run what you \
            just wrote and get the verdicts in the same answer.
            - run: execute testcases, scopes or a campaign on a robot and wait; failures come back grouped by cause with \
            the failing line, the message and what the page actually contained. What already passed in this session and has \
            not changed is not run again, so running the whole folder after a fix costs only what changed. debug:true starts Cerberus' debug mode \
            on one testcase (paused before each action; do:next with count, retry, stop) and shows the page after each move.
            - undo: revert a delta.
            An error lists every problem with its line and a fix; nothing is written until the write is clean.""";

    public static final String FULL = """
            MCP Delta — document reference

            TESTCASE LINE
              testcase <Folder>/<Testcase>: <title>
              Quote the reference if it has spaces: testcase "Business Activity Monitor/0002A": title

            HEADER (indented, key=value, any order, several lines allowed)
              application=<app> status=<TCSTATUS> priority=<n> countries=FR,BE labels=smoke,login
              type=AUTOMATED|MANUAL|PRIVATE active=yes|no muted=yes|no activeQA= activeUAT= activePROD=
              details="long description" comment="..." bugs='[json]' useragent= screensize=
              fromMajor= fromMinor= toMajor= toMinor= targetMajor= targetMinor= implementer= executor= origin= refOrigin=
              if <condition operator> "v1" "v2"      (testcase condition)
              Attributes left out keep their current value; write attr="" to empty one.

            PROPERTIES
              prop <name> = <type> "value1" ["value2" ["value3"]] [options] [countries=FR] [// description]
              options: db=<PROPERTYDATABASE> length= rows= nature=STATIC|RANDOM|RANDOMNEW|NOTINUSE cache= retry= retryPeriod= rank=
              Without countries= a property applies to every country of the testcase.
              Use it in values as %property.<name>%.

            STEPS
              step <n> [library] [forced] [loop=<STEPLOOP>] [if <operator> "v1"] [use="Folder/Testcase#<step position>"]: <title>
              n is informational: the order of the lines decides. A step with use= runs that library step and has no actions.

            ACTIONS (indent 2) and CONTROLS (indent 4, under the action they check)
              <name> "value1" ["value2" ["value3"]] [fatal=no] [timeout=ms] [highlight=ms] [minSimilarity=] [typeDelay=]
                     [waitBefore=ms] [waitAfter=ms] [shot=before|after|both] [shotName=] [if <operator> "v1" "v2" [if.timeout=ms]] [// description]
              Element values use Cerberus syntax: "id=x", "xpath=//a", "css=.btn", "name=q", "link=Home", "data-cerberus=x".

            STRINGS
              "..." with \\" \\\\ \\n escapes, or a \"\"\" block \"\"\" for multi-line values (SQL, JS).
              Lines starting with # are comments.

            WRITE
              docs:    ["<full document>", ...]          create a testcase, or replace what it is
              edits:   [{ref, old, new, all?}]          change the document as read, like a file editor; old must be unique
              replace: [{find, with, in, regex?, ignoreCase?, fields?}, ...]   rules applied in order
                       in = scope: Folder | Folder/Testcase | Folder/PREFIX-* | app:X | label:X | status:X | * ; comma = union
                       fields = values (default, with properties) | properties | descriptions | all
              delete:  ["Folder/Testcase"]            held as a plan until confirmed
              dryRun:true → simulated with the real statements then rolled back; answer = plan id
              plan:"p3"   → apply a plan exactly as simulated (refused if the testcases changed since)
              Everything in one call is one transaction: all of it applies, or none of it.

            RUN
              refs = testcases or scopes; country, env, robot (see read targets). One call waits up to 45 s, then
              reports what is finished; run {tag:"<tag>"} waits for the rest.
              A testcase that passed in this session on the same target and has not changed since (library steps
              included) is listed as already OK instead of being run again; force:true runs everything.

            UNDO
              undo dN reverts a delta, refused if a testcase changed since (undo the later delta first).

            OBJECTS
              <kind> <key>: <title>          header line (key parts joined by /)
                attr=value attr=value         attributes (left out = kept)
                <line> <key values> attr=value // description     child lines (the document lists them all)
              application:<name>   lines: object <name> value=<selector>; env <system> <country> <env> ip= url=
              service:<name>       type= method= path= request= bodyType= auth...; lines: content <key> value=; header <key> value=
                                   curl="curl ..." fills method, URL, body, credentials and headers
              campaign:<name>      lines: param COUNTRY FR; label <name>; schedule "<cron>" active=yes; hook <EVENT> <CONNECTOR> <recipient>
              robot:<name>         platform= browser= ...; lines: capability <name> value=; executor <name> host= port=
              environment:<system>/<country>/<env>   active= build= revision=...; lines: app <application> ip= url=
              folder:<name>        active= parent=
              invariant:<IDNAME>   lines: value <value> sort= // description   (private invariants are held for confirmation)
              labels:<system>      lines: label <name> type= color= parent=
              datalib:<name>[/<system>/<env>/<country>]  type= script=...; lines: sub "" value=  (key entry) and sub <name> value=
              context:<login>      systems=DEFAULT,SYS2 (within the systems an administrator allows)
              Deletions are held as a plan; an object others depend on (testcases of an application...) is refused.

            RUN, MORE
              refs ["campaign:NAME"] runs a campaign; country/env/robot accept lists; options {screenshot, video, verbose, pageSource,
              robotLog, consoleLog, timeout, retries, priority, manualExecution, manualUrl, manualHost, manualContextRoot,
              manualLoginRelativeUrl, manualEnvData, executor}; {tag, action: cancel|pause|resume}.
              Debug mode: run {refs:["F/TC"], debug:true} → session; run {debug:"<session>", do:"next", count:5} | "retry" | "stop" | "status".
            """;
}
