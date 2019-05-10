package com.deep3.medusLabs.model;

public class Student {

    private String username;
    private String password;
    private String loginUrl;

    public Student(String user, String pass, String accountId) {
        this.username = user;
        this.password = pass;
        setLoginUrl(accountId);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String accountId) {
        this.loginUrl = "https://" + accountId + ".signin.aws.amazon.com/console";
    }
}
