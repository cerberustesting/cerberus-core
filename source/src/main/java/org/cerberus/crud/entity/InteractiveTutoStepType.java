package org.cerberus.crud.entity;

public enum InteractiveTutoStepType {
    DEFAULT("default"), GENERAL("general"), CHANGE_PAGE_AFTER_CLICK("changePageAfterClick");

    private final String type;

    InteractiveTutoStepType(String type) {
        this.type=type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static InteractiveTutoStepType getEnum(String type) {
        for(InteractiveTutoStepType v : values())
            if(v.toString().equalsIgnoreCase(type)) return v;
        throw new IllegalArgumentException();
    }

}
