package org.cerberus.crud.entity;

import java.util.List;

public class InteractiveTuto {

    public enum Level {
        EASY(1), MEDIUM(2), HARD(3);

        private int value;
        private Level(int value){
            this.value = value;
        }

        public static Level getEnum(int level) {
            for(Level v : values())
                if(v.value == level) return v;
            throw new IllegalArgumentException();
        }

        public int getValue() {
            return value;
        }
    }

    private int id;
    private String title;
    private String description;
    private String role;
    private int order;
    private Level level;

    private List<InteractiveTutoStep> steps;

    public InteractiveTuto() {
    }

    public InteractiveTuto(int id, String title, String description, String role, int order, Level level, List<InteractiveTutoStep> step) {
        this.id = id;
        this.title = title;
        this.role = role;
        this.description = description;
        this.steps = steps;
        this.level = level;
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public List<InteractiveTutoStep> getSteps() {
        return steps;
    }

    public void setSteps(List<InteractiveTutoStep> steps) {
        this.steps = steps;
    }
}
