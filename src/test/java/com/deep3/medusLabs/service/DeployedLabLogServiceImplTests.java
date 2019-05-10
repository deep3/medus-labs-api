package com.deep3.medusLabs.service;

import com.deep3.medusLabs.model.DeployedLab;
import com.deep3.medusLabs.model.LogEntry;
import com.deep3.medusLabs.model.enums.LogLevel;
import com.deep3.medusLabs.repository.DeployedLabLogRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DeployedLabLogServiceImplTests {

    @Mock
    DeployedLabLogRepository deployedLabLogRepository;

    @Mock
    DeployedLabServiceImpl deployedLabService;

    @Mock
    SocketServiceImpl socketService;

    @Mock
    DeployedLab deployedLab;

    @Test
    public void testCreateLog() {
        DeployedLabLogServiceImpl deployedLabLogService = new DeployedLabLogServiceImpl(deployedLabLogRepository, deployedLabService, socketService);

        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        when(deployedLabLogService.save(any(LogEntry.class))).thenReturn(entry);

        LogEntry entryTest = deployedLabLogService.createLog(deployedLab, LogLevel.ERROR, "test message");

        Assert.assertNotNull(entryTest);
    }

    @Test
    public void testCreateLogFormatting() {
        DeployedLabLogServiceImpl deployedLabLogService = new DeployedLabLogServiceImpl(deployedLabLogRepository, deployedLabService, socketService);

        when(deployedLabLogRepository.save(any(LogEntry.class))).thenReturn(null);

        LogEntry test = deployedLabLogService.createLog(deployedLab, LogLevel.INFO, "this is %s message about %s", "my", "this");

        Assert.assertEquals(test.getMessage(), "this is my message about this");
    }

    @Test
    public void testGetLog() {
        DeployedLabLogServiceImpl deployedLabLogService = new DeployedLabLogServiceImpl(deployedLabLogRepository, deployedLabService, socketService);

        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        when(deployedLabLogRepository.findById(anyLong())).thenReturn(Optional.of(entry));

        LogEntry found = deployedLabLogService.getLog(1L);

        Assert.assertEquals(found.getId(), entry.getId());
        Assert.assertEquals(found.getMessage(), entry.getMessage());
        Assert.assertEquals(found.getLogLevel(), entry.getLogLevel());

    }

    @Test
    public void testGetLogsByLab() {
        DeployedLabLogServiceImpl deployedLabLogService = new DeployedLabLogServiceImpl(deployedLabLogRepository, deployedLabService, socketService);

        List<LogEntry> data = new ArrayList<>();

        DeployedLab lab1 = new DeployedLab();
        lab1.setId(1L);
        DeployedLab lab2 = new DeployedLab();
        lab2.setId(2L);

        data.add(new LogEntry(lab1, LogLevel.ERROR, "Test-Message-1"));
        data.add(new LogEntry(lab1, LogLevel.INFO, "Test-Message-2"));
        data.add(new LogEntry(lab2, LogLevel.ERROR, "Test-Message-3"));

        when(deployedLabLogRepository.findAll()).thenReturn(data);

        List<LogEntry> logsForLab1 = deployedLabLogService.getLogsByLab(lab1.getId());

        Assert.assertNotNull(logsForLab1);
        Assert.assertEquals(logsForLab1.size(), 2);
        Assert.assertEquals(logsForLab1.get(0).getLogLevel(), LogLevel.ERROR);
        Assert.assertEquals(logsForLab1.get(1).getLogLevel(), LogLevel.INFO);
    }
}
