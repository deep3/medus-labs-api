package com.deep3.medusLabs.controller;

import com.amazonaws.SdkClientException;
import com.deep3.medusLabs.aws.utilities.AWSMetadataUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AWSMetadataUtils.class)
public class InstanceMetadataControllerTests {

    private MockMvc mockMvc;

    @InjectMocks
    private InstanceMetadataController instanceMetadataController;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(instanceMetadataController).build();
        PowerMockito.mockStatic(AWSMetadataUtils.class);
    }

    @Test
    public void testGetInstanceMetadata() throws Exception {
        PowerMockito.when(AWSMetadataUtils.getInstanceId()).thenReturn("myId!");
        mockMvc.perform(
                get("/metadata/instanceid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]", is("myId!")));
    }

    @Test
    public void testGetInstanceMetadataFailure() throws Exception {
        PowerMockito.when(AWSMetadataUtils.getInstanceId()).thenThrow(SdkClientException.class);
        mockMvc.perform(
                get("/metadata/instanceid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(0)));
    }

    @Test
    public void testGetRoleArn() throws Exception {
        PowerMockito.when(AWSMetadataUtils.getRoleArn()).thenReturn("myId!");
        mockMvc.perform(
                get("/metadata/instancerole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]", is("myId!")));
    }

    @Test
    public void testGetRoleArnFailure() throws Exception {
        PowerMockito.when(AWSMetadataUtils.getRoleArn()).thenThrow(SdkClientException.class);
        mockMvc.perform(
                get("/metadata/instancerole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(0)));
    }

}
