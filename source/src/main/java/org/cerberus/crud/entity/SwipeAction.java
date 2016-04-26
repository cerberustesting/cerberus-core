/*
 * Cerberus  Copyright (C) 2016  vertigo17
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
package org.cerberus.crud.entity;

import java.awt.geom.Line2D;

/**
 * A swipe action.
 * <p>
 * A swipe action is defined by its {@link SwipeAction#actionType} and optionally a {@link SwipeAction#customDirection} if it is a custom action
 *
 * @author Aurelien Bourdon.
 */
public class SwipeAction {

    /**
     * {@link SwipeAction} types
     */
    public enum ActionType {
        UP, DOWN, LEFT, RIGHT, CUSTOM
    }

    /**
     * A {@link Direction} represents the from and to coordinates in case of custom {@link SwipeAction}
     */
    public static class Direction {

        public static final String DELIMITER = ";";

        /**
         * Get a {@link Direction} from the given {@link String}
         * <p>
         * Format is the following: x1;y1;x2;y2 where (x1, y1) is the start coordinate and (x2, y2) is the end coordinate
         *
         * @param direction the {@link String} direction to parse
         * @return a {@link Direction} from the given {@link String}
         * @throws IllegalArgumentException if argument is not valid
         * @throws NumberFormatException    if coordinates are not valid
         */
        public static Direction fromString(String direction) {
            // Check null argument
            if (direction == null) {
                throw new IllegalArgumentException("Null direction");
            }

            // Parse direction
            String[] chunks = direction.split(DELIMITER);
            if (chunks == null || chunks.length != 4) {
                throw new IllegalArgumentException("Bad direction format: " + direction);
            }

            // And create it
            return new Direction(new Line2D.Double(
                    Integer.parseInt(chunks[0]),
                    Integer.parseInt(chunks[1]),
                    Integer.parseInt(chunks[2]),
                    Integer.parseInt(chunks[3])
            ));
        }

        public static Direction fromLine(Line2D direction) {
            if (direction == null) {
                throw new IllegalArgumentException("Null direction");
            }
            return new Direction(direction);
        }

        private Line2D direction;

        private Direction(Line2D direction) {
            this.direction = direction;
        }

        public int getX1() {
            return (int) direction.getX1();
        }

        public int getY1() {
            return (int) direction.getY1();
        }

        public int getX2() {
            return (int) direction.getX2();
        }

        public int getY2() {
            return (int) direction.getY2();
        }

    }

    public static class SwipeActionException extends Exception {
        public SwipeActionException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Create a new {@link SwipeAction} from the given {@link String} action type and direction
     *
     * @param actionType the {@link String} {@link ActionType}
     * @param direction  the {@link String} {@link Direction}
     * @return the {@link SwipeAction} from the given action type and direction
     * @throws SwipeActionException if creation fails
     * @see Direction#fromString(String)
     */
    public static SwipeAction fromStrings(String actionType, String direction) throws SwipeActionException {
        try {
            SwipeAction action = new SwipeAction(ActionType.valueOf(actionType));
            if (action.isCustom()) {
                action.setCustomDirection(Direction.fromString(direction));
            }
            return action;
        } catch (Exception e) {
            throw new SwipeActionException(e);
        }
    }

    private ActionType actionType;

    private Direction customDirection;

    private SwipeAction(ActionType actionType) {
        this(actionType, null);
    }

    private SwipeAction(ActionType actionType, Direction customDirection) {
        this.actionType = actionType;
        this.customDirection = customDirection;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public boolean isCustom() {
        return actionType == ActionType.CUSTOM;
    }

    public Direction getCustomDirection() {
        if (!isCustom()) {
            throw new IllegalStateException("Unable to get custom direction on non custom swipe action");
        }
        return customDirection;
    }

    public void setCustomDirection(Direction customDirection) {
        if (!isCustom()) {
            throw new IllegalStateException("Unable to set custom direction on non custom swipe action");
        }
        this.customDirection = customDirection;
    }

}
