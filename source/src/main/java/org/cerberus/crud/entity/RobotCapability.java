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

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Objects;

/**
 * A {@link Robot} capability is a single configuration that can be used by
 * {@link Robot}.
 *
 * Configuration is composed by a key ({@link #capability} and a value (
 * {@link #value}).
 *
 * @author Aurelien Bourdon
 */
@Entity
public class RobotCapability {

    /**
     * The {@link RobotCapability}'s technical identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The {@link Robot}'s name
     */
    @ManyToOne
    @JoinColumn(name = "robot")
    @JsonIgnore
    private Robot robot;

    /**
     * The capability key
     */
    private String capability;

    /**
     * The capability value
     */
    private String value;

    /**
     * Get the technical identifier from this {@link RobotCapability}
     *
     * @return the technical identifier from this {@link RobotCapability}
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the technical identifier of this {@link RobotCapability}
     *
     * @param id the new technical identifier of this {@link RobotCapability}
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the {@link Robot}'s name associated to this {@link RobotCapability}
     *
     * @return the {@link Robot}'s name associated to this
     * {@link RobotCapability}
     */
    public Robot getRobot() {
        return robot;
    }

    /**
     * Set the {@link Robot}'s name associated to this {@link RobotCapability}
     *
     * @param robot the new {@link Robot}'s name associated to this
     * {@link RobotCapability}
     */
    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    /**
     * Get the capability key of this {@link RobotCapability}
     *
     * @return the capability key of this {@link RobotCapability}
     */
    public String getCapability() {
        return capability;
    }

    /**
     * Set the capability key of this {@link RobotCapability}
     *
     * @param capability the new capability key of this {@link RobotCapability}
     */
    public void setCapability(String capability) {
        this.capability = capability;
    }

    /**
     * Get the capability value of this {@link RobotCapability}
     *
     * @return the capability value of this {@link RobotCapability}
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the capability value of this {@link RobotCapability}
     *
     * @param value the new capability value of this {@link RobotCapability}
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    public boolean hasSameKey(RobotCapability other) {
        if (other == null) {
            return false;
        }
        if (!Objects.equals(robot, other.robot)) {
            return false;
        }
        if (!Objects.equals(capability, other.capability)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.robot);
        hash = 17 * hash + Objects.hashCode(this.capability);
        hash = 17 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RobotCapability other = (RobotCapability) obj;
        if (!Objects.equals(this.robot, other.robot)) {
            return false;
        }
        if (!Objects.equals(this.capability, other.capability)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
