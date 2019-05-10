

package com.deep3.medusLabs.security.config;
/*
import UserController;
import User;
import UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class WebSecurityTest {

    private MockMvc mockMvc;

    private  MockHttpSession httpSession;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private UserController userController;

    @Before
    public void initialize()
    {
        SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter = new SecurityContextHolderAwareRequestFilter();
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(securityContextHolderAwareRequestFilter)
                .build();

       httpSession = new MockHttpSession(webApplicationContext.getServletContext(), UUID.randomUUID().toString());
    }

    @Test
    public void accessProtected() throws Exception {


        // Users to return
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "Bill", "BillPa$$word"));
        users.add(new User(2L, "Bob", "BobPa$$word"));

        // Mock the function of the service

        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                .session(httpSession))
                .andExpect(status().isUnauthorized());

    }

}

*/


// Hours wasted here = 9
