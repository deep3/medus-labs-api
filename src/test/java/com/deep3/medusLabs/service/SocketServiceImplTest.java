package com.deep3.medusLabs.service;

import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.matches;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class SocketServiceImplTest {


    @Mock
    SimpMessagingTemplate template;

    @Before
    public void initialize()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() throws Exception {

        SocketServiceImpl service = new SocketServiceImpl(template);

        Assert.assertNotNull("Error creating instance of SocketServiceImpl",service);
    }

    @Test
    public void testTestSendMessage() throws JsonProcessingException {
        SocketServiceImpl service = new SocketServiceImpl(template);
        service.sendMessage(new SocketMessage("Test"));
        verify(template).convertAndSend(matches("/update/message"),anyString());
    }
}
