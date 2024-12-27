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
package org.cerberus.core.engine.queuemanagement.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
// Component = instancié au déploiement. Singleton. 1 par instance
@Component
public class ExecutionQueueThreadPool {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(ExecutionQueueThreadPool.class);

    private ExecutorService executor;
//    private Integer totalNumberOfThread;
//    private Integer size;
//    private Integer inExecution;
//    Map<String, List<Future<?>>> map = new HashMap<>();

    @PostConstruct
    public void init() {
        executor = Executors.newCachedThreadPool();
//        totalNumberOfThread = 0;
//        inExecution = 0;
        LOG.debug("Starting Execution Queueing !! (ExecutionQueueThreadPool).");
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

//Changer le nombre de tache simultanée...
//    public void setNumberOfPool(Integer numberOfPool) {
//        totalNumberOfThread = numberOfPool;
//    }

//    public Integer getSize() {
//        return size;
//    }
//
//    public Integer getNumberOfThread() {
//        return totalNumberOfThread;
//    }

//    public void setSize(Integer size) {
//        this.size = size;
//    }
//
    public void reset() {
        this.stop();
        init();
    }

    public void stop() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

//    public Integer getInExecution() {
//        return inExecution;
//    }
//
//    public void setInExecution(Integer inExecution) {
//        this.inExecution = inExecution;
//    }
//
//    public void incrementInExecution() {
//        this.inExecution++;
//    }
//
//    public void decrementInExecution() {
//        this.inExecution--;
//    }

}
