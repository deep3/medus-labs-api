package com.deep3.medusLabs.controller;

import com.deep3.medusLabs.model.User;
import com.deep3.medusLabs.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserControllerTests {

    private MockMvc mockMvc;

    @Mock
    private UserServiceImpl userService;


    @InjectMocks
    private UserController userController;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetAllUsers() throws Exception {

        // Users to return
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "Bill", "BillPa$$word"));
        users.add(new User(2L, "Bob", "BobPa$$word"));

        // Mock the function of the service

        when(userService.findAll()).thenReturn(users);

        // Perform GET on URL which maps to UserController
        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].username", is("Bill")))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].username", is("Bob")));
    }

    @Test
    public void testGetUserByID() throws Exception
    {
        User user = new User(1L, "Bill", "BillPa$$word");

        // Mock the function of the service
        when(userService.findOne(any(Long.class))).thenReturn(user);

        mockMvc.perform(
                get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].username", is("Bill")));
    }

    @Test
    public void testSaveUser() throws Exception
    {
        User user = new User("Bill", "BillPa$$word");

        // Mock the function of the service
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(
                post("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectAsJson(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content[0].username", is("Bill")));
    }

    @Test
    public void modifyUser() throws Exception
    {
        User user = new User(1l, "Bill", "BillPa$$word");

        // Mock the function of the service
        when(userService.modify(any(User.class))).thenReturn(user);


        mockMvc.perform(
                put("/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectAsJson(user)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].id", is(1)))
                        .andExpect(jsonPath("$.content[0].username", is("Bill")));

    }


    @Test
    public void testDeleteUser() throws Exception {
        User user = new User(1l, "Bill", "BillPa$$word");

        when(userService.findOne(any(Long.class))).thenReturn(user);

        mockMvc.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isOk());

    }


    @Test
    public void testFindByUsername() throws Exception {
        User user = new User(1l, "Bill", "BillPa$$word");

        when(userService.findByUsername(any(String.class))).thenReturn(user);

        mockMvc.perform(
                get("/users?username=" + user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].username", is("Bill")));

    }

    /*
     * Converts a Java object into JSON
     */
    public static String objectAsJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
