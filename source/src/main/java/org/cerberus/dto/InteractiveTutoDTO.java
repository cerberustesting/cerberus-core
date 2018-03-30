package org.cerberus.dto;

import java.util.List;

public class InteractiveTutoDTO {

    private int id;
    private String title;
    private String description;
    private String role;
    private int order;
    private int level;

    private List<InteractiveTutoStepDTO> steps;

    public InteractiveTutoDTO() {
    }

    public InteractiveTutoDTO(int id, String title, String description, String role, int order, int level) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.role = role;
        this.order = order;
        this.level = level;
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

    public List<InteractiveTutoStepDTO> getSteps() {
        return steps;
    }

    public void setSteps(List<InteractiveTutoStepDTO> steps) {
        this.steps = steps;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
