package com.deep3.medusLabs.security.service;

import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserAccountServiceTest {

    @Mock
    UserRepository userRepository;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testLoadUserByUsernameNotNull() {
        User user = new User ("Test-Username", "Password");
        UserAccountService userAccountService = new UserAccountService(userRepository);

        when(userRepository.findByUsername(anyString())).thenReturn(user);

        UserDetails userDetails = userAccountService.loadUserByUsername(anyString());

        String testName = userDetails.getUsername();

        Assert.assertEquals(testName, user.getUsername());
    }


    @Test(expected = UsernameNotFoundException.class)
    public void testLoadUserByUsernameNull() {

        User user = new User ("Test-Username", "Password");
        UserAccountService userAccountService = new UserAccountService(userRepository);

        // when(userAccountRepository.findByUsername(anyString())).thenReturn(user);

        UserDetails userDetails = userAccountService.loadUserByUsername(anyString());

        String testName = userDetails.getUsername();

        Assert.assertEquals(testName, null);
    }
}
