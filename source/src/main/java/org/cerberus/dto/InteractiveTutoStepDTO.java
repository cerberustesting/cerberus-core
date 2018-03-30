package org.cerberus.dto;

import org.cerberus.crud.entity.InteractiveTutoStepType;

public class InteractiveTutoStepDTO {
    private String selectorJquery;
    private String text;
    private String attr1;
    private InteractiveTutoStepType type;

    public InteractiveTutoStepDTO(int id, String selectorJquery, String text, InteractiveTutoStepType type, String attr1) {
        this.selectorJquery = selectorJquery;
        this.text = text;
        this.type = type;
        this.attr1=attr1;
    }

    public String getSelectorJquery() {
        return selectorJquery;
    }

    public void setSelectorJquery(String selectorJquery) {
        this.selectorJquery = selectorJquery;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public InteractiveTutoStepType getType() {
        return type;
    }

    public void setType(InteractiveTutoStepType type) {
        this.type = type;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }
}
