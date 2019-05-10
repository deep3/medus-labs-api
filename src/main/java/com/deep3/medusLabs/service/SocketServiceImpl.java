package com.deep3.medusLabs.service;

import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class SocketServiceImpl implements SocketService {

    private SimpMessagingTemplate template;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SocketServiceImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.template = simpMessagingTemplate;
    }

    /**
     * Send a message to the defined endpoint
     * @param socketMessageMessage The Message to send
     * @throws JsonProcessingException
     */
    @Override
    public void sendMessage(SocketMessage socketMessageMessage) throws JsonProcessingException {
        this.template.convertAndSend("/update/message",mapper.writeValueAsString(socketMessageMessage));
    }
}
