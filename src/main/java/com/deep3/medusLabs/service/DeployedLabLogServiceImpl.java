package com.deep3.medusLabs.service;

import com.deep3.medusLabs.aws.exceptions.ObjectNotFoundException;;
import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.model.socketmessages.SocketMessage;
import com.deep3.medusLabs.repository.DeployedLabLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeployedLabLogServiceImpl implements DeployedLabLogService {

    private DeployedLabLogRepository deployedLabLogRepository;
    private DeployedLabServiceImpl deployedLabService;
    private SocketServiceImpl socketService;

    @Autowired
    public DeployedLabLogServiceImpl(DeployedLabLogRepository deployedLabLogRepository,
                                     DeployedLabServiceImpl deployedLabService,
                                     SocketServiceImpl socketService
                                     ) {
        this.deployedLabLogRepository = deployedLabLogRepository;
        this.deployedLabService = deployedLabService;
        this.socketService = socketService;

    }

    /**
     * Create a new LogEntry in the data store
     * @param deployedLab The DeployedLab that this log relates too.
     * @param logLevel The LogLevel
     * @param message  The Log Message
     */
    @Override
    public LogEntry createLog(DeployedLab deployedLab, LogLevel logLevel, String message, String... args) {
        LogEntry log = new LogEntry(deployedLab, logLevel, String.format(message, (Object[])args));

        save(log);

        if (log.getLogLevel() == LogLevel.ERROR || log.getLogLevel() == LogLevel.SUCCESS) {
            sendLogViaSocket(log);
        }

        return log;
    }

    /**
     * Create a new LogEntry in the data store
     * @param id The DeployedLabId that this log relates too.
     * @param logLevel The LogLevel
     * @param message  The Log Message
     */
    @Override
    public LogEntry createLog(long id, LogLevel logLevel, String message, String... args) {

        try {
            LogEntry log = new LogEntry(deployedLabService.findDeployedLabById(id), logLevel, String.format(message, (Object[])args));
            save(log);

            if (log.getLogLevel() == LogLevel.ERROR || log.getLogLevel() == LogLevel.SUCCESS) {
                sendLogViaSocket(log);
            }

            return log;
        }
        catch (ObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * Function to send a LogEntry via the SocketService to the UI
     * @param log The LogEntry to send
     */
    @Override
    public void sendLogViaSocket(LogEntry log) {
        try {
            socketService.sendMessage(new SocketMessage<>(log));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a LogEntry from the data store
     * @param logID The ID of the LogEntry to return
     * @return A LogEntry Object
     */
    @Override
    public LogEntry getLog(Long logID) {
        return deployedLabLogRepository.findById(logID).get();
    }

    /**
     * Get all LogEntries relating to the specified DeployedLab
     * @param labId The DeployedLab that the LogEntries should relate too.
     * @return A List of LogEntry Objects
     */
    @Override
    public List<LogEntry> getLogsByLab(Long labId) {
        return IteratorUtils.toList(deployedLabLogRepository.findAll().iterator())
                .stream().filter(log -> log.getLab().getId().equals(labId))
                .collect(Collectors.toList());
    }

    /**
     * Saves a LogEntry to the local Data Source
     * @param entry The LogEntry object
     * @return A LogEntry
     */
    @Override
    public LogEntry save(LogEntry entry) {
        return deployedLabLogRepository.save(entry);
    }
}
