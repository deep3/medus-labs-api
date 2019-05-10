package com.deep3.medusLabs.controller;

import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.LabServiceImpl;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class LabControllerTests {

    private MockMvc mockMvc;

    @Mock
    private LabServiceImpl labService;

    @Mock
    private DeployedLabService deployedLabService;

    @InjectMocks
    private LabController labController;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(labController).build();
    }

    @Test
    public void testGetLabByIdentification() throws Exception {

        Lab testLab = new Lab ("Test-Name", "my-template-url");
        testLab.setId(1l);

        when(labService.findOne(any(Long.class))).thenReturn(testLab);

        mockMvc.perform(
                get("/labs/{id}", testLab.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)));

    }

    @Test
    public void testGetAllDeployedLabs() throws Exception {
        List<DeployedLab> deployedLabs = new ArrayList<>();
        deployedLabs.add(new DeployedLab(new Lab("EC2", "my-template-url"), Arrays.asList("1","2")));
        deployedLabs.add(new DeployedLab(new Lab("Lex", "my-template-url-2"), Arrays.asList("3","4","5")));
        when(deployedLabService.findDeployedLabs()).thenReturn(deployedLabs);

        mockMvc.perform(
                get("/labs/deployed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].lab.name", is("EC2")))
                .andExpect(jsonPath("$.content[0].lab.templateUrl", is("my-template-url")))
                .andExpect(jsonPath("$.content[1].lab.name", is("Lex")))
                .andExpect(jsonPath("$.content[1].lab.templateUrl", is("my-template-url-2")))
                .andExpect(jsonPath("$.content[0].labAccounts", hasSize(2)))
                .andExpect(jsonPath("$.content[1].labAccounts", hasSize(3)));
    }



    @Test
    public void testGetAllLabs() throws Exception {

        // Labs to return
        List<Lab> labs = new ArrayList<>();

        labs.add(new Lab("EC2", "my-template-url"));
        labs.add(new Lab("LEX", "my-template-url-2"));

        // Mock the function of the service
        when(labService.findAll()).thenReturn(labs);

        // Perform GET on URL which maps to LabsController
        mockMvc.perform(get("/labs/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("EC2")))
                .andExpect(jsonPath("$.content[0].templateUrl", is("my-template-url")))
                .andExpect(jsonPath("$.content[1].name", is("LEX")))
                .andExpect(jsonPath("$.content[1].templateUrl", is("my-template-url-2")));
    }

    @Test
    public void testSaveLab() throws Exception{

        Lab testLab = new Lab ("Test-Name", "my-template-url");

        // Mock the function of the service
        when(labService.save(any(Lab.class))).thenReturn(testLab);

        mockMvc.perform(
                post("/labs/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectAsJson(testLab)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content[0].name", is("Test-Name")))
                .andExpect(jsonPath("$.content[0].id", is(testLab.getId())));

    }

    @Test
    public void testUpdateLab() throws Exception{

        Date date = new Date();

        Lab testLab = new Lab ("Test-Name", "my-template-url");
        testLab.setId(999L);
        // Mock the function of the service
        when(labService.findByName("Test-Name")).thenReturn(testLab);

        when(labService.save(any(Lab.class))).thenReturn(testLab);

        mockMvc.perform(
                put("/labs/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectAsJson(testLab)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content[0].name", is("Test-Name")))
                .andExpect(jsonPath("$.content[0].id", is(999)));

    }

    @Test
    public void testGetNumberOfCleanAccounts() throws Exception
    {
        when(deployedLabService.getNumberOfCleanAccounts()).thenReturn(2);

        // Perform GET on URL which maps to LabsController
        mockMvc.perform(
                get("/labs/accounts/clean"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0]", is(2)));
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