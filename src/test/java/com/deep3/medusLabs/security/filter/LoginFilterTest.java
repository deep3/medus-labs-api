package com.deep3.medusLabs.security.filter;

import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class LoginFilterTest {

    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    AuthenticationManager authenticationManger;
    @Mock
    FilterChain filterChain;
    @Mock
    TokenAuthenticationService tokenAuthenticationService;

    @Mock
    User user;


    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void testAttemptAuthentication() throws IOException, ServletException {

        String test = "null";
        byte[] byteArray = test.getBytes(StandardCharsets.UTF_8);
        InputStream targetStream = new ByteArrayInputStream(byteArray);

        when(httpServletRequest.getInputStream()).thenReturn(getInputStream(byteArray));

        LoginFilter loginFilter = new LoginFilter("Test", authenticationManger, tokenAuthenticationService);
        Authentication authentication = loginFilter.attemptAuthentication(httpServletRequest, httpServletResponse);

    }

    @Test
    public void testSuccessfulAuthentication() throws IOException, ServletException {

        Authentication auth = null;
        LoginFilter loginFilter = mock(LoginFilter.class);
        loginFilter.successfulAuthentication(httpServletRequest, httpServletResponse, filterChain, auth);

        verify(loginFilter, times(1)).successfulAuthentication(httpServletRequest, httpServletResponse, filterChain, auth);

    }


    public static ServletInputStream getInputStream(byte[] byteArray) throws IOException {
        return new CustomServletInputStream(byteArray); // <1>
    }

    private static class CustomServletInputStream extends ServletInputStream {

        private ByteArrayInputStream buffer;

        public CustomServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");
        }
    }


}
