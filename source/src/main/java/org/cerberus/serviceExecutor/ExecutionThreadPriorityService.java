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
package org.cerberus.serviceExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author bcivel
 */
public class ExecutionThreadPriorityService extends ThreadPoolExecutor {

    public ExecutionThreadPriorityService(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    protected <T> RunnableFuture<T> newTaskForValue(Runnable runnable, T value) {
        return new ComparableFutureTask<T>(runnable, value);
    }

    protected class ComparableFutureTask<T> extends FutureTask<T> implements Comparable<ComparableFutureTask<T>> {

        private Object object;

        public ComparableFutureTask(Runnable runnable, T result) {
            super(runnable, result);
            object = runnable;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compareTo(ComparableFutureTask<T> o) {
            if (this == o) {
                return 0;
            }
            if (o == null) {
                return -1; // this has higher priority than null
            }
            if (object != null && o.object != null) {
                if (object.getClass().equals(o.object.getClass())) {
                    if (object instanceof Comparable) {
                        return ((Comparable) object).compareTo(o.object);
                    }
                }
            }
            return 0;
        }
    }

}
