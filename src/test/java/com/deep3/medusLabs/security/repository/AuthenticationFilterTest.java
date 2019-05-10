package com.deep3.medusLabs.security.repository;

import com.deep3.medusLabs.security.filter.AuthenticationFilter;
import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationFilterTest {

    @Mock
    Authentication authentication;

    ServletRequest servletRequest;
    @Mock
    ServletResponse servletResponse;
    @Mock
    FilterChain filterChain;
    @Mock
    TokenAuthenticationService tokenAuthenticationService;



    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoFilter() throws IOException, ServletException, NoSuchFieldException, IllegalAccessException {

        when(tokenAuthenticationService.getAuthentication(any())).thenReturn(authentication);

        AuthenticationFilter authenticationFilter = new AuthenticationFilter(tokenAuthenticationService);

        Field fieldTokenHandler = AuthenticationFilter.class.getDeclaredField("tokenAuthenticationService");
        fieldTokenHandler.setAccessible(true);
        fieldTokenHandler.set(authenticationFilter, tokenAuthenticationService);

        authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(tokenAuthenticationService, times(1)).getAuthentication(any());
        Mockito.verify(filterChain, times(1)).doFilter(servletRequest, servletResponse);
    }

}
