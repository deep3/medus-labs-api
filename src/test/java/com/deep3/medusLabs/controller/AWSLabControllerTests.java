package com.deep3.medusLabs.controller;

import com.deep3.medusLabs.aws.service.AccountWranglerService;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.security.service.TokenAuthenticationService;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.LabServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)

public class AWSLabControllerTests {

    private MockMvc mockMvc;

    @Mock
    private AccountWranglerService wranglerService;

    @Mock
    private LabServiceImpl labService;

    @Mock
    private DeployedLabService deployedLabService;

    @Mock
    private DeployedLabLogService deployedLabLogService;

    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private TokenAuthenticationService tokenAuthenticationService;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(new AWSLabController(wranglerService, labService,
                                                                       deployedLabService, deployedLabLogService,
                                                                       organisationsService, tokenAuthenticationService )).build();
    }

    @Test
    public void testGetAllLabs() throws Exception {

        List<Lab> labs = new ArrayList<>();
        Date now = new Date();
        labs.add(new Lab("my-lab", "test-template-url"));
        labs.add(new Lab("my-lab-2", "test-template-url-2"));

        Mockito.when(labService.findAll()).thenReturn(labs);
        mockMvc.perform(get("/aws/labs/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("my-lab")))
                .andExpect(jsonPath("$.content[0].templateUrl", is("test-template-url")))
                .andReturn().getResponse();
    }


    @Test
    /**
     * Currently testing empty endpoint, we are not implementing the public params until we find a use case for them.
     */
    public void testGetAllLabParams() throws Exception {

        Date now = new Date();
        Lab testLab = new Lab("my-lab", "test-template-url");
        Mockito.when(labService.findByName("my-lab")).thenReturn(testLab);

        mockMvc.perform(get("/aws/labs/param").param("service","my-lab"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andReturn().getResponse();
    }

    @Test
    public void testUndeploy() throws Exception {
        DeployedLab deployedLab = new DeployedLab();
        deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.ACTIVE);
        deployedLab.setLabAccounts(Arrays.asList("123456789", "987654321"));

        Mockito.when(deployedLabService.findDeployedLabById(anyLong())).thenReturn(deployedLab);

        mockMvc.perform(delete("/aws/labs/undeploy").param("deployedLabId", "1"))
                .andExpect(status().isAccepted());
        Assert.assertEquals(DeployedLab.DeployedLabStatus.DELETING, deployedLab.getDeployedLabStatus());
        verify(deployedLabService).saveDeployedLab(any());
    }

    @Test
    public void testUndeployAlreadyDeleting() throws Exception {
        DeployedLab deployedLab = new DeployedLab();
        deployedLab.setDeployedLabStatus(DeployedLab.DeployedLabStatus.DELETING);
        deployedLab.setLabAccounts(Arrays.asList("123456789", "987654321"));

        Mockito.when(deployedLabService.findDeployedLabById(anyLong())).thenReturn(deployedLab);

        mockMvc.perform(delete("/aws/labs/undeploy").param("deployedLabId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUndeployBadId() throws Exception {

    }
}
