package com.deep3.medusLabs.security.service;

import com.deep3.medusLabs.security.TokenHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class TokenAuthenticationServiceImplTest {

    @Mock
    Authentication authentication;
    @Mock
    TokenHandler tokenHandler;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    HttpServletRequest httpServletRequest;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddAuthentication() {

        String test = "Test";

        when(authentication.getName()).thenReturn(test);

        tokenHandler.TOKEN_PREFIX = "PREFIX";
        tokenHandler.TOKEN_HEADER = "Authorization";

        TokenAuthenticationServiceImpl token = new TokenAuthenticationServiceImpl(tokenHandler);

        token.addAuthentication(httpServletResponse, authentication);

        verify(httpServletResponse, times(1)).addHeader(anyString(), anyString());

    }

    @Test
    public void testGetAuthenticationPrincipal() throws NoSuchFieldException, IllegalAccessException {
        TokenAuthenticationServiceImpl tokenAuthenticationServiceImpl = new TokenAuthenticationServiceImpl(tokenHandler);

        String token = "Bearer";

        tokenHandler.TOKEN_PREFIX = token;
        tokenHandler.TOKEN_HEADER = "Authorization";

        when(httpServletRequest.getHeader(any())).thenReturn(token);

        when(tokenHandler.parseToken(any())).thenReturn("Username");

        Field fieldTokenHandler = TokenAuthenticationServiceImpl.class.getDeclaredField("tokenHandler");
        fieldTokenHandler.setAccessible(true);
        fieldTokenHandler.set(tokenAuthenticationServiceImpl, tokenHandler);

        Authentication result = tokenAuthenticationServiceImpl.getAuthentication(httpServletRequest);

        Assert.assertEquals(result.getPrincipal(), "Username");

    }

}
