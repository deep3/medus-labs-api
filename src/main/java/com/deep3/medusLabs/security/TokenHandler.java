package com.deep3.medusLabs.security;

import com.deep3.medusLabs.security.service.UserAccountService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TokenHandler {

    @Value("${token.expirationTime}")
    public long TOKEN_EXPIRATION_TIME;

    @Value("${token.secret}")
    public String TOKEN_SECRET;

    @Value("${token.prefix}")
    public String TOKEN_PREFIX;

    @Value("${token.header}")
    public String TOKEN_HEADER;

    private UserAccountService userAccountService;

    @Autowired
    public TokenHandler(UserAccountService userAccountService){
        this.userAccountService = userAccountService;
    }

    /**
     * Generate a token from the username.
     * @param username	- The subject from which generate the token.
     * @return - The generated token.
     */
    public String buildToken(String username) {

        Date now = new Date();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, TOKEN_SECRET)
                .compact();
    }

    /**
     * Parse a token and then extract the username from it
     * @param token - A token to parseToken.
     * @return - The subject (username) of the token.
     */
    public String parseToken(String token) {

        String usernameFromToken = Jwts.parser()
                .setSigningKey(TOKEN_SECRET)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody()
                .getSubject();

        return getUserAccountService().loadUserByUsername(usernameFromToken).getUsername();
    }

    public UserAccountService getUserAccountService() {
        return userAccountService;
    }
}
