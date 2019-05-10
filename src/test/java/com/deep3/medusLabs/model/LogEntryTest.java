package com.deep3.medusLabs.model;

import com.deep3.medusLabs.model.enums.LogLevel;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class LogEntryTest {

    DeployedLab deployedLab;

    @Test
    public void testConstructor() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        Assert.assertNotNull(entry);
        Assert.assertEquals(entry.getMessage(), "test message");
        Assert.assertEquals(entry.getLogLevel(), LogLevel.ERROR);
        Assert.assertNotNull(entry.getDate());
    }

    @Test
    public void testGetSetId() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");
        Long id = 100L;

        entry.setId(id);

        Assert.assertEquals(entry.getId(), id);
    }

    @Test
    public void testGetSetDeployedLab() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        entry.setLab(deployedLab);

        Assert.assertEquals(entry.getLab(), deployedLab);
    }

    @Test
    public void testGetSetMessage() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        String message = "another message";

        entry.setMessage(message);

        Assert.assertEquals(entry.getMessage(), message);
    }

    @Test
    public void testGetSetLogLevel() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        LogLevel level = LogLevel.SUCCESS;

        entry.setLogLevel(level);

        Assert.assertEquals(entry.getLogLevel(), level);
    }

    @Test
    public void testGetSetDate() {
        LogEntry entry = new LogEntry(deployedLab, LogLevel.ERROR, "test message");

        Date date = new Date();

        entry.setDate(date);

        Assert.assertEquals(entry.getDate(), date);
    }
}
