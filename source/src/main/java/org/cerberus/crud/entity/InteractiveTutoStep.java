package org.cerberus.crud.entity;

public class InteractiveTutoStep {
    private int id;
    private String selectorJquery;
    private String text;
    private String attr1;
    private InteractiveTutoStepType type;

    public InteractiveTutoStep(int id, String selectorJquery, String text, InteractiveTutoStepType type, String attr1) {
        this.id = id;
        this.selectorJquery = selectorJquery;
        this.text = text;
        this.type = type;
        this.attr1=attr1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
