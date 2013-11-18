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
package org.cerberus.refactor;

import java.sql.Timestamp;

/**
 *
 * @author ip100003
 */
public class DateUtil {

    public static Timestamp diff(java.util.Date t1, java.util.Date t2) {
        // Make sure the result is always > 0
        if (t1.compareTo(t2) < 0) {
            java.util.Date tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        // Timestamps mix milli and nanoseconds in the API, so we have to separate the two
        long diffSeconds = (t1.getTime() / 1000) - (t2.getTime() / 1000);
        // For normals dates, we have millisecond precision
        int nano1 = ((int) t1.getTime() % 1000) * 1000000;
        // If the parameter is a Timestamp, we have additional precision in nanoseconds
        if (t1 instanceof Timestamp) {
            nano1 = ((Timestamp) t1).getNanos();
        }
        int nano2 = ((int) t2.getTime() % 1000) * 1000000;
        if (t2 instanceof Timestamp) {
            nano2 = ((Timestamp) t2).getNanos();
        }

        int diffNanos = nano1 - nano2;
        if (diffNanos < 0) {
            // Borrow one second
            diffSeconds--;
            diffNanos += 1000000000;
        }

        // mix nanos and millis again
        Timestamp result = new Timestamp((diffSeconds * 1000) + (diffNanos / 1000000));
        // setNanos() with a value of in the millisecond range doesn't affect the value of the time field
        // while milliseconds in the time field will modify nanos! Damn, this API is a *mess*
        result.setNanos(diffNanos);
        return result;
    }

    public static Timestamp sum(java.util.Date t1, java.util.Date t2) {

        // Timestamps mix milli and nanoseconds in the API, so we have to separate the two
        long sumSeconds = (t1.getTime() / 1000) + (t2.getTime() / 1000);
        // For normals dates, we have millisecond precision
        int nano1 = ((int) t1.getTime() % 1000) * 1000000;
        // If the parameter is a Timestamp, we have additional precision in nanoseconds
        if (t1 instanceof Timestamp) {
            nano1 = ((Timestamp) t1).getNanos();
        }
        int nano2 = ((int) t2.getTime() % 1000) * 1000000;
        if (t2 instanceof Timestamp) {
            nano2 = ((Timestamp) t2).getNanos();
        }

        int sumNanos = nano1 + nano2;

        // mix nanos and millis again
        Timestamp result = new Timestamp((sumSeconds * 1000) + (sumNanos / 1000000));
        // setNanos() with a value of in the millisecond range doesn't affect the value of the time field
        // while milliseconds in the time field will modify nanos! Damn, this API is a *mess*
        result.setNanos(sumNanos);
        return result;
    }
}
