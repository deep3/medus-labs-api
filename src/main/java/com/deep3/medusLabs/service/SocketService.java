package com.deep3.medusLabs.service;

import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public interface SocketService {

    void sendMessage(SocketMessage socketMessageMessage) throws JsonProcessingException;

}
