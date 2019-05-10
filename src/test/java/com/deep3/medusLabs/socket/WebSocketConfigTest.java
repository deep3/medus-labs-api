package com.deep3.medusLabs.socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class WebSocketConfigTest {

    @Mock
    StompEndpointRegistry stompEndpointRegistry;
    @Mock
    StompWebSocketEndpointRegistration stompWebSocketEndpointRegistration;

    @Mock
    MessageBrokerRegistry messageBrokerRegistry;
    @Mock
    SimpleBrokerRegistration simpleBrokerRegistration;


    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void testRegisterStompEndpoints() {
        when(stompEndpointRegistry.addEndpoint(anyString())).thenReturn(stompWebSocketEndpointRegistration);
        when(stompWebSocketEndpointRegistration.setAllowedOrigins(anyString())).thenReturn(stompWebSocketEndpointRegistration);

        WebSocketConfig webSocketConfig = new WebSocketConfig();
        webSocketConfig.SOCKET_PATH = "A_TEST_PATH";
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry).addEndpoint(anyString());
        verify(stompWebSocketEndpointRegistration).setAllowedOrigins(anyString());
    }

    @Test
    public void testConfigureMessageBroker(){

        when(messageBrokerRegistry.setApplicationDestinationPrefixes(anyString())).thenReturn(messageBrokerRegistry);
        when(messageBrokerRegistry.enableSimpleBroker(anyString())).thenReturn(simpleBrokerRegistration);

        WebSocketConfig webSocketConfig = new WebSocketConfig();
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).setApplicationDestinationPrefixes(anyString());
        verify(messageBrokerRegistry).enableSimpleBroker(anyString());

    }


}
