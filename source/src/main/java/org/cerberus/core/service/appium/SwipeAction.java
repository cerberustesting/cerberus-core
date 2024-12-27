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
package org.cerberus.core.service.appium;

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
     * {@link SwipeAction} types that correspond to which direction this {@link SwipeAction} has to be done
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
         * and ; is the {@link Direction#DELIMITER}
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
            String[] coordinates = direction.split(DELIMITER);
            if (coordinates == null || coordinates.length != 4) {
                throw new IllegalArgumentException("Bad direction format: " + direction);
            }

            // And create it
            return new Direction(new Line2D.Double(
                    Integer.parseInt(coordinates[0]),
                    Integer.parseInt(coordinates[1]),
                    Integer.parseInt(coordinates[2]),
                    Integer.parseInt(coordinates[3])
            ));
        }

        /**
         * Get a {@link Direction} from the given {@link Line2D} direction
         * <p>
         * {@link Direction#getX1()} == {@link Line2D#getX1()}
         * {@link Direction#getY1()} == {@link Line2D#getY1()}
         * {@link Direction#getX2()} == {@link Line2D#getX2()}
         * {@link Direction#getY2()} == {@link Line2D#getY2()}
         *
         * @param direction the {@link Line2D} direction to parse
         * @return a {@link Direction} based on the given {@link Line2D}
         */
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

    /**
     * Specific {@link Exception} class for a {@link SwipeAction}
     */
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
     * @throws SwipeActionException if creation failed
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

    /**
     * Get the associated {@link ActionType} to this {@link SwipeAction}
     *
     * @return the associated {@link ActionType} to this {@link SwipeAction}
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Check if this {@link SwipeAction} is customized, i.e., if {@link SwipeAction#getActionType()} == {@link ActionType#CUSTOM}
     *
     * @return <code>true</code> if this {@link SwipeAction} is customized, <code>false</code> otherwise
     */
    public boolean isCustom() {
        return actionType == ActionType.CUSTOM;
    }

    /**
     * Get the custom {@link Direction}, if and only if this {@link SwipeAction} is customized, i.e., {@link SwipeAction#isCustom()}
     *
     * @return the custom {@link Direction}
     * @throws IllegalStateException if this {@link SwipeAction} is not customized
     */
    public Direction getCustomDirection() {
        if (!isCustom()) {
            throw new IllegalStateException("Unable to get custom direction on non custom swipe action");
        }
        return customDirection;
    }

    /**
     * Set the custom {@link Direction} by the given one, if and only if this {@link SwipeAction} is customized, i.e., {@link SwipeAction#isCustom()}
     *
     * @param customDirection the custom {@link Direction} to set
     * @throws IllegalStateException if this {@link SwipeAction} is not customized
     */
    public void setCustomDirection(Direction customDirection) {
        if (!isCustom()) {
            throw new IllegalStateException("Unable to set custom direction on non custom swipe action");
        }
        this.customDirection = customDirection;
    }

}
