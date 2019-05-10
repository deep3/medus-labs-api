package com.deep3.medusLabs.security.filter;

import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationFilter extends GenericFilterBean {

    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    public AuthenticationFilter(TokenAuthenticationService tokenAuthenticationService)
    {
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // sets TokenAuthenticationService to handle the authentication
        Authentication authentication = tokenAuthenticationService.getAuthentication((HttpServletRequest)request);

        // Apply Authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue with the request
        filterChain.doFilter(request,response);

        // Clear the authentication from current context
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}