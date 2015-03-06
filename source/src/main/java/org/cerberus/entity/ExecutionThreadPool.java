/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.entity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
@Component
public class ExecutionThreadPool {

    private ExecutorService executor;
    private Integer size;
    private Integer inExecution;
    private boolean numberOfPoolInitialized;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(3);
        size = 0;
        inExecution = 0;
        numberOfPoolInitialized = false;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
    
    public void setNumberOfPool(Integer numberOfPool){
        this.executor = Executors.newFixedThreadPool(numberOfPool);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
    
    public void incrementSize() {
        this.size++;
    }
    
    public void decrementSize() {
        this.size--;
    }
    
    public void reset() {
        this.stop();
        init();
    }

    public void stop() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public Integer getInExecution() {
        return inExecution;
    }

    public void setInExecution(Integer inExecution) {
        this.inExecution = inExecution;
    }
    
    public void incrementInExecution() {
        this.inExecution++;
    }
    
    public void decrementInExecution() {
        this.inExecution--;
    }

    public boolean isNumberOfPoolInitialized() {
        return numberOfPoolInitialized;
    }

    public void setNumberOfPoolInitialized(boolean numberOfPoolInitialized) {
        this.numberOfPoolInitialized = numberOfPoolInitialized;
    }
    
}
