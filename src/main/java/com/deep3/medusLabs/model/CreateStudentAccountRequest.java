package com.deep3.medusLabs.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CreateStudentAccountRequest {

    @NotNull
    @Min(1)
    @Max(30)
    private int accounts;

    @Email
    private String email;

    /**
     * Get the number accounts for a given request
     * @return number of accounts
     */
    public int getAccounts() {
        return accounts;
    }

    /**
     * Set the number of accounts
     * @param accounts number of accounts
     */
    public void setAccounts(int accounts) {
        this.accounts = accounts;
    }

    /**
     * get the base email from a request
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * set the base email for a request
     * @param email - base email address to use with the alias feature
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
