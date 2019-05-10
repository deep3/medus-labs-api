package com.deep3.medusLabs.security.service;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TokenAuthenticationService {

    /**
     * Create a Token for the user if they log in successfully.
     * @param res - An HTTP response that will contain an AUTHENTICATION header (which contains the token).
     */
    void addAuthentication(HttpServletResponse res, Authentication authentication);

    /**
     * AuthenticationFilter uses this function to determine if authentication was successful.
     * The authentication will fail if the token is not valid (request will be refused)
     *
     * @param request	The http request Object. This will be checked to try and find the authentication token to verify.
     * @return
     */
    Authentication getAuthentication(HttpServletRequest request);

    /**
     * Return the currently logged in User a String value.
     *
     * @return String Username.
     */
    String getUsername();

}
