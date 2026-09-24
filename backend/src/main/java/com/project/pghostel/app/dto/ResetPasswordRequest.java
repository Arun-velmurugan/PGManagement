package com.project.pghostel.app.dto;

public class ResetPasswordRequest {

    private String username;

    private String password;


    // ==========================================
    // DEFAULT CONSTRUCTOR
    // ==========================================

    public ResetPasswordRequest() {
    }


    // ==========================================
    // GET USERNAME
    // ==========================================

    public String getUsername() {

        return username;
    }


    // ==========================================
    // SET USERNAME
    // ==========================================

    public void setUsername(String username) {

        this.username = username;
    }


    // ==========================================
    // GET PASSWORD
    // ==========================================

    public String getPassword() {

        return password;
    }


    // ==========================================
    // SET PASSWORD
    // ==========================================

    public void setPassword(String password) {

        this.password = password;
    }

}