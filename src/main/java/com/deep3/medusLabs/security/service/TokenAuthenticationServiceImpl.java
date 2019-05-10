package com.deep3.medusLabs.security.service;

import com.deep3.medusLabs.security.TokenHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Collections.emptyList;

@Service
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

    private TokenHandler tokenHandler;
    private String user;

    public TokenAuthenticationServiceImpl(TokenHandler tokenHandler)
    {
        this.tokenHandler = tokenHandler;
    }

    /**
     * When a user successfully logs into the application, create a token for that user.
     * @param res - An http response that will be filled with an 'Authentication' header containing the token.
     */
    @Override
    public void addAuthentication(HttpServletResponse res, Authentication authentication) {

        String user = authentication.getName();

        String JWT = tokenHandler.buildToken(user);

        res.addHeader(tokenHandler.TOKEN_HEADER, tokenHandler.TOKEN_PREFIX + " " + JWT);
    }

    /**
     * The AuthenticationFilter calls this method to verify the user authentication.
     * If the token is not valid, the authentication fails and the request will be refused.
     *
     * @param request - An http request that will be check for authentication token to verify.
     * @return
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {

        String token = request.getHeader(tokenHandler.TOKEN_HEADER);

        if (token != null && token.startsWith(tokenHandler.TOKEN_PREFIX)) {
            try {
                user = tokenHandler.parseToken(token);
            } catch (Exception e) {

            }
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, emptyList());
            } else {
                return null;
            }
        }

        return null;
    }

    /**
     * Used to return a string value of the current user logged in.
     *
     * @return String current user
     */
    @Override
    public String getUsername() {
        return this.user;
    }
}