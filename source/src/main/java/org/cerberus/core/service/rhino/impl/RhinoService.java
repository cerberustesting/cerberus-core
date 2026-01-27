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
package org.cerberus.core.service.rhino.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.ExecutionLog;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.rhino.IRhinoService;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Service;
import org.mozilla.javascript.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class RhinoService implements IRhinoService {

    private static final Logger LOG = LogManager.getLogger(RhinoService.class);

    private ExecutorService executorService;

    @PostConstruct
    void init() {
        executorService = Executors.newCachedThreadPool();
        LOG.info("RhinoService initialized");
    }

    @PreDestroy
    void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        LOG.info("RhinoService shutdown");
    }

    @Override
    public AnswerItem<String> eval(TestCaseExecution tce, String script) {

        AnswerItem<String> answer = new AnswerItem<>();
        MessageEvent msg;

        if (script == null || script.trim().isEmpty()) {
            LOG.warn("JS script is empty");
            tce.addExecutionLog(ExecutionLog.STATUS_ERROR, "JS script is empty");
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION).resolveDescription("EXCEPTION", "JS script is empty");
            answer.setResultMessage(msg);
            return answer;
        }

        final String scriptCopy = script;

        try {
            Future<String> future = executorService.submit(() -> execute(scriptCopy));
            String value = future.get(500, TimeUnit.MILLISECONDS);
            answer.setItem(value);

            if (value == null) {
                LOG.warn("JS evaluation returned null");
                tce.addExecutionLog(ExecutionLog.STATUS_ERROR, "JS evaluation returned null");
                msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION).resolveDescription("EXCEPTION", "JS evaluation returned null");
                answer.setResultMessage(msg);
                return answer;
            }

            msg = new MessageEvent(MessageEventEnum.PROPERTY_SUCCESS_JS).resolveDescription("VALUE", value).resolveDescription("SCRIPT", script);
            answer.setResultMessage(msg);
            tce.addExecutionLog(ExecutionLog.STATUS_INFO, msg.getDescription());
            return answer;

        } catch (TimeoutException e) {
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION).resolveDescription("EXCEPTION", "JS execution timeout : " + e.getMessage());
            answer.setResultMessage(msg);
            tce.addExecutionLog(ExecutionLog.STATUS_ERROR, msg.getDescription());
            return answer;
        } catch (Exception e) {
            msg = new MessageEvent(MessageEventEnum.PROPERTY_FAILED_JS_EXCEPTION).resolveDescription("EXCEPTION", "JS execution error : " + e.getMessage());
            answer.setResultMessage(msg);
            tce.addExecutionLog(ExecutionLog.STATUS_ERROR, msg.getDescription());
            return answer;
        }
    }

    private String execute(String script) {
        Context cx = Context.enter();
        try {
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_ES6);

            cx.setClassShutter(className -> false);

            ScriptableObject scope = cx.initSafeStandardObjects();

            //Remove Java Access
            scope.delete("Packages");
            scope.delete("java");
            scope.delete("javax");
            scope.delete("org");
            scope.delete("com");
            scope.delete("sun");

            Object result = cx.evaluateString(scope, script, "cerberus-js", 1, null);

            // Convertir en String **dans le même thread**
            return Context.toString(result);

        } finally {
            Context.exit();
        }
    }


}
