package com.deep3.medusLabs.service;

import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.model.enums.LogLevel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DeployedLabLogService {

    LogEntry createLog(DeployedLab deployedLab, LogLevel logLevel, String message, String... args);

    LogEntry createLog(long id, LogLevel logLevel, String message, String... args);

    void sendLogViaSocket(LogEntry log);

    LogEntry getLog(Long logID);

    List<LogEntry> getLogsByLab(Long labId);

    LogEntry save(LogEntry log);
}
