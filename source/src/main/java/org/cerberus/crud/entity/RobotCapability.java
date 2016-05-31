package org.cerberus.crud.entity;

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
public class RobotCapability {

    /**
     * The {@link RobotCapability}'s technical identifier
     */
    private int id;

    /**
     * The {@link Robot}'s name
     */
    private String robot;

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
    public int getId() {
        return id;
    }

    /**
     * Set the technical identifier of this {@link RobotCapability}
     *
     * @param id the new technical identifier of this {@link RobotCapability}
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the {@link Robot}'s name associated to this {@link RobotCapability}
     *
     * @return the {@link Robot}'s name associated to this
     * {@link RobotCapability}
     */
    public String getRobot() {
        return robot;
    }

    /**
     * Set the {@link Robot}'s name associated to this {@link RobotCapability}
     *
     * @param robotId the new {@link Robot}'s name associated to this
     * {@link RobotCapability}
     */
    public void setRobot(String robot) {
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
