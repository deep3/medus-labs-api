package com.deep3.medusLabs.controller;

import com.amazonaws.services.organizations.model.Account;
import com.deep3.medusLabs.aws.exceptions.BadRequestException;
import com.deep3.medusLabs.aws.model.AWSAccountWrapper;
import com.deep3.medusLabs.aws.service.AccountWranglerService;
import com.deep3.medusLabs.aws.service.OrganisationsService;
import com.deep3.medusLabs.model.CreateStudentAccountRequest;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.Lab;
import com.deep3.medusLabs.service.DeployedLabLogService;
import com.deep3.medusLabs.service.DeployedLabService;
import com.deep3.medusLabs.service.SocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class MemberOrganisationControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private AccountWranglerService accountWranglerService;

    @Mock
    private DeployedLabService deployedLabService;
    private DeployedLabLogService deployedLabLogService;
    private SocketService socketService;
    private AWSAccountWrapper awsAccountWrapper;


    ObjectMapper mapper = new ObjectMapper();

    public MemberOrganisationControllerTest() {
    }


    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(new MemberOrganisationController(organisationsService, accountWranglerService, deployedLabService)).build();
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        Account account = new Account();
        account.setStatus("OK");
        account.setName("testAccount");
        account.setId("1234");
        List<Account> validAccounts = new ArrayList<>();
        validAccounts.add(account);
        validAccounts.add(account);

        List<Account> invalidAccounts = new ArrayList<>();
        invalidAccounts.add(account);

        List<Account> allAccounts = new ArrayList<>();
        allAccounts.addAll(validAccounts);
        allAccounts.addAll(invalidAccounts);

        when(organisationsService.getAllAccounts()).thenReturn(allAccounts);
        when(organisationsService.getInvalidAccounts()).thenReturn(invalidAccounts);
        when(organisationsService.getValidAccounts()).thenReturn(validAccounts);

        mockMvc.perform(get("/organisations/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(3)))

                .andReturn().getResponse();

        mockMvc.perform(get("/organisations/").param("type","invalid"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))

                .andReturn().getResponse();

         mockMvc.perform(get("/organisations/").param("type","valid"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))

                .andReturn().getResponse();



    }

    @Test(expected = BadRequestException.class)
    public void testBadGetAllAccounts() throws Throwable {
        try {
            mockMvc.perform(get("/organisations/").param("type","knackered"));
        } catch (NestedServletException e) {
            // MockMVC wraps the thrown exception in its own exception, the cause is the exception thrown by the method being tested
            throw e.getCause();
        }
    }

    @Test
    public void testGetRootAccountId () throws Exception {

        when(organisationsService.getAccountId()).thenReturn(String.valueOf(100));

        mockMvc.perform(get("/organisations/rootaccountid"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content[0]", is("100")))
                .andReturn().getResponse();
    }

    @Test
    public void testCreateNewAccount() throws Exception {

        CreateStudentAccountRequest request = new CreateStudentAccountRequest();
        request.setAccounts(5);
        request.setEmail("test@test.com");
        ArrayList<String> mockedResult = new ArrayList<>();
        mockedResult.add("A");
        mockedResult.add("B");
        mockedResult.add("C");
        when(organisationsService.createAccounts(eq(5), eq("test@test.com"))).thenReturn(mockedResult);

        mockMvc.perform(
                post("/organisations/create")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("$.content[0]", is("A")))
                .andExpect(jsonPath("$.content[1]", is("B")))
                .andExpect(jsonPath("$.content[2]", is("C")))

                .andReturn().getResponse();
    }

    public void testWipeAccounts(Lab lab) throws Exception {

        AccountWranglerService testedAccountWrangler = new AccountWranglerService(organisationsService, socketService, deployedLabService, deployedLabLogService);

        testedAccountWrangler.wipeAllAccounts(any(Long.class));
        verify(awsAccountWrapper).wipeAccount(any(Long.class));
    }

    @Test
    public void testNukeFunctionality() throws Exception {

        DeployedLab testLab = (new DeployedLab(new Lab("EC2", "my-template-url"), Arrays.asList("1","2")));
        testLab.setId(1l);
        Long labToNukeID = testLab.getId();
        ArrayList<DeployedLab> x =  new ArrayList<>();
        x.add(0, testLab);
        when(deployedLabService.findDeployedLabs()).thenReturn(x);

    mockMvc.perform(
                delete( "/organisations/nuke")
                .content(mapper.writeValueAsString(labToNukeID))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect((content().contentType(MediaType.APPLICATION_JSON_UTF8)));

    }


    public void setDeployedLabLogService(DeployedLabLogService deployedLabLogService) {
        this.deployedLabLogService = deployedLabLogService;
    }
}
