package com.deep3.medusLabs.security.filter;

import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsUtils;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private TokenAuthenticationService tokenAuthenticationService;

    /**
     * Constructor for LoginFilter
     * @param url - request url to check against ant pathmatcher
     * @param authManager - Spring AuthenticationManager object
     * @param tokenAuthenticationService - Token management service instance
     */
    public LoginFilter(String url, AuthenticationManager authManager, TokenAuthenticationService tokenAuthenticationService) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException {

        if (CorsUtils.isPreFlightRequest(req)) {
            res.setStatus(HttpServletResponse.SC_OK);
            return null;
        }

        JsonNode node = new ObjectMapper().readTree(req.getInputStream());

        // Verify the correctness of login details, and if correct, the successfulAuthentication() method is executed.
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                       node.get("username").asText(),
                        node.get("password").asText(),
                        Collections.emptyList()
                )
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) {
        // Pass authenticated user data to the tokenAuthenticationService in order to add a JWT to the http response.
        tokenAuthenticationService.addAuthentication(res, auth);
    }

}
