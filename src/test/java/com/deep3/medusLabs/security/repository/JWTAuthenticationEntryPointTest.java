package com.deep3.medusLabs.security.repository;

import com.deep3.medusLabs.security.filter.JWTAuthenticationEntryPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.times;

@RunWith(SpringJUnit4ClassRunner.class)
public class JWTAuthenticationEntryPointTest {

    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    AuthenticationException authenticationException;


    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCommence() throws IOException {
        JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JWTAuthenticationEntryPoint();
        jwtAuthenticationEntryPoint.commence(httpServletRequest, httpServletResponse, authenticationException);

        Mockito.verify(httpServletResponse, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED,  "Unauthorized");
    }
}
