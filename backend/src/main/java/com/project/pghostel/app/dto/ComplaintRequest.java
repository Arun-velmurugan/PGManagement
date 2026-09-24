package com.project.pghostel.app.dto;

public class ComplaintRequest {

    private String category;
    private String title;
    private String description;

    public ComplaintRequest() {
    }

    public ComplaintRequest(String category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}