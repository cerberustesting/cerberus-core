package org.cerberus.crud.entity;

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
	 * @param id
	 *            the new technical identifier of this {@link RobotCapability}
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the {@link Robot}'s name associated to this {@link RobotCapability}
	 * 
	 * @return the {@link Robot}'s name associated to this
	 *         {@link RobotCapability}
	 */
	public String getRobot() {
		return robot;
	}

	/**
	 * Set the {@link Robot}'s name associated to this {@link RobotCapability}
	 * 
	 * @param robotId
	 *            the new {@link Robot}'s name associated to this
	 *            {@link RobotCapability}
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
	 * @param capability
	 *            the new capability key of this {@link RobotCapability}
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
	 * @param value
	 *            the new capability value of this {@link RobotCapability}
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
